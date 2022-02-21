package com.tang.draw.utils;

import com.tang.draw.ResourceTable;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.Text;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.Context;

/********
 *文件名: ToastUtils
 *创建者: 醉意丶千层梦
 *创建时间:2021/12/16 18:28
 *描述: ToastUtils
 ********/
public class ToastUtils {
    public static void show(Context context,String message) {
        new ToastDialog(context)
                .setContentText(message)
                .setAlignment(LayoutAlignment.BOTTOM)
                .show();
    }
}
