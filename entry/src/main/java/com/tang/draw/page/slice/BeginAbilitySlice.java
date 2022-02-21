package com.tang.draw.page.slice;

import com.tang.draw.Constants;
import com.tang.draw.ResourceTable;
import com.tang.draw.dialog.DeviceDialog;
import com.tang.draw.page.GameAbility;
import com.tang.draw.service.RemoteProxy;
import com.tang.draw.service.ServiceAbility;
import com.tang.draw.utils.DeviceUtils;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.utils.ToastUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.element.FrameAnimationElement;
import ohos.bundle.ElementName;
import ohos.rpc.IRemoteObject;

/********
 *文件名: BeginAbilitySlice
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/9 14:43
 *描述: BeginAbilitySlice
 ********/
public class BeginAbilitySlice extends AbilitySlice {
    private Button singleGameBtn,doubleGameBtn,dataManageBtn;
    private FrameAnimationElement frameAnimationElement;
    private DirectionalLayout componentContainer;
    private String mMainDeviceId, mRemoteDeviceId;



    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_begin);
        initAnimator();
        initComponents();
    }

    private void initComponents(){
        singleGameBtn=findComponentById(ResourceTable.Id_btn_single_game);
        doubleGameBtn=findComponentById(ResourceTable.Id_btn_double_game);
        dataManageBtn=findComponentById(ResourceTable.Id_btn_data_manage);
        componentContainer =findComponentById(ResourceTable.Id_frame_container);

        Component component = new Component(getContext());
        component.setWidth(300);
        component.setHeight(300);
        component.setBackground(frameAnimationElement);
        componentContainer.addComponent(component);
        //开始动画
        frameAnimationElement.start();
        singleGameBtn.setClickedListener(components -> startSingleGame());
        doubleGameBtn.setClickedListener(components -> startDoubleGame());
        dataManageBtn.setClickedListener(component1 -> startDataPage());
    }

    /**
     * 开始单人模式
     *
     */
    private void startSingleGame(){
        LogUtils.info(getClass().getSimpleName()+" --- startSingleGame()");
        //startDrawPage(Constants.SINGLE_GAME_MODEL);
        startSettingPage(Constants.SINGLE_GAME_MODEL);
    }

    /**
     * 开始双人模式
     */
    private void startDoubleGame(){
        LogUtils.info(getClass().getSimpleName()+" --- startDoubleGame()");
        showDeviceDialog();
    }

    /**
     * 初始化动画
     */
    private void initAnimator() {
        frameAnimationElement = new FrameAnimationElement(getContext(), ResourceTable.Graphic_animation_element);
    }

    /**
     * 打开游戏设置界面
     *
     * @param gameModel 单人模式 or 双人模式
     */
    private void startSettingPage(int gameModel){
        Intent intent=new Intent();
        //当为双人模式时需要把主机端和远程端设备ID一并传入
        if (gameModel==Constants.DOUBLE_GAME_MODEL){
            intent.setParam(Constants.PARAM_KEY_MAIN_DEVICE_ID, mMainDeviceId);
            intent.setParam(Constants.PARAM_KEY_REMOTE_DEVICE_ID, mRemoteDeviceId);
        }
        //设置游戏模式
        intent.setParam(Constants.STRING_GAME_MODEL,gameModel);
        //设置为主机端
        intent.setParam(Constants.PARAM_KEY_IS_MAIN_DEVICE,true);
        //直接切换slice
        present(new SettingsAbilitySlice(),intent);
    }



    /**
     * 打开设连接备列表
     *
     */
    private void showDeviceDialog(){
        DeviceDialog deviceDialog=new DeviceDialog(getContext(), deviceInfo -> {
            mRemoteDeviceId =deviceInfo.getDeviceId();
            //远程端设备上打开页面
            //startAnswerPage(mRemoteDeviceId);
            startSettingPage(Constants.DOUBLE_GAME_MODEL);
        });
        deviceDialog.show();
    }

    /**
     * 打开数据管理界面
     *
     */
    private void startDataPage(){
        Intent intent=new Intent();
        //直接切换slice
        present(new DataAbilitySlice(),intent);
    }

}
