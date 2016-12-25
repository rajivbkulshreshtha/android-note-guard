package com.rajiv.noteguard;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rajiv on 24-12-2016.
 */

public class PointCollector implements View.OnTouchListener {

    public final static int NUM_POINT = 4;
    List<Point> pointList = new ArrayList<Point>();
    PointCollectorListener listener;

    public PointCollectorListener getListener() {
        return listener;
    }

    public void setListener(PointCollectorListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        pointList.add(new Point(x, y));

        if (pointList.size() == NUM_POINT) {
            if (listener != null) {
                listener.pointCollected(pointList);
            }
        }

        return false;
    }


    public void clear() {
        pointList.clear();
    }

}
