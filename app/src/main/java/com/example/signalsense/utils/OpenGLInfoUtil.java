package com.example.signalsense.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;

public class OpenGLInfoUtil {
    public static String getOpenGLInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("GL Version: ").append(configurationInfo.getGlEsVersion()).append("\n");

        // Check if OpenGL ES 2.0 is supported
        if (configurationInfo.reqGlEsVersion >= 0x20000) {
            sb.append("Renderer: ").append(GLES20.glGetString(GLES20.GL_RENDERER)).append("\n");
            sb.append("Vendor: ").append(GLES20.glGetString(GLES20.GL_VENDOR)).append("\n");
            sb.append("Version: ").append(GLES20.glGetString(GLES20.GL_VERSION)).append("\n");
            sb.append("Extensions: ").append(GLES20.glGetString(GLES20.GL_EXTENSIONS));
        } else {
            sb.append("OpenGL ES 2.0 is not supported on this device.");
        }

        return sb.toString();
    }
}

