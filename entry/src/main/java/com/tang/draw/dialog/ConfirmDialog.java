package com.tang.draw.dialog;

import com.tang.draw.ResourceTable;
import com.tang.draw.utils.LogUtils;
import com.tang.draw.view.StrokeWidthChooseView;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.utils.TextAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;

/********
 *文件名: ConfirmDialog
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/7 17:24
 *描述: ConfirmDialog
 ********/
public class ConfirmDialog extends CommonDialog implements Component.ClickedListener {


    private Context context;
    private Text  detailText;
    private Button okBtn,cancelBtn;
    private OnDialogClickListener dialogClickListener;
    public ConfirmDialog(Context context) {
        super(context);
        this.context=context;

        //居中
        setAlignment(LayoutAlignment.CENTER);

        //设置高度为自适应,宽度为屏幕的0.8
        setSize(context.getResourceManager().getDeviceCapability().width
                * context.getResourceManager().getDeviceCapability().screenDensity
                / 160*4/5, MATCH_CONTENT);
        //设置是否启用触摸对话框外区域关闭对话框的功能
        setAutoClosable(false);
        initComponents();

        LogUtils.info(getClass().getSimpleName()+" --- create dialog");
    }

    @Override
    public void onClick(Component component) {
        if (component==okBtn){
            LogUtils.info(getClass().getSimpleName()+" --- ok click");
            //关闭dialog
            hide();
            if(dialogClickListener != null){
                dialogClickListener.onOKClick();
            }
        }
        else if(component==cancelBtn){
            LogUtils.info(getClass().getSimpleName()+" --- cancel click");
            //关闭dialog
            hide();
            if(dialogClickListener != null){
                dialogClickListener.onCancelClick();
            }
        }
    }

    /**
     *按钮接口
     */
    public interface OnDialogClickListener{
        void onOKClick();
        void onCancelClick();
    }

    /**
     * 初始化组件以及设置对应按钮监听事件
     */
    private void initComponents(){
        //设置布局xml
        Component component= LayoutScatter.getInstance(context)
                .parse(ResourceTable.Layout_dialog_confirm,null,true);

        detailText=component.findComponentById(ResourceTable.Id_text_detail);
        okBtn=component.findComponentById(ResourceTable.Id_btn_ok);
        cancelBtn=component.findComponentById(ResourceTable.Id_btn_cancel);

        //设置监听
        okBtn.setClickedListener(this::onClick);
        cancelBtn.setClickedListener(this::onClick);


        super.setContentCustomComponent(component);
    }

    public void setOnDialogClickListener(OnDialogClickListener clickListener){
        dialogClickListener = clickListener;
    }


    /**
     * 设置提示内容
     *
     * @param text
     */
    public void setDetailText(String text){
        if (detailText!=null){
            detailText.setText(text);
        }
    }
}
