package com.tang.draw.dialog;

import com.tang.draw.ResourceTable;
import com.tang.draw.provide.DeviceItemProvider;
import com.tang.draw.utils.DeviceUtils;
import ohos.agp.components.*;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.app.Context;
import ohos.distributedschedule.interwork.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;

/********
 *文件名: DeviceDialog
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/8 17:18
 *描述: DeviceDialog
 ********/
public class DeviceDialog extends CommonDialog {
    //圆角矩形的圆角半径
    private static final int RADIO_SIZE =10;

    //支持分布式协同的设备列表
    private final List<DeviceInfo> mDeviceList = new ArrayList<>();

    //设备选择回调监听
    private final DeviceSelectListener mDeviceSelectListener;

    //当前上下文对象
    private final Context mContext;

    public DeviceDialog(Context context, DeviceSelectListener mDeviceSelectListener) {
        super(context);
        this.mDeviceSelectListener = mDeviceSelectListener;
        this.mContext = context;
    }


    @Override
    public void onCreate(){
        super.onCreate();
        //初始化设备列表
        initDeviceList();

        //初始化界面布局
        Component rootView = LayoutScatter.getInstance(mContext).parse(ResourceTable.Layout_dialog_device,null,false);

        //初始化列表组件
        ListContainer listContainer= (ListContainer) rootView.findComponentById(ResourceTable.Id_list_container_device);
        BaseItemProvider deviceItemProvider =new DeviceItemProvider(mContext,mDeviceList,ResourceTable.Layout_item_device);
        listContainer.setItemProvider(deviceItemProvider);

        //设置列表项点击监听
        listContainer.setItemClickedListener(((listContainer1, component, i, l) -> {
            if (mDeviceSelectListener!=null){
                //通过接口队伍回调用户选中的设备信息
                mDeviceSelectListener.onDeviceSelected(mDeviceList.get(i));
                //隐藏对话框
                hide();
            }
        }));
        //初始化取消按钮
        Button buttonCancel= (Button) rootView.findComponentById(ResourceTable.Id_operate_no);

        //设置点击监听
        buttonCancel.setClickedListener(component -> {
            hide();
        });

        //设置对话框尺寸
        setSize(MATCH_PARENT,MATCH_CONTENT);

        //设置对话框位置
        setAlignment(LayoutAlignment.CENTER);

        //设置对话框的圆角背景
        setCornerRadius(RADIO_SIZE);

        //设置对话框背景为透明
        setTransparent(true);

        //设置对话框的布局
        setContentCustomComponent(rootView);

    }

    public interface DeviceSelectListener{
        void onDeviceSelected(DeviceInfo deviceInfo);
    }

    private void initDeviceList(){
        List<DeviceInfo> deviceInfoList = DeviceUtils.getRemoteDeviceList();
        mDeviceList.clear();
        mDeviceList.addAll(deviceInfoList);
    }
}
