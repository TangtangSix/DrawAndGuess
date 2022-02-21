package com.tang.draw.dialog;

import com.tang.draw.ResourceTable;
import com.tang.draw.provide.DeviceItemProvider;
import com.tang.draw.provide.SelectItemProvider;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.app.Context;
import ohos.distributedschedule.interwork.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;
import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;

/********
 *文件名: SelectDialog
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/15 14:17
 *描述: SelectDialog
 ********/
public class SelectDialog extends CommonDialog {
    //圆角矩形的圆角半径
    private static final int RADIO_SIZE =10;

    //支持分布式协同的设备列表
    private  String[] mDataList ;

    //设备选择回调监听
    private final ItemSelectListener mItemSelectListener;

    //当前上下文对象
    private final Context mContext;

    public SelectDialog(Context context, String[] dataList,ItemSelectListener itemSelectListener) {
        super(context);
        this.mItemSelectListener = itemSelectListener;
        this.mContext = context;
        this.mDataList =dataList;
    }


    @Override
    public void onCreate(){
        super.onCreate();

        //初始化界面布局
        Component rootView = LayoutScatter.getInstance(mContext).parse(ResourceTable.Layout_dialog_select,null,false);

        //初始化picker
        Picker picker=rootView.findComponentById(ResourceTable.Id_picker);

        //初始化按钮
        Button cancelBtn= rootView.findComponentById(ResourceTable.Id_btn_cancel);
        Button okBtn=rootView.findComponentById(ResourceTable.Id_btn_ok);


        if (mDataList==null || mDataList.length==0){
            String item[]={"没有数据"};
            picker.setDisplayedData(item);
            picker.setValue(0);
            picker.setEnabled(false);
            okBtn.setClickedListener(component -> {
                destroy();
            });
        }
        else{
            ShapeElement shape = new ShapeElement();
            shape.setShape(ShapeElement.RECTANGLE);
            shape.setRgbColor(RgbColor.fromArgbInt(0xCFCFCF));
            picker.setDisplayedLinesElements(shape, shape);
            picker.setDisplayedData(mDataList);
            okBtn.setClickedListener(component -> {
                mItemSelectListener.onItemSelected(mDataList[picker.getValue()]);
                destroy();
            });
        }
        //设置点击监听
        cancelBtn.setClickedListener(component -> {
            destroy();
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

    public interface ItemSelectListener{
        void onItemSelected(String item);
    }
}
