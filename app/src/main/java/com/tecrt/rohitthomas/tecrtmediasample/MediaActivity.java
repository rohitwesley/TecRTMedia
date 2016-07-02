package com.tecrt.rohitthomas.tecrtmediasample;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaAudioEncoder;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaEncoder;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaMuxerWrapper;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaVideoEncoder;

import java.io.File;
import java.io.IOException;

public class MediaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /**
     * for camera preview display
     */
    private CameraGLView mCameraView;
    /**
     * for camera preview display
     */
    private VideoGLView mVideoView;
    /**
     * for scale mode display
     */
    private TextView mScaleModeView;
    /**
     * muxer for audio/video recording
     */
    private MediaMuxerWrapper mMuxer;

    private FrameLayout frameLayout;

    // Common observer for all Global Buttons
    private final GlobalObserver mGlobal_OnClickListener = new GlobalObserver();
    // Common observer for all View Buttons
    private final ViewObserver mView_OnClickListener = new ViewObserver();
    // Common observer for all Adjustment Buttons
    private final AdjustmentObserver mAdjustment_OnClickListener = new AdjustmentObserver();
    // Common observer for all Filters Buttons
    private final FiltersObserver mFilters_OnClickListener = new FiltersObserver();
    // Common observer for all SeekBars.
    private final SeekBarObserver mObserverSeekBar = new SeekBarObserver();
    // Application shared preferences instance.
    private SharedPreferences mPreferences;
    // Shared data instance.
    private tecrtData mSharedData = new tecrtData();

    File mVideoFolder;
    File mVideoFile;
    private ArrayAdapter<String> mMovieFiles;
    private int mSelectedMovie;
    //private MediaPlayer player;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        // Set Menu Button OnClickListeners.
        findViewById(R.id.button_menu).setOnClickListener(mGlobal_OnClickListener);

        frameLayout = (FrameLayout) findViewById(R.id.MainLayout);

        // Set Vew Button OnClickListeners.
        findViewById(R.id.button_camrotate).setOnClickListener(mView_OnClickListener);
        findViewById(R.id.button_stop_rec).setOnClickListener(mView_OnClickListener);
        findViewById(R.id.button_add).setOnClickListener(mView_OnClickListener);
        findViewById(R.id.button_reset).setOnClickListener(mView_OnClickListener);
        findViewById(R.id.button_play_pause).setOnClickListener(mView_OnClickListener);
        findViewById(R.id.button_saveedit).setOnClickListener(mView_OnClickListener);

        // Set Adjustment Button OnClickListeners.
        findViewById(R.id.button_adjust).setOnClickListener(mAdjustment_OnClickListener);
        findViewById(R.id.brightnessButton).setOnClickListener(mAdjustment_OnClickListener);
        findViewById(R.id.contrastButton).setOnClickListener(mAdjustment_OnClickListener);
        findViewById(R.id.saturationButton).setOnClickListener(mAdjustment_OnClickListener);
        findViewById(R.id.vinyetButton).setOnClickListener(mAdjustment_OnClickListener);


        // Get preferences instance.
        mPreferences = getPreferences(MODE_PRIVATE);
        // Set Filter Button OnClickListeners.
        findViewById(R.id.button_filters).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_none).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_bnw).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Ansel).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Sepia).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Retro).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Georgia).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Sahara).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Polaroid).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Edges).setOnClickListener(mFilters_OnClickListener);
        findViewById(R.id.filter_Cartoon).setOnClickListener(mFilters_OnClickListener);

        // SeekBar ids as triples { SeekBar id, key id, default value }.
        final int SEEKBAR_IDS[][] = {
                { R.id.seekBarBrightness, R.string.key_brightness, 5 },
                { R.id.seekBarContrast, R.string.key_contrast, 5 },
                { R.id.seekBarSaturation, R.string.key_saturation, 8 },
                { R.id.seekBarVinyet, R.string.key_corner_radius, 3 } };
        // Set SeekBar OnSeekBarChangeListeners and default progress.
        for (int ids[] : SEEKBAR_IDS) {
            SeekBar seekBar = (SeekBar) findViewById(ids[0]);
            assert seekBar != null;
            seekBar.setOnSeekBarChangeListener(mObserverSeekBar);
            seekBar.setProgress(mPreferences.getInt(getString(ids[1]), ids[2]));
            // SeekBar.setProgress triggers observer only in case its value
            // changes. And we're relying on this trigger to happen.
            if (seekBar.getProgress() == 0) {
                seekBar.setProgress(1);
                seekBar.setProgress(0);
            }
        }

        try {
            //Get Gallery Folder or create it if doesnt exist
            mVideoFolder = MiscUtils.createVideoFolder("Splode");
            mVideoFile = MiscUtils.createVideoFileName(mVideoFolder,"fbo-gl-recording", ".mp4");//new File(getFilesDir(), "fbo-gl-recording.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mVideoFile = new File(getFilesDir(), "fbo-gl-recording.mp4");


        // Need to create one of these fancy ArrayAdapter thingies, and specify the generic layout
        // for the widget itself.
        try {

            //TODO hide item list in cam view
            ArrayAdapter<String> adapter;
            //Get asset file for first time user.
            AssetManager man = getAssets();
            useAssets = true;
            String[] mMovieString = prepend(man.list("samples"),"samples/");//man.list("samples");
            mMovieFiles = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mMovieString);
            //TODO add Splode Gallery file to list without crashing
            //mMovieString = prepend(MiscUtils.getFiles(mVideoFolder, "*.mp4"),"Splode/");//MiscUtils.getFiles(getFilesDir(), "*.mp4");
            //mMovieFiles.addAll(mMovieString);

            mSelectedMovie = -1;
            mMovieFiles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Populate file-selection spinner.
            Spinner spinner = (Spinner) findViewById(R.id.playMovieFile_spinner);
            // Apply the adapter to the spinner.
            spinner.setAdapter(mMovieFiles);
            spinner.setOnItemSelectedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Pause on load activity to let everything load
        new CountDownTimer(500, 100) {

            public void onTick(long millisUntilFinished) {
                // You don't need anything here
            }

            public void onFinish() {
                findViewById(R.id.layout_menu).setVisibility(View.GONE);
                findViewById(R.id.layout_menuCamera).setVisibility(View.GONE);
                findViewById(R.id.layout_menuVideo).setVisibility(View.GONE);
                findViewById(R.id.layout_filters).setVisibility(View.GONE);
                findViewById(R.id.layout_adjustment).setVisibility(View.GONE);
                findViewById(R.id.layout_adjustButton).setVisibility(View.VISIBLE);

            }

        }.start();

        findViewById(R.id.append).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = ProgressDialog.show(MediaActivity.this, null, null);
                mProgressDialog.dismiss();
            }
        });



    }

    public String[] prepend(String[] input, String prepend) {
        String[] output = new String[input.length];
        for (int index = 0; index < input.length; index++) {
            output[index] = "" + prepend + input[index];
        }
        return output;
    }

    @Override
    public void onResume() {
        super.onResume();
        //if(player != null)player.stop();
        if(mVideoView != null)mVideoView.onResume();
        if(mCameraView != null)mCameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //if(player != null)player.stop();
        if(mVideoView != null)mVideoView.onPause();
        if(mCameraView != null)mCameraView.onPause();
        stopRecording();
    }

    /*
     * Called when the movie Spinner gets touched.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner) parent;
        mSelectedMovie = spinner.getSelectedItemPosition();
        //Release player before loading new video
//        if (player != null) {
//            player.release();
//            VideoUpdate = true;
//        }
        if(mVideoView != null) StopVideo();
        ImageButton toggleButton = (ImageButton) findViewById(R.id.button_play_pause);
        toggleButton.setImageResource(R.mipmap.ic_pause);
        togglePlay = false;
        //TODO put a countdown while video changing
        if(mVideoView != null) StartVideo();
        toggleButton = (ImageButton) findViewById(R.id.button_play_pause);
        toggleButton.setImageResource(R.mipmap.ic_play);
        togglePlay = false;
        Toast.makeText(MediaActivity.this, "onItemSelected: " + mSelectedMovie + " '" + mMovieFiles.getItem(mSelectedMovie) + "'", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    boolean toggleMenu = false;
    boolean toggleAdjustment = false;
    boolean toggleFilters = false;
//    boolean toggleVideoMenu = true;
    boolean toggleMode = true;
    boolean toggleRec = true;
    boolean togglePlay = false;
    // Check if video changed
    boolean VideoUpdate = false;
    // Check if using asset
    boolean useAssets = false;
    /**
     * Global On click listener for all views in activity
     * Handle animation of buttons and views here
     */
    private final class GlobalObserver implements View.OnClickListener {

        public void onClick(final View v) {
            switch(v.getId()) {
                case R.id.button_menu:
                    if(toggleMenu) {
                        hideUIElement(Techniques.FadeOutUp, 700, findViewById(R.id.layout_menu));
                        //hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuCamera));
                        //hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuVideo));
                        hideUIElement(Techniques.FadeOutLeft, 700, findViewById(R.id.layout_filters));
                        hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.layout_adjustment));
                        toggleMenu = false;
                        //toggleButton.setImageResource(R.mipmap.ic_rec);
                        hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuCamera));
                        hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuVideo));
                    }
                    else {
                        if(toggleMode) {
                            StartVideo();
                            showUIElement(Techniques.FadeInRight, 700, findViewById(R.id.layout_menuVideo));
                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuCamera));
                            Toast.makeText(MediaActivity.this, "Video Editor Active", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            StartCamera();
                            showUIElement(Techniques.FadeInRight, 700, findViewById(R.id.layout_menuCamera));
                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuVideo));
                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.playMovieFile_spinner));
                            Toast.makeText(MediaActivity.this, "Camera Active", Toast.LENGTH_SHORT).show();
                        }
                        showUIElement(Techniques.FadeInDown, 700, findViewById(R.id.layout_menu));
                        toggleMenu = true;
                    }
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_menu));
//                    toggleVideoMenu = false;
                    break;
            }
        }
    };

    /**
     * View Buttons
     */
    private final class ViewObserver implements View.OnClickListener {

        public void onClick(final View v) {
            ImageButton toggleButton;
            switch(v.getId()) {

                //View Buttons
//                case R.id.button_camera_video://Replased with master button
//                    toggleButton = (ImageButton) findViewById(R.id.button_camera_video);
//                    if(toggleVideoMenu) {
//                        //toggleButton.setImageResource(R.mipmap.ic_rec);
//                        if(toggleMode) {
//                            StartVideo();
//                            showUIElement(Techniques.FadeInRight, 700, findViewById(R.id.layout_menuVideo));
//                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuCamera));
//                            Toast.makeText(MediaActivity.this, "Video Editor Active", Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            StartCamera();
//                            showUIElement(Techniques.FadeInRight, 700, findViewById(R.id.layout_menuCamera));
//                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuVideo));
//                            hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.playMovieFile_spinner));
//                            Toast.makeText(MediaActivity.this, "Camera Active", Toast.LENGTH_SHORT).show();
//                        }
//                        toggleVideoMenu = false;
//                    }
//                    else {
//                        //toggleButton.setImageResource(R.mipmap.ic_rec);
//                        hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuCamera));
//                        hideUIElement(Techniques.FadeOutRight, 700, findViewById(R.id.layout_menuVideo));
//                        toggleVideoMenu = true;
//                    }
//                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_camera_video));
//                    break;
                //Camera Buttons
                case R.id.button_camrotate:
                    Toast.makeText(MediaActivity.this, "Camera Changed", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_camrotate));
                    break;
                case R.id.button_stop_rec:
                    toggleButton = (ImageButton) findViewById(R.id.button_stop_rec);
                    if(toggleRec && mMuxer == null) {
                        toggleButton.setImageResource(R.mipmap.ic_rec);
                        toggleRec = false;
                        Toast.makeText(MediaActivity.this, "Record Start", Toast.LENGTH_SHORT).show();
                        startRecording();
                    }
                    else {
                        toggleButton.setImageResource(R.mipmap.ic_stop);
                        toggleRec = true;
                        Toast.makeText(MediaActivity.this, "Record Stop", Toast.LENGTH_SHORT).show();
                        stopRecording();
                    }
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_stop_rec));
                    break;
                //Video Editor Buttons
                case R.id.button_add:
                    onPause();
                    selectVideo();
                    Toast.makeText(MediaActivity.this, "Gallery Video", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_add));
                    break;
                case R.id.button_reset:
