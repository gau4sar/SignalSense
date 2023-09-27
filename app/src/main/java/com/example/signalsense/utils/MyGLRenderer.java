package com.example.signalsense.utils;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Initialize OpenGL settings here
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Handle surface changes (e.g., screen rotation)
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Render the scene and retrieve GPU information
        String vendor = GLES30.glGetString(GLES30.GL_VENDOR);
        String renderer = GLES30.glGetString(GLES30.GL_RENDERER);
        String VERSION = GLES30.glGetString(GLES30.GL_VERSION);
        String EXTENSIONS = GLES30.glGetString(GLES30.GL_EXTENSIONS);


        // You can also retrieve GPU load or other information here

        // Log or display the GPU information as needed
        Log.d("GPU Info", "GPU Info Vendor: " + vendor + ", Renderer: " + renderer);
        // Log or display the GPU information as needed
        Log.d("GPU Info", "GPU Info VERSION: " + VERSION + ", EXTENSIONS: " + EXTENSIONS);

        try {
            FileInputStream fis = new FileInputStream("/sys/class/kgsl/kgsl-3d0/gpuclk");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                // The line contains GPU clock frequency information
                // Process and display the information as needed
                Log.d("GPU Info", "GPU Info line: " + line);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
