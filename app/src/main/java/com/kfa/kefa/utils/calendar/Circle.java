package com.kfa.kefa.utils.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.kfa.kefa.R;

public class Circle extends View {
    private Paint paint;
    private View parentView;
    private Context context;
    int i,x,y;
    public Circle(Context context,View parentView, int i) {
        super(context);
        this.context = context;
        this.paint = new Paint();
        this.parentView = parentView;
        this.i = i;
        int color_interested = ContextCompat.getColor(context, R.color.colorGold);
        paint.setColor(color_interested);

    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (i==0){}
        else if(i==1){
            x = getWidth()/2;
            y = getHeight()*3/4;
            canvas.drawCircle(x, y, 7, paint);
        }
        else if(i==2){
            x = getWidth() * 3/  5;
            y = getHeight()*3/4;
            canvas.drawCircle(x, y, 7, paint);
            x = getWidth() * 2 / 5;
            y = getHeight() * 3 / 4;
            canvas.drawCircle(x, y, 7, paint);
        }
        else if(i==3) {

        }
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
