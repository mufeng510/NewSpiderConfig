package xin.developer97.halfsaltedfish.spiderconfig;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.ddz.floatingactionbutton.FloatingActionButton;
import com.ddz.floatingactionbutton.FloatingActionMenu;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  android.view.GestureDetector.OnGestureListener{

    private Tools tools = new Tools();
    TextView updateTime,text;
    SharedPreferences sp;
    private Handler mHandler;
    GestureDetector gd;
    Intent intent_service;
    static String versionName_new = "查询失败";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        tools.setContext(getApplicationContext());
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        intent_service = new Intent(this, MyService.class);
        tools.hideInRecents();
        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE
        };
        //创建手势检测器
        gd = new GestureDetector(this,this);
        // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
        List<String> mPermissionList = new ArrayList<>();

        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (!mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            String[] permission = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permission, 1);
        }
        //自定义壁纸
        RelativeLayout linearLayout = (RelativeLayout)findViewById(R.id.layoutmain);
        if (sp.getString("backpath",null)!= null){
            String uri = sp.getString("backpath",null);
            Drawable drawable= (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        }else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }

        //检查版本更新
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        //获取本地版本号
                        int versionCode = 59;
                        try {
                            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        //获取最新版本号
                        HttpURLConnection con=null;
                        String path="http://" + getApplicationContext().getString(R.string.host) +"/android_connect/get_version.php";
                        try {
                            URL url = new URL(path);
                            con= (HttpURLConnection) url.openConnection();
                            con.setDoInput(true);
                            con.setDoOutput(true);
                            con.setUseCaches(false);
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Connection", "keep-alive");
                            con.setRequestProperty("contentType", "application/json");

                            con.connect();

                            OutputStream out = con.getOutputStream();
                            // 写入请求的字符串
                            out.write((getPackageName()).getBytes("utf-8"));
                            out.flush();
                            out.close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String lines;
                            StringBuffer sbf = new StringBuffer();
                            while ((lines = reader.readLine()) != null) {
                                lines = new String(lines.getBytes(), "utf-8");
                                sbf.append(lines);
                            }
                            String versionJson = sbf.toString();
                            Log.i("versionJson",versionJson);
                            if (versionJson != "{\"success\":0,\"message\":\"No products found\"}") {
                                try {
                                    JSONObject versionText = new JSONObject(versionJson);
                                    int versionCode_new = versionText.getInt("versionCode");
                                    versionName_new = versionText.getString("versionName");
                                    final String updataText = versionText.getString("updataText");
                                    System.out.println(updataText);
                                    if(versionCode<versionCode_new){
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog alert = null;
                                                AlertDialog.Builder builder = null;
                                                builder = new AlertDialog.Builder(MainActivity.this);
                                                alert = builder.setTitle("有新版本")
                                                        .setMessage(updataText)
                                                        .setNegativeButton("取消",null)
                                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent();
                                                                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D500dbsykLTTnkUpBTJg97HcVZH5yzpfB" ));
                                                                try {
                                                                    startActivity(intent);
                                                                } catch (Exception e) {
                                                                    tools.mes("未安装手Q或安装的版本不支持");
                                                                }
                                                            }
                                                        }).create();             //创建AlertDialog对象
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();

        //启动服务
        if(sp.getBoolean("openService",true)){
            MyService.notFirstRun = false;
            startService(intent_service);
        }
        //控件
        final FloatingActionMenu fam1 = (FloatingActionMenu)findViewById(R.id.fam1);
        FloatingActionButton useTutorial = (FloatingActionButton)findViewById(R.id.useTutorial);
        FloatingActionButton get_packet = (FloatingActionButton)findViewById(R.id.get_packet);
        FloatingActionButton red =(FloatingActionButton)findViewById(R.id.red);
        ImageButton getIp = (ImageButton)findViewById(R.id.getIp);
        ImageButton getweb = (ImageButton)findViewById(R.id.getweb);
        ImageButton toTiny = (ImageButton)findViewById(R.id.toTiny);
        final FloatingActionMenu fam2 = (FloatingActionMenu)findViewById(R.id.fam2);
        FloatingActionButton reward = (FloatingActionButton)findViewById(R.id.reward);
        FloatingActionButton group = (FloatingActionButton)findViewById(R.id.group);
        FloatingActionButton set = (FloatingActionButton)findViewById(R.id.set);


        updateTime = (TextView)findViewById(R.id.updateTime);
        text = (TextView)findViewById(R.id.text);


        if (!sp.getBoolean("doset",false)){
            AlertDialog alert = null;
            AlertDialog.Builder builder = null;
            builder = new AlertDialog.Builder(MainActivity.this);
            alert = builder.setTitle("声明")
                    .setMessage("请认真看一遍视频教程\n\n本软件完全免费，仅供娱乐使用，切勿用于非法用途！造成的一切后果与开发者无关！")
                    .setPositiveButton("看教程", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tiny";
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf";
                            File dir = new File(directory);
                            File file = new File(path);
                            if (!dir.exists() | !file.exists()) {
                                try {
                                    dir.mkdir();
                                    file.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            new Thread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            NewConfig newConfig = tools.getConfig();
                                            if (newConfig != null) {
                                                final String config = newConfig.getConfig();
                                                //写入
                                                try {
                                                    tools.savaFileToSD(sp.getString("path",Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf"),config.toString());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                            ).start();
                            Uri uri = Uri.parse("http://dd.ma/xoLS9iwS");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    }).create();             //创建AlertDialog对象
            alert.show();
        }

        //使用教程
        useTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://dd.ma/xoLS9iwS");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        //        手动抓包
        get_packet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                Intent intent = new Intent(MainActivity.this, GetPacket.class);
                startActivity(intent);
            }
        });
        //检测ip
        getIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.checkip();
            }
        });
        //红包码
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam1.collapse();
                tools.copy("528207543");
                tools.mes("已复制红包吗，请在搜索框中粘贴搜索");
                tools.openApp("com.eg.android.AlipayGphone");
            }
        });
        //获取服务器配置
        getweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getConfig();
            }
        });
        //跳转小火箭
        toTiny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.openApp(sp.getString("packgeName","com.cqyapp.tinyproxy"));
            }
        });
        //设置
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent(MainActivity.this, set.class);
                startActivity(intent);
            }
        });
        //        一键加群
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent();
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D500dbsykLTTnkUpBTJg97HcVZH5yzpfB" ));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "未安装手Q或安装的版本不支持", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //捐赠
        reward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam2.collapse();
                Intent intent = new Intent(MainActivity.this, Reward.class);
                startActivity(intent);
            }
        });
    }

    private void getConfig(){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        NewConfig newConfig = tools.getConfig();
                        if (newConfig != null) {
                            String time = newConfig.getTime();
                            final String config = newConfig.getConfig();
                            int usetime = tools.getDatePoor(time);
                            if ((120 - usetime) > 0) {
                                tools.restartTimedTask();
                                updataUI("生成于" + usetime + "分钟前 " + "大概剩余" + (120 - usetime) + "分钟",config);
                                tools.mes("获取成功，大概剩余" + (120 - usetime) + "分钟");
                                //写入
                                try {
                                    tools.savaFileToSD(sp.getString("path",Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf"),config.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    tools.mes("写入失败");
                                }
                                switch (sp.getString("packgeName","com.cqyapp.tinyproxy")){
                                    case "com.cqyapp.tinyproxy":
                                        Log.i("Main","case");
                                        tools.autopoint();
                                        break;
                                    default:
                                        Log.i("Main","default");
                                        tools.openApp(sp.getString("packgeName","com.cqyapp.tinyproxy"));
                                        break;
                                }
                            } else tools.mes("服务器最新配置已失效，请手动抓包");
                        } else
                            tools.mes("获取失败");
                    }
                }
        ).start();
    }
    //更新ui
    private void updataUI(final String time, final String config){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTime.setText(time);
                text.setText(config);
            }
        });
    }
    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float minMove = 120;         //最小滑动距离
        float minVelocity = 0;      //最小滑动速度
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if(beginX-endX>minMove&&Math.abs(velocityX)>minVelocity){   //左滑
            Intent intent = new Intent(MainActivity.this, GetPacket.class);
            startActivity(intent);
//            Toast.makeText(this,velocityX+"左滑",Toast.LENGTH_SHORT).show();
        }
//        else if(endX-beginX>minMove&&Math.abs(velocityX)>minVelocity){   //右滑
//            Toast.makeText(this,velocityX+"右滑",Toast.LENGTH_SHORT).show();
//        }else if(beginY-endY>minMove&&Math.abs(velocityY)>minVelocity){   //上滑
//            Toast.makeText(this,velocityX+"上滑",Toast.LENGTH_SHORT).show();
//        }else if(endY-beginY>minMove&&Math.abs(velocityY)>minVelocity){   //下滑
//            Toast.makeText(this,velocityX+"下滑",Toast.LENGTH_SHORT).show();
//        }

        return false;
    }
    @Override
    public void onBackPressed() {


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("退出提示");
        dialog.setMessage("您确定退出应用吗?");
        dialog.setNegativeButton("取消",null);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopService(intent_service);
                finish();
//                System.exit(0);
            }
        });
        dialog.show();

    }
}
