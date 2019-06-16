package xin.developer97.halfsaltedfish.spiderconfig;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;

public class Tools {

    private static Context context;
    private SharedPreferences sp;

    public void setContext(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
    }

    public Tools() {
    }

    public Tools(Context Context) {
        super();
        this.context = Context;
    }

    //往SD卡写入文件的方法
    public void savaFileToSD(String filename, String filecontent) throws Exception {
        //如果手机已插入sd卡,且app具有读写sd卡的权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
            //这里就不要用openFileOutput了,那个是往手机内存中写数据的
            FileOutputStream output = new FileOutputStream(filename);
            output.write(filecontent.getBytes());
            //将String字符串以字节流的形式写入到输出流中
            output.close();
            //关闭输出流
        } else Toast.makeText(context, "文件不存在或者不可读写", Toast.LENGTH_SHORT).show();
    }
    //读取SD卡中文件的方法
    //定义读取文件的方法:
    public String readFromSD(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //打开文件输入流
            FileInputStream input = new FileInputStream(filename);
            byte[] temp = new byte[1024];

            int len = 0;
            //读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            //关闭输入流
            input.close();
        }
        return sb.toString();
    }

    //获取服务器配置
    public NewConfig getConfig() {
        if (isNetworkConnected()) {
            try {
                NewConfig newConfig = receive();
                return newConfig;
            } catch (Exception e) {
                e.printStackTrace();
                mes("获取失败,请检查网络");
                return null;
            }
        } else {
            mes("请检查网络连接");
            return null;
        }

    }
    //获取服务器配置
    public NewConfig receive() {
        String response = executeHttpGet("http://" + context.getString(R.string.host) + "/android_connect/get_config.php");
        if (response != "{\"success\":0,\"message\":\"No products found\"}") {
            try {
                JSONObject res = new JSONObject(response);
                JSONArray config = new JSONArray(res.getString("configs"));
                JSONObject con = config.getJSONObject(0);
                NewConfig newConfig = new NewConfig(context,con.getString("Time"),con.getString("Guid"),con.getString("Token"));
                return newConfig;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    //获取服务器返回的数据
    private String executeHttpGet(String path) {
        HttpURLConnection con = null;
        InputStream in = null;
        try {
            con = (HttpURLConnection) new URL(path).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                in = con.getInputStream();
                return parseInfo(in);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    private String parseInfo(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
    //计算时间差
    public int getDatePoor(String configTime) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        Date date = null;
        //转换成Date型
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(configTime);
            // long ns = 1000;
            // 获得两个时间的毫秒时间差异
            Date dNow = new Date( );
            long diff = dNow.getTime() - date.getTime();
//        // 计算差多少天
//        long day = diff / nd;
//        // 计算差多少小时
//        long hour = diff % nd / nh;
//        //计算差多少分钟
//        long min = diff % nd % nh / nm;
            // 计算差多少秒//输出结果
            // long sec = diff % nd % nh % nm / ns;
            long min = diff / nm;
            return (int) min;
        } catch (ParseException e) {
            return 0;
        }

    }
    //判断网络状态
    public boolean isNetworkConnected() {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    //判断是否是wifi
    public boolean iswifi(){
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        Boolean result = info.getType() == ConnectivityManager.TYPE_WIFI;
        MyService.beWifi = result;
        return result;
//        if (info == null) {
//            return false;
//        }
    }
    //发送消息
    public void mes(final String text){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    //发送长显示消息
    public void longMes(final String text){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }
    //复制
    public void copy(CharSequence text){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }
    //收起通知栏
    public void collapseStatusBar() {
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    //唤醒APP
    public void openApp(String name){
        /**获取ActivityManager*/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        /**获得当前运行的task(任务)*/
        Boolean hasFind = false;
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            /**找到本应用的 task，并将它切换到前台*/
            if (taskInfo.topActivity.getPackageName().equals(name)) {
                hasFind = true;
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
        if(!hasFind) {
            // 获取包管理器
            PackageManager manager = context.getPackageManager();
            // 指定入口,启动类型,包名
            Intent intent = new Intent(Intent.ACTION_MAIN);//入口Main
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);// 启动LAUNCHER,跟MainActivity里面的配置类似
            intent.setPackage(name);//包名
            //查询要启动的Activity
            List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
            if (apps.size() > 0) {//如果包名存在
                ResolveInfo ri = apps.get(0);
                // //获取包名
                String packageName = ri.activityInfo.packageName;
                //获取app启动类型
                String className = ri.activityInfo.name;
                //组装包名和类名
                ComponentName cn = new ComponentName(packageName, className);
                //设置给Intent
                intent.setComponent(cn);
                //根据包名类型打开Activity
                context.startActivity(intent);
            } else {
                mes("未安装应用");
            }
        }
    }

    //自动点击
    public void autopoint(){
        if (isVpnUsed()) {
            Log.i("vpn","存在");
            autopointTwo();
        } else {
            Log.i("vpn","不存在");
            autopointOne();
        }
    }

    private boolean isVpnUsed() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if(niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    if(!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    Log.i("isVpnUsed() Name: ",intf.getName());
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())){
                        return true; // The VPN is up
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
    //点击一次
    private void autopointOne(){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(sp.getBoolean("autoClick",true)){
                            try{
                                if(isServiceON(ClickService.class.getName())) {
                                    if (sp.getBoolean("autoBack",false)){
                                        Log.i("顶应用",ClickService.foregroundPackageName);
                                    }
                                    Log.i("tool","运行");
                                    openApp("com.cqyapp.tinyproxy");
                                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent("auto.click");
                                            intent.putExtra("flag",1);
                                            intent.putExtra("id","menu_item_switch");
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.sendBroadcast(intent);
                                        }
                                    },2000);
                                    Thread.sleep(3500);
                                    if (sp.getBoolean("autoCheckIp",false)) checkip();
                                } else {
                                    Log.i("tool","未运行");
                                    openAccessibilityServiceSettings();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                mes("自动点击失败");
                            }
                        }else {
                            openApp("com.cqyapp.tinyproxy");
                        }
                    }
                }
        ).start();
    }
    //点击两次
    private void autopointTwo(){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(sp.getBoolean("autoClick",true)){
                            try{
                                if(isServiceON(ClickService.class.getName())) {
                                    if (sp.getBoolean("autoBack",false)){

                                    }
                                    openApp("com.cqyapp.tinyproxy");
                                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent("auto.click");
                                            intent.putExtra("flag",1);
                                            intent.putExtra("id","menu_item_switch");
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.sendBroadcast(intent);
                                        }
                                    },1500);
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent("auto.click");
                                            intent.putExtra("flag",1);
                                            intent.putExtra("id","menu_item_switch");
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.sendBroadcast(intent);
                                        }
                                    },1000);
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (sp.getBoolean("autoCheckIp",false)) checkip();
                                } else {
                                    openAccessibilityServiceSettings();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                mes("自动点击失败");
                            }
                        }else {
                            openApp("com.cqyapp.tinyproxy");
                        }
                    }
                }
        ).start();
    }
    /** 打开辅助服务的设置*/
    public void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            longMes("找[王卡配置助手],然后开启服务\n无需本功能请关闭自动点击");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //检测ip
    public void checkip() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String url_ip = sp.getString("ipPort","http://myip.ipip.net");
                        String ip = "ip查询失败";
                        switch (sp.getString("ipWay","helper")){
                            case "helper":
                                try {
                                    String result = executeHttpGet(url_ip);
                                    Log.i("url_ip",url_ip);
                                    Log.i("result",result);
                                    if(result.length()>2){
                                        switch (url_ip){
                                            case "http://myip.ipip.net":
                                                ip = result;
                                                break;
                                            case "http://cip.cc":
                                                String pattern = "<pre>([\\S\\s]+?)URL";
                                                Pattern r = Pattern.compile(pattern);
                                                Matcher m = r.matcher(result);
                                                if(m.find()){
                                                    ip = m.group(1);
                                                }
                                                break;
                                            case "https://ip.cn/index.php":
                                                String pattern2 = "所在地理位置：<code>(.+?)</code>";
                                                Pattern r2 = Pattern.compile(pattern2);
                                                Matcher m2 = r2.matcher(result);
                                                if(m2.find()){
                                                    ip = m2.group(1);
                                                }
                                                break;
                                        }
                                    }
                                    Log.i("ip",ip);
                                    mes(ip);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    mes(ip);
                                }
                                break;
                            case "browser":
                                Uri uri = Uri.parse(url_ip);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                context.startActivity(intent);
                                break;
                            case "not":
                                break;
                        }
                    }
                }
        ).start();
    }
    //获取uri真实路径
    public static String getRealPathFromUri( Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    //启动定时任务
    public void openTimedTask(){
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent("TimedTask"), 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        int anHour = sp.getInt("autotime", 60) * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
    }
    //关闭定时任务
    public void closeTimedTask(){
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent("TimedTask"), 0);
        AlarmManager alarm=(AlarmManager)context.getSystemService(ALARM_SERVICE);
        try {
            alarm.cancel(pi);
        }catch (Exception e){
            e.printStackTrace();
            mes("停止定时任务失败");
        }
        MyService.hasGet = false;
    }
    //重置定时任务
    public void restartTimedTask(){
        closeTimedTask();
        openTimedTask();
    }
    //多任务列表隐藏
    public void hideInRecents() {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (am != null && android.os.Build.VERSION.SDK_INT >= 21) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                ((ActivityManager.AppTask) tasks.get(0)).setExcludeFromRecents(sp.getBoolean("hide",false));
            }
        }
    }
    //查看应用使用情况权限检测
    public boolean hasEnableLookPermission(){
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){   // 如果大于等于5.0 再做判断
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager=(UsageStatsManager)context.getSystemService(Service.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    //检查无障碍
    public static boolean isServiceON(String className){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>
                runningServices = activityManager.getRunningServices(100);
        if (runningServices.size() < 0 ){
            return false;
        }
        for (int i = 0;i<runningServices.size();i++){
            ComponentName service = runningServices.get(i).service;
            if (service.getClassName().contains(className)){
                return true;
            }
        }
        return false;
    }
}
