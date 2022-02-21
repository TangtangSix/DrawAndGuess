package com.tang.draw.dialog;

import com.tang.draw.ResourceTable;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.components.element.PixelMapElement;
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
 *文件名: AlertDialog
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/16 16:16
 *描述: AlertDialog
 ********/
public class AlertDialog extends CommonDialog {
    private Context mContext;
    private Text titleText, contentText;
    private Button okBtn;
    private DialogClickListener dialogClickListener;

    public AlertDialog(Context context) {
        super(context);
        this.mContext = context;
        initComponents();
        initClickedListener();

    }


    private void initClickedListener() {
        okBtn.setClickedListener(component -> {
                hide();
                if (dialogClickListener != null) {
                    dialogClickListener.onOKClick();
                }
            }
        );

    }

    private void initComponents() {
        Component component = LayoutScatter.getInstance(mContext)
                .parse(ResourceTable.Layout_dialog_alert, null, true);

        contentText = component.findComponentById(ResourceTable.Id_text_content);
        titleText = component.findComponentById(ResourceTable.Id_text_title);
        okBtn = component.findComponentById(ResourceTable.Id_btn_ok);

        //设置对话框的布局
        setContentCustomComponent(component);
        //居中
        setAlignment(LayoutAlignment.CENTER);

        setCornerRadius(50);

        //设置高度为自适应,宽度为屏幕的0.8
        setSize(mContext.getResourceManager().getDeviceCapability().width
                * mContext.getResourceManager().getDeviceCapability().screenDensity
                / 160*4/5, MATCH_CONTENT);

    }

    public void setDialogClickListener(DialogClickListener dialogClickListener){
        this.dialogClickListener=dialogClickListener;
    }


    public void setmContentText(String text,boolean isWinner){
        if (ohos.system.version.SystemVersion.getApiVersion()==7){
            RichText richText=new RichText(text);
            UnderlineRichStyle underlineRichStyle = new UnderlineRichStyle(Color.BLUE);
            richText.setRichStyle(underlineRichStyle,0,text.length(), RangedRichStyle.Flag.EXCLUDE);
            ImageRichStyle imageRichStyle=getImageRichStyle(isWinner);
            if (imageRichStyle!=null){
                richText.setRichStyle(imageRichStyle,text.length()-1,text.length(),RangedRichStyle.Flag.EXCLUDE);
            }
            contentText.setRichText(richText);
        }

        else {
            contentText.setText(text);
        }
    }
    public void setmContentText(String text){
        contentText.setText(text);
    }

    private ImageRichStyle getImageRichStyle(boolean isWinner){
        ImageRichStyle imageRichStyle = null;
        ImageSource imageSource;
        try {
            if (isWinner){
                imageSource = ImageSource.create(mContext.getResourceManager().getResource(ResourceTable.Media_icon_victory),null);
            }
            else {
                imageSource = ImageSource.create(mContext.getResourceManager().getResource(ResourceTable.Media_icon_defeat),null);
            }

            ImageSource.DecodingOptions decodingOpts = new ImageSource.DecodingOptions();

            decodingOpts.desiredSize = new Size(contentText.getHeight()-10, contentText.getHeight()-10);

            decodingOpts.desiredRegion = new Rect(0, 0, 0, 0);

            decodingOpts.desiredPixelFormat = PixelFormat.ARGB_8888;

            PixelMapElement pixelMapElement =new PixelMapElement(imageSource.createPixelmap(decodingOpts));

            imageRichStyle=new ImageRichStyle(pixelMapElement);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
        return imageRichStyle;
    }

    public interface DialogClickListener {
        void onOKClick();
    }


}
