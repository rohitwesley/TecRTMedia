package com.tecrt.rohitthomas.tecrtmedia.glutils;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: GLDrawer2D.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


import com.tecrt.rohitthomas.tecrtmedia.tecrtData;
import com.tecrt.rohitthomas.tecrtmedia.tecrtShader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Helper class to draw to whole view using specific texture and texture matrix
 */
public class GLDrawer2D {
    private static final boolean DEBUG = false; // TODO set false on release
    private static final String TAG = "GLDrawer2D";

    private static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
    private static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

    private final FloatBuffer pVertex;
    private final FloatBuffer pTexCoord;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    private final float[] mMvpMatrix = new float[16];
    // Shared data instance.
    private tecrtData mSharedData = new tecrtData();
    //Shaders
    private final tecrtShader shaderP = new tecrtShader();
    private final tecrtShader shaderIntro = new tecrtShader();
    private final tecrtShader mShaderFilterAnsel = new tecrtShader();
    private final tecrtShader mShaderFilterBlackAndWhite = new tecrtShader();
    private final tecrtShader mShaderFilterCartoon = new tecrtShader();
    private final tecrtShader mShaderFilterIntro = new tecrtShader();
    private final tecrtShader mShaderFilterEdges = new tecrtShader();
    private final tecrtShader mShaderFilterGeorgia = new tecrtShader();
    private final tecrtShader mShaderFilterPolaroid = new tecrtShader();
    private final tecrtShader mShaderFilterRetro = new tecrtShader();
    private final tecrtShader mShaderFilterSahara = new tecrtShader();
    private final tecrtShader mShaderFilterSepia = new tecrtShader();

    private static final int FLOAT_SZ = Float.SIZE / 8;
    private static final int VERTEX_NUM = 4;
    private static final int VERTEX_SZ = VERTEX_NUM * 2;
    private int surfacewidth, surfaceheight;

    /**
     * Constructor
     * this should be called in GL context
     */
    public GLDrawer2D() {

        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);


        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc( GLES20.GL_LEQUAL );  // Passes if the incoming depth value is less than or equal to the stored depth value.
        //GLES20.glDepthMask( true ); // enable writing into the depth buffer

        // cull backface
        GLES20.glEnable( GLES20.GL_CULL_FACE );
        GLES20.glCullFace(GLES20.GL_BACK);

        pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(VERTICES);
        pVertex.flip();
        pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put(TEXCOORD);
        pTexCoord.flip();

        //shaderP.setProgram("basic");
        shaderP.setProgram("adjustment");
        mShaderFilterBlackAndWhite.setProgram("blacknwhite");
        mShaderFilterAnsel.setProgram("ansel");
        mShaderFilterSepia.setProgram("sepia");
        mShaderFilterRetro.setProgram("retro");
        mShaderFilterGeorgia.setProgram("georgia");
        mShaderFilterSahara.setProgram("sahara");
        mShaderFilterPolaroid.setProgram("polaroid");
        mShaderFilterCartoon.setProgram("cartoon");
        mShaderFilterEdges.setProgram("edges");
        mShaderFilterIntro.setProgram("intro");
        shaderP.useProgram();
        maPositionLoc = shaderP.getAHandle("aPosition");
        maTextureCoordLoc = shaderP.getAHandle("aTextureCoord");
        muMVPMatrixLoc = shaderP.getUHandle("uMVPMatrix");
        muTexMatrixLoc = shaderP.getUHandle("uTexMatrix");

