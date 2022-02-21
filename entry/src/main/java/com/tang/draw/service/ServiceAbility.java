package com.tang.draw.service;


import com.tang.draw.Constants;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.view.DrawView;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.render.Path;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.utils.Color;
import ohos.agp.utils.Point;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;
import ohos.rpc.*;

import java.util.ArrayList;
import java.util.List;

public class ServiceAbility extends Ability {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100, "Demo");

    @Override
    public void onStart(Intent intent) {
        HiLog.info(LABEL_LOG, "ServiceAbility::onStart");
        super.onStart(intent);
    }

    @Override
    public void onBackground() {
        super.onBackground();
        HiLog.info(LABEL_LOG, "ServiceAbility::onBackground");
    }

    @Override
    public void onStop() {
        super.onStop();
        HiLog.info(LABEL_LOG, "ServiceAbility::onStop");
    }

    @Override
    public void onCommand(Intent intent, boolean restart, int startId) {
    }

    @Override
    public IRemoteObject onConnect(Intent intent) {
        HiLog.info(LABEL_LOG,"ServiceAbility::onConnect");
        return new GameRemoteObject("GameRemoteObject").asObject();
    }

    @Override
    public void onDisconnect(Intent intent) {
    }


    /**
     * 用于接受跨端信息
     *
     */
    private static class GameRemoteObject extends RemoteObject implements IRemoteBroker{

        public GameRemoteObject(String descriptor) {
            super(descriptor);
        }

        @Override
        public IRemoteObject asObject() {
            return this;
        }

        public boolean onRemoteRequest(int code, MessageParcel data, MessageParcel reply, MessageOption option){

            LogUtils.info(getClass().getSimpleName()+" code: "+code);
            if(code==Constants.SEND_COMMAND){
                //接受跨端信息
                int message =data.readInt();

                //通过 ConnectionHelper 将消息回调给 PageAbility
                ConnectionHelper.getInstance().sendMessage(message);

                //回传结果给消息发送者
                reply.writeInt(Constants.ERR_OK);

            }

            else if (code==Constants.SEND_DRAW_DATA){

                float pointX[]=data.readFloatArray();
                float pointY[]=data.readFloatArray();
                int colors=data.readInt();
                int states=data.readInt();
                float widths=data.readFloat();

                List<Point> points=new ArrayList<>();
                for (int i = 0; i < pointX.length; i++) {
                    points.add(new Point(pointX[i],pointY[i]));
                }

                LogUtils.info(getClass().getSimpleName()+" points "+points);
                LogUtils.info(getClass().getSimpleName()+" colors "+colors);
                LogUtils.info(getClass().getSimpleName()+" states "+states);
                LogUtils.info(getClass().getSimpleName()+" widths "+widths);
                ConnectionHelper.getInstance().sendDrawData(points,colors,states==0? DrawView.State.PEN: DrawView.State.ERASER,widths);
                reply.writeInt(Constants.ERR_OK);
            }


            return true;
        }
    }

}