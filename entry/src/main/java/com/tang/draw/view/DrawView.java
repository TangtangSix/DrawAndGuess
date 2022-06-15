package com.tang.draw.view;

import com.tang.draw.utils.LogUtils;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;

import ohos.agp.render.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/********
 *文件名: DrawView
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/4 22:51
 *描述: DrawView
 ********/
public class DrawView extends Component implements Component.DrawTask,Component.EstimateSizeListener,Component.TouchEventListener {
    private static final String TAG = DrawView.class.getSimpleName();
    private static final HiLogLabel HI_LOG_LABEL=new HiLogLabel(HiLog.LOG_APP,0x00201,TAG);

    private Paint drawPaint;
    private Paint eraserPaint;
    private Paint backPaint;
    private Path mPath;
    private float lastX, lastY;

    private boolean canDraw=true;

    private PixelMap cacheBitmap;
    private Canvas cacheCanvas;
    private int width;
    private int height;

    public enum State {
        PEN,
        ERASER
    }

    private State mCurrentState = State.PEN;
    private float mStrokeWidth;
    private float mEraserWidth;
    private Color mPaintColor = Color.BLACK;
    private List<Point> points;
    private Stack<Path> paths;
    private Stack<Integer> states;
    private Stack<Color> colors;
    private Stack<Float> widths;

    private Paint circlePaint;

    private DrawCallback callback;
    public interface DrawCallback {
        void sendDrawData(List<Point> points,int color,DrawView.State state,float width);
    }


    public DrawView(Context context, AttrSet attrs) {
        super(context, attrs);
        LogUtils.info(getClass().getSimpleName()+" --- DrawView");
        init();

        // 设置测量组件的侦听器
        setEstimateSizeListener(this);
        // 设置TouchEvent响应事件
        setTouchEventListener(this);
        // 添加绘制任务
        addDrawTask(this);
    }

    public void init() {

        circlePaint = new Paint();
        circlePaint.setColor(Color.YELLOW);
        circlePaint.setStrokeWidth(100f);
        circlePaint.setStyle(Paint.Style.STROKE_STYLE);


        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE_STYLE);
        drawPaint.setStrokeCap(Paint.StrokeCap.ROUND_CAP);
        drawPaint.setColor(mPaintColor);
        drawPaint.setStrokeJoin(Paint.Join.ROUND_JOIN);

        backPaint = new Paint();
        backPaint.setColor(Color.WHITE);
        backPaint.setAntiAlias(true);


        eraserPaint = new Paint();
        eraserPaint.setAlpha(0);

//        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//        eraserPaint.set
        //eraserPaint.setColor(Color.RED);
        eraserPaint.setColor(Color.WHITE);
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setStyle(Paint.Style.STROKE_STYLE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND_JOIN);

