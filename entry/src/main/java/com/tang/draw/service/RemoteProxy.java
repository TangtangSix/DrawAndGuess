package com.tang.draw.service;


import com.tang.draw.Constants;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.view.DrawView;
import ohos.agp.render.Path;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;
import ohos.rpc.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RemoteProxy implements IRemoteBroker {

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(0,0x01008,"RemoteProxy");

    private final IRemoteObject remote;

    /**
     * 构造方法
     *
     * @param remote IRemoteObject实例
     */
    public RemoteProxy(IRemoteObject remote) {
        this.remote = remote;
    }

    @Override
    public IRemoteObject asObject() {
        return remote;
    }


    public void sendMessage(int message){
        //把消息封装到MessageParcel
        MessageParcel data= MessageParcel.obtain();
        data.writeInt(message);


        MessageParcel reply =MessageParcel.obtain();
        MessageOption option =new MessageOption(MessageOption.TF_SYNC);
        try {
            //通过RemoteObject实例发送消息
            remote.sendRequest(Constants.SEND_COMMAND,data,reply,option);

            //获取消息传递结果
            int ec=reply.readInt();
            LogUtils.info(getClass().getSimpleName()+" sendMessage ec:"+ec);
            if(ec!= Constants.ERR_OK){
                throw new RemoteException();
            }
        } catch (RemoteException e) {
            HiLog.error(LABEL_LOG,"RemoteException: %{public}s",e.getMessage());
        }
    }

    /**
     * 发送绘制线的点坐标信息和画笔信息
     * @param points
     * @param colors
     * @param states
     * @param widths
     */
    public void sendDrawData(List<Point> points, int colors, DrawView.State states, float widths){
        LogUtils.info(getClass().getSimpleName()+" points "+points);
        LogUtils.info(getClass().getSimpleName()+" colors "+colors);
        LogUtils.info(getClass().getSimpleName()+" states "+states);
        LogUtils.info(getClass().getSimpleName()+" widths "+widths);
        //把消息封装到MessageParcel
        MessageParcel data= MessageParcel.obtain();

        float pointX[]=new float[points.size()];
        float pointY[]=new float[points.size()];

        for (int i = 0; i < points.size(); i++) {
            pointX[i]=points.get(i).getPointX();
            pointY[i]=points.get(i).getPointY();
        }
        data.writeFloatArray(pointX);
        data.writeFloatArray(pointY);
        data.writeInt(colors);
        data.writeInt(states== DrawView.State.PEN?0:1);
        data.writeFloat(widths);

        MessageParcel reply =MessageParcel.obtain();
        MessageOption option =new MessageOption(MessageOption.TF_SYNC);
        try {
            //通过RemoteObject实例发送消息
            remote.sendRequest(Constants.SEND_DRAW_DATA,data,reply,option);
            //获取消息传递结果
            int ec=reply.readInt();

            if(ec!= Constants.ERR_OK){
                throw new RemoteException();
            }
        } catch (RemoteException e) {
            HiLog.error(LABEL_LOG,"RemoteException: %{public}s",e.getMessage());
        }

    }

}
