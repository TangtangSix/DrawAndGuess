package com.tang.draw.service;

import com.tang.draw.Constants;
import com.tang.draw.view.DrawView;
import ohos.agp.render.Path;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.media.image.PixelMap;

import java.util.List;

public class ConnectionHelper {

    /**
     * 私有构造方法,避免单例被额外实例化
     */
    private ConnectionHelper(){}


    /**
     * 静态内部类,持有唯一的 ConnectionHelper 实例
     */
    private static class ConnectionHelperHolder{
        private static final ConnectionHelper INSTANCE =new ConnectionHelper();
    }


    /**
     * 对外提供获取实例的方法
     *
     * @return ConnectionHelper 实例
     */
    public static ConnectionHelper getInstance(){
        return ConnectionHelperHolder.INSTANCE;
    }

    /**
     * 通信回调
     */
    private IConnectionCallback mConnectionCallback;

    private SendDrawDataCallback sendDrawDataCallback;

    /**
     * 设置通信回调
     *
     * @param connectionCallback 通信回调
     */
    public void setCallback(IConnectionCallback connectionCallback){
        this.mConnectionCallback=connectionCallback;
    }


    public void setSendDrawDataCallback(SendDrawDataCallback sendDrawDataCallback) {
        this.sendDrawDataCallback = sendDrawDataCallback;
    }

    /**
     * 发消息
     *
     * @param message 消息
     */
    public void sendMessage(int message){
        if(mConnectionCallback!=null){
            mConnectionCallback.onCallback(message);
        }
    }

    /**
     * 发消息
     *
     * @param
     */
    public void sendDrawData(List<Point> points, int colors, DrawView.State states, float widths){
        if(sendDrawDataCallback!=null){
            sendDrawDataCallback.onCallback(points, colors, states, widths);
        }
    }

    public interface IConnectionCallback {

        /**
         * 通信回调
         *
         * @param message 消息
         */
        void onCallback(int message);
    }


    public interface SendDrawDataCallback{

        /**
         * 用于传输绘图数据
         *
         * @param points 组成线的点集合
         * @param colors paint颜色
         * @param states paint模式,0:pen 1:eraser
         * @param widths paint粗细
         */
        void onCallback(List<Point> points, int colors, DrawView.State states, float widths);
    }
}
