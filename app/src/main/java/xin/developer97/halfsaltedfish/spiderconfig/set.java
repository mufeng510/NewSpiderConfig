package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class set extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private SharedPreferences sp;
    private String packgeName, path, backpath;
    EditText autotime, ip, packge, confPath;
    Switch hide, openService, screenOff, changeOpen,autoClick, autoBack,autoCheckIp;
    Tools tools = new Tools();
    SharedPreferences.Editor editor;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            backpath = tools.getRealPathFromUri(uri);
            tools.mes("ok，重启软件生效");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set);
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        editor = sp.edit();
        //自定义壁纸
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layoutset);
        if (sp.getString("backpath",null)!= null){
            String uri = sp.getString("backpath",null);
            Drawable drawable= (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        }else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }

        hide = (Switch)findViewById(R.id.hide);
        hide.setOnCheckedChangeListener(this);
        openService = (Switch)findViewById(R.id.openService);
        openService.setOnCheckedChangeListener(this);
        screenOff = (Switch) findViewById(R.id.screenOff);
        screenOff.setOnCheckedChangeListener(this);
        changeOpen = (Switch) findViewById(R.id.changeOpen);
        changeOpen.setOnCheckedChangeListener(this);
        autoClick = (Switch) findViewById(R.id.autoClick);
        autoClick.setOnCheckedChangeListener(this);
        autoBack = (Switch) findViewById(R.id.autoBack);
        autoBack.setOnCheckedChangeListener(this);
        autoCheckIp = (Switch) findViewById(R.id.autoCheckIp);
        autoCheckIp.setOnCheckedChangeListener(this);


        RadioGroup tinyGroup = (RadioGroup) findViewById(R.id.tinyGroup);
        RadioGroup ipGroup = (RadioGroup) findViewById(R.id.ipGroup);
        RadioGroup ipWays = (RadioGroup) findViewById(R.id.ipWays);
        RadioGroup ipPorts = (RadioGroup) findViewById(R.id.ipPorts);

        autotime = (EditText) findViewById(R.id.autotime);
        packge = (EditText) findViewById(R.id.packge);
        confPath = (EditText) findViewById(R.id.confPath);
        ip = (EditText) findViewById(R.id.ip);

        Button background = (Button) findViewById(R.id.background);
        Button setting = (Button) findViewById(R.id.setting);
        tools.setContext(getApplicationContext());

        //关于
        final TextView About_software = findViewById(R.id.About_software);
        //获取本地版本号
        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "获取失败";
        }
        final String final_versionName = versionName;
        About_software.setText("当前版本："+ final_versionName+"\n"+"最新版本：" + MainActivity.versionName_new);

        //获取选中tiny
        tinyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tinyversion = (RadioButton) findViewById(checkedId);
                int version = tinyversion.getId();
                CharSequence tinyproxy = "TinyProxy", TyProxy = "TyProxy", TyFlow = "TyFlow", VpnProxy = "VpnProxy";
                if (version == R.id.TinyProxy) {
                    packgeName = "com.cqyapp.tinyproxy";
                    String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tiny";
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf";
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
                }
                if (version == R.id.TyProxy) {
                    packgeName = "com.android.mantis.typroxy2";
                    path = "/data/user/0/com.android.mantis.typroxy2/files/tiny.conf";
                    dialog("/data/user/0/com.android.mantis.typroxy2");
                }
                if (version == R.id.TyFlow) {
                    packgeName = "com.android.TyFlow";
                    path = "/data/user/0/com.android.TyFlow/files/Tiny.conf";
                    dialog("/data/user/0/com.android.TyFlow");
                }
                if (version == R.id.VpnProxy) {
                    packgeName = "org.vpn.proxy";
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf";
                }
                packge.setText(packgeName);
                confPath.setText(path);
            }
        });

        //获取选中ip
        ipGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radbtn = (RadioButton) findViewById(checkedId);
                ip.setText(radbtn.getText());
            }
        });
        //ip查询方式
        ipWays.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.check_ip_by_helper:
                        editor.putString("ipWay","helper");
                        break;
                    case R.id.check_ip_by_browser:
                        editor.putString("ipWay","browser");
                        break;
                    case R.id.not_check_ip:
                        editor.putString("ipWay","not");
                        break;
                    default:
                        break;
                }
                editor.commit();
            }
        });
        //ip查询接口
        ipPorts.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.check_ip_use_ipip:
                        editor.putString("ipPort","http://myip.ipip.net");
                        break;
                    case R.id.check_ip_use_cip:
                        editor.putString("ipPort","http://cip.cc");
                        break;
                    case R.id.check_use_ipcn:
                        editor.putString("ipPort","https://ip.cn/index.php");
                        break;
                    case R.id.check_use_pconline:
                        editor.putString("ipPort","http://whois.pconline.com.cn/ip.jsp");
                        break;
                    default:
                        break;
                }
                editor.commit();
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, 1100);
            }
        });

        //开启设置以保存的设置
        settingStart();

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //存入数据
                editor.putInt("autotime", Integer.parseInt(autotime.getText().toString()));
                editor.putString("backpath", backpath);
                editor.putString("packgeName", packge.getText().toString());
                editor.putString("path", confPath.getText().toString());
                editor.putString("ip", ip.getText().toString());
                editor.putBoolean("doset", true);
                editor.commit();
                Toast.makeText(getBaseContext(), "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    //启动函数
    public void settingStart() {
        hide.setChecked(sp.getBoolean("hide",false));
        openService.setChecked(sp.getBoolean("openService",true));
        screenOff.setChecked(sp.getBoolean("screenOff", false));
        changeOpen.setChecked(sp.getBoolean("changeOpen", false));
        autoClick.setChecked((sp.getBoolean("autoClick",true)));
        autoBack.setChecked(sp.getBoolean("autoBack", false));
        autoCheckIp.setChecked(sp.getBoolean("autoCheckIp", false));

        autotime.setText(sp.getInt("autotime", 60) + "");
        packge.setText(sp.getString("packgeName", ""));
        confPath.setText(sp.getString("path", ""));
        ip.setText(sp.getString("ip", "157.255.173.182"));
        backpath = sp.getString("backpath", null);
        switch (sp.getString("ipWay","shell")){
            case "helper":
                RadioButton radioButton2 = (RadioButton)findViewById(R.id.check_ip_by_helper);
                radioButton2.setChecked(true);
                break;
            case "browser":
                RadioButton radioButton3 = (RadioButton)findViewById(R.id.check_ip_by_browser);
                radioButton3.setChecked(true);
                break;
            case "not":
                RadioButton radioButton = (RadioButton)findViewById(R.id.not_check_ip);
                radioButton.setChecked(true);
                break;
        }
        switch (sp.getString("ipPort","http://myip.ipip.net")){
            case "http://myip.ipip.net":
                RadioButton radioButton = (RadioButton)findViewById(R.id.check_ip_use_ipip);
                radioButton.setChecked(true);
                break;
            case "http://cip.cc":
                RadioButton radioButton2 = (RadioButton)findViewById(R.id.check_ip_use_cip);
                radioButton2.setChecked(true);
                break;
            case "https://ip.cn/index.php":
                RadioButton radioButton3 = (RadioButton)findViewById(R.id.check_use_ipcn);
                radioButton3.setChecked(true);
                break;
            case "http://whois.pconline.com.cn/ip.jsp":
                RadioButton radioButton4 = (RadioButton)findViewById(R.id.check_use_pconline);
                radioButton4.setChecked(true);
                break;
            default:
                break;
        }

    }

    //对话框
    public void dialog(String path) {
        AlertDialog alert = null;
        AlertDialog.Builder builder = null;
        builder = new AlertDialog.Builder(set.this);
        alert = builder.setTitle("注意")
                .setMessage("如果出现写入失败的情况，请将下面这个目录的权限给满\n" + path)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();             //创建AlertDialog对象
        alert.show();
    }
    //几个开关
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            //多任务隐藏
            case R.id.hide:
                editor.putBoolean("hide",hide.isChecked());
                editor.commit();
                tools.hideInRecents();
                break;
            case R.id.openService:
                editor.putBoolean("openService",openService.isChecked());
                editor.commit();
                break;
            case R.id.screenOff:
                editor.putBoolean("screenOff",screenOff.isChecked());
                editor.commit();
                break;
            case R.id.changeOpen:
                editor.putBoolean("changeOpen",changeOpen.isChecked());
                editor.commit();
                break;
            case R.id.autoClick:
                editor.putBoolean("autoClick",autoClick.isChecked());
                editor.commit();
                break;
            case R.id.autoBack:
                editor.putBoolean("autoBack", autoBack.isChecked());
                editor.commit();
                if(autoBack.isChecked()&&!tools.hasEnableLookPermission()) {
                    AlertDialog alert = null;
                    AlertDialog.Builder builder = null;
                    builder = new AlertDialog.Builder(set.this);
                    alert = builder.setTitle("注意")
                            .setMessage("该功能需要‘有权查看使用情况的应用’,请授予")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                    startActivity(intent);
                                }
                            }).create();             //创建AlertDialog对象
                    alert.show();
                }
                break;
            case R.id.autoCheckIp:
                editor.putBoolean("autoCheckIp",autoCheckIp.isChecked());
                editor.commit();
                break;
        }
    }


}
