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