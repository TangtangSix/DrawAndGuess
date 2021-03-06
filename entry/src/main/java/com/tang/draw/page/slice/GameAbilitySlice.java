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
            //????????????,???????????????
            mRemoteProxy = new RemoteProxy(iRemoteObject);
            getUITaskDispatcher().asyncDispatch(() -> {
                countdownText.setText("?????????");

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
        // ???????????????????????????????????????????????????
//        getWindow().addFlags(WindowManager.LayoutConfig.MARK_FULL_SCREEN|
//                WindowManager.LayoutConfig.MARK_TRANSLUCENT_STATUS);
//


        //?????????????????????
        isMainDevice = intent.getBooleanParam(Constants.PARAM_KEY_IS_MAIN_DEVICE, true);
        gameModel = intent.getIntParam(Constants.STRING_GAME_MODEL, Constants.SINGLE_GAME_MODEL);
        mRemoteDeviceId = intent.getStringParam(Constants.PARAM_KEY_REMOTE_DEVICE_ID);
        mMainDeviceId = intent.getStringParam(Constants.PARAM_KEY_MAIN_DEVICE_ID);
        key=intent.getStringParam(Constants.PARAM_GAME_KEY);
        time=intent.getIntParam(Constants.PARAM_GAME_TIME,1)*60;

        initComponents();
        initListener();
        initDialog();

        //????????????????????????
        if(gameModel!=Constants.SINGLE_GAME_MODEL){
            //??? ConnectionHelper ????????????,????????????????????????
            ConnectionHelper.getInstance().setCallback(this::handleMessage);
            ConnectionHelper.getInstance().setSendDrawDataCallback(this::handleDrawData);
        }

    }

    @Override
    public void onActive() {
        super.onActive();
        LogUtils.info(getClass().getSimpleName() + " --- onActive");
        //???????????????????????????????????????
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
     * ???????????????
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

        //??????????????????????????????????????????
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
     * ?????????????????????????????????
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
                alertDialog.setmContentText("????????????", true);
                sendMessage(Constants.GUESS_RIGHT);
            }
            else{
                alertDialog.setmContentText("????????????", false);
                sendMessage(Constants.GUESS_ERROR);
            }
            isGameOver=true;
            alertDialog.show();
        });
    }

    /**
     * ??????????????????
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
        finishConfirmDialog.setDetailText("??????????????????????????????");
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
     * ?????????pen
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
     * ?????????eraser
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
     * ????????????
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
     * ????????????????????????????????????
     *
     * @param message ??????
     */
    private void handleMessage(int message) {
        LogUtils.info(getClass().getSimpleName() + " --- handleMessage " + message);
        //??????????????????
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
     * ????????????????????????????????????path??????
     *
     * @param points path??????point??????
     * @param colors ????????????
     * @param states paint or eraser
     * @param widths ????????????
     */
    private void handleDrawData(List<Point> points, int colors, DrawView.State states, float widths) {
        //??????????????????
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
     * ??????service
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

        //??????intent??????????????? service
        connectAbility(intent, connection);
    }


    /**
     * ???????????????????????????????????????
     *
     * @param message
     */
    private void sendMessage(int message) {
        if (mRemoteProxy == null) {
            ToastUtils.show(getContext(), "?????????????????????");
        } else {
            mRemoteProxy.sendMessage(message);
        }
    }

    /**
     * ????????????path????????????????????????
     *
     */
    private void sendDrawData(List<Point> points,int color,DrawView.State state,float width) {
        LogUtils.info(getClass().getSimpleName() + " sendPixelMap");
        if (mRemoteProxy == null) {
            ToastUtils.show(getContext(), "?????????????????????");
        } else {
            mRemoteProxy.sendDrawData(points, color, state, width);
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     */
    private void playerQuit(){
        AlertDialog tmpDialog=new AlertDialog(this);
        tmpDialog.setDialogClickListener(this::quit);
        if(isMainDevice){
            tmpDialog.setmContentText("???????????????,????????????");
        }
        else {
            tmpDialog.setmContentText("???????????????,????????????");
        }
        isGameOver=true;
        tmpDialog.show();

    }

    /**
     * ??????????????????
     *
     */
    private void quit(){
        terminate();
    }

    /**
     * ????????????
     *
     * @param guessRight ?????????????????????
     */
    private void gameOver(boolean guessRight){
        if(guessRight){
            alertDialog.setmContentText("?????????????????????,????????????");
        }
        else {
            alertDialog.setmContentText("?????????????????????,????????????");
        }
        isGameOver=true;
        disableComponents();
        alertDialog.show();
    }

    /**
     * ?????????????????????
     *
     */
    private void finish(){
        alertDialog.setmContentText("???????????????????????????,???????????????");
        alertDialog.show();
    }

    /**
     * ???????????????
     *
     */
    private void timeOut(){
        LogUtils.info(getClass().getSimpleName() + " timeOut");
        alertDialog.setmContentText("?????????,????????????");
        isGameOver=true;
        disableComponents();
        alertDialog.show();
    }

    /**
     * ????????????????????????????????????
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
