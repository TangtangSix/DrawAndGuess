package com.tang.draw.view;


import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.ScrollView;
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

import java.util.ArrayList;
import java.util.List;

/********
 *文件名: ColorHorizontalScrollView
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/5 0:08
 *描述: ColorHorizontalScrollView
 ********/
public class ColorHorizontalScrollView extends Component implements Component.DrawTask,Component.EstimateSizeListener,Component.TouchEventListener {
    private static final String TAG = ColorHorizontalScrollView.class.getSimpleName();
    private static final HiLogLabel HI_LOG_LABEL=new HiLogLabel(HiLog.LOG_APP,0x00201,TAG);

    private Context context;
    private int width;
    private int height;
    private Paint mPaint;
    private int radius = 50;
    private List<Rect> rectList;
    private List<Region> regionList;
    private float startX, startY;
    private float dx, dy;
    private StrokeWidthChooseView strokeWidthChooseView;
    private int currentIndex = 4;
    private DrawView.State mCurrentState = DrawView.State.PEN;
    private onStateChanged onStateChangedListener;
    private int[] colors = new int[]{
            Color.getIntColor("#fd039d"),
            Color.getIntColor("#ff4d3f"),
            Color.getIntColor("#fda602"),
            Color.getIntColor("#fff001"),
            Color.getIntColor("#000000"),
            Color.getIntColor("#00b181"),
            Color.getIntColor("#004bfe"),
            Color.getIntColor("#2c6281"),
            Color.getIntColor("#4e4c61"),
            Color.getIntColor("#edd93f"),
            Color.getIntColor("#666666"),
            Color.getIntColor("#66b502"),
            Color.getIntColor("#66fecb"),
            Color.getIntColor("#03c1fe"),
            Color.getIntColor("#966b59"),
            Color.getIntColor("#fda7a4"),
            Color.getIntColor("#f42728"),
            Color.getIntColor("#2c6281"),
            Color.getIntColor("#4e4c61"),
            Color.getIntColor("#edd93f"),
            Color.getIntColor("#666666"),
            Color.getIntColor("#c9c9c9"),
            Color.getIntColor("#8efbf6"),
            Color.getIntColor("#78d1b8"),
            Color.getIntColor("#bb18fd"),
            Color.getIntColor("#ffffcc"),
            Color.getIntColor("#fdcdb7"),
            Color.getIntColor("#993300"),
    };

    public ColorHorizontalScrollView(Context context) {
        this(context, null);
    }

    public ColorHorizontalScrollView(Context context, AttrSet attrs) {
        super(context, attrs);
        this.context=context;
        init();

        // 设置测量组件的侦听器
        setEstimateSizeListener(this);
        // 设置TouchEvent响应事件
        setTouchEventListener(this);
        // 添加绘制任务
        addDrawTask(this);
    }


    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL_STYLE);

        rectList = new ArrayList<>();
        regionList = new ArrayList<>();
    }


    @Override
    public void onDraw(Component component, Canvas canvas) {
        Rect rect;
        Rect chooseRect;
        switch (mCurrentState){
            //橡皮擦模式不突出选中颜色
            case ERASER:
                for (int i = 0; i < rectList.size(); i++){
                    mPaint.setColor(new Color(colors[i]));
                    rect = rectList.get(i);
                    rect = new Rect(rect.left-getScrollValue(AXIS_X), rect.top , rect.right-getScrollValue(AXIS_X), rect.bottom );
                    canvas.drawRect(rect, mPaint);
                }
                break;
            case PEN:
                for (int i = 0; i < rectList.size(); i++){
                    mPaint.setColor(new Color(colors[i]));
                    rect = rectList.get(i);
                    //把选中的颜色突出显示
                    if (currentIndex == i){
                        chooseRect = new Rect(rect.left-getScrollValue(AXIS_X), rect.bottom - 10, rect.right-getScrollValue(AXIS_X), rect.bottom);
                        rect = new Rect(rect.left-getScrollValue(AXIS_X), rect.top - 30, rect.right-getScrollValue(AXIS_X), rect.bottom - 30);
                        canvas.drawRect(chooseRect, mPaint);
                        canvas.drawRect(rect, mPaint);
                    }else{
                        rect = new Rect(rect.left-getScrollValue(AXIS_X), rect.top , rect.right-getScrollValue(AXIS_X), rect.bottom );
                        canvas.drawRect(rect, mPaint);
                    }

                }
                break;
        }
    }

    @Override
    public boolean onEstimateSize(int widthEstimateConfig, int heightEstimateConfig) {

        int widthSpce = EstimateSpec.getMode(widthEstimateConfig);
        int heightSpce = EstimateSpec.getMode(heightEstimateConfig);

        int widthConfig = 0;
        switch (widthSpce) {
            case EstimateSpec.UNCONSTRAINT:
            case EstimateSpec.PRECISE:
                width = EstimateSpec.getSize(widthEstimateConfig);
                widthConfig = EstimateSpec.getSizeWithMode(width, EstimateSpec.PRECISE);
                break;
            case EstimateSpec.NOT_EXCEED:
                widthConfig = EstimateSpec.getSizeWithMode(width, EstimateSpec.PRECISE);
                break;
            default:
                break;
        }

        int heightConfig = 0;
        switch (heightSpce) {
            case EstimateSpec.UNCONSTRAINT:
            case EstimateSpec.PRECISE:
                height = EstimateSpec.getSize(heightEstimateConfig);
                heightConfig = EstimateSpec.getSizeWithMode(height, EstimateSpec.PRECISE);
                break;
            case EstimateSpec.NOT_EXCEED:
                heightConfig = EstimateSpec.getSizeWithMode(height, EstimateSpec.PRECISE);
                break;
            default:
                break;
        }
        setEstimatedSize(widthConfig, heightConfig);
        Rect rect;
        for (int i = 0; i < colors.length; i++){
            int centerX = 20 * i + radius * 2 * i + radius;
            int centerY = height/2;
            rect = new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            rectList.add(rect);
        }
        return true;
    }

    /**
     * 根据点击区域判断选中的颜色
     * @param component
     * @param touchEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        MmiPoint mmiPoint =touchEvent.getPointerPosition(touchEvent.getIndex());;
        switch (touchEvent.getAction()){
            case TouchEvent.PRIMARY_POINT_DOWN:
                HiLog.info(HI_LOG_LABEL,"TouchEvent.PRIMARY_POINT_DOWN");

                startX = mmiPoint.getX();
                startY = mmiPoint.getY();
                break;
            case TouchEvent.POINT_MOVE:
                HiLog.info(HI_LOG_LABEL,"TouchEvent.POINT_MOVE");

                dx = mmiPoint.getX() - startX;
                dy = mmiPoint.getY() - startY;
                HiLog.info(HI_LOG_LABEL," --- dx "+dx+", dy "+dy);
                if (Math.abs(dx) -  Math.abs(dy) >0){
                    if (getScrollValue(AXIS_X) + (-dx) < 0 || getScrollValue(AXIS_X) + (-dx) > getWidth()){
                        return true;
                    }
                    this.scrollBy((int) -dx, 0);
                    HiLog.info(HI_LOG_LABEL,"TouchEvent.Scroll"+getScrollValue(AXIS_X));
                    startX = mmiPoint.getX();
                    startY = mmiPoint.getY();
                }
                invalidate();
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+"TouchEvent.PRIMARY_POINT_UP");

                if (Math.abs(dx) <= 10 && Math.abs(dy) <= 10){
                    for (int i = 0; i < rectList.size(); i++){
                        if (rectList.get(i).contains((int) startX+getScrollValue(AXIS_X)  , (int) startY+getScrollValue(AXIS_Y) ,(int)startX+getScrollValue(AXIS_X),(int)startY+getScrollValue(AXIS_Y))){
                            HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- check rect "+i);
                            Rect rect;
                            rectList.clear();
                            for (int j = 0; j < colors.length; j++){
                                int centerX = 20 * j + radius * 2 * j + radius;
                                int centerY = height/2;
                                rect = new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
                                rectList.add(rect);
                            }

                            currentIndex = i;
                            onStateChangedListener.onPen();
                            strokeWidthChooseView.setmPaintColor(colors[i]);

                            invalidate();

                        }
                    }
                }

                startX = 0;
                startY = 0;
                dx = 0;
                dy = 0;
                break;
        }
        return true;
    }

    public interface onStateChanged{
        void onPen();
    }

    public void setOnStateChnagedListener(onStateChanged onStateChnagedListener) {
        this.onStateChangedListener = onStateChnagedListener;
    }

    public void setStrokeWidthChooseView(StrokeWidthChooseView strokeWidthChooseView) {
        this.strokeWidthChooseView = strokeWidthChooseView;
    }

    public void setmCurrentState(DrawView.State mCurrentState) {
        this.mCurrentState = mCurrentState;
        invalidate();
    }

    public int getScreenHeight() {
        return context.getResourceManager().getDeviceCapability().height
                * context.getResourceManager().getDeviceCapability().screenDensity
                / 160;
    }

}

