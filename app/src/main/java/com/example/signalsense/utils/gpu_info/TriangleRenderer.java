package com.example.signalsense.utils.gpu_info;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRenderer implements GLSurfaceView.Renderer {
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private Triangle mTriangle;
    private float angle = 0.0f; // Initial rotation angle


    GPUUsageEstimator mGpuUsageEstimator;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mGpuUsageEstimator = new GPUUsageEstimator(); // Reference to GPUUsageEstimator

        // Initialize the triangle
        mTriangle = new Triangle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // Set the projection matrix
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // Set the view matrix
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -5, 0, 0, 0, 0, 1, 0);

        // Calculate the combined matrix
        Matrix.multiplyMM(mModelMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    int helloYou = 1;
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Start measuring GPU usage before rendering the triangle
        mGpuUsageEstimator.startFrame();

        // Update the rotation angle (you can modify the speed as needed)
        angle += 1.0f;

        // Apply rotation to the model matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        // Render the triangle
        mTriangle.draw(mModelMatrix);

        // End GPU usage measurement after rendering the triangle
        double gpuUsage = mGpuUsageEstimator.endFrame();

        if (gpuUsageListenerInterface != null) {
            gpuUsageListenerInterface.onGpuUsageChange(gpuUsage);
        }
    }


    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private GpuUsageListenerInterface gpuUsageListenerInterface;

    public void gpuUsageListener(GpuUsageListenerInterface listener) {
        this.gpuUsageListenerInterface = listener;
    }

    public interface GpuUsageListenerInterface {
        void onGpuUsageChange(double newValue);
    }

}