package com.tecrt.rohitthomas.tecrtmediasample;


import android.os.Bundle;

import com.tecrt.rohitthomas.tecrtmedia.MediaActivity;

public class VideoEditActivity  extends MediaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toggleMode = true;
    }
}
