package com.tang.draw.utils;

import com.tang.draw.Constants;
import ohos.app.Context;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.distributedschedule.interwork.DeviceInfo;
import ohos.distributedschedule.interwork.DeviceManager;

import java.util.List;

/********
 *文件名: DeviceUtils
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/8 17:19
 *描述: DeviceUtils
 ********/
public class DeviceUtils {
    private DeviceUtils(){}
    /*获取远程设备信息列表*
     *@return远程设备信息列表*/
    public static List<DeviceInfo> getRemoteDeviceList(){
        return DeviceManager.getDeviceList(DeviceInfo.FLAG_GET_ONLINE_DEVICE);
    };
    /**
     *获取当前设备ID
     * @param context 当前上下文
     * @return设备ID,若获取不到则返回字符串*
     **/
    public static String getLocalDeviceId(Context context){
        if (context == null) {
            return Constants.EMPTY_STRING;
        }
        return KvManagerFactory.getInstance().createKvManager(new KvManagerConfig(context)).getLocalDeviceInfo().getId();
    }
}
