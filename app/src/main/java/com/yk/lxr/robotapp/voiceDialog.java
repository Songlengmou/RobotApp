package com.yk.lxr.robotapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.yk.lxr.bean.ChatMessage;
import com.yk.lxr.utils.HttpUtils;
import com.yk.lxr.utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class voiceDialog extends AppCompatActivity {

    private static String TAG = voiceDialog.class.getSimpleName();
    private Toast mToast;

    private ImageView Imgview;
    private ImageButton yy_btn;
    /**************************************************************************/
    // 语音听写对象
    private SpeechRecognizer mIat;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private String mResultText = "。";

    /**************************************************************************/
    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    // 引擎类型s
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String mResourceText = "。";


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int key = msg.what;
            switch (key) {
                case 1:
                    ChatMessage answer = (ChatMessage) msg.obj;
                    Speaking(answer.getMsg().toString());
                    break;
                case 2:
//                    Toast.makeText(MainActivity.this, "下载不了!", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_dialog);

        InitIat();
        InitTts();

        InitLayout();
        InitListener();

        Speaking("欢迎进入小宋的世界,有什么问题就问我吧");
    }

    private void InitLayout() {
        Imgview = (ImageView) findViewById(R.id.imagev);
        yy_btn = (ImageButton) findViewById(R.id.image_yy_btn);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    private void InitListener() {

        Imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTts.stopSpeaking();
                mResourceText = "。";
                mResultText = "。";
            }
        });

        Imgview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(voiceDialog.this, MainActivity.class));
                return false;
            }
        });

        yy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int ret = 0;
//                ret = mIat.startListening(mRecognizerListener);
//                if (ret != ErrorCode.SUCCESS) {
//                    showTip("听写失败,错误码：" + ret);
//                } else {
//                    showTip("请讲话！");
//                }
            }
        });

        yy_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int ret = 0;
                        ret = mIat.startListening(mRecognizerListener);
                        if (ret != ErrorCode.SUCCESS) {
                            showTip("听写失败,错误码：" + ret);
                        } else {
                            showTip("请讲话！");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//                        Speaking(mResultText);
//                        mResultText = "";
                        mIat.stopListening();
                        mResourceText = mResultText;
                        mResultText = "。";

                        Thread t = new Thread() {
                            @Override
                            public void run() {

                                ChatMessage resultMsg = new HttpUtils().sendMessage(mResourceText.toString());
                                Message msg = new Message();
                                msg.obj = resultMsg;
                                msg.what = 1;
                                handler.sendMessage(msg);
                            }
                        };
                        t.start();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    private void InitIat() {
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(voiceDialog.this, mInitListener);

        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    //听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + bytes.length);
        }

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d("Result:", recognizerResult.getResultString());
            printResult(recognizerResult);
        }

        @Override
        public void onError(SpeechError speechError) {
            showTip(speechError.getPlainDescription(true));
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText = resultBuffer.toString();
        showTip(mResultText);

    }


    private void Speaking(String resText) {
        //开始合成
        mTts.startSpeaking(resText.toString(), mTtsListener);
        mResourceText = "。";
        mResultText = "。";
    }

    private void InitTts() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(voiceDialog.this, mTtsInitListener);

        //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "40");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    //合成监听器
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError == null) {
                showTip("播放完成");
            } else if (speechError != null) {
                showTip(speechError.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}
