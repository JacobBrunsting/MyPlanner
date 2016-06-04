package com.myplanner.myplanner;


import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class CustomAnimation {
    // returns an animation to shift a view a specified amount by adjusting its margins
    public static Animation shiftViewVertical(final float shiftAmount, final int duration, final View view) {
        final float oldMargin = ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin;
        final Animation shiftDown = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                float multiplier;
                if (interpolatedTime >= 0.5) {
                    multiplier = (float) (Math.pow((interpolatedTime * 2) - 1, 0.7) + 1) / 2;
                } else {
                    multiplier = (float) ((-Math.pow(-((interpolatedTime * 2) - 1), 0.7)) + 1) / 2;

                }
                params.topMargin = (int)(oldMargin + (shiftAmount * multiplier));
                view.setLayoutParams(params);
            }
        };
        shiftDown.setDuration(duration);
        return shiftDown;
    }

    // return an animation to fade the alpha of the provided view to the specified value (from 0-1)
    public static Animation fadeView(final float alpha, final int duration, final View view) {
        final float initialAlpha = view.getAlpha();
        final float alphaChange = alpha - initialAlpha;
        final Animation shiftAlpha = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                final float newAlpha = initialAlpha + (alphaChange * interpolatedTime);
                view.setAlpha(newAlpha);
            }
        };
        shiftAlpha.setDuration(duration);
        return shiftAlpha;
    }
}
