package net.margaritov.preference.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class ColorPickerView extends View {
    private static final float BORDER_WIDTH_PX = 1.0f;
    private static final int PANEL_ALPHA = 2;
    private static final int PANEL_HUE = 1;
    private static final int PANEL_SAT_VAL = 0;
    private float ALPHA_PANEL_HEIGHT;
    private float HUE_PANEL_WIDTH;
    private float PALETTE_CIRCLE_TRACKER_RADIUS;
    private float PANEL_SPACING;
    private float RECTANGLE_TRACKER_OFFSET;
    private int mAlpha;
    private Paint mAlphaPaint;
    private AlphaPatternDrawable mAlphaPattern;
    private RectF mAlphaRect;
    private Shader mAlphaShader;
    private String mAlphaSliderText;
    private Paint mAlphaTextPaint;
    private int mBorderColor;
    private Paint mBorderPaint;
    private float mDensity;
    private float mDrawingOffset;
    private RectF mDrawingRect;
    private float mHue;
    private Paint mHuePaint;
    private RectF mHueRect;
    private Shader mHueShader;
    private Paint mHueTrackerPaint;
    private int mLastTouchedPanel;
    private OnColorChangedListener mListener;
    private float mSat;
    private Shader mSatShader;
    private Paint mSatValPaint;
    private RectF mSatValRect;
    private Paint mSatValTrackerPaint;
    private boolean mShowAlphaPanel;
    private int mSliderTrackerColor;
    private Point mStartTouchPoint;
    private float mVal;
    private Shader mValShader;

    public interface OnColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.HUE_PANEL_WIDTH = 30.0f;
        this.ALPHA_PANEL_HEIGHT = 20.0f;
        this.PANEL_SPACING = 10.0f;
        this.PALETTE_CIRCLE_TRACKER_RADIUS = 8.0f;
        this.RECTANGLE_TRACKER_OFFSET = 2.0f;
        this.mDensity = BORDER_WIDTH_PX;
        this.mAlpha = MotionEventCompat.ACTION_MASK;
        this.mHue = 360.0f;
        this.mSat = 0.0f;
        this.mVal = 0.0f;
        this.mAlphaSliderText = "";
        this.mSliderTrackerColor = -14935012;
        this.mBorderColor = -9539986;
        this.mShowAlphaPanel = false;
        this.mLastTouchedPanel = 0;
        this.mStartTouchPoint = null;
        init();
    }

    private void init() {
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        this.PALETTE_CIRCLE_TRACKER_RADIUS *= this.mDensity;
        this.RECTANGLE_TRACKER_OFFSET *= this.mDensity;
        this.HUE_PANEL_WIDTH *= this.mDensity;
        this.ALPHA_PANEL_HEIGHT *= this.mDensity;
        this.PANEL_SPACING *= this.mDensity;
        this.mDrawingOffset = calculateRequiredOffset();
        initPaintTools();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {
        this.mSatValPaint = new Paint();
        this.mSatValTrackerPaint = new Paint();
        this.mHuePaint = new Paint();
        this.mHueTrackerPaint = new Paint();
        this.mAlphaPaint = new Paint();
        this.mAlphaTextPaint = new Paint();
        this.mBorderPaint = new Paint();
        this.mSatValTrackerPaint.setStyle(Style.STROKE);
        this.mSatValTrackerPaint.setStrokeWidth(this.mDensity * 2.0f);
        this.mSatValTrackerPaint.setAntiAlias(true);
        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);
        this.mHueTrackerPaint.setStyle(Style.STROKE);
        this.mHueTrackerPaint.setStrokeWidth(this.mDensity * 2.0f);
        this.mHueTrackerPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setColor(0xff1c1c1c);
        this.mAlphaTextPaint.setTextSize(14.0f * this.mDensity);
        this.mAlphaTextPaint.setAntiAlias(true);
        this.mAlphaTextPaint.setTextAlign(Align.CENTER);
        this.mAlphaTextPaint.setFakeBoldText(true);
    }

    private float calculateRequiredOffset() {
        return 1.5f * Math.max(Math.max(this.PALETTE_CIRCLE_TRACKER_RADIUS, this.RECTANGLE_TRACKER_OFFSET), BORDER_WIDTH_PX * this.mDensity);
    }

    private int[] buildHueColorArray() {
        int[] hue = new int[361];
        int count = 0;
        int i = hue.length - 1;
        while (i >= 0) {
            hue[count] = Color.HSVToColor(new float[]{(float) i, BORDER_WIDTH_PX, BORDER_WIDTH_PX});
            i--;
            count += PANEL_HUE;
        }
        return hue;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDrawingRect.width() > 0.0f && this.mDrawingRect.height() > 0.0f) {
            drawSatValPanel(canvas);
            drawHuePanel(canvas);
            drawAlphaPanel(canvas);
        }
    }

    private void drawSatValPanel(Canvas canvas) {
        RectF rect = this.mSatValRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(this.mDrawingRect.left, this.mDrawingRect.top, BORDER_WIDTH_PX + rect.right, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, -1, ViewCompat.MEASURED_STATE_MASK, TileMode.CLAMP);
        }
        this.mSatShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, -1, Color.HSVToColor(new float[]{this.mHue, BORDER_WIDTH_PX, BORDER_WIDTH_PX}), TileMode.CLAMP);
        this.mSatValPaint.setShader(new ComposeShader(this.mValShader, this.mSatShader, Mode.MULTIPLY));
        canvas.drawRect(rect, this.mSatValPaint);
        Point p = satValToPoint(this.mSat, this.mVal);
        this.mSatValTrackerPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        canvas.drawCircle((float) p.x, (float) p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS - (BORDER_WIDTH_PX * this.mDensity), this.mSatValTrackerPaint);
        this.mSatValTrackerPaint.setColor(0xffdddddd);
        canvas.drawCircle((float) p.x, (float) p.y, this.PALETTE_CIRCLE_TRACKER_RADIUS, this.mSatValTrackerPaint);
    }

    private void drawHuePanel(Canvas canvas) {
        RectF rect = this.mHueRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(rect.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, rect.right + BORDER_WIDTH_PX, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
        if (this.mHueShader == null) {
            this.mHueShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, buildHueColorArray(), null, TileMode.CLAMP);
            this.mHuePaint.setShader(this.mHueShader);
        }
        canvas.drawRect(rect, this.mHuePaint);
        float rectHeight = (4.0f * this.mDensity) / 2.0f;
        Point p = hueToPoint(this.mHue);
        RectF r = new RectF();
        r.left = rect.left - this.RECTANGLE_TRACKER_OFFSET;
        r.right = rect.right + this.RECTANGLE_TRACKER_OFFSET;
        r.top = ((float) p.y) - rectHeight;
        r.bottom = ((float) p.y) + rectHeight;
        canvas.drawRoundRect(r, 2.0f, 2.0f, this.mHueTrackerPaint);
    }

    private void drawAlphaPanel(Canvas canvas) {
        if (this.mShowAlphaPanel && this.mAlphaRect != null && this.mAlphaPattern != null) {
            RectF rect = this.mAlphaRect;
            this.mBorderPaint.setColor(this.mBorderColor);
            canvas.drawRect(rect.left - BORDER_WIDTH_PX, rect.top - BORDER_WIDTH_PX, BORDER_WIDTH_PX + rect.right, BORDER_WIDTH_PX + rect.bottom, this.mBorderPaint);
            this.mAlphaPattern.draw(canvas);
            float[] hsv = new float[]{this.mHue, this.mSat, this.mVal};
            this.mAlphaShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, Color.HSVToColor(hsv), Color.HSVToColor(0, hsv), TileMode.CLAMP);
            this.mAlphaPaint.setShader(this.mAlphaShader);
            canvas.drawRect(rect, this.mAlphaPaint);
            if (!(this.mAlphaSliderText == null || this.mAlphaSliderText == "")) {
                canvas.drawText(this.mAlphaSliderText, rect.centerX(), rect.centerY() + (4.0f * this.mDensity), this.mAlphaTextPaint);
            }
            float rectWidth = (4.0f * this.mDensity) / 2.0f;
            Point p = alphaToPoint(this.mAlpha);
            RectF r = new RectF();
            r.left = ((float) p.x) - rectWidth;
            r.right = ((float) p.x) + rectWidth;
            r.top = rect.top - this.RECTANGLE_TRACKER_OFFSET;
            r.bottom = rect.bottom + this.RECTANGLE_TRACKER_OFFSET;
            canvas.drawRoundRect(r, 2.0f, 2.0f, this.mHueTrackerPaint);
        }
    }

    private Point hueToPoint(float hue) {
        RectF rect = this.mHueRect;
        float height = rect.height();
        Point p = new Point();
        p.y = (int) ((height - ((hue * height) / 360.0f)) + rect.top);
        p.x = (int) rect.left;
        return p;
    }

    private Point satValToPoint(float sat, float val) {
        RectF rect = this.mSatValRect;
        float height = rect.height();
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((sat * width) + rect.left);
        p.y = (int) (((BORDER_WIDTH_PX - val) * height) + rect.top);
        return p;
    }

    private Point alphaToPoint(int alpha) {
        RectF rect = this.mAlphaRect;
        float width = rect.width();
        Point p = new Point();
        p.x = (int) ((width - ((((float) alpha) * width) / 255.0f)) + rect.left);
        p.y = (int) rect.top;
        return p;
    }

    private float[] pointToSatVal(float x, float y) {
        RectF rect = this.mSatValRect;
        float[] result = new float[PANEL_ALPHA];
        float width = rect.width();
        float height = rect.height();
        if (x < rect.left) {
            x = 0.0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x -= rect.left;
        }
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        result[0] = (BORDER_WIDTH_PX / width) * x;
        result[PANEL_HUE] = BORDER_WIDTH_PX - ((BORDER_WIDTH_PX / height) * y);
        return result;
    }

    private float pointToHue(float y) {
        RectF rect = this.mHueRect;
        float height = rect.height();
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        return 360.0f - ((y * 360.0f) / height);
    }

    private int pointToAlpha(int x) {
        RectF rect = this.mAlphaRect;
        int width = (int) rect.width();
        if (((float) x) < rect.left) {
            x = 0;
        } else if (((float) x) > rect.right) {
            x = width;
        } else {
            x -= (int) rect.left;
        }
        return 255 - ((x * MotionEventCompat.ACTION_MASK) / width);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean update = false;
        if (event.getAction() == PANEL_ALPHA) {
            switch (this.mLastTouchedPanel) {
                case 0 /*0*/:
                    float sat = this.mSat + (x / 50.0f);
                    float val = this.mVal - (y / 50.0f);
                    if (sat < 0.0f) {
                        sat = 0.0f;
                    } else if (sat > BORDER_WIDTH_PX) {
                        sat = BORDER_WIDTH_PX;
                    }
                    if (val < 0.0f) {
                        val = 0.0f;
                    } else if (val > BORDER_WIDTH_PX) {
                        val = BORDER_WIDTH_PX;
                    }
                    this.mSat = sat;
                    this.mVal = val;
                    update = true;
                    break;
                case PANEL_HUE /*1*/:
                    float hue = this.mHue - (y * 10.0f);
                    if (hue < 0.0f) {
                        hue = 0.0f;
                    } else if (hue > 360.0f) {
                        hue = 360.0f;
                    }
                    this.mHue = hue;
                    update = true;
                    break;
                case PANEL_ALPHA /*2*/:
                    if (this.mShowAlphaPanel && this.mAlphaRect != null) {
                        int alpha = (int) (((float) this.mAlpha) - (x * 10.0f));
                        if (alpha < 0) {
                            alpha = 0;
                        } else if (alpha > MotionEventCompat.ACTION_MASK) {
                            alpha = MotionEventCompat.ACTION_MASK;
                        }
                        this.mAlpha = alpha;
                        update = true;
                        break;
                    }
                    update = false;
                    break;
            }
        }
        if (!update) {
            return super.onTrackballEvent(event);
        }
        if (this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean update = false;
        switch (event.getAction()) {
            case 0 /*0*/:
                this.mStartTouchPoint = new Point((int) event.getX(), (int) event.getY());
                update = moveTrackersIfNeeded(event);
                break;
            case PANEL_HUE /*1*/:
                this.mStartTouchPoint = null;
                update = moveTrackersIfNeeded(event);
                break;
            case PANEL_ALPHA /*2*/:
                update = moveTrackersIfNeeded(event);
                break;
        }
        if (!update) {
            return super.onTouchEvent(event);
        }
        if (this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
        return true;
    }

    private boolean moveTrackersIfNeeded(MotionEvent event) {
        if (this.mStartTouchPoint == null) {
            return false;
        }
        int startX = this.mStartTouchPoint.x;
        int startY = this.mStartTouchPoint.y;
        if (this.mHueRect.contains((float) startX, (float) startY)) {
            this.mLastTouchedPanel = PANEL_HUE;
            this.mHue = pointToHue(event.getY());
            return true;
        } else if (this.mSatValRect.contains((float) startX, (float) startY)) {
            this.mLastTouchedPanel = 0;
            float[] result = pointToSatVal(event.getX(), event.getY());
            this.mSat = result[0];
            this.mVal = result[PANEL_HUE];
            return true;
        } else if (this.mAlphaRect == null || !this.mAlphaRect.contains((float) startX, (float) startY)) {
            return false;
        } else {
            this.mLastTouchedPanel = PANEL_ALPHA;
            this.mAlpha = pointToAlpha((int) event.getX());
            return true;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthAllowed = MeasureSpec.getSize(widthMeasureSpec);
        int heightAllowed = MeasureSpec.getSize(heightMeasureSpec);
        widthAllowed = chooseWidth(widthMode, widthAllowed);
        heightAllowed = chooseHeight(heightMode, heightAllowed);
        if (this.mShowAlphaPanel) {
            width = (int) ((((float) heightAllowed) - this.ALPHA_PANEL_HEIGHT) + this.HUE_PANEL_WIDTH);
            if (width > widthAllowed) {
                width = widthAllowed;
                height = (int) ((((float) widthAllowed) - this.HUE_PANEL_WIDTH) + this.ALPHA_PANEL_HEIGHT);
            } else {
                height = heightAllowed;
            }
        } else {
            height = (int) ((((float) widthAllowed) - this.PANEL_SPACING) - this.HUE_PANEL_WIDTH);
            if (height > heightAllowed || getTag().equals("landscape")) {
                height = heightAllowed;
                width = (int) ((((float) height) + this.PANEL_SPACING) + this.HUE_PANEL_WIDTH);
            } else {
                width = widthAllowed;
            }
        }
        setMeasuredDimension(width, height);
    }

    private int chooseWidth(int mode, int size) {
        return (mode == ExploreByTouchHelper.INVALID_ID || mode == 1073741824) ? size : getPrefferedWidth();
    }

    private int chooseHeight(int mode, int size) {
        return (mode == ExploreByTouchHelper.INVALID_ID || mode == 1073741824) ? size : getPrefferedHeight();
    }

    private int getPrefferedWidth() {
        int width = getPrefferedHeight();
        if (this.mShowAlphaPanel) {
            width = (int) (((float) width) - (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
        }
        return (int) ((((float) width) + this.HUE_PANEL_WIDTH) + this.PANEL_SPACING);
    }

    private int getPrefferedHeight() {
        int height = (int) (200.0f * this.mDensity);
        if (this.mShowAlphaPanel) {
            return (int) (((float) height) + (this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT));
        }
        return height;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = this.mDrawingOffset + ((float) getPaddingLeft());
        this.mDrawingRect.right = (((float) w) - this.mDrawingOffset) - ((float) getPaddingRight());
        this.mDrawingRect.top = this.mDrawingOffset + ((float) getPaddingTop());
        this.mDrawingRect.bottom = (((float) h) - this.mDrawingOffset) - ((float) getPaddingBottom());
        setUpSatValRect();
        setUpHueRect();
        setUpAlphaRect();
    }

    private void setUpSatValRect() {
        RectF dRect = this.mDrawingRect;
        float panelSide = dRect.height() - 2.0f;
        if (this.mShowAlphaPanel) {
            panelSide -= this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT;
        }
        float left = dRect.left + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        this.mSatValRect = new RectF(left, top, left + panelSide, top + panelSide);
    }

    private void setUpHueRect() {
        RectF dRect = this.mDrawingRect;
        this.mHueRect = new RectF((dRect.right - this.HUE_PANEL_WIDTH) + BORDER_WIDTH_PX, dRect.top + BORDER_WIDTH_PX, dRect.right - BORDER_WIDTH_PX, (dRect.bottom - BORDER_WIDTH_PX) - (this.mShowAlphaPanel ? this.PANEL_SPACING + this.ALPHA_PANEL_HEIGHT : 0.0f));
    }

    private void setUpAlphaRect() {
        if (this.mShowAlphaPanel) {
            RectF dRect = this.mDrawingRect;
            this.mAlphaRect = new RectF(dRect.left + BORDER_WIDTH_PX, (dRect.bottom - this.ALPHA_PANEL_HEIGHT) + BORDER_WIDTH_PX, dRect.right - BORDER_WIDTH_PX, dRect.bottom - BORDER_WIDTH_PX);
            this.mAlphaPattern = new AlphaPatternDrawable((int) (5.0f * this.mDensity));
            this.mAlphaPattern.setBounds(Math.round(this.mAlphaRect.left), Math.round(this.mAlphaRect.top), Math.round(this.mAlphaRect.right), Math.round(this.mAlphaRect.bottom));
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
    }

    public void setBorderColor(int color) {
        this.mBorderColor = color;
        invalidate();
    }

    public int getBorderColor() {
        return this.mBorderColor;
    }

    public int getColor() {
        return Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal});
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean callback) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
        this.mAlpha = alpha;
        this.mHue = hsv[0];
        this.mSat = hsv[PANEL_HUE];
        this.mVal = hsv[PANEL_ALPHA];
        if (callback && this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(this.mAlpha, new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
    }

    public float getDrawingOffset() {
        return this.mDrawingOffset;
    }

    public void setAlphaSliderVisible(boolean visible) {
        if (this.mShowAlphaPanel != visible) {
            this.mShowAlphaPanel = visible;
            this.mValShader = null;
            this.mSatShader = null;
            this.mHueShader = null;
            this.mAlphaShader = null;
            requestLayout();
        }
    }

    public boolean getAlphaSliderVisible() {
        return this.mShowAlphaPanel;
    }

    public void setSliderTrackerColor(int color) {
        this.mSliderTrackerColor = color;
        this.mHueTrackerPaint.setColor(this.mSliderTrackerColor);
        invalidate();
    }

    public int getSliderTrackerColor() {
        return this.mSliderTrackerColor;
    }

    public void setAlphaSliderText(int res) {
        setAlphaSliderText(getContext().getString(res));
    }

    public void setAlphaSliderText(String text) {
        this.mAlphaSliderText = text;
        invalidate();
    }

    public String getAlphaSliderText() {
        return this.mAlphaSliderText;
    }
}
