package com.yk.lxr.robotapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.yk.lxr.bean.ChatMessage;
import com.yk.lxr.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mMsgs;
    private EditText mInputMsg;
    private Button mSendMsg;

    private MessageAdapter mAdapter;
    private List<ChatMessage> mDatas;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int key = msg.what;
            switch (key) {
                case 1:
                    ChatMessage answer = (ChatMessage) msg.obj;
                    mDatas.add(answer);
                    mAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "下载不了!", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
        InitDatas();

        InitListener();
    }

    private void InitView(){
        mMsgs = (ListView) findViewById(R.id.id_listview_msgs);
        mInputMsg = (EditText) findViewById(R.id.id_input_msg);
        mSendMsg = (Button) findViewById(R.id.id_send_msg);

    }

    private void InitDatas(){
        mDatas = new ArrayList<ChatMessage>();
//        mDatas.add(new ChatMessage("你好!小机灵为您服务", ChatMessage.Type.INCOMING, new Date()));
//        mDatas.add(new ChatMessage("你好!萌萌豆有事请教", ChatMessage.Type.OUTCOMING, new Date()));
        mAdapter = new MessageAdapter(this, mDatas);
        mMsgs.setAdapter(mAdapter);
    }

    private void InitListener(){
        mSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(MainActivity.this,"发送",Toast.LENGTH_SHORT).show();
                final String toMsg = mInputMsg.getText().toString();
                if (TextUtils.isEmpty(toMsg))
                {
                    Toast.makeText(MainActivity.this, "发送消息不能为空！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ChatMessage toMessage = new ChatMessage();
                toMessage.setDate(new Date());
                toMessage.setMsg(toMsg);
                toMessage.setType(ChatMessage.Type.OUTCOMING);


                mDatas.add(toMessage);
                mAdapter.notifyDataSetChanged();
//                mMsgs.setSelection(mAdapter.getCount()-1);

                mInputMsg.setText("");


                Thread t = new Thread(){
                    @Override
                    public void run() {
                        ChatMessage resultMsg = new HttpUtils().sendMessage(toMsg);
                        Message msg = new Message();
                        msg.obj = resultMsg;
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                };
                t.start();
            }
        });
    }
}
