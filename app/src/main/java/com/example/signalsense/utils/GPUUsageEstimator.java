package com.example.signalsense.utils;

import android.opengl.GLES30;
import android.util.Log;

public class GPUUsageEstimator {

    private static final long FRAME_TIME_THRESHOLD = 1_066_667; // 16.67ms for 60Hz display
    private long lastFrameTimestamp;
    private int highFrameCount;
    private int totalFrameCount;

    public void startFrame() {

        // Define two timestamp queries
        int[] timestampQueries = new int[2];
        GLES30.glGenQueries(2, timestampQueries, 0);

        lastFrameTimestamp = System.nanoTime();
        Log.d("gpuinfo","gpuinfoxx startFrame ->"+lastFrameTimestamp);
    }

    public double endFrame() {
        long frameTime = System.nanoTime() - lastFrameTimestamp;
        totalFrameCount++;

        Log.d("gpuinfo","gpuinfoxx FRAME_TIME_THRESHOLD ->"+FRAME_TIME_THRESHOLD);
        if (frameTime > FRAME_TIME_THRESHOLD) {
            highFrameCount++;
        }


        Log.d("gpuinfo","gpuinfoxx frameTime ->"+frameTime);
        Log.d("gpuinfo","gpuinfoxx highFrameCount ->"+highFrameCount);

        // Calculate the estimated GPU usage percentage
        return (double) highFrameCount / totalFrameCount * 100;
    }
}
