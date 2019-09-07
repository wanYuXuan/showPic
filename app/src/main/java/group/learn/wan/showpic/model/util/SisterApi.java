package group.learn.wan.showpic.model.util;

import android.nfc.Tag;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import group.learn.wan.showpic.model.Sister;

/**
 * 网络请求的处理类
 */

public class SisterApi {
    private static final String TGA = "Network";
    private static final String BASE_URL="http://gank.io/api/data/福利/";

    /**
     * 查询信息
     */
    public ArrayList<Sister> fetchSister(int count,int page){
        String fetchUrl = BASE_URL + count + "/"+page;
        ArrayList<Sister> sisters = new ArrayList<>();
        try{
            URL url = new URL(fetchUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            Log.v(TGA,"Server response:"+code);
            if(code==200){
                InputStream in  =connection.getInputStream();
                byte[] data = readFromStream(in);
                String result = new String(data,"UTF-8");
                sisters = parseSister(result);
            }else {
                Log.e(TGA,"请求失败:"+code);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return sisters;
    }

    /**
     * 解析返回Json数据
     */
    public ArrayList<Sister> parseSister(String content) throws Exception{
        ArrayList<Sister> sisters = new ArrayList<>();
        JSONObject object = new JSONObject(content);
        JSONArray array = object.getJSONArray("results");
        for (int i=0;i<array.length();i++){
            JSONObject results = (JSONObject) array.get(i);
            Sister sister = new Sister();
            sister.set_id(results.getString("_id"));
            sister.setCreateAt(results.getString("createdAt"));
            sister.setDesc(results.getString("desc"));
            sister.setPublishedAt(results.getString("publishedAt"));
            sister.setSource(results.getString("source"));
            sister.setType(results.getString("type"));
            sister.setUrl(results.getString("url"));
            sister.setUsed(results.getBoolean("used"));
            sister.setWho(results.getString("who"));
            sisters.add(sister);
        }
        return sisters;
    }



    /**
     * 读取流中数据的方式
     */
    public byte[] readFromStream(InputStream inputStream) throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while((len = inputStream.read(buffer))!=-1){
            outputStream.write(buffer,0,len);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }


}
