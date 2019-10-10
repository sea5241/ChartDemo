package com.example.chartdemo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.example.chartdemo.R;
import com.example.chartdemo.util.CommonUtils;
import com.example.chartdemo.util.DateTimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ChartView
 * 自定义折线图表View
 * @author sea
 * @date 2019/9/28
 */
public class ChartView extends View {
    private final String TAG = "ChartLine";
    private Paint mPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();
    /**
     * 某时刻数据展示框的高
     */
    private float mSelectRateShowHeight;
    /**
     * 某时刻数据展示框的宽
     */
    private float mSelectRateShowWidth;
    /**
     * 某时刻数据的值
     */
    private float mSelectRateShowValue = 0;
    /**
     * 某时刻的时间
     */
    private String mSelectRateShowTime = "";

    /**
     * X坐标轴的高度
     */
    private float mXHeight;

    /**
     * Y坐标轴的宽度
     */
    private float mYWidth;

    private float mYTextHeight;

    /**
     * 数据
     */
    private TreeMap<Long, Float> mMapChartData = new TreeMap<>();

    /**
     * Y轴最大值
     */
    private float mMaxY;
    /**
     * Y轴最小值
     */
    private float mMinY;
    /**
     * Y轴值间距
     */
    private float mDistanceY;
    /**
     * X轴最大值
     */
    private long mMaxX;
    /**
     * X轴最小值
     */
    private long mMinX;
    /**
     * X轴值间距
     */
    private long mDistanceX;

    /**
     * 的x坐标轴集合
     */
    private List<Float> mListChartDataX = new LinkedList<>();

    /**
     * 值集合
     */
    private List<Long> mListChartDataTime = new LinkedList<>();


    /**
     * 绘制主颜色
     */
    int mColor = Color.GRAY;


    /**
     * 纵线x轴坐标
     */
    private float mVerticalLineX = -1;

    /**
     * 是否消费事件
     */
    private boolean mIsConsumeEvent = false;
    /**
     * 当前的Key
     */
    private float mNowKey = 0;
    /**
     * 绘制字体大小
     */
    private int mTextSize;
    public ChartView(Context context) {
        super(context);
        init();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        mSelectRateShowHeight = CommonUtils.dip2px( 30);
        mSelectRateShowWidth = CommonUtils.dip2px(120);
        mXHeight = CommonUtils.dip2px(30);
        mYWidth = CommonUtils.dip2px( 30);
        mTextSize = CommonUtils.dip2px( 12);
    }
    /**
     * 设置数据
     */
    public void setMapChartData(Map<Long, Float> mapChartData) {
        if (mapChartData == null || mapChartData.size() < 1) {
            Log.e(TAG, "传过来的数据为空！");
            return;
        }
        this.mMapChartData.clear();
        this.mMapChartData.putAll(mapChartData);
        setChartXY();
        if (mMapChartData.size() == 1) {
            invalidate();
        } else {
            startAnimator();
        }
    }

    public void setIsConsumeEvent(boolean isConsumeEvent) {
        this.mIsConsumeEvent = isConsumeEvent;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(CommonUtils.dip2px(360), CommonUtils.dip2px( 360));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(CommonUtils.dip2px( 360), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, CommonUtils.dip2px( 360));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        //处理 padding
        float paddingLeft = (getPaddingLeft() == 0) ? CommonUtils.dip2px( 30) : getPaddingLeft();
        float paddingRight = (getPaddingRight() == 0) ? CommonUtils.dip2px( 30) : getPaddingRight();
        float paddingTop = (getPaddingRight() == 0) ? CommonUtils.dip2px(30) : getPaddingRight();
        float paddingBottom = (getPaddingRight() == 0) ? CommonUtils.dip2px( 30) : getPaddingRight();


        Log.d(TAG, "onDraw: paddingLeft:" + paddingLeft + " paddingRight:" + paddingRight +
                " paddingTop:" + paddingTop + " paddingBottom:" + paddingBottom +
                " width:" + width + " height:" + height);

        // Y轴
        drawY(canvas, paddingLeft, paddingTop + mSelectRateShowHeight,
                paddingLeft + mYWidth, height - paddingBottom - mXHeight);
        // X轴
        drawX(canvas, paddingLeft + mYWidth, height - paddingBottom - mXHeight,
                width - paddingRight, height - paddingBottom);

        //数据展示框
        drawChartLine(canvas, paddingLeft + mYWidth, paddingTop + mSelectRateShowHeight,
                width - paddingRight, height - paddingBottom - mXHeight);
        drawRateResult(canvas,paddingLeft, paddingTop + mSelectRateShowHeight,
                paddingLeft + mYWidth, paddingTop/2 + mSelectRateShowHeight);
        if (mVerticalLineX >= paddingLeft + mYWidth && mVerticalLineX <= width - paddingRight && mIsConsumeEvent) {
            //数值详情框
            drawSelectRateResult(canvas, mVerticalLineX - mSelectRateShowWidth / 2, 0,
                    mVerticalLineX + mSelectRateShowWidth / 2, mSelectRateShowHeight);
            //数值框下面的线
            drawVerticalLine(canvas, mVerticalLineX, mSelectRateShowHeight, height - paddingBottom - mXHeight);
        } else {
            drawTextSlidingToShow(canvas, paddingLeft, paddingTop, width - paddingRight, paddingTop + mSelectRateShowHeight);
        }
    }


    /**
     * 绘制左侧Y轴的标注
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawY(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mColor);
        // 设置抗锯齿
        mTextPaint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float unitY = (bottom - top) / 6;
        //baseline 公式 int baseline = height/2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent,height 是文字区域的高度
        float baseLine = (fontMetrics.bottom - fontMetrics.top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        String textStr;
        if(mDistanceY<8){
            float textY = bottom + baseLine - (fontMetrics.bottom - fontMetrics.top) / 2;
            float textY2 = top -( baseLine - (fontMetrics.bottom - fontMetrics.top) / 2);
            //X坐标为x轴绘制区域的中心点，因为设置了mTextPaint.setTextAlign(Paint.Align.CENTER);
            textStr = (((int)(mMinY*100))/100f) + "";
            canvas.drawText((((int)(mMinY*100))/100f) + "", (right + left) / 2, textY, mTextPaint);
            canvas.drawText((((int)(mMaxY*100))/100f) + "", (right + left) / 2, textY2, mTextPaint);
        }else {
            // 文字内容
            int textContent= (int)(mMinY);
            int d = (int) (mDistanceY / 7);
            for (int i = 0; i <= 6; i++) {
                // Y值在 baseline 上，最后减掉的是字体一半的高度
                float textY = bottom - unitY * i + baseLine - (fontMetrics.bottom - fontMetrics.top) / 2;
                //X坐标为x轴绘制区域的中心点，因为设置了mTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(textContent + "", (right + left) / 2, textY, mTextPaint);
                textContent += d;
            }
            textStr = textContent+"";
        }
        mYTextHeight = (fontMetrics.descent - fontMetrics.ascent);
        float textWidth = getTextWidth(mTextPaint,textStr+"");
        mPaint.reset();
        mPaint.setStrokeWidth(CommonUtils.dip2px( 2));
        mPaint.setColor(Color.parseColor("#FFCCCCCC"));
        mPaint.setAntiAlias(true);
        canvas.drawLine((right + left) / 2+textWidth/2+CommonUtils.dip2px(2), top-mYTextHeight/2, (right + left) / 2+textWidth/2+CommonUtils.dip2px(2), bottom+mYTextHeight/2, mPaint);
        // 辅助绘制的矩形框，正式版注释掉
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }
    private int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }
    /**
     * 绘制X轴坐标上的点和数据
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawX(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        //宽度分成24份
        float unitX = (right - left) / 6;
        //半径距离
        float radius = CommonUtils.dip2px(2);
        long d = mDistanceX/7;
        for (int i = 0; i <= 6; i++) {
            float x = left + unitX * i;
            canvas.drawText(DateTimeUtil.getDateString(DateTimeUtil.PATTERN_HH_MM,mMinX+i*d), x, bottom, mTextPaint);
            canvas.drawCircle(x, top+mXHeight/2, radius, mPaint);
        }
        mPaint.reset();
        mPaint.setStrokeWidth(CommonUtils.dip2px( 2));
        mPaint.setColor(Color.parseColor("#FFCCCCCC"));
        mPaint.setAntiAlias(true);
        canvas.drawLine(left-CommonUtils.dip2px( 4), top+mXHeight/2-mYTextHeight/2, right, top+mXHeight/2-mYTextHeight/2, mPaint);
        //辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 画折线
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawChartLine(Canvas canvas, float left, float top, float right, float bottom) {
        mPaint.reset();

        //宽度的一个单位，最大是24个单位
        float unitX = (right - left) / mMapChartData.size();
        //高度的一个单位，最大是180个单位
        float unitY = (float) ((bottom - top) / mDistanceY);

        // 最高值，用来绘制渐变
        float maxRateValue = 0;

        // 绘制参考线 60-100
        mPaint.setColor(Color.GRAY);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(CommonUtils.dip2px( 1));
        mPaint.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));
        canvas.drawLine(left, bottom - mDistanceY/4f*3 * unitY, right, bottom - mDistanceY/4f*3* unitY,mPaint);


        //如果只有一条数据，就画一个点
        if (mMapChartData.size() == 1) {
            mPaint.reset();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
            Long key = mMapChartData.firstKey();
            float x = left;
            mListChartDataX.clear();
            mListChartDataTime.clear();
            mListChartDataX.add(x);
            mListChartDataTime.add(key);
            canvas.drawCircle(x, bottom - (mMapChartData.get(key)-mMinY) * unitY, CommonUtils.dip2px(2), mPaint);
            mPaint.reset();
            return;
        }

        //画折线
        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(CommonUtils.getColor(R.color.colorAccent));
        mPaint.setStrokeWidth(CommonUtils.dip2px( 2));
        mPaint.setAntiAlias(true);
        Path path = new Path();
        mListChartDataX.clear();
        mListChartDataTime.clear();
        float firstKey = mMapChartData.firstKey();
        int count = 0;
        for (Long key : mMapChartData.keySet()) {
            float x = count * unitX + left;
            mListChartDataX.add(x);
            mListChartDataTime.add(key);
            if (key > mNowKey) {
                break;
            }
            if (key == firstKey) {
                path.moveTo(x, (bottom - (mMapChartData.get(key)-mMinY) * unitY));
            } else {
                path.lineTo(x, (bottom - (mMapChartData.get(key)-mMinY) * unitY));
            }
            maxRateValue = maxRateValue < (mMapChartData.get(key)-mMinY) ? mMapChartData.get(key)-mMinY : maxRateValue;
            ++count;
        }
        canvas.drawPath(path, mPaint);


        //draw 渐变色
//        path.lineTo(mNowKey * unitX + left, bottom);
//        path.lineTo(firstKey * unitX + left, bottom);
//        path.close();
//        int[] shadeColors = new int[]{Color.argb(0x99, 0xFF, 0xFF, 0xFF), Color.argb(0x00, 0xFF, 0xFF, 0xFF)};
//        Shader mShader = new LinearGradient(0, bottom - maxRateValue * unitY, 0, bottom, shadeColors, null, Shader.TileMode.CLAMP);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setShader(mShader);
//        canvas.drawPath(path, mPaint);

        //辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制选中的某时刻的展示的数据框
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawSelectRateResult(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mPaint.reset();

        //绘制胶囊形状View
        mPaint.setColor(CommonUtils.getColor(R.color.colorAccent));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        float radius = (bottom - top) / 2;
        RectF rectLeft = new RectF(left, top, left + radius * 2, bottom);
        RectF rectRight = new RectF(right - radius * 2, top, right, bottom);
        Path path = new Path();
        path.moveTo(left + radius, bottom);
        path.arcTo(rectLeft, 90, 180);
        path.lineTo(right - radius, top);
        path.arcTo(rectRight, -90, 180);
        path.close();
        canvas.drawPath(path, mPaint);

        //draw 值
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = bottom -( (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
        canvas.drawText(mSelectRateShowValue + "", left + CommonUtils.dip2px( 12), baseLine, mTextPaint);
//        mTextPaint.setTextAlign(Paint.Align.RIGHT);

        baseLine = ((bottom + top) / 2 + top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText(mSelectRateShowTime, left+ CommonUtils.dip2px( 12), baseLine, mTextPaint);

        // 辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    /**
     * 绘制左上比例值
     *
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void drawRateResult(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        //draw 值
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(CommonUtils.getColor(R.color.colorAccent));
        mTextPaint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = bottom -( (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
        float d= mSelectRateShowValue-mMapChartData.firstEntry().getValue();
        canvas.drawText("$"+ d+ "("+d/mMapChartData.firstEntry().getValue()+")", left + CommonUtils.dip2px( 12), baseLine, mTextPaint);
//        mTextPaint.setTextAlign(Paint.Align.RIGHT);

        // 辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
    }
    /**
     * 绘制“长击滑动以查看”
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param right  绘制区域的右侧坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawTextSlidingToShow(Canvas canvas, float left, float top, float right, float bottom) {
        mTextPaint.reset();
        mTextPaint.setColor(Color.parseColor("#66FFFFFF"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float baseLine = (bottom + top) / 2 + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;
        canvas.drawText("长按滑动以查看", (left + right) / 2, baseLine, mTextPaint);
        mTextPaint.reset();

        // 辅助绘制的矩形框
//        mPaint.reset();
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(mColor);
//        canvas.drawRect(left, top, right, bottom, mPaint);
//        canvas.drawRect(left, top, right, (bottom + top) / 2, mPaint);
    }

    /**
     * 绘制滑动的线
     *
     * @param left   绘制区域的左侧坐标
     * @param top    绘图区域的顶部坐标
     * @param bottom 绘制区域的底部坐标
     */
    private void drawVerticalLine(Canvas canvas, float left, float top, float bottom) {
        mPaint.reset();
        mPaint.setStrokeWidth(CommonUtils.dip2px( 2));
        mPaint.setColor(CommonUtils.getColor(R.color.colorAccent));
        mPaint.setAntiAlias(true);
        canvas.drawLine(left, top, left, bottom+mXHeight/2-mYTextHeight/2, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mListChartDataX.size() < 1 || mListChartDataTime.size() < 1 || mListChartDataX.size() != mListChartDataTime.size()) {
                    Log.e(TAG, "error data!");
                    break;
                }

                float x = event.getX();
                int size = mListChartDataX.size();
                float firstX = mListChartDataX.get(0);
                float lastX = mListChartDataX.get(size - 1);
                if (x <= firstX) {
                    mVerticalLineX = firstX;
                    long time = mListChartDataTime.get(0);
                    mSelectRateShowTime = DateTimeUtil.getDateString(DateTimeUtil.PATTERN_YYYY_MM_DD_HH_MM,time);
                    mSelectRateShowValue = mMapChartData.get(time);
                    break;
                } else if (x >= lastX) {
                    mVerticalLineX = lastX;
                    long time = mListChartDataTime.get(size - 1);
                    mSelectRateShowTime = DateTimeUtil.getDateString(DateTimeUtil.PATTERN_YYYY_MM_DD_HH_MM,time);
                    mSelectRateShowValue = mMapChartData.get(time);
                    break;
                }
                for (int i = 1; i < size - 1; i++) {
                    float nowX = mListChartDataX.get(i);
                    float nextX = mListChartDataX.get(i + 1);
                    if (x >= nowX && x <= nextX) {
                        if (Math.abs(x - nowX) <= Math.abs(x - nextX)) {
                            mVerticalLineX = nowX;
                            long time = mListChartDataTime.get(i);
                            mSelectRateShowTime = DateTimeUtil.getDateString(DateTimeUtil.PATTERN_YYYY_MM_DD_HH_MM,time);
                            mSelectRateShowValue = mMapChartData.get(time);
                        } else {
                            mVerticalLineX = nextX;
                            long time = mListChartDataTime.get(i + 1);
                            mSelectRateShowTime = DateTimeUtil.getDateString(DateTimeUtil.PATTERN_YYYY_MM_DD_HH_MM,time);
                            mSelectRateShowValue = mMapChartData.get(time);
                        }
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mVerticalLineX = -1;
                mIsConsumeEvent = false;
                break;
            default:
                break;
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    /**
     * 绘制折线的动画
     */
    public void startAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(mMapChartData.firstKey(), mMapChartData.lastKey());
        animator.setDuration(1000L);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNowKey = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 设置XY值间距，最大最小值
     */
    private void setChartXY(){
        mMaxY=0;
        mMinY = Float.MAX_VALUE;
        if(mMapChartData!=null&&mMapChartData.size()>0) {
            for (Float d:mMapChartData.values()){
                if(mMaxY<d){
                    mMaxY=d;
                }
                if(mMinY>d){
                    mMinY =d;
                }
            }
        }else{
            mMinY =0;
        }
        mDistanceY = mMaxY-mMinY;
        mMinX = mMapChartData.firstKey();
        mMaxX = mMapChartData.lastKey();
        mDistanceX = mMaxX-mMinX;
    }
}
