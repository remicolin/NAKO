package com.kfa.kefa.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;

public class MyMapView extends MapView {
    private boolean intercepMove = false;

    public MyMapView(@NonNull Context context) {
        super(context);
    }

    public MyMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMapView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyMapView(@NonNull Context context, @Nullable GoogleMapOptions options) {
        super(context, options);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Prevent  the ViewPager to swipe instead of scrolling
        // See https://stackoverflow.com/questions/8594361/horizontal-scroll-view-inside-viewpager
        // Touching the borders allow the view pager to swipe
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // See if we touch the screen borders
                intercepMove = 100 * event.getX() > 5 * getWidth() && 100 * event.getX() < 95 * getWidth();
                break;
            case MotionEvent.ACTION_MOVE:
                if (intercepMove && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
        }
        return false;

    }
}
