package com.yk.lxr.robotapp;


import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by A on 2017/2/26.
 */

public class RobotApplication extends Application {
    @Override
    public void onCreate() {
        // 初始化语音组件
        SpeechUtility.createUtility(RobotApplication.this, SpeechConstant.APPID +"=58abb97d");
        super.onCreate();
    }
}
