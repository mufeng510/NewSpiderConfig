package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.List;

public class set extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private SharedPreferences sp;
    private String backpath;
    EditText autotime, ip;
    TextView confPath;
    Switch hide, openService, screenOff, changeOpen,autoClick,autoCheckIp;
    Tools tools = Tools.getTools();
    SharedPreferences.Editor editor;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode==1100) {
                Uri uri = data.getData();
                backpath = tools.getRealPathFromUri(uri);
                tools.mes("ok，重启软件生效");
            }
            if(requestCode==1000){
                List<String> list = data.getStringArrayListExtra("paths");
                confPath.setText(list.get(0));
            }
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
        autoCheckIp = (Switch) findViewById(R.id.autoCheckIp);
        autoCheckIp.setOnCheckedChangeListener(this);

        RadioGroup ipGroup = (RadioGroup) findViewById(R.id.ipGroup);
        Button ipWays = (Button) findViewById(R.id.ipWays);
        Button ipPorts = (Button) findViewById(R.id.ipPorts);

        autotime = (EditText) findViewById(R.id.autotime);
        confPath = (TextView) findViewById(R.id.confPath);
        ip = (EditText) findViewById(R.id.ip);

        Button chroseConf = (Button)findViewById(R.id.chroseConf);
        Button background = (Button) findViewById(R.id.background);
        Button setting = (Button) findViewById(R.id.setting);

        //关于
        final TextView About_software = (TextView) findViewById(R.id.About_software);
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

        //获取选中ip
        ipGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radbtn = (RadioButton) findViewById(checkedId);
                ip.setText(radbtn.getText());
            }
        });
        //ip查询方式
        ipWays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(set.this,ipWays);
                popup.getMenuInflater().inflate(R.menu.menu_pop, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.check_ip_by_helper:
                                ipWays.setText("助手");
                                editor.putString("ipWay","helper");
                                break;
                            case R.id.check_ip_by_browser:
                                ipWays.setText("浏览器");
                                editor.putString("ipWay","browser");
                                break;
                            case R.id.not_check_ip:
                                ipWays.setText("不查询");
                                editor.putString("ipWay","not");
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        //ip查询接口
        ipPorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(set.this,ipPorts);
                popup.getMenuInflater().inflate(R.menu.menu_ipapi, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.check_ip_use_ipip:
                                ipPorts.setText("ipip");
                                editor.putString("ipPort","ipip");
                                break;
                            case R.id.check_ip_use_cip:
                                ipPorts.setText("cip");
                                editor.putString("ipPort","cip");
                                break;
                            case R.id.check_use_ipcn:
                                ipPorts.setText("纯真");
                                editor.putString("ipPort","cz88");
                                break;
                            case R.id.check_use_pconline:
                                ipPorts.setText("pconline");
                                editor.putString("ipPort","pconline");
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        chroseConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LFilePicker()
                        .withActivity(set.this)
                        .withRequestCode(1000)
                        .withMutilyMode(false)
                        .withFileFilter(new String[]{".conf"})
                        .withStartPath("/storage/emulated/0/tiny")
                        .start();
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
                editor.putString("confPath", confPath.getText().toString());
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
        autoClick.setChecked((sp.getBoolean("autoClick",false)));
        autoCheckIp.setChecked(sp.getBoolean("autoCheckIp", true));

        autotime.setText(sp.getInt("autotime", 60) + "");
        confPath.setText(sp.getString("confPath", "/storage/emulated/0/tiny/王卡配置.conf"));
        ip.setText(sp.getString("ip", "157.255.173.182"));
        backpath = sp.getString("backpath", null);
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
            case R.id.autoCheckIp:
                editor.putBoolean("autoCheckIp",autoCheckIp.isChecked());
                editor.commit();
                break;
        }
    }


}
