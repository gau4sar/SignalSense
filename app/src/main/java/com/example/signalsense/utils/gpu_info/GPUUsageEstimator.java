package com.example.signalsense.utils.gpu_info;

import android.opengl.GLES30;
import android.util.Log;

public class GPUUsageEstimator {

    private static final long FRAME_TIME_THRESHOLD = 150_000;
    private long lastFrameTimestamp;
    private static final long ONE_SECOND_NANOSECONDS = 1_000_000_000; // 1 second in nanoseconds

    private double maxGPUUsageUnder1Sec; // Keep track of the maximum observed GPU usage
    private long observationStartTime; // Start time of the 1-second observation interval
    public GPUUsageEstimator() {
        maxGPUUsageUnder1Sec = 0.0;
        observationStartTime = System.nanoTime();
    }

    public void startFrame() {

        // Define two timestamp queries
        int[] timestampQueries = new int[2];
        GLES30.glGenQueries(2, timestampQueries, 0);

        lastFrameTimestamp = System.nanoTime();
        Log.d("gpuinfo","GPUUsagexxx startFrame ->"+lastFrameTimestamp);
    }

    public double endFrame() {
        long frameTime = System.nanoTime() - lastFrameTimestamp;

        // Calculate the estimated GPU usage percentage based on frame time
        long gpuUsage =  frameTime / FRAME_TIME_THRESHOLD;

        // Check if we need to update the maxGPUUsage within the 1-second interval
        long currentTime = System.nanoTime();
        if (currentTime - observationStartTime >= ONE_SECOND_NANOSECONDS) {

            // If 1 second has passed, reset the observation interval and update maxGPUUsage
            observationStartTime = currentTime;
            maxGPUUsageUnder1Sec = gpuUsage;
        } else {
            // Otherwise, update maxGPUUsage if the current GPU usage is higher
            if (gpuUsage > maxGPUUsageUnder1Sec) {
                maxGPUUsageUnder1Sec = gpuUsage;
            }
        }

        return maxGPUUsageUnder1Sec;
    }
}