        mPath = new Path();
        points=new ArrayList<>();
        paths = new Stack<>();
        states = new Stack<>();
        colors = new Stack<>();
        widths = new Stack<>();

    }

    public PixelMap getCacheBitmap() {
        return cacheBitmap;
    }

    public void setCacheBitmap(PixelMap pixelMap){
        this.cacheBitmap=pixelMap;
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        HiLog.info(HI_LOG_LABEL, getClass().getSimpleName()+" --- onDraw");

        if (cacheBitmap!=null){
            PixelMapHolder pixelMapHolder=new PixelMapHolder(cacheBitmap);
            canvas.drawPixelMapHolder(pixelMapHolder,0,0,backPaint);
        }
    }


    @Override
    public boolean onEstimateSize(int widthEstimateConfig, int heightEstimateConfig) {
        int componentWidth = EstimateSpec.getSize(widthEstimateConfig);
        int componentHeight = EstimateSpec.getSize(heightEstimateConfig);
        this.width = componentWidth;
        this.height = componentHeight;
        setEstimatedSize(

                EstimateSpec.getChildSizeWithMode(componentWidth, componentWidth, EstimateSpec.PRECISE),

                EstimateSpec.getChildSizeWithMode(componentHeight, componentHeight, EstimateSpec.PRECISE)
        );
        initCache();
        return true;
    }

    public void initCache() {
        PixelMap.InitializationOptions initializationOptions = new PixelMap.InitializationOptions();
        initializationOptions.size = new Size(this.width, this.height);
        initializationOptions.pixelFormat = PixelFormat.ARGB_8888;
        initializationOptions.editable = true;

        cacheBitmap = PixelMap.create(initializationOptions);
        Texture texture = new Texture(cacheBitmap);
        cacheCanvas=new Canvas(texture);
    }

    /**
     * 按下开始画线,抬起结束画线,把画线结果保存
     * @param component
     * @param touchEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        if(!canDraw){
            return true;
        }
        MmiPoint point = touchEvent.getPointerPosition(touchEvent.getIndex());
        float x = point.getX();
        float y = point.getY();
        //HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- point ("+x+","+y+")");
        switch (touchEvent.getAction()) {
            case TouchEvent.PRIMARY_POINT_DOWN:
               // HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- TouchEvent.PRIMARY_POINT_DOWN ");
                TouchDown(x, y);
                break;
            case TouchEvent.POINT_MOVE:
                //HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- TouchEvent.POINT_MOVE ");
                TouchMove(x, y);
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                //HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- TouchEvent.PRIMARY_POINT_UP ");
                TouchUp();
                break;
        }
        //HiLog.info(HI_LOG_LABEL, getClass().getSimpleName()+" --- path size " +paths.size());
        return true;
    }



    private void TouchUp() {
        Path path = new Path(mPath);
        paths.push(path);
        mPath.reset();
        if (mCurrentState == State.PEN) {
            states.push(0);
            Color color = drawPaint.getColor();
            colors.push(color);
            widths.push(drawPaint.getStrokeWidth());
        } else {
            states.push(1);
            Color color = eraserPaint.getColor();
            colors.push(color);
            widths.push(eraserPaint.getStrokeWidth());
        }

        if (canDraw && callback!=null)
            callback.sendDrawData(points,colors.lastElement().getValue(),mCurrentState,widths.lastElement());
    }

    private void TouchMove(float x, float y) {
        float dx = Math.abs(x - lastX);
        float dy = Math.abs(y - lastY);
        mPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
        lastX = x;
        lastY = y;

        points.add(new Point(x, y));
        drawPath();
        invalidate();

    }

    private void TouchDown(float x, float y) {
        if (mPath==null)
            mPath=new Path();
        points.clear();
        points.add(new Point(x,y));
        mPath.moveTo(x, y);
        lastX = x;
        lastY = y;
        drawPath();
    }

    public void setCurrentState(State state) {
        this.mCurrentState = state;
        HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- change state "+this.mCurrentState);
        invalidate();
    }

    public State getCurrentState() {
        return mCurrentState;
    }

    private void drawPath() {
//        HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" --- mPath "+mPath);

        switch (mCurrentState) {
            case PEN:
                cacheCanvas.drawPath(mPath, drawPaint);
                break;
            case ERASER:
                cacheCanvas.drawPath(mPath, eraserPaint);
                break;

        }
    }

    public void drawPoints(List<Point> tmpPoints){
        if (tmpPoints!=null){
            int size=tmpPoints.size();
            for (int i = 0; i <size ; i++) {
                if (i==0){
                    TouchDown(tmpPoints.get(i).getPointX(),tmpPoints.get(i).getPointY());
                }
                else if (i>0 && i<size-1){
                    TouchMove(tmpPoints.get(i).getPointX(),tmpPoints.get(i).getPointY());
                }
                else {
                    TouchUp();
                }
            }

        }
    }

    public void setDrawPaintStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        drawPaint.setStrokeWidth(mStrokeWidth);
    }

    public void setDrawPaintColor(Color mPaintColor) {
        this.mPaintColor = mPaintColor;
        drawPaint.setColor(mPaintColor);
    }


    public void clear() {
        LogUtils.info(getClass().getSimpleName()+" --- clear() ");
        if (cacheCanvas!=null){
            cacheCanvas.drawColor(Color.TRANSPARENT.getValue(), Canvas.PorterDuffMode.CLEAR);
        }

        mPath.reset();
        paths.clear();
        states.clear();
        colors.clear();
        widths.clear();
        invalidate();
    }

    public void revoke() {
        LogUtils.info(getClass().getSimpleName()+" --- back() ");
        if (!paths.empty()) {
            //清空画布
            cacheCanvas.drawColor(Color.TRANSPARENT.getValue(), Canvas.PorterDuffMode.CLEAR);
            mPath.reset();
            paths.pop();
            states.pop();
            colors.pop();
            widths.pop();


            Paint pen = new Paint();
            Paint eraser = new Paint();

            pen.setAntiAlias(true);
            pen.setStyle(Paint.Style.STROKE_STYLE);
            pen.setStrokeCap(Paint.StrokeCap.ROUND_CAP);
            pen.setStrokeJoin(Paint.Join.ROUND_JOIN);

            eraser.setAlpha(0);
            eraser.setAntiAlias(true);
            eraser.setDither(true);
            eraser.setStyle(Paint.Style.STROKE_STYLE);
            eraser.setStrokeJoin(Paint.Join.ROUND_JOIN);

            HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" ---back() path sizes  "+paths.size());
            for (int i = 0; i < paths.size(); i++) {

                //画笔状态
                if (states.get(i) == 0) {
                    HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" ---back() pen"+paths.get(i).isEmpty()+" widths "+widths.get(i)+" color "+colors.get(i).getValue() );
                    pen.setColor(colors.get(i));
                    pen.setStrokeWidth(widths.get(i));
                    cacheCanvas.drawPath(paths.get(i), pen);
                } else {
                    HiLog.info(HI_LOG_LABEL,getClass().getSimpleName()+" ---back() eraser"+i);
                    eraser.setColor(colors.get(i));
                    eraser.setStrokeWidth(widths.get(i));
                    cacheCanvas.drawPath(paths.get(i), eraser);
                }
            }

            invalidate();
        }
    }

    public void setEraserPaintStrokeWidth(float mEraserWidth) {
        this.mEraserWidth = mEraserWidth;
        eraserPaint.setStrokeWidth(mEraserWidth);
    }

    public void setCallback(DrawCallback callback) {
        this.callback = callback;
    }

    public void setCanDraw(Boolean canDraw){
        this.canDraw=canDraw;
    }


}

