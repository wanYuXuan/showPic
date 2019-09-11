package group.learn.wan.showpic.crash;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import group.learn.wan.showpic.MainActivity;
import group.learn.wan.showpic.logUtil.LogUtils;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    private static CrashHandler instance;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private Context mContext;

    private Map<String,String> infos = new HashMap<>(); //用于存储设备信息与异常信息

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private String appPage;

    private CrashHandler(){super();}

    public static CrashHandler getInstance(){
        if(instance == null){
            instance = new CrashHandler();
        }
        return  instance;
    }

    public void init(Context context,String appPage){
        mContext = context;
        this.appPage=appPage;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /*自定义错误处理，错误信息采集，日志文件保存，如果处理了返回True,否则返回False*/
    private boolean handleException(Throwable throwable){
        if(throwable == null) return false;
        try{
                new Thread(){
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(mContext,"程序出现异常,即将重启",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }.start();
                getDeviceInfo(mContext);
                saveCrashInfoToFile(throwable);
            SystemClock.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if(!handleException(e) && mDefaultHandler!=null){
            mDefaultHandler.uncaughtException(t,e);
        }else {
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent =new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash",true);
            PendingIntent restartIntent = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_ONE_SHOT);
            mgr.set(AlarmManager.RTC,System.currentTimeMillis()+5000,restartIntent); //1秒钟后从新启动应用
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
    /*        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent LaunchIntent = mContext.getPackageManager().getLaunchIntentForPackage(appPage);
                    LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(LaunchIntent);
                }
            }, 1000);// 1秒钟后重启应用  没用*/
     //       RestartAPPTool.restartAPP(mContext.getApplicationContext(),1000);
        }
    }
    /*采集应用版本与设备信息*/
    private void getDeviceInfo(Context context){
        //获取 APP 版本
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(),PackageManager.GET_ACTIVITIES);
            if(info!=null){
                infos.put("VersionName",info.versionName);
                infos.put("VerionsCode",info.versionCode+"");
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG,"an error occured when collect package info");
        }
        //获取系统设备相关信息
        Field[] fields = Build.class.getDeclaredFields();
        for(Field field:fields){
            try {
                field.setAccessible(true);
                infos.put(field.getName(),field.get(null).toString());
            } catch (IllegalAccessException e) {
                LogUtils.e(TAG,"an error occured when collect package info");
            }
        }
    }

    /*把错误信息写入到文件中,返回文件名称*/
    private String saveCrashInfoToFile(Throwable throwable) throws Exception {
        StringBuilder sb = new StringBuilder();
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date= simpleDateFormat.format(new java.util.Date());
            sb.append("\r\n").append(date).append("\n");
            for(Map.Entry<String,String> entry:infos.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append("=").append(value).append("\n");
            }
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            return writeFile(sb.toString());
        }catch (Exception e){
            LogUtils.e(TAG,"an error occured when collect package info");
            sb.append("an error occured when collect package info");
            writeFile(sb+"");
        }
        return null;
    }


    /*获取Crash文件夹的存储路径*/
    private static String getGlobalPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator+"Crash"+File.separator;
    }
    /*将字符串写入日志文件,返回文件名*/
    private String writeFile(String sb) throws Exception {
        String time = formatter.format(new Date());
        String fileName="crash_"+time+".log";
        //判断储存卡是否可用
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            String path = getGlobalPath();
            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path+fileName,true);
            fos.write(sb.getBytes());
            fos.flush();
            fos.close();

        }
        return fileName;
    }

}
