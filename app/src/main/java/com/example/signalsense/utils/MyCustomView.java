package com.example.signalsense.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;

public class MyCustomView extends View {

    public MyCustomView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Simulate a highly graphics-intensive task
        Random random = new Random();

        for (int i = 0; i < 500000000; i++) {
            int left = random.nextInt(getWidth());
            int top = random.nextInt(getHeight());
            int right = left + random.nextInt(200); // Random width
            int bottom = top + random.nextInt(200); // Random height

            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);

            Paint paint = new Paint();
            paint.setColor(Color.rgb(red, green, blue));
            paint.setStyle(Paint.Style.FILL);

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}
