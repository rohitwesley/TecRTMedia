package com.tecrt.rohitthomas.tecrtmediasample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaAudioEncoder;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaEncoder;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaMuxerWrapper;
import com.tecrt.rohitthomas.tecrtmediasample.encoder.MediaVideoEncoder;

import java.io.IOException;

public class CameraCaptureActivity extends MediaActivity {

    CameraCaptureActivity(){
        super();
        toggleMode = false;
    }
}

//{
//    private static final boolean DEBUG = false;	// TODO set false on release
//        private static final String TAG = "CameraFragment";
//
//        /**
//         * for camera preview display
//         */
//        private CameraGLView mCameraView;
//        /**
//         * for scale mode display
//         */
//        private TextView mScaleModeView;
//        /**
//         * button for start/stop recording
//         */
//        private ImageButton mRecordButton;
//        /**
//         * muxer for audio/video recording
//         */
//        private MediaMuxerWrapper mMuxer;
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_camera_capture);
//
//            mCameraView = (CameraGLView) findViewById(R.id.cameraView);
//            mCameraView.setVideoSize(1280, 720);
//            mCameraView.setOnClickListener(mOnClickListener);
//            mScaleModeView = (TextView) findViewById(R.id.scalemode_textview);
//            updateScaleModeText();
//            mRecordButton = (ImageButton) findViewById(R.id.record_button);
//            mRecordButton.setOnClickListener(mOnClickListener);
//        }
//
//        @Override
//        public void onResume() {
//            super.onResume();
//            if (DEBUG) Log.v(TAG, "onResume:");
//            mCameraView.onResume();
//        }
//
//        @Override
//        public void onPause() {
//            if (DEBUG) Log.v(TAG, "onPause:");
//            stopRecording();
//            mCameraView.onPause();
//            super.onPause();
//        }
//
//        /**
//         * method when touch record button
//         */
//        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//                switch (view.getId()) {
//                    case R.id.cameraView:
//                        final int scale_mode = (mCameraView.getScaleMode() + 1) % 4;
//                        mCameraView.setScaleMode(scale_mode);
//                        updateScaleModeText();
//                        break;
//                    case R.id.record_button:
//                        if (mMuxer == null)
//                            startRecording();
//                        else
//                            stopRecording();
//                        break;
//                }
//            }
//        };
//
//        private void updateScaleModeText() {
//            final int scale_mode = mCameraView.getScaleMode();
//            mScaleModeView.setText(
//                    scale_mode == 0 ? "scale to fit"
//                            : (scale_mode == 1 ? "keep aspect(viewport)"
//                            : (scale_mode == 2 ? "keep aspect(matrix)"
//                            : (scale_mode == 3 ? "keep aspect(crop center)" : ""))));
//        }
//
//        /**
//         * start resorcing
//         * This is a sample project and call this on UI thread to avoid being complicated
//         * but basically this should be called on private thread because prepareing
//         * of encoder is heavy work
//         */
//        private void startRecording() {
//            if (DEBUG) Log.v(TAG, "startRecording:");
//            try {
//                mRecordButton.setColorFilter(0xffff0000);	// turn red
//                mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.
//                if (true) {
//                    // for video capturing
//                    new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraView.getVideoWidth(), mCameraView.getVideoHeight());
//                }
//                if (true) {
//                    // for audio capturing
//                    new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
//                }
//                mMuxer.prepare();
//                mMuxer.startRecording();
//            } catch (final IOException e) {
//                mRecordButton.setColorFilter(0);
//                Log.e(TAG, "startCapture:", e);
//            }
//        }
//
//        /**
//         * request stop recording
//         */
//        private void stopRecording() {
//            if (DEBUG) Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
//            mRecordButton.setColorFilter(0);	// return to default color
//            if (mMuxer != null) {
//                mMuxer.stopRecording();
//                mMuxer = null;
//                // you should not wait here
//            }
//        }
//
//        /**
//         * callback methods from encoder
//         */
//        private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
//            @Override
//            public void onPrepared(final MediaEncoder encoder) {
//                if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
//                if (encoder instanceof MediaVideoEncoder)
//                    mCameraView.setVideoEncoder((MediaVideoEncoder)encoder);
//            }
//
//            @Override
//            public void onStopped(final MediaEncoder encoder) {
//                if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
//                if (encoder instanceof MediaVideoEncoder)
//                    mCameraView.setVideoEncoder(null);
//            }
//        };
//}