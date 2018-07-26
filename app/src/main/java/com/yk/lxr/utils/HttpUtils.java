package com.yk.lxr.utils;

import com.google.gson.Gson;
import com.yk.lxr.bean.ChatMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by A on 2017/2/26.
 */

public class HttpUtils {
    final String getURL = "http://www.tuling123.com/openapi/api";
    final String API_KEY = "342f33bf38904cb2aa5fbd4d62905ab7";

    public ChatMessage sendMessage(String msg)
    {
        ChatMessage chatMessage = new ChatMessage();
        String robotnswer = doGet(msg);

        chatMessage.setMsg(JsonAnswerParser(robotnswer));
        chatMessage.setType(ChatMessage.Type.INCOMING);

        return chatMessage;
    }

    public String doGet(String msg){
        String result = "";
        String url = setParams(msg);
        InputStream is = null;
        ByteArrayOutputStream osbuf = null;
        try {
            // 把robotapi地址封装成URL
            URL roboturl = new URL(url);
            // 获取一个服务器与客户端的连接对象
            HttpURLConnection conn = (HttpURLConnection) roboturl.openConnection();
            // 设置请求方式，大写
            conn.setRequestMethod("GET");
//            conn.setRequestMethod("POST");
            // 设置连接超时
            conn.setConnectTimeout(10000);
            // 设置读取超时
            conn.setReadTimeout(10000);
            // 开始连接
            conn.connect();

            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                int len = -1;
                byte[] buf = new byte[128];
                osbuf = new ByteArrayOutputStream();

                while ((len = is.read(buf)) != -1) {
                    osbuf.write(buf, 0, len);
                }
                osbuf.flush();
                result = new String(osbuf.toByteArray());
            }else{
                result = "连接失败！";
            }

        }catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (osbuf != null) {
                    osbuf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    private String setParams(String msg)
    {
        String url = "";
        try
        {
            url = getURL + "?key=" + API_KEY + "&info=" + URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return url;
    }

    private String JsonAnswerParser(String src){
        List<Map<String,Object>> list=null;
        Gson gson=new Gson();	//用来解析Json数据
        HashMap<String,Object> resultMap=gson.fromJson(src, HashMap.class);
        StringBuffer fromMsg = new StringBuffer();

        double code = (double)resultMap.get("code");
        String text = resultMap.get("text").toString();
        list=(List<Map<String,Object>>)resultMap.get("list");

        int i= 1;

        if(code==100000){
            text = resultMap.get("text").toString();
        }else if(code==200000){
            String Imgurl = resultMap.get("url").toString();

        }else if(code==302000){
            fromMsg.append(text.toString());
            i= 1;
//            if(list.size()<5){
                for(Map<String,Object> temp:list){
                    fromMsg.append("，新闻"+i+"："+temp.get("article"));
                    fromMsg.append("，来源："+temp.get("source"));
                    i++;
                }
//            }else{
//                for(int j = 0; j < 5; j++){
//                    fromMsg.append("，新闻"+i+"："+list.get(j).get("article"));
//                    fromMsg.append("，来源："+list.get(j).get("source"));
//                    i++;
//                }
//            }

            text = fromMsg.toString();
        }else if(code==305000){
            fromMsg.append(text.toString());
            for(Map<String,Object> temp:list){
                fromMsg.append("，车次："+temp.get("trainnum"));
                fromMsg.append("，起始站:"+temp.get("start"));
                fromMsg.append("，到达站:"+temp.get("terminal"));
                fromMsg.append("，出发时间:"+temp.get("starttime"));
                fromMsg.append("，到达时间:"+temp.get("endtime"));
            }
            text = fromMsg.toString();
        }else if(code==306000){
            fromMsg.append(text.toString());
            for(Map<String,Object> temp:list){
                fromMsg.append("，航班:"+temp.get("flight"));
                fromMsg.append("，航线:"+temp.get("route"));
                fromMsg.append("，到达站:"+temp.get("terminal"));
                fromMsg.append("，起飞时间:"+temp.get("starttime"));
                fromMsg.append("，到达时间:"+temp.get("endtime"));
                fromMsg.append("，航班状态:"+temp.get("state"));
            }
            text = fromMsg.toString();
        }else if(code==308000){

            fromMsg.append(text.toString());
//            if(list.size()<3){
                i= 1;
                for(Map<String,Object> temp:list){
                    fromMsg.append("，菜名"+i+":"+temp.get("name"));
                    fromMsg.append("，详情:"+temp.get("info"));
                    i++;
                }
//            }else{
//                for(int j = 0; j < 3; j++){
//                    fromMsg.append("菜名:"+list.get(j).get("name"));
//                    fromMsg.append("详情:"+list.get(j).get("info"));
//                }
//            }

            text = fromMsg.toString();
        }
        return text;
    }

}
