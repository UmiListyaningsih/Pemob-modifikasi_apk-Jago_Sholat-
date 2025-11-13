package id.duglegir.jagosholat.util;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import id.duglegir.jagosholat.R;
import id.duglegir.jagosholat.ui.compass.KompasFragment;


public class KompasRose extends View {

    private int width = 240;
    private int height = 240;
    private float directionNorth = 0;

    private float centre_x = width * 0.5f;
    private float centre_y = height * 0.5f;

    private Matrix rotateNeedle = new Matrix();


    private Bitmap compassBackground;
    private Bitmap compassNeedle;


    public KompasRose(Context context) {
        super(context);
        initCompassView();
    }
    public KompasRose(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCompassView();
    }
    public KompasRose(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        setMeasuredDimension(width, height);
    }

    private void initCompassView() {


        compassBackground = BitmapFactory.decodeResource(getResources(), R.drawable.kompas_back);
        compassNeedle = BitmapFactory.decodeResource(getResources(), R.drawable.kompas_front);

        if (compassBackground == null || compassNeedle == null) {

            return;
        }


        width = compassBackground.getWidth()*2;
        height = compassBackground.getHeight()*2;

        centre_x = width  * 0.5f;
        centre_y = height * 0.5f;

        rotateNeedle.postTranslate(centre_x - compassNeedle.getWidth()/2, centre_y - compassNeedle.getHeight()/2);
        invalidate();
    }

    public void setDirections(float directionsNorth, float directionsQibla) {

        if (compassNeedle == null) {
            return;
        }

        this.directionNorth = directionsNorth;


        rotateNeedle = new Matrix();
        float degree = (float) KompasFragment.degree; // Mengambil derajat Kiblat

        rotateNeedle.postRotate(degree, compassNeedle.getWidth()/2, compassNeedle.getHeight()/2);
        rotateNeedle.postTranslate(centre_x - compassNeedle.getWidth()/2, centre_y - compassNeedle.getHeight()/2);
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (compassBackground == null || compassNeedle == null) {
            super.onDraw(canvas);
            return;
        }

        Paint p = new Paint();
        canvas.rotate(-directionNorth, centre_x, centre_y);




        canvas.drawBitmap(compassBackground,
                centre_x - (compassBackground.getWidth() / 2f),
                centre_y - (compassBackground.getHeight() / 2f),
                p);

        canvas.drawBitmap(compassNeedle, rotateNeedle, p);
    }
}