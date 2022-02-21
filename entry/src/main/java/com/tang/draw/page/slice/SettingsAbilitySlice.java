package com.tang.draw.page.slice;

import com.tang.draw.Constants;
import com.tang.draw.ResourceTable;
import com.tang.draw.dialog.AlertDialog;
import com.tang.draw.dialog.DeviceDialog;
import com.tang.draw.dialog.SelectDialog;
import com.tang.draw.page.GameAbility;
import com.tang.draw.utils.DeviceUtils;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.utils.ToastUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.utils.net.Uri;

import java.util.ArrayList;
import java.util.List;

/********
 *文件名: SettingsAbilitySlice
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/10 16:53
 *描述: SettingsAbilitySlice
 ********/
public class SettingsAbilitySlice extends AbilitySlice {

    private Button selectKeyBtn,selectTimeBtn;
    private Button startBtn,backBtn;

    private DataAbilityHelper databaseHelper;
    private String mMainDeviceId, mRemoteDeviceId;
    private int gameModel;
    private String key;
    private String time;
    private String keys[];
    private String times[];
    private AlertDialog alertDialog;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_settings);
        gameModel = intent.getIntParam(Constants.STRING_GAME_MODEL, Constants.SINGLE_GAME_MODEL);
        mRemoteDeviceId = intent.getStringParam(Constants.PARAM_KEY_REMOTE_DEVICE_ID);
        mMainDeviceId = intent.getStringParam(Constants.PARAM_KEY_MAIN_DEVICE_ID);
        initComponents();
        initClickedListener();
        initDatabaseHelper();
        initDialog();
        initData();
    }

    private void initData() {
        selectKeyBtn.setText("选择题目");
        selectTimeBtn.setText("选择时间");
        key="";
        time="";
        keys=query();
        times=new String[]{"1","2","3","4","5","6","7","8","9","10"};
    }

    private void initDialog() {
        alertDialog=new AlertDialog(this);
    }

    private void initDatabaseHelper() {
        databaseHelper = DataAbilityHelper.creator(this);
    }

    private void initComponents() {
        selectKeyBtn=findComponentById(ResourceTable.Id_btn_select_key);
        selectTimeBtn=findComponentById(ResourceTable.Id_btn_select_time);
        startBtn = findComponentById(ResourceTable.Id_btn_start);
        backBtn=findComponentById(ResourceTable.Id_btn_back);
    }

    private void initClickedListener(){
        selectKeyBtn.setClickedListener(this::showSelectKeyDialog);
        selectTimeBtn.setClickedListener(this::showSelectTimeDialog);
        startBtn.setClickedListener(component -> {
            if(key.isEmpty()){
                alertDialog.setmContentText("请选择题目!");
                alertDialog.show();
                return;
            }
            if(time.isEmpty()){
                alertDialog.setmContentText("请选择时间!");
                alertDialog.show();
                return;
            }
            if(gameModel==Constants.SINGLE_GAME_MODEL){
                startDrawPage(gameModel);
            }
            else if(gameModel==Constants.DOUBLE_GAME_MODEL){
                startAnswerPage(mRemoteDeviceId);
            }
        });

        backBtn.setClickedListener(component -> terminate());
    }


    private void showSelectKeyDialog(Component component){

        SelectDialog selectDialog=new SelectDialog(getContext(),keys, item -> {
            selectKeyBtn.setText(item);
            key=item;
        });

        selectDialog.show();
    }

    private void showSelectTimeDialog(Component component){

        SelectDialog selectDialog=new SelectDialog(getContext(),times, item -> {
            selectTimeBtn.setText(item+" 分钟");
            time=item;
        });

        selectDialog.show();
    }

    /**
     * 打开绘图界面
     *
     * @param gameModel 单人模式 or 双人模式
     */
    private void startDrawPage(int gameModel){
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
        //设置游戏时间
        intent.setParam(Constants.PARAM_GAME_TIME,Integer.parseInt(time));
        //设置游戏题目
        intent.setParam(Constants.PARAM_GAME_KEY,key);
        //直接切换slice
        present(new GameAbilitySlice(),intent);
    }

    /**
     * 打开猜图界面
     *
     * @param mRemoteDeviceId 远程设备ID
     */
    private void startAnswerPage(String mRemoteDeviceId){
        mMainDeviceId = DeviceUtils.getLocalDeviceId(getContext());
        if(mMainDeviceId ==null || mMainDeviceId.isEmpty()){
            ToastUtils.show(getContext(),"本机设备ID获取失败");
        }

        //构建 Intent
        Intent intent=new Intent();
        Operation operation=new Intent.OperationBuilder()
                .withDeviceId(mRemoteDeviceId)
                .withBundleName(getBundleName())
                .withAbilityName(GameAbility.class.getName())
                .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                .build();
        intent.setOperation(operation);
        //设置为远程端
        intent.setParam(Constants.PARAM_KEY_IS_MAIN_DEVICE,false);
        //把主机端设备ID、远程端设备ID封装到Intent
        intent.setParam(Constants.PARAM_KEY_MAIN_DEVICE_ID, mMainDeviceId);
        intent.setParam(Constants.PARAM_KEY_REMOTE_DEVICE_ID, mRemoteDeviceId);
        //设置游戏模式为双人模式
        intent.setParam(Constants.STRING_GAME_MODEL,Constants.DOUBLE_GAME_MODEL);
        //设置游戏时间
        intent.setParam(Constants.PARAM_GAME_TIME,Integer.parseInt(time));
        //设置游戏题目
        intent.setParam(Constants.PARAM_GAME_KEY,key);
        //唤起远程端FA
        startAbility(intent);
        //主机上打开绘图界面
        startDrawPage(Constants.DOUBLE_GAME_MODEL);
    }


    private String [] query() {

        String[] columns = new String[]{Constants.DB_COLUMN_KEY};
        String[] result = new String[0];

        DataAbilityPredicates predicates = new DataAbilityPredicates();
        try {
            ResultSet resultSet = databaseHelper.query(Uri.parse(Constants.BASE_URI + Constants.DATA_PATH), columns,
                    predicates);
            if (!resultSet.goToFirstRow())
                return null;
            int index=resultSet.getColumnIndexForName(Constants.DB_COLUMN_KEY);
            List<String> tmp=new ArrayList<>();
            do {
                String key = resultSet.getString(index);
                tmp.add(key);
            } while (resultSet.goToNextRow());
            resultSet.close();
            result=new String[tmp.size()];
            for (int i = 0; i < tmp.size() ; i++) {
                result[i]=tmp.get(i);
            }
        } catch (DataAbilityRemoteException | IllegalStateException exception) {
            LogUtils.error( "query: dataRemote exception|illegalStateException"+exception.getMessage());
        }

        return result;
    }
}