        Matrix.setIdentityM(mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);


    }

    /**
     * terminatinng, this should be called in GL context
     */
    public void release() {
        if (shaderP != null) GLES20.glDeleteProgram(shaderP.getProgram());
        if (mShaderFilterBlackAndWhite != null) GLES20.glDeleteProgram(mShaderFilterBlackAndWhite.getProgram());
        if (mShaderFilterAnsel != null) GLES20.glDeleteProgram(mShaderFilterAnsel.getProgram());
        if (mShaderFilterSepia != null) GLES20.glDeleteProgram(mShaderFilterSepia.getProgram());
        if (mShaderFilterRetro != null) GLES20.glDeleteProgram(mShaderFilterRetro.getProgram());
        if (mShaderFilterGeorgia != null) GLES20.glDeleteProgram(mShaderFilterGeorgia.getProgram());
        if (mShaderFilterSahara != null) GLES20.glDeleteProgram(mShaderFilterSahara.getProgram());
        if (mShaderFilterPolaroid != null) GLES20.glDeleteProgram(mShaderFilterPolaroid.getProgram());
        if (mShaderFilterCartoon != null) GLES20.glDeleteProgram(mShaderFilterCartoon.getProgram());
        if (mShaderFilterEdges != null) GLES20.glDeleteProgram(mShaderFilterEdges.getProgram());
        if (mShaderFilterIntro != null) GLES20.glDeleteProgram(mShaderFilterIntro.getProgram());
    }


    /**
     * draw specific texture with specific texture matrix
     * @param tex_id texture ID
     * @param tex_matrix texture matrix、if this is null, the last one use(we don't check size of this array and needs at least 16 of float)
     */
    public void draw(final int tex_id, final float[] tex_matrix) {

        mSharedData.time++;

        GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        tecrtShader shader;

//		GLES20.glUseProgram(hProgram);
        // Draw Video texture
        switch (mSharedData.mFilter) {
            case 1:
                shader = mShaderFilterBlackAndWhite;
                shader.useProgram();
                break;
            case 2:
                shader = mShaderFilterAnsel;
                shader.useProgram();
                break;
            case 3:
                shader = mShaderFilterSepia;
                shader.useProgram();
                break;
            case 4:
                shader = mShaderFilterRetro;
                shader.useProgram();
                break;
            case 5:
                shader = mShaderFilterGeorgia;
                shader.useProgram();
                break;
            case 6:
                shader = mShaderFilterSahara;
                shader.useProgram();
                break;
            case 7:
                shader = mShaderFilterPolaroid;
                shader.useProgram();
                break;
            case 8:
                shader = mShaderFilterCartoon;
                shader.useProgram();
                break;
            case 9:
                shader = mShaderFilterEdges;
                shader.useProgram();
                break;
            case 10:
                shader = mShaderFilterIntro;
                shader.useProgram();
            default:
                if(mSharedData.startIntro){
                    shader = mShaderFilterIntro;
                    if(mSharedData.time/30>mSharedData.introsec){
                        mSharedData.startIntro = false;
                    }
                }
                else {
                    shader = shaderP;
                }
                shader.useProgram();
                break;
        }
        int uBrightness = shader.getUHandle("uBrightness");
        int uContrast = shader.getUHandle("uContrast");
        int uSaturation = shader.getUHandle("uSaturation");
        int uCornerRadius = shader.getUHandle("uCornerRadius");
        int uPixelSize = shader.getUHandle("iResolution");
        int uTime = shader.getUHandle("uTime");


        // Store uniform variables into use.
        GLES20.glUniform2f(uPixelSize, (float) mSharedData.videoWidth, (float) mSharedData.videoHeight);
        GLES20.glUniform1f(uTime, (float) mSharedData.time);
        GLES20.glUniform1f(uBrightness, mSharedData.mBrightness);
        GLES20.glUniform1f(uContrast, mSharedData.mContrast);
        GLES20.glUniform1f(uSaturation, mSharedData.mSaturation);
        GLES20.glUniform1f(uCornerRadius, mSharedData.mCornerRadius);

        if (tex_matrix != null)
            GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex_id);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

//        GLES20.glUseProgram(0);
//        GLES20.glFinish();
//        GLES20.glFlush();
    }

    /**
     * Set model/view/projection transform matrix
     * @param matrix
     * @param offset
     */
    public void setMatrix(final float[] matrix, final int offset) {
        if ((matrix != null) && (matrix.length >= offset + 16)) {
            System.arraycopy(matrix, offset, mMvpMatrix, 0, 16);
        } else {
            Matrix.setIdentityM(mMvpMatrix, 0);
        }
    }
    /**
     * create external texture
     * @return texture ID
     */
    public static int initTex() {
        if (DEBUG) Log.v(TAG, "initTex:");
        final int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,  GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        return tex[0];
    }

    /**
     * delete specific texture
     */
    public static void deleteTex(final int hTex) {
        if (DEBUG) Log.v(TAG, "deleteTex:");
        final int[] tex = new int[] {hTex};
        GLES20.glDeleteTextures(1, tex, 0);
    }

    /**
     * Setter for shared data.
     */
    public void setSharedData(tecrtData sharedData) {
        mSharedData = sharedData;
    }

    public void setsurfacesize(int width, int height) {
        surfacewidth = width;
        surfaceheight = height;
    }
}
