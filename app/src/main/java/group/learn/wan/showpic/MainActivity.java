package group.learn.wan.showpic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import group.learn.wan.showpic.tool.PermissionListener;
import group.learn.wan.showpic.tool.PictureLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button showBtn;
    private ImageView showImg;
    private ArrayList<String> urls;
    private int curPos =0;
    private PictureLoader loader;
    private  Activity activity;
    private PermissionListener mListener;

    private String[] PERMISSION_STORAGE ={
            "android.permission.INTERNET"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        loader = new PictureLoader();
        initData();
        initUI();
    }

    private void initData() {
        urls = new ArrayList<>();
        urls.add("http://ww4.sinaimg.cn/large/610dc034jw1f6ipaai7wgj20dw0kugp4.jpg");
        urls.add("http://ww3.sinaimg.cn/large/610dc034jw1f6gcxc1t7vj20hs0hsgo1.jpg");
        urls.add("http://ww4.sinaimg.cn/large/610dc034jw1f6f5ktcyk0j20u011hacg.jpg");
        urls.add("http://ww1.sinaimg.cn/large/610dc034jw1f6e1f1qmg3j20u00u0djp.jpg");
        urls.add("http://ww3.sinaimg.cn/large/610dc034jw1f6aipo68yvj20qo0qoaee.jpg");
        urls.add("http://ww3.sinaimg.cn/large/610dc034jw1f69c9e22xjj20u011hjuu.jpg");
        urls.add("http://ww3.sinaimg.cn/large/610dc034jw1f689lmaf7qj20u00u00v7.jpg");
        urls.add("http://ww3.sinaimg.cn/large/c85e4a5cjw1f671i8gt1rj20vy0vydsz.jpg");
        urls.add("http://ww2.sinaimg.cn/large/610dc034jw1f65f0oqodoj20qo0hntc9.jpg");
        urls.add("http://ww2.sinaimg.cn/large/c85e4a5cgw1f62hzfvzwwj20hs0qogpo.jpg");
    }

    private void initUI(){
        showBtn = findViewById(R.id.btn_next);
        showImg = findViewById(R.id.Image_view);
        if(Build.VERSION.SDK_INT >= 23){
            requestRuntimePremissions(PERMISSION_STORAGE, new PermissionListener() {
                @Override
                public void granted() {
                    Toast.makeText(activity,"已获取全部权限",Toast.LENGTH_SHORT).show();
                    showBtn.setVisibility(View.VISIBLE);
                }

                @Override
                public void denied(List<String> deniedList) {
                    StringBuffer stringBuffer=new StringBuffer();
                    for (String per : deniedList){
                        stringBuffer.append(per+"\n");
                    }
                    Toast.makeText(activity,"以下权限未授权:\n"+stringBuffer,Toast.LENGTH_LONG).show();
                    showBtn.setVisibility(View.GONE);
                }
            });
        }
        showBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                curPos++;
                if(curPos>=(urls.size())){
                    curPos=0;
                }
                loader.load(showImg,urls.get(curPos));
                break;
        }
    }
    /**
     * 动态申请权限
     */
    public void requestRuntimePremissions(String[] permissions, PermissionListener listener){
        mListener = listener;
        List<String> permissionList = new ArrayList<>();
        //遍历每一个声请的权限,把没有通过的权限放在集合中
        for(String per : permissions){
            if(ContextCompat.checkSelfPermission(activity,per)!=
                    PackageManager.PERMISSION_GRANTED){
                permissionList.add(per);
            }else {
                mListener.granted();
            }
        }
        //申请权限
        if(!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(activity,permissionList.toArray(new String[permissionList.size()]),1);
        }

    }

    /**
     * 申请后的处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            List<String> deniedList = new ArrayList<>();
            for (int i=0;i<grantResults.length;i++){
                int grantResult = grantResults[i];
                if(grantResult == PackageManager.PERMISSION_GRANTED){
                    mListener.granted();
                }else {
                    deniedList.add(permissions[i]);
                }
            }
            if(!deniedList.isEmpty()){
                mListener.denied(deniedList);
            }
        }
    }
}
