package com.tecrt.rohitthomas.tecrtmedia;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.tecrt.rohitthomas.tecrtmedia.encoder.MediaVideoEncoder;
import com.tecrt.rohitthomas.tecrtmedia.glutils.GLDrawer2D;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by rohitthomas on 28/06/16.
 */
public class VideoGLView extends GLSurfaceView {

    private String TAG = "VideoGLView";

    VideoRender mRenderer;
    public MediaPlayer mMediaPlayer = null;
    private File file = null;
    private String filePath = null;
    private Uri uri = null;

    public VideoGLView(Context context, File file) {
        super(context);

        this.file = file;

        init();
        StartVideo(true,null);

    }

    public VideoGLView(Context context, String filePath) {
        super(context);

        this.filePath = filePath;

        init();
        StartVideo(true,null);

    }

    public VideoGLView(Context context, Uri uri) {
        super(context);

        this.uri = uri;

        init();
        StartVideo(true,null);
    }

    public VideoGLView(Context context, boolean videoUpdate, MediaPlayer player) {
        super(context);
        init();
        StartVideo(videoUpdate, player);
    }

    private void init() {

        setEGLContextClientVersion(2);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mRenderer = new VideoRender(this);
        setRenderer(mRenderer);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        StopVideo();
    }

    @Override
    protected void onDetachedFromWindow() {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();

        if (mMediaPlayer != null) {
//            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    /**
     * onClick handler for "play" button.
     */
    public boolean StartVideo(boolean VideoUpdate, MediaPlayer player) {
        if ( VideoUpdate ) {
            if (player != null ) {
                mMediaPlayer = player;
            }
            else {

                mMediaPlayer = new MediaPlayer();

                if (file != null) {
                    try {
                        mMediaPlayer.setDataSource(file.getAbsolutePath());
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (filePath != null) {
                    try {
                        mMediaPlayer.setDataSource(filePath);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (uri != null) {
                    try {
                        mMediaPlayer.setDataSource(getContext(), uri);
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            //SurfaceView mediaView = (SurfaceView)findViewById(R.id.mediaGLView);
            //mMediaPlayer.setDisplay(mediaView.getHolder());
            //player.setSurface(new Surface(mVideoRecordRenderThread.getVideoTexture()));
            mRenderer.mSTexture = new SurfaceTexture(mRenderer.mTextureID);
            mRenderer.mSTexture.setOnFrameAvailableListener(mRenderer);
            Surface surface = new Surface(mRenderer.mSTexture);
            mMediaPlayer.setSurface(surface);
            surface.release();
            try {
                mMediaPlayer.prepare();
            } catch (IOException t) {

                Toast.makeText(getContext(), "media player prepare failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "media player prepare failed");
            }

            synchronized (this) {
                mRenderer.requesrUpdatSTexture = false;
            }

            mMediaPlayer.start();

        }
        else
            mMediaPlayer.start();

        return false;
    }
    /**
     * onClick handler for "stop" button.
     */
    public boolean StopVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        return true;
    }
    /**
     * onClick handler for "pause" button.
     */
    public boolean PauseVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
        return false;
    }

    private static final int SCALE_STRETCH_FIT = 0;
    private static final int SCALE_KEEP_ASPECT_VIEWPORT = 1;
    private static final int SCALE_KEEP_ASPECT = 2;
    private static final int SCALE_CROP_CENTER = 3;
    private int mVideoWidth, mVideoHeight;
    private int mScaleMode = SCALE_STRETCH_FIT;

    //TODO control scale mode and size
    public void setScaleMode(final int mode) {
//        if (mScaleMode != mode) {
//            mScaleMode = mode;
//            queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    mRenderer.updateViewport();
//                }
//            });
//        }
    }

    public int getScaleMode() {
        return mScaleMode;
    }

    public void setVideoSize(final int width, final int height) {
//        if ((mRotation % 180) == 0) {
            mVideoWidth = width;
            mVideoHeight = height;
//        } else {
//            mVideoWidth = height;
//            mVideoHeight = width;
//        }
//        queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                mRenderer.updateViewport();
//            }
//        });
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
//        if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
        mRenderer.onSurfaceDestroyed();
        super.surfaceDestroyed(holder);
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        Toast.makeText(getContext(), "setVideoEncoder:tex_id=" + mRenderer.mTextureID + ",encoder=" + encoder , Toast.LENGTH_SHORT).show();
        //if (DEBUG) Log.v(TAG, "setVideoEncoder:tex_id=" + mRenderer.mTextureID + ",encoder=" + encoder);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (mRenderer) {
                    if (encoder != null) {
                        encoder.setEglContext(EGL14.eglGetCurrentContext(), mRenderer.mTextureID);
                    }
                    mRenderer.mVideoEncoder = encoder;
                }
            }
        });
    }

    /**
     * Setter for shared data.
     */
    public void setSharedData(tecrtData sharedData) {
        mRenderer.setSharedData(sharedData);
    }

    private class VideoRender implements Renderer,
            SurfaceTexture.OnFrameAvailableListener {
        private String TAG = "VideoRender";

        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES;
        private static final int TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 0;

        private SurfaceTexture mSTexture;
        private volatile boolean requesrUpdatSTexture = false;
        private int mTextureID;
        private GLDrawer2D mDrawer;
        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];
        private MediaVideoEncoder mVideoEncoder;

        private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

        public VideoRender(final VideoGLView parent) {

            Matrix.setIdentityM(mSTMatrix, 0);
            Matrix.setIdentityM(mMVPMatrix, 0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // create object for preview display
            mDrawer = new GLDrawer2D();
            mDrawer.setMatrix(mMVPMatrix, 0);

//
//            StartVideo(true,null);

        }

        @Override
        public void onSurfaceChanged(GL10 glUnused, int width, int height) {

            GLES20.glViewport(0, 0, width, height);
            mDrawer.setsurfacesize(width,height);

        }

        /**
         * when GLSurface context is soon destroyed
         */
        public void onSurfaceDestroyed() {
//            if (DEBUG) Log.v(TAG, "onSurfaceDestroyed:");
            if (mDrawer != null) {
                mDrawer.release();
                mDrawer = null;
            }
            if (mSTexture != null) {
                mSTexture.release();
                mSTexture = null;
            }
            GLDrawer2D.deleteTex(mTextureID);
        }

        private boolean flip = true;
        /**
         * drawing to GLSurface
         * we set renderMode to GLSurfaceView.RENDERMODE_WHEN_DIRTY,
         * this method is only called when #requestRender is called(= when texture is required to update)
         * if you don't set RENDERMODE_WHEN_DIRTY, this method is called at maximum 60fps
         */
        @Override
        public void onDrawFrame(final GL10 glUnused) {

            synchronized (this) {
                if (requesrUpdatSTexture) {
                    requesrUpdatSTexture = false;
                    // update texture(came from camera)
                    mSTexture.updateTexImage();
                    // get texture matrix
                    mSTexture.getTransformMatrix(mSTMatrix);
                }
            }

            // draw to preview screen
            mDrawer.draw(mTextureID, mSTMatrix);

            checkGlError("glDrawArrays");
//            GLES20.glFinish();

            flip = !flip;
            if (flip) {	// ~30fps
                synchronized (this) {
                    if (mVideoEncoder != null) {
                        // notify to capturing thread that the camera frame is available.
//						mVideoEncoder.frameAvailableSoon(mSTMatrix);
                        mVideoEncoder.frameAvailableSoon(mSTMatrix, mMVPMatrix);
                    }
                }
            }

        }

        @Override
        synchronized public void onFrameAvailable(SurfaceTexture surface) {
            synchronized (this)
            {
                requesrUpdatSTexture = true;
            }
        }

        private void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Toast.makeText(getContext(), op + ": glError " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        /**
         * Setter for shared data.
         */
        public void setSharedData(tecrtData sharedData) {
            if(mDrawer !=null) mDrawer.setSharedData(sharedData);
        }

    }

}
