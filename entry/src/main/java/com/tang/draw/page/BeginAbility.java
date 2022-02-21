package com.tang.draw.page;

import com.tang.draw.page.slice.BeginAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.security.SystemPermission;

/********
 *文件名: MainAbility
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/10 14:48
 *描述: MainAbility
 ********/
public class BeginAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(BeginAbilitySlice.class.getName());
        requestPermissionsFromUser(new String[]{
                SystemPermission.DISTRIBUTED_DATASYNC,SystemPermission.WRITE_MEDIA
        },0);
    }
}