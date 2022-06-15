package com.tang.draw.page.slice;

import com.tang.draw.Constants;
import com.tang.draw.ResourceTable;
import com.tang.draw.dialog.AlertDialog;
import com.tang.draw.dialog.ConfirmDialog;
import com.tang.draw.dialog.PromptDialog;
import com.tang.draw.service.ConnectionHelper;
import com.tang.draw.service.RemoteProxy;
import com.tang.draw.service.ServiceAbility;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.utils.ToastUtils;
import com.tang.draw.view.ColorHorizontalScrollView;
import com.tang.draw.view.DrawView;
import com.tang.draw.view.StrokeWidthChooseView;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.agp.window.service.WindowManager;
import ohos.bundle.ElementName;
import ohos.global.resource.NotExistException;
import ohos.rpc.IRemoteObject;

import java.io.IOException;
import java.util.List;

public class GameAbilitySlice extends AbilitySlice implements ColorHorizontalScrollView.onStateChanged {

    private AlertDialog alertDialog;
    private ConfirmDialog clearConfirmDialog,finishConfirmDialog;
    private PromptDialog promptDialog;
    private DrawView drawView;
    private ColorHorizontalScrollView colorHorizontalScrollView;
    private StrokeWidthChooseView strokeWidthChooseView;
    private Button backBtn;
    private Button finishBtn;
    private Text topicText, countdownText;
    private Button revokeBtn;
    private Button eraserBtn;
    private Button clearBtn;
    private Button submitBtn;
    private DirectionalLayout drawLayout, answerLayout;
    private TextField answerInput;
    private String key;
    private int time ;
    private int gameModel;
    private boolean isMainDevice = false;
    private boolean isGameOver=false;
    private String mMainDeviceId;
    private String mRemoteDeviceId;

    private static RemoteProxy mRemoteProxy = null;

    private final IAbilityConnection connection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            //连接成功,实例化代理
            mRemoteProxy = new RemoteProxy(iRemoteObject);
            getUITaskDispatcher().asyncDispatch(() -> {
                countdownText.setText("已连接");

            });

            LogUtils.info(getClass().getSimpleName() + " --- connection ");
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {

        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_game);
        WindowManager.getInstance().getTopWindow().get().setStatusBarColor(Color.getIntColor("#cdcdcd"));
        // 隐藏状态栏、设置状态栏和导航栏透明
//        getWindow().addFlags(WindowManager.LayoutConfig.MARK_FULL_SCREEN|
//                WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
//


        //获取传来的数据
        isMainDevice = intent.getBooleanParam(Constants.PARAM_KEY_IS_MAIN_DEVICE, true);
        gameModel = intent.getIntParam(Constants.STRING_GAME_MODEL, Constants.SINGLE_GAME_MODEL);
        mRemoteDeviceId = intent.getStringParam(Constants.PARAM_KEY_REMOTE_DEVICE_ID);
        mMainDeviceId = intent.getStringParam(Constants.PARAM_KEY_MAIN_DEVICE_ID);
        key=intent.getStringParam(Constants.PARAM_GAME_KEY);
        time=intent.getIntParam(Constants.PARAM_GAME_TIME,1)*60;

        initComponents();
        initListener();
        initDialog();

