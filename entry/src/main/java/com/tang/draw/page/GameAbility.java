package com.tang.draw.page;

import com.tang.draw.page.slice.GameAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

/********
 *文件名: GameAbility
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/10 15:14
 *描述: GameAbility
 ********/
public class GameAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(GameAbilitySlice.class.getName());
    }
}