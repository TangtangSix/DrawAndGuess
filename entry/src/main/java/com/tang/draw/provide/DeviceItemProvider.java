package com.tang.draw.provide;

import com.tang.draw.ResourceTable;
import ohos.agp.components.*;
import ohos.app.Context;
import ohos.distributedschedule.interwork.DeviceInfo;

import java.util.List;

/********
 *文件名: DeviceItemProvider
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/8 17:23
 *描述: DeviceItemProvider
 ********/
public class DeviceItemProvider extends BaseItemProvider {

    private final List<DeviceInfo> mDeviceDataList;
    private final Context mContext;
    private final int mItemLayoutId;

    /**
     *  @param mDeviceDataList 支持分布式协同的设备列表
     * @param mContext 上下文
     * @param mItemLayoutId 子项布局Id
     */
    public DeviceItemProvider(Context mContext, List<DeviceInfo> mDeviceDataList, int mItemLayoutId) {
        this.mDeviceDataList = mDeviceDataList;
        this.mContext = mContext;
        this.mItemLayoutId = mItemLayoutId;
    }

    @Override
    public int getCount() {
        if(mDeviceDataList==null || mDeviceDataList.isEmpty()){
            return 0;
        }
        else {
            return mDeviceDataList.size();
        }
    }

    @Override
    public Object getItem(int i) {
        if(mDeviceDataList==null || mDeviceDataList.isEmpty()){
            return null;
        }
        else {
            return mDeviceDataList.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Component getComponent(int i, Component convertComponent, ComponentContainer componentContainer) {
        final Component component;
        //绘制子项布局,如果没有实例,则使用LayoutScatter进行绘制,否则复用原型
        if (convertComponent==null){
            component= LayoutScatter.getInstance(mContext).parse(mItemLayoutId,null,false);
        }
        else {
            component=convertComponent;
        }

        //绘制子项布局控件
        Image itemImageType= (Image) component.findComponentById(ResourceTable.Id_item_type);
        Text itemTextDesc= (Text) component.findComponentById(ResourceTable.Id_item_desc);
        Text itemTextId= (Text) component.findComponentById(ResourceTable.Id_item_id);

        //将对应索引的设备信息显示到界面上
        DeviceInfo itemInfo=mDeviceDataList.get(i);
        itemTextDesc.setText(itemInfo.getDeviceName());
        itemTextId.setText(itemInfo.getDeviceId());
        switch (itemInfo.getDeviceType()){
            case SMART_PHONE:
                itemImageType.setPixelMap(ResourceTable.Media_dv_phone);
                break;
            case SMART_PAD:
                itemImageType.setPixelMap(ResourceTable.Media_dv_pad);
                break;
            case SMART_WATCH:
                itemImageType.setPixelMap(ResourceTable.Media_dv_watch);
                break;
        }
        return component;
    }
}
