package com.tang.draw;

/********
 *文件名: Constants
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/8 17:20
 *描述: Constants
 ********/
public interface Constants {
    String PARAM_KEY_IS_MAIN_DEVICE="is_main_device";//当前应用是否运行在主机端
    String PARAM_KEY_MAIN_DEVICE_ID="main_device_id";//主机端设备ID
    String PARAM_KEY_REMOTE_DEVICE_ID="remote_device_id";//主机端设备ID
    String PARAM_GAME_TIME="game_time";
    String PARAM_GAME_KEY="game_key";

    String EMPTY_STRING="";//空字符串
    String STRING_GAME_MODEL="game_model";//游戏模式

    String BASE_URI="dataability:///com.tang.draw.page.DataAbility";//DataAbility base uri 必须三个///
    String DB_NAME = "draw.db";//数据库名称
    String DB_TAB_NAME = "key";//数据库中的表名
    String DB_COLUMN_KEY = "key";//数据库字段名
    String DATA_PATH = "/draw";//data path
    String [] PRESET_KEY={"苹果","香蕉","西瓜","手机","电脑"};

    int ERR_OK=0;//数据传送成功与否标志

    int SINGLE_GAME_MODEL=1;//单人模式
    int DOUBLE_GAME_MODEL=2;//双人模式

    int SEND_COMMAND=3;//发送指令代码
    int SEND_DRAW_DATA=4;//发送绘图数据


    int BACK_COM=5;//返回指令
    int FINISH_COM=6;//完成指令
    int CLEAR_COM=7;//清空指令
    int REVOKE_COM=8;//撤回指令
    int GUESS_RIGHT=9;//猜对了指令
    int GUESS_ERROR=10;//猜错了指令
    int TIME_OUT=11;//时间到指令



}
