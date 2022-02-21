package com.tang.draw.provide;

import com.tang.draw.ResourceTable;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.app.Context;

import java.util.GregorianCalendar;
import java.util.List;

/********
 *文件名: KeyItemProvider
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/17 14:52
 *描述: KeyItemProvider
 ********/
public class KeyItemProvider extends BaseItemProvider {

    private final List<String> mDataList;
    private final Context mContext;


    public KeyItemProvider(Context mContext,List<String> mDataList) {
        this.mDataList = mDataList;
        this.mContext = mContext;

    }

    @Override
    public int getCount() {
        if(mDataList==null || mDataList.isEmpty()){
            return 0;
        }
        else {
            return mDataList.size();
        }
    }

    @Override
    public String getItem(int i) {
        if(mDataList==null || mDataList.isEmpty()){
            return null;
        }
        else {
            return mDataList.get(i);
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
            component= LayoutScatter.getInstance(mContext).parse(ResourceTable.Layout_item_list,null,false);
        }
        else {
            component=convertComponent;
        }
        ShapeElement shapeElement;
        if ((i+1)%2==1){
            shapeElement=new ShapeElement(this.mContext,ResourceTable.Graphic_background_item_single);
        }
        else{
            shapeElement=new ShapeElement(this.mContext,ResourceTable.Graphic_background_item_double);
        }


        component.setBackground(shapeElement);
        Text itemText=component.findComponentById(ResourceTable.Id_text_item);
        itemText.setText(mDataList.get(i));

        return component;
    }
}