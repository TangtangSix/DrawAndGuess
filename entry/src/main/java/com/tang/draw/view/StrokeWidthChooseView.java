package com.tang.draw.view;


import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Region;
import ohos.agp.utils.Color;
import ohos.agp.utils.Rect;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/********
 *文件名: StrokeWidthChooseView
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/5 0:10
 *描述: StrokeWidthChooseView
 ********/
public class StrokeWidthChooseView extends Component implements Component.DrawTask,Component.EstimateSizeListener,Component.TouchEventListener  {

    private static final String TAG = StrokeWidthChooseView.class.getSimpleName();
    private static final HiLogLabel HI_LOG_LABEL=new HiLogLabel(HiLog.LOG_APP,0x00201,TAG);

    private Paint mPaint;
    private Paint mChoosePaint;
    private Paint mWhitePaint;
    private int mWidth;
    private int mHeight;
    private int[] widthArray = new int[]{10, 20, 30, 40};
    private List<Rect> rectList;
    private List<Region> regionList;
    private int mPaintColor = Color.BLACK.getValue();
    private int mCurrentIndex = 0;
    private int downX, downY;
    private DrawView drawView;
    private DrawView.State mCurrentState = DrawView.State.PEN;

    public StrokeWidthChooseView(Context context) {
        this(context, null);
    }

    public StrokeWidthChooseView(Context context, @Nullable AttrSet attrs) {
        super(context, attrs);
        initPaint();
        // 设置测量组件的侦听器
        setEstimateSizeListener(this);
        // 设置TouchEvent响应事件
        setTouchEventListener(this);
        // 添加绘制任务
        addDrawTask(this);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_STYLE);
        mPaint.setAntiAlias(true);

        mChoosePaint = new Paint();
        mChoosePaint.setAntiAlias(true);
        mChoosePaint.setStyle(Paint.Style.FILL_STYLE);


        mWhitePaint = new Paint();
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setStyle(Paint.Style.FILL_STYLE);
        mWhitePaint.setColor(Color.WHITE);

        init();
    }

    /**
     * 初始化组件,进行绘制组件
     */
    private void init(){
        rectList = new ArrayList<>();
        regionList = new ArrayList<>();
        int perWidth = mWidth / widthArray.length;
        Rect rect;
        Region region;
        Rect chooseRect;
        for (int i = 0; i < widthArray.length; i++){
            rect = new Rect(perWidth * i + perWidth / 2 - widthArray[i],
                    mHeight / 2 - widthArray[i],
                    perWidth * i + perWidth / 2 + widthArray[i],
                    mHeight / 2 + widthArray[i]);
            chooseRect = new Rect(perWidth * i + perWidth / 2 - widthArray[widthArray.length - 1],
                    mHeight / 2 - widthArray[widthArray.length - 1],
                    perWidth * i + perWidth / 2 + widthArray[widthArray.length - 1],
                    mHeight / 2 + widthArray[widthArray.length - 1]);
            region = new Region(chooseRect);
            rectList.add(rect);
            regionList.add(region);
        }
    }

    /**
     * 重新绘图函数,当界面刷新时自动调用
     * @param component
     * @param canvas
     */
    @Override
    public void onDraw(Component component, Canvas canvas) {
        mPaint.setColor(new Color(mPaintColor));
        HiLog.info(HI_LOG_LABEL, getClass().getSimpleName()+" --- onDraw");
        for (int i = 0; i < rectList.size(); i++){
            Rect rect = rectList.get(i);
            float radius = widthArray[i];
            switch (mCurrentState){
                case PEN:
                    mChoosePaint.setColor(Color.WHITE);
                    if (mCurrentIndex == i){
                        canvas.drawCircle(rect.getCenterX(), rect.getCenterY(), radius + 10, mChoosePaint);
                    }
                    canvas.drawCircle(rect.getCenterX(), rect.getCenterY(), radius, mPaint);
                    break;
                case ERASER:
                    mChoosePaint.setColor(Color.BLACK);
                    if (mCurrentIndex == i){
                        canvas.drawCircle(rect.getCenterX(), rect.getCenterY(), radius + 10, mChoosePaint);
                    }
                    canvas.drawCircle(rect.getCenterX(), rect.getCenterY(), radius, mWhitePaint);
                    break;
            }

        }
    }

    /**
     * 绘制组件大小
     * @param widthEstimateConfig
     * @param heightEstimateConfig
     * @return
     */
    @Override
    public boolean  onEstimateSize(int widthEstimateConfig, int heightEstimateConfig) {
        int componentWidth = EstimateSpec.getSize(widthEstimateConfig);
        int componentHeight = EstimateSpec.getSize(heightEstimateConfig);
        this.mWidth = componentWidth;
        this.mHeight = componentHeight;
        setEstimatedSize(

        EstimateSpec.getChildSizeWithMode(componentWidth, componentWidth, EstimateSpec.PRECISE),

        EstimateSpec.getChildSizeWithMode(componentHeight, componentHeight, EstimateSpec.PRECISE)
        );

        init();
        return true;
    }

    /**
     * 触摸事件
     * @param component
     * @param touchEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        MmiPoint mmiPoint=touchEvent.getPointerPosition(0);
        int x = (int) mmiPoint.getX();
        int y = (int) mmiPoint.getY();
        switch (touchEvent.getAction()){
            case TouchEvent.PRIMARY_POINT_DOWN:
                downX = x;
                downY = y;
                break;
            case TouchEvent.POINT_MOVE:
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                for (int i = 0; i < regionList.size(); i++){
                    Region region = regionList.get(i);
                    if (region.contains(x, y) && region.contains(downX, downY)){
                        mCurrentIndex = i;
                        switch (mCurrentState){
                            case PEN:
                                drawView.setDrawPaintStrokeWidth(widthArray[i]);
                                break;
                            case ERASER:
                                drawView.setEraserPaintStrokeWidth(widthArray[i]);
                                break;
                        }

                    }
                }
                invalidate();
                break;
        }
        return true;
    }
    public void setWidthArray(int[] widthArray) {
        this.widthArray = widthArray;
    }

    public void setmPaintColor(int mPaintColor) {
        this.mPaintColor = mPaintColor;
        drawView.setDrawPaintColor(new Color(mPaintColor));
        invalidate();
    }

    public void setDrawView(DrawView drawView) {
        this.drawView = drawView;
        drawView.setDrawPaintStrokeWidth(widthArray[0]);
        drawView.setEraserPaintStrokeWidth(widthArray[0]);
    }

    public void setmCurrentState(DrawView.State mCurrentState) {
        this.mCurrentState = mCurrentState;
        invalidate();
        if (mCurrentState == DrawView.State.ERASER){
            drawView.setEraserPaintStrokeWidth(widthArray[mCurrentIndex]);
        }else{
            drawView.setDrawPaintStrokeWidth(widthArray[mCurrentIndex]);
        }
    }
}

