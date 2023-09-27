package com.example.signalsense.utils;

import static org.lwjgl.opengl.ARBTimerQuery.GL_TIMESTAMP;
import static org.lwjgl.opengl.ARBTimerQuery.glQueryCounter;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {

    private int[] timestampQueries = new int[2];

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Initialize OpenGL settings, shaders, textures, etc.
        // This is where you set up your rendering environment.
    }

    private long frameStartTime; // Initialize this in your rendering setup

    // Call this function at the start of each frame rendering
    private void startFrameTiming() {
        frameStartTime = System.nanoTime();
    }

    // Call this function at the end of each frame rendering
    private long endFrameTiming() {
        long frameEndTime = System.nanoTime();
        return frameEndTime - frameStartTime; // Frame time in nanoseconds
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // Start timestamp
        glQueryCounter(timestampQueries[0], GL_TIMESTAMP);

        // Start frame timing
        startFrameTiming();


        // Render your scene here
        // This is where you would draw your 2D/3D objects, apply shaders, etc.



        // End frame timing
        long frameTime = endFrameTiming();

        // End timestamp
        glQueryCounter(timestampQueries[1], GL_TIMESTAMP);

        // Calculate GPU rendering time and utilization percentage
        long gpuRenderingTime = getGpuRenderingTime();
        double gpuUtilizationPercentage = (gpuRenderingTime / (double)frameTime) * 100.0;

        // Update and display GPU utilization percentage
        // You can log it or display it in your app's UI as needed.

        Log.d("gpuinfo","gpuUtilizationPercentage => "+gpuUtilizationPercentage);
    }

    private long getGpuRenderingTime() {
        // Retrieve the timestamp values from the queries
        int[] queryResults = new int[2];
        GLES30.glGetQueryObjectuiv(timestampQueries[0], GLES30.GL_QUERY_RESULT, queryResults, 0);
        GLES30.glGetQueryObjectuiv(timestampQueries[1], GLES30.GL_QUERY_RESULT, queryResults, 1);

        // Calculate the GPU rendering time in nanoseconds
        long startTime = queryResults[0];
        long endTime = queryResults[1];
        long gpuRenderingTime = endTime - startTime;

        // The time is in nanoseconds; you can convert it to milliseconds if needed
        // long gpuRenderingTimeMillis = gpuRenderingTime / 1000000;

        return gpuRenderingTime;
    }


    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // Handle changes in the surface size, viewport, projection matrix, etc.
    }

    // Implement other methods and functions as needed for your rendering setup
}