        //单人模式无需代理
        if(gameModel!=Constants.SINGLE_GAME_MODEL){
            //向 ConnectionHelper 注册回调,用于接收跨端消息
            ConnectionHelper.getInstance().setCallback(this::handleMessage);
            ConnectionHelper.getInstance().setSendDrawDataCallback(this::handleDrawData);
        }

    }

    @Override
    public void onActive() {
        super.onActive();
        LogUtils.info(getClass().getSimpleName() + " --- onActive");
        //防止第二次进入界面无法操作
        if(drawView!=null && finishBtn !=null){
            drawView.setCanDraw(true);
            finishBtn.setEnabled(true);
        }
        if (drawView != null) {
            drawView.clear();
        }

        if (gameModel == Constants.DOUBLE_GAME_MODEL) {
            if (isMainDevice) {
                connectService(mRemoteDeviceId);
            } else {
                connectService(mMainDeviceId);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.info(getClass().getSimpleName() + " --- onStop");

    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
        LogUtils.info(getClass().getSimpleName() + " --- onForeground");

    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        drawView = findComponentById(ResourceTable.Id_draw_view);

        backBtn = findComponentById(ResourceTable.Id_btn_back);
        topicText =findComponentById(ResourceTable.Id_text_topic);
        countdownText = findComponentById(ResourceTable.Id_text_countdown);
        finishBtn = findComponentById(ResourceTable.Id_btn_finish);

        revokeBtn = findComponentById(ResourceTable.Id_btn_revoke);
        eraserBtn = findComponentById(ResourceTable.Id_btn_eraser);
        clearBtn = findComponentById(ResourceTable.Id_btn_clear);

        drawLayout = findComponentById(ResourceTable.Id_layout_draw);
        answerLayout = findComponentById(ResourceTable.Id_layout_answer);
        answerInput = findComponentById(ResourceTable.Id_input_answer);
        submitBtn = findComponentById(ResourceTable.Id_btn_submit);


        countdownText.setText(time+"");
        countdownTime();

        LogUtils.info(getClass().getSimpleName() + " --- isMainDevice " + isMainDevice);
        if(gameModel==Constants.DOUBLE_GAME_MODEL){
            drawView.setCallback(this::sendDrawData);
        }

        //根据是否为主机端设置不同界面
        if (isMainDevice) {
            answerLayout.setVisibility(Component.HIDE);
            colorHorizontalScrollView = findComponentById(ResourceTable.Id_colorHorizontalScrollView);
            strokeWidthChooseView = findComponentById(ResourceTable.Id_strokeWidthChooseView);
            strokeWidthChooseView.setDrawView(drawView);
            colorHorizontalScrollView.setStrokeWidthChooseView(strokeWidthChooseView);
            colorHorizontalScrollView.setOnStateChnagedListener(this);
            topicText.setText(key);


        } else {
            drawLayout.setVisibility(Component.HIDE);
            drawView.setCanDraw(false);
            topicText.setVisibility(Component.HIDE);
            finishBtn.setVisibility(Component.INVISIBLE);
            submitBtn.setEnabled(false);
        }
    }

    /**
     * 设置对应按钮的监听事件
     */
    private void initListener() {
        backBtn.setClickedListener(component -> {
            LogUtils.info(getClass().getSimpleName() + " --- backBtn onclick");
            if(gameModel==Constants.DOUBLE_GAME_MODEL){
                sendMessage(Constants.BACK_COM);
            }

            quit();
        });

        finishBtn.setClickedListener(component -> {
            LogUtils.info(getClass().getSimpleName() + " --- finishBtn onclick");
            finishConfirmDialog.show();
        });

        revokeBtn.setClickedListener(component -> {
            drawView.revoke();
            if(gameModel==Constants.DOUBLE_GAME_MODEL){
                sendMessage(Constants.REVOKE_COM);
            }

        });

        eraserBtn.setClickedListener(component -> {
            if (drawView.getCurrentState() == DrawView.State.ERASER) {
                becomePen();
            } else if (drawView.getCurrentState() == DrawView.State.PEN) {
                becomeEraser();
            }

        });

        clearBtn.setClickedListener(component -> {
            clearConfirmDialog.show();

        });

        answerInput.addTextObserver(new Text.TextObserver() {
            @Override
            public void onTextUpdated(String s, int i, int i1, int i2) {
                if(s==null || s.isEmpty()){
                    submitBtn.setEnabled(false);
                }
                else{
                    submitBtn.setEnabled(true);
                }
            }
        });

        submitBtn.setClickedListener(component -> {
            answerInput.clearFocus();
            if(answerInput.getText().equals(key)){
                alertDialog.setmContentText("回答正确", true);
                sendMessage(Constants.GUESS_RIGHT);
            }
            else{
                alertDialog.setmContentText("回答错误", false);
                sendMessage(Constants.GUESS_ERROR);
            }
            isGameOver=true;
            alertDialog.show();
        });
    }

    /**
     * 初始化对话框
     *
     */
    private void initDialog() {
        clearConfirmDialog = new ConfirmDialog(getContext());
        clearConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
            @Override
            public void onOKClick() {
                drawView.clear();
                if(gameModel==Constants.DOUBLE_GAME_MODEL){
                    sendMessage(Constants.CLEAR_COM);
                }

            }

            @Override
            public void onCancelClick() {

            }
        });
        finishConfirmDialog=new ConfirmDialog(this);
        finishConfirmDialog.setDetailText("提交后将无法进行绘制");
        finishConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
            @Override
            public void onOKClick() {
                if(gameModel==Constants.DOUBLE_GAME_MODEL){
                    sendMessage(Constants.FINISH_COM);
                }

                drawView.setCanDraw(false);
            }

            @Override
            public void onCancelClick() {

            }
        });
        alertDialog=new AlertDialog(this);

        promptDialog = new PromptDialog(getContext());
        promptDialog.setAutoClosable(true);


    }

    @Override
    public void onPen() {
        becomePen();
    }

    /**
     * 切换到pen
     *
     */
    private void becomePen() {
        drawView.setCurrentState(DrawView.State.PEN);
        strokeWidthChooseView.setmCurrentState(DrawView.State.PEN);
        colorHorizontalScrollView.setmCurrentState(DrawView.State.PEN);
        try {
            eraserBtn.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_icon_rubbish)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到eraser
     *
     */
    private void becomeEraser() {
        drawView.setCurrentState(DrawView.State.ERASER);
        strokeWidthChooseView.setmCurrentState(DrawView.State.ERASER);
        colorHorizontalScrollView.setmCurrentState(DrawView.State.ERASER);
        try {
            eraserBtn.setBackground(new PixelMapElement(getResourceManager().getResource(ResourceTable.Media_icon_pen)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计时线程
     *
     */
    private void countdownTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (time > 0 && !isGameOver) {
                    try {
                        Thread.sleep(1000);
                        time--;
                        getUITaskDispatcher().asyncDispatch(() -> {
                            countdownText.setText(time + "");
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(time==0 && gameModel==Constants.DOUBLE_GAME_MODEL){
                    sendMessage(Constants.TIME_OUT);
                }
            }
        }).start();
    }


    /**
     * 接受并处理收到的跨端信息
     *
     * @param message 消息
     */
    private void handleMessage(int message) {
        LogUtils.info(getClass().getSimpleName() + " --- handleMessage " + message);
        //切换到主线程
        getUITaskDispatcher().asyncDispatch(() -> {
            switch (message){
                case Constants.BACK_COM:
                    playerQuit();
                    break;
                case Constants.FINISH_COM:
                    finish();
                    break;
                case Constants.REVOKE_COM:
                     drawView.revoke();
                     break;
                case Constants.CLEAR_COM:
                    drawView.clear();
                    break;
                case Constants.GUESS_RIGHT:
                    gameOver(true);
                    break;
                case Constants.GUESS_ERROR:
                    gameOver(false);
                    break;
                case Constants.TIME_OUT:
                    timeOut();
                    break;
                default:
                    break;
            }
        });

    }

    /**
     * 远程端处理主机端发送来的path数据
     *
     * @param points path经过point集合
     * @param colors 画笔颜色
     * @param states paint or eraser
     * @param widths 画笔宽度
     */
    private void handleDrawData(List<Point> points, int colors, DrawView.State states, float widths) {
        //切换到主线程
        getUITaskDispatcher().asyncDispatch(() -> {
            LogUtils.info(getClass().getSimpleName()+" points "+points);
            drawView.setDrawPaintColor(new Color(colors));
            drawView.setCurrentState(states);
            if(states== DrawView.State.PEN){
                drawView.setDrawPaintStrokeWidth(widths);
            }
            else {
                drawView.setEraserPaintStrokeWidth(widths);
            }
            drawView.drawPoints(points);
        });
    }


    /**
     * 连接service
     *
     * @param deviceId
     */
    private void connectService(String deviceId) {
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId(deviceId)
                .withBundleName(getBundleName())
                .withAbilityName(ServiceAbility.class.getName())
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);

        //使用intent远程连接端 service
        connectAbility(intent, connection);
    }


    /**
     * 主机端和远程端互相发送消息
     *
     * @param message
     */
    private void sendMessage(int message) {
        if (mRemoteProxy == null) {
            ToastUtils.show(getContext(), "无跨端连接代理");
        } else {
            mRemoteProxy.sendMessage(message);
        }
    }

    /**
     * 把新画的path数据传送给远程端
     *
     */
    private void sendDrawData(List<Point> points,int color,DrawView.State state,float width) {
        LogUtils.info(getClass().getSimpleName() + " sendPixelMap");
        if (mRemoteProxy == null) {
            ToastUtils.show(getContext(), "无跨端连接代理");
        } else {
            mRemoteProxy.sendDrawData(points, color, state, width);
        }
    }

    /**
     * 当其中一方退出游戏则游戏结束
     *
     */
    private void playerQuit(){
        AlertDialog tmpDialog=new AlertDialog(this);
        tmpDialog.setDialogClickListener(this::quit);
        if(isMainDevice){
            tmpDialog.setmContentText("远程端退出,游戏结束");
        }
        else {
            tmpDialog.setmContentText("主机端退出,游戏结束");
        }
        isGameOver=true;
        tmpDialog.show();

    }

    /**
     * 退出游戏界面
     *
     */
    private void quit(){
        terminate();
    }

    /**
     * 游戏结束
     *
     * @param guessRight 远程端是否答对
     */
    private void gameOver(boolean guessRight){
        if(guessRight){
            alertDialog.setmContentText("远程端回答正确,游戏结束");
        }
        else {
            alertDialog.setmContentText("远程端回答错误,游戏结束");
        }
        isGameOver=true;
        disableComponents();
        alertDialog.show();
    }

    /**
     * 主机端点击完成
     *
     */
    private void finish(){
        alertDialog.setmContentText("主机端已经绘制完成,请抓紧时间");
        alertDialog.show();
    }

    /**
     * 游戏时间到
     *
     */
    private void timeOut(){
        LogUtils.info(getClass().getSimpleName() + " timeOut");
        alertDialog.setmContentText("时间到,游戏结束");
        isGameOver=true;
        disableComponents();
        alertDialog.show();
    }

    /**
     * 当游戏结束后禁用部分组件
     *
     */
    private void disableComponents(){
        if(!isMainDevice) {
            answerInput.setEditable(false);
            submitBtn.setEnabled(false);
        }
        else {
            finishBtn.setEnabled(false);
            drawView.setCanDraw(false);
        }
    }

}
