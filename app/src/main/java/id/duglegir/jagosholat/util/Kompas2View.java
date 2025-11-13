package id.duglegir.jagosholat.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class Kompas2View extends View {

    private Paint paint;
    private Path jarumKiblatPath;

    private float azimuth = 0f;
    private float derajatKiblat = 0f;

    private int lebar, tinggi;

    public Kompas2View(Context context) {
        super(context);
        init();
    }

    public Kompas2View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.GRAY);

        jarumKiblatPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        lebar = w;
        tinggi = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cx = lebar / 2;
        int cy = tinggi / 2;
        int radius = Math.min(cx, cy) - 20;

        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(cx, cy, radius, paint);


        canvas.save();
        canvas.rotate(-azimuth, cx, cy);

        paint.setColor(Color.GRAY);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);

        canvas.drawText("U", cx - (paint.measureText("U") / 2), cy - radius + 50, paint);
        canvas.drawText("S", cx - (paint.measureText("S") / 2), cy + radius - 20, paint);
        canvas.drawText("B", cx - radius + 20, cy + (paint.getTextSize() / 3), paint);
        canvas.drawText("T", cx + radius - 50, cy + (paint.getTextSize() / 3), paint);

        canvas.restore();


        canvas.save();
        canvas.rotate(derajatKiblat - azimuth, cx, cy);

        jarumKiblatPath.reset();
        jarumKiblatPath.moveTo(cx, cy - (radius * 0.8f));
        jarumKiblatPath.lineTo(cx - 20, cy);
        jarumKiblatPath.lineTo(cx + 20, cy);
        jarumKiblatPath.close();

        paint.setColor(Color.parseColor("#FF009688"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(jarumKiblatPath, paint);

        canvas.restore(); // Kembalikan kanvas

        paint.setColor(Color.RED);
        canvas.drawCircle(cx, cy, 10, paint);
    }

    public void updateAzimuth(float azimuth) {
        this.azimuth = azimuth;
        invalidate();
    }

    public void updateDerajatKiblat(float derajatKiblat) {
        this.derajatKiblat = derajatKiblat;
        invalidate();
    }
}