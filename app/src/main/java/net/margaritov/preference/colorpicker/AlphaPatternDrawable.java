package net.margaritov.preference.colorpicker;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class AlphaPatternDrawable extends Drawable {
    private Bitmap mBitmap;
    private Paint mPaint;
    private Paint mPaintGray;
    private Paint mPaintWhite;
    private int mRectangleSize;
    private int numRectanglesHorizontal;
    private int numRectanglesVertical;

    public AlphaPatternDrawable(int rectangleSize) {
        this.mRectangleSize = 10;
        this.mPaint = new Paint();
        this.mPaintWhite = new Paint();
        this.mPaintGray = new Paint();
        this.mRectangleSize = rectangleSize;
        this.mPaintWhite.setColor(-1);
        this.mPaintGray.setColor(0xffcbcbcb);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.mBitmap, null, getBounds(), this.mPaint);
    }

    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("Alpha is not supported by this drawwable.");
    }

    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("ColorFilter is not supported by this drawwable.");
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        int height = bounds.height();
        this.numRectanglesHorizontal = (int) Math.ceil((double) (bounds.width() / this.mRectangleSize));
        this.numRectanglesVertical = (int) Math.ceil((double) (height / this.mRectangleSize));
        generatePatternBitmap();
    }

    private void generatePatternBitmap() {
        if (getBounds().width() > 0 && getBounds().height() > 0) {
            this.mBitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Config.ARGB_8888);
            Canvas canvas = new Canvas(this.mBitmap);
            Rect r = new Rect();
            boolean verticalStartWhite = true;
            for (int i = 0; i <= this.numRectanglesVertical; i++) {
                boolean isWhite = verticalStartWhite;
                for (int j = 0; j <= this.numRectanglesHorizontal; j++) {
                    r.top = this.mRectangleSize * i;
                    r.left = this.mRectangleSize * j;
                    r.bottom = r.top + this.mRectangleSize;
                    r.right = r.left + this.mRectangleSize;
                    canvas.drawRect(r, isWhite ? this.mPaintWhite : this.mPaintGray);
                    if (isWhite) {
                        isWhite = false;
                    } else {
                        isWhite = true;
                    }
                }
                if (verticalStartWhite) {
                    verticalStartWhite = false;
                } else {
                    verticalStartWhite = true;
                }
            }
        }
    }
}