//                    if (player != null) {
//                        player.release();
//                        VideoUpdate = true;
//                    }
                    StopVideo();
                    toggleButton = (ImageButton) findViewById(R.id.button_play_pause);
                    toggleButton.setImageResource(R.mipmap.ic_pause);
                    togglePlay = false;
                    //TODO reset view to show start of video
                    Toast.makeText(MediaActivity.this, "Reset", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_reset));
                    break;
                case R.id.button_play_pause:
                    toggleButton = (ImageButton) findViewById(R.id.button_play_pause);
                    if(togglePlay) {
                        toggleButton.setImageResource(R.mipmap.ic_pause);
//                        if(player != null)player.pause();
                        PauseVideo();
                        togglePlay = false;
                        Toast.makeText(MediaActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        toggleButton.setImageResource(R.mipmap.ic_play);

                        StartVideo();

                        togglePlay = true;
                        Toast.makeText(MediaActivity.this, "Play", Toast.LENGTH_SHORT).show();
                    }
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_play_pause));
                    break;
                case R.id.button_saveedit:
                    toggleButton = (ImageButton) findViewById(R.id.button_saveedit);
                    if(toggleRec && mMuxer == null) {
                        toggleButton.setImageResource(R.mipmap.ic_rec);
                        toggleRec = false;
                        Toast.makeText(MediaActivity.this, "Record Start", Toast.LENGTH_SHORT).show();
                        startRecording();
                    }
                    else {
                        toggleButton.setImageResource(R.mipmap.ic_stop);
                        toggleRec = true;
                        Toast.makeText(MediaActivity.this, "Record Stop", Toast.LENGTH_SHORT).show();
                        stopRecording();
                    }
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_saveedit));
                    break;

            }
        }
    };

    /**
     * Filter Buttons
     */
    private final class FiltersObserver implements View.OnClickListener {

        public void onClick(final View v) {
            switch(v.getId()) {
                //Filters Buttons
                case R.id.button_filters:
                    if(toggleFilters) {
                        hideUIElement(Techniques.FadeOutLeft, 700, findViewById(R.id.layout_filters));
                        toggleFilters = false;
                    }
                    else {
                        showUIElement(Techniques.FadeInRight, 700, findViewById(R.id.layout_filters));
                        toggleFilters = true;
                        Toast.makeText(MediaActivity.this, "Choose a Filter", Toast.LENGTH_SHORT).show();
                    }
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.layout_adjustment));
                    toggleAdjustment = false;
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_filters));
                    break;
                // None Filter button
                case R.id.filter_none:
                    mSharedData.mFilter = 0;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_none));
                    break;
                // BnW Filter button
                case R.id.filter_bnw:
                    mSharedData.mFilter = 1;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_bnw));
                    break;
                // Ansel Filter button
                case R.id.filter_Ansel:
                    mSharedData.mFilter = 2;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Ansel));
                    break;
                // Sepia Filter button
                case R.id.filter_Sepia:
                    mSharedData.mFilter = 3;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Sepia));
                    break;
                // Retro Filter button
                case R.id.filter_Retro:
                    mSharedData.mFilter = 4;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Retro));
                    break;
                // Georgia Filter button
                case R.id.filter_Georgia:
                    mSharedData.mFilter = 5;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Georgia));
                    break;
                // Sahara Filter button
                case R.id.filter_Sahara:
                    mSharedData.mFilter = 6;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Sahara));
                    break;
                // Polaroid Filter button
                case R.id.filter_Polaroid:
                    mSharedData.mFilter = 7;
                    YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.filter_Polaroid));
                    break;
                // Cartoon Filter button
                case R.id.filter_Cartoon:
                    mSharedData.mFilter = 10;
                    break;
                // Edges Filter button
                case R.id.filter_Edges:
                    mSharedData.mFilter = 8;
                    break;

            }
            if (mVideoView != null)mVideoView.setSharedData(mSharedData);
            if (mCameraView != null)mCameraView.setSharedData(mSharedData);
        }
    };

    /**
     * Adjustment Buttons
     */
    private final class AdjustmentObserver implements View.OnClickListener  {

        public void onClick(final View v) {
            switch(v.getId()) {

                //Adjustment Buttons
                case R.id.button_adjust:
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_brightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarBrightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_contrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarContrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_saturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarSaturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_vinyet));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarVinyet));
                    if(toggleAdjustment) {
                        hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.layout_adjustment));
                        toggleAdjustment = false;
                    }
                    else {
                        showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.layout_adjustment));
                        toggleAdjustment = true;
                        Toast.makeText(MediaActivity.this, "Adjust you video", Toast.LENGTH_SHORT).show();
                    }
                    hideUIElement(Techniques.FadeOutLeft, 700, findViewById(R.id.layout_filters));
                    toggleFilters = false;
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.button_adjust));
                    break;
                case R.id.brightnessButton:
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_brightness));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarBrightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_contrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarContrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_saturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarSaturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_vinyet));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarVinyet));

                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.text_brightness));
                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.seekBarBrightness));

                    Toast.makeText(MediaActivity.this, "Brightness", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.brightnessButton));
                    break;
                case R.id.contrastButton:
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_brightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarBrightness));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_contrast));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarContrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_saturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarSaturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_vinyet));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarVinyet));

                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.text_contrast));
                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.seekBarContrast));

                    Toast.makeText(MediaActivity.this, "Contrast", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.contrastButton));
                    break;
                case R.id.saturationButton:
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_brightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarBrightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_contrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarContrast));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_saturation));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarSaturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_vinyet));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarVinyet));

                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.text_saturation));
                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.seekBarSaturation));

                    Toast.makeText(MediaActivity.this, "Saturation", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.saturationButton));
                    break;
                case R.id.vinyetButton:
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_brightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarBrightness));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_contrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarContrast));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_saturation));
                    hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarSaturation));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.text_vinyet));
                    //hideUIElement(Techniques.FadeOutDown, 700, findViewById(R.id.seekBarVinyet));

                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.text_vinyet));
                    showUIElement(Techniques.FadeInUp, 700, findViewById(R.id.seekBarVinyet));

                    Toast.makeText(MediaActivity.this, "Corner Radius", Toast.LENGTH_SHORT).show();
                    YoYo.with(Techniques.RotateIn).duration(700).playOn(findViewById(R.id.vinyetButton));
                    break;
            }
            if (mVideoView != null)mVideoView.setSharedData(mSharedData);
            if (mCameraView != null)mCameraView.setSharedData(mSharedData);
        }
    };

    /**
     * Class for implementing SeekBar related callbacks.
     */
    private final class SeekBarObserver implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            switch (seekBar.getId()) {
                // On brightness recalculate shared value and update preferences.
                case R.id.seekBarBrightness: {
                    mPreferences.edit()
                            .putInt(getString(R.string.key_brightness), progress)
                            .apply();
                    mSharedData.mBrightness = (progress - 5) / 10f;

                    TextView textView = (TextView) findViewById(R.id.text_brightness);
                    assert textView != null;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.seekbar_brightness,
                            progress - 5));
                    break;
                }
                // On contrast recalculate shared value and update preferences.
                case R.id.seekBarContrast: {
                    mPreferences.edit()
                            .putInt(getString(R.string.key_contrast), progress)
                            .apply();
                    mSharedData.mContrast = (progress - 5) / 10f;
                    TextView textView = (TextView) findViewById(R.id.text_contrast);
                    assert textView != null;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.seekbar_contrast,
                            progress - 5));
                    break;
                }
                // On saturation recalculate shared value and update preferences.
                case R.id.seekBarSaturation: {
                    mPreferences.edit()
                            .putInt(getString(R.string.key_saturation), progress)
                            .apply();
                    mSharedData.mSaturation = (progress - 5) / 10f;
                    TextView textView = (TextView) findViewById(R.id.text_saturation);
                    assert textView != null;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.seekbar_saturation,
                            progress - 5));
                    break;
                }
                // On radius recalculate shared value and update preferences.
                case R.id.seekBarVinyet: {
                    mPreferences
                            .edit()
                            .putInt(getString(R.string.key_corner_radius), progress)
                            .apply();
                    mSharedData.mCornerRadius = progress / 10f;
                    TextView textView = (TextView) findViewById(R.id.text_vinyet);
                    assert textView != null;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.seekbar_corner_radius,
                            -progress));
                    break;
                }
            }
//            mRenderer.requestRender();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            findViewById(R.id.text_brightness).setVisibility(View.GONE);
            findViewById(R.id.text_contrast).setVisibility(View.GONE);
            findViewById(R.id.text_saturation).setVisibility(View.GONE);
            findViewById(R.id.text_vinyet).setVisibility(View.GONE);
        }
    }

    /**
     * onClick handler for "play" button.
     */
    public void StartVideo() {

        try {

            //Get address of video to be played
            MediaPlayer player = new MediaPlayer();
            if (videoAddr != null) {
                player.setDataSource(this, videoAddr);
            } else if (mMovieFiles.getItem(mSelectedMovie) != null) {

                if (useAssets) {
                    AssetFileDescriptor afd = getAssets().openFd(mMovieFiles.getItem(mSelectedMovie));
                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } else {
                    player.setDataSource(this, Uri.parse(new File(mVideoFolder, mMovieFiles.getItem(mSelectedMovie)).getAbsolutePath()));
                }
            } else {
                AssetFileDescriptor afd = getAssets().openFd("samples/sample1.mp4");
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            }

            //Check if VideoView is present else create it
            if(mVideoView == null) {
                // mVideoView = new VideoGLView(this, new File("/sdcard/1.mp4"));
                // videoView = new VideoGLView(this, "/sdcard/1.mp4");
                // videoView = new VideoGLView(this, Uri.parse("android.resource://com.example.samplevideoview/" + R.raw.video));
                mVideoView = new VideoGLView(MediaActivity.this, VideoUpdate, player);
                VideoUpdate = false;
                frameLayout.addView(mVideoView);
                findViewById(R.id.UILayout).bringToFront();
                //sendViewToBack(mVideoView);
            }
            else {
                VideoUpdate = mVideoView.StartVideo( VideoUpdate, player);
            }
            mVideoView.setSharedData(mSharedData);
            //mVideoView.setVideoSize(1280, 720);
            mVideoView.setVideoSize(mSharedData.videoWidth, mSharedData.videoHeight);
//            showUIElement(Techniques.FadeIn, 700, mVideoView);
            //TODO Need to find a better way then accessing VideoGLView mediaplayer directly
            mVideoView.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    ImageButton toggleButton = (ImageButton) findViewById(R.id.button_play_pause);
                    toggleButton.setImageResource(R.mipmap.ic_pause);
                    //player.pause();
                    PauseVideo();
                    togglePlay = false;
                    Toast.makeText(MediaActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * "stop" Video.
     */
    public void StopVideo() {
        if(mVideoView != null) {
//            hideUIElement(Techniques.FadeOut, 700, mVideoView);
            VideoUpdate = mVideoView.StopVideo();
//            frameLayout.removeView(mVideoView);
            //Pause on load activity to let everything load
            new CountDownTimer(2000, 500) {

                public void onTick(long millisUntilFinished) {
                    // You don't need anything here
                }

                public void onFinish() {

                }

            }.start();
        }
    }
    /**
     * "pause" Video.
     */
    public void PauseVideo() {
        if(mVideoView != null) {
            VideoUpdate = mVideoView.PauseVideo();
        }
    }

    /**
     * "start" Camera.
     */
    public void StartCamera() {
        //mCameraView = (CameraGLView) findViewById(R.id.cameraView);
        if(mCameraView == null) {
            mCameraView = new CameraGLView(MediaActivity.this);
            mCameraView.setSharedData(mSharedData);
            frameLayout.addView(mCameraView);
            //mVideoView.setVideoSize(1280, 720);
            mCameraView.setVideoSize(mSharedData.videoWidth, mSharedData.videoHeight);
            findViewById(R.id.UILayout).bringToFront();
            mCameraView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int scale_mode = (mCameraView.getScaleMode() + 1) % 4;
                    mCameraView.setScaleMode(scale_mode);
                    updateScaleModeText();
                }
            });
        }
        else {
            mCameraView.onResume();
        }
//        showUIElement(Techniques.FadeIn, 700, mCameraView);
        mScaleModeView = (TextView) findViewById(R.id.info);
        updateScaleModeText();
    }
    /**
     * "stop" Camera.
     */
    public void StopCamera() {
        if(mCameraView != null) {
//            hideUIElement(Techniques.FadeOut, 700, mCameraView);
            frameLayout.removeView(mCameraView);
        }
    }
    /**
     * onClick handler for "pause" button.
     */
    public void PauseCamera() {
        mCameraView.onPause();
    }

    private void updateScaleModeText() {
        final int scale_mode = mCameraView.getScaleMode();
        mScaleModeView.setText(
                scale_mode == 0 ? "scale to fit"
                        : (scale_mode == 1 ? "keep aspect(viewport)"
                        : (scale_mode == 2 ? "keep aspect(matrix)"
                        : (scale_mode == 3 ? "keep aspect(crop center)" : ""))));
    }
    /**
     * start resorcing
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    private void startRecording() {
        Toast.makeText(MediaActivity.this, "startRecording:" , Toast.LENGTH_SHORT).show();
        try {
            mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                if(toggleMode) {
                    new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mVideoView.getVideoWidth(), mVideoView.getVideoHeight());
                }
                else
                    new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraView.getVideoWidth(), mCameraView.getVideoHeight());
            }
            if (true) {
                // for audio capturing
                if(toggleMode) {//TODO Priority: record audio from video not mic
                    new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
                }
                else
                    new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
//            mRecordButton.setColorFilter(0);
//            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * request stop recording
     */
    private void stopRecording() {
        Toast.makeText(MediaActivity.this, "stopRecording:mMuxer=" + mMuxer, Toast.LENGTH_SHORT).show();
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
        //TODO Attempt to make file viewable in gallery
        MiscUtils.BroadcastGallery(mVideoFolder, MediaActivity.this);
        //TODO put a countdown while file saved
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            Toast.makeText(MediaActivity.this, "onPrepared:encoder=" + encoder, Toast.LENGTH_SHORT).show();

            if (encoder instanceof MediaVideoEncoder) {
                if(toggleMode) {
                    mVideoView.setVideoEncoder((MediaVideoEncoder)encoder);
                }
                else
                    mCameraView.setVideoEncoder((MediaVideoEncoder)encoder);
            }
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            Toast.makeText(MediaActivity.this, "onStopped:encoder=" + encoder, Toast.LENGTH_SHORT).show();
            if (encoder instanceof MediaVideoEncoder) {

                if(toggleMode) {
                    mVideoView.setVideoEncoder(null);
                }
                else
                    mCameraView.setVideoEncoder(null);
            }
        }
    };

    /**
     * functions to get video from gallery
     */
    //Alert options
    private String userChoosenTask;
    //File URL
    Uri videoAddr;
    private int SELECT_FILE = 1;

    private void selectVideo() {
//        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
//                "Cancel" };

        final CharSequence[] items = { "Choose from Gallery",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MediaActivity.this);
        builder.setTitle("Add Video!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MediaActivity.this);

//                if (items[item].equals("Take Photo")) {
//                    userChoosenTask ="Take Photo";
//                    if(result)
//                        cameraIntent();
//
//                } else
                if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask ="Choose from Gallery";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("video/*");
//        image/jpeg
//        audio/mpeg4-generic
//        text/html
//        audio/mpeg
//        audio/aac
//        audio/wav
//        audio/ogg
//        audio/midi
//        audio/x-ms-wma
//        video/mp4
//        video/x-msvideo
//        video/x-ms-wmv
//        image/png
//        image/jpeg
//        image/gif
//                .xml ->text/xml
//            .txt -> text/plain
//            .cfg -> text/plain
//            .csv -> text/plain
//            .conf -> text/plain
//            .rc -> text/plain
//            .htm -> text/html
//            .html -> text/html
//            .pdf -> application/pdf
//            .apk -> application/vnd.android.package-archive
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
//            else if (requestCode == REQUEST_CAMERA)
//                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        if (data != null) {
            videoAddr = data.getData();
            VideoUpdate = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if(userChoosenTask.equals("Take Photo"))
//                        cameraIntent();
//                    else
                    if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private static class Utility {
        public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public static boolean checkPermission(final Context context)
        {
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
            {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Permission necessary");
                        alertBuilder.setMessage("External storage permission is necessary");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        });
                        android.support.v7.app.AlertDialog alert = alertBuilder.create();
                        alert.show();

                    } else {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }


    /**
     * use to give a pause to show animation before hiding UI element
     * @param animType
     * @param Duration
     * @param hideUIbyId
     */
    private static void hideUIElement(Techniques animType, int Duration, final View hideUIbyId) {

        YoYo.with(animType).duration(Duration).playOn(hideUIbyId);

        new CountDownTimer(700, 10) {

            public void onTick(long millisUntilFinished) {
                // You don't need anything here
            }

            public void onFinish() {
                hideUIbyId.setVisibility(View.GONE);
            }
        }.start();
    }

    /**
     * use to give a pause to show animation on showing UI element
     * @param animType
     * @param Duration
     * @param hideUIbyId
     */
    private static void showUIElement(Techniques animType, int Duration, final View hideUIbyId) {

        hideUIbyId.setVisibility(View.VISIBLE);
        YoYo.with(animType).duration(Duration).playOn(hideUIbyId);
    }

}
