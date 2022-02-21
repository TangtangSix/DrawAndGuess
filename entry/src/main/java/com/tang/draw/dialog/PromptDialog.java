package com.tang.draw.dialog;

import com.tang.draw.ResourceTable;
import com.tang.draw.utils.LogUtils;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.text.RichText;
import ohos.agp.text.richstyles.ImageRichStyle;
import ohos.agp.text.richstyles.RangedRichStyle;
import ohos.agp.text.richstyles.UnderlineRichStyle;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.media.image.ImageSource;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

import java.io.IOException;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;

/********
 *文件名: PromptDialog
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/9 22:36
 *描述: PromptDialog
 ********/
public class PromptDialog extends CommonDialog {
    private Context mContext;
    private Text titleText,errorText;
    private TextField input;
    private Button okBtn,cancelBtn;
    private DialogClickListener dialogClickListener;

    public PromptDialog(Context context) {
        super(context);
        this.mContext =context;

        initComponents();
        initClickedListener();
    }

    private void initClickedListener() {
        okBtn.setClickedListener(this::onClick);
        cancelBtn.setClickedListener(this::onClick);

    }
    public void onClick(Component component) {
        if (component==okBtn){
            LogUtils.info(getClass().getSimpleName()+" --- ok click");
            //关闭dialog

            if(input.getText().length()>8){
                setError(true,"最大长度8");
            }
            else{
                if(dialogClickListener != null){
                    dialogClickListener.onOKClick(input.getText());
                    input.setText("");
                }
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

    private void initComponents(){
        Component component= LayoutScatter.getInstance(mContext)
                .parse(ResourceTable.Layout_dialog_prompt,null,true);
        okBtn=component.findComponentById(ResourceTable.Id_btn_ok);
        cancelBtn=component.findComponentById(ResourceTable.Id_btn_cancel);
        input=component.findComponentById(ResourceTable.Id_field_input);
        titleText=component.findComponentById(ResourceTable.Id_text_title);
        errorText=component.findComponentById(ResourceTable.Id_error_tip_text);
        okBtn.setEnabled(false);
        input.addTextObserver(new Text.TextObserver() {
            @Override
            public void onTextUpdated(String s, int i, int i1, int i2) {
                if(s==null || s.isEmpty()){
                    okBtn.setEnabled(false);
                }
                else {
                    okBtn.setEnabled(true);
                }
            }
        });

        input.setFocusChangedListener(new Component.FocusChangedListener() {
            @Override
            public void onFocusChange(Component component, boolean hasFocus) {
                if(hasFocus){
                  setError(false,"");

                }
            }
        });

        super.setContentCustomComponent(component);

        //居中
        setAlignment(LayoutAlignment.CENTER);

        setCornerRadius(50);

        //设置高度为自适应,宽度为屏幕的0.8
        setSize(mContext.getResourceManager().getDeviceCapability().width
                * mContext.getResourceManager().getDeviceCapability().screenDensity
                / 160*4/5, MATCH_CONTENT);
    }



    /**
     *按钮接口
     */
    public interface DialogClickListener{
        void onOKClick(String inputData);
        void onCancelClick();
    }

    public void setOnDialogClickListener(DialogClickListener clickListener){
        dialogClickListener = clickListener;
    }

    public void setError(boolean isError,String message){
        if(isError){
            errorText.setVisibility(Component.VISIBLE);
            errorText.setText(message);
            ShapeElement errorElement = new ShapeElement(this.mContext, ResourceTable.Graphic_background_text_field_error);
            input.setBackground(errorElement);
            input.clearFocus();
        }
        else{
            errorText.setVisibility(Component.INVISIBLE);
            ShapeElement errorElement = new ShapeElement(this.mContext, ResourceTable.Graphic_background_field_input);
            input.setBackground(errorElement);
        }
    }
}
