package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPacket extends AppCompatActivity implements  android.view.GestureDetector.OnGestureListener{
    CharSequence old;
    Tools tools = Tools.getTools();
    SharedPreferences sp;
    GestureDetector gd;

    static JSONObject jsonObject=new JSONObject();
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if (result == RESULT_OK) {
            try {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.tencent.mtt");
                                    if (intent != null) {
                                        intent.putExtra("type", "110");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setAction("android.intent.action.VIEW");
                                        Uri uri = Uri.parse("http://qbact.html5.qq.com/qbcard?addressbar=hide&ADTAG=tx.qqlq.grzx\n");
                                        intent.setData(uri);
                                        getApplicationContext().startActivity(intent);
                                    } else tools.mes("打开失败：未安装应用");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    tools.mes("无法打开");
                                }
                            }
                        }
                ).start();
                Thread.sleep(1000);
                Intent intent = new Intent(this, VPNService.class);
                startService(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //去除标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_packet);
        sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        //创建手势检测器
        gd = new GestureDetector(this,this);
        //自定义壁纸
        RelativeLayout linearLayout = (RelativeLayout)findViewById(R.id.layoutget);
        if (sp.getString("backpath",null)!= null){
            String uri = sp.getString("backpath",null);
            Drawable drawable= (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        }else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }
        Button generate = (Button)findViewById(R.id.generate);
        Button copyConfig = (Button)findViewById(R.id.copyConfig);

        final EditText guid = (EditText)findViewById(R.id.guid);

        old = guid.getText();
        ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            guid.setText(cm.getPrimaryClip().toString());
        }catch (Exception e){

        }

        //自动抓包
//        Button autopull = (Button)findViewById(R.id.autopull);
//        autopull.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = VpnService.prepare(getApplicationContext());
//                if (intent != null) {
//                    startActivityForResult(intent, 0);
//                } else {
//                    onActivityResult(0, RESULT_OK, null);
//                }
//            }
//        });
        //生成配置
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] text = getGuidToken(guid.getText().toString());
                if (text!=null){
                    tools.restartTimedTask();
                    NewConfig newConfig = new NewConfig(getApplicationContext(),text[0],text[1]);
                    String config = newConfig.getConfig();
                    guid.setText(config);
                    showDialog(newConfig);
                    Toast.makeText(GetPacket.this, "生成成功", Toast.LENGTH_SHORT).show();
                    if(sp.getBoolean("autoOpen",true)){
                        String path = sp.getString("path",Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "tiny/王卡配置.conf");
                        String name = sp.getString("packgeName","com.cqyapp.tinyproxy");
                        write(path,config);
                        open(name);
                    }

                }
                else Toast.makeText(GetPacket.this, "生成失败,请检查信息是否正确!", Toast.LENGTH_SHORT).show();
            }
        });
        //复制
        copyConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence config = guid.getText();
                if(config != old){
                    tools.copy(config);
                    tools.mes("复制成功");
                }
                else
                    tools.mes("请先生成配置");
            }
        });
        //抓包教程
        Button captureTutorial = (Button)findViewById(R.id.captureTutorial);
        captureTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://dd.ma/b2vCpile");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
    //获取guid和token
    public String[] getGuidToken(String text){
        String pattern = "Q-GUID[: |]*(\\w*?)[,\\s]";
        String pattern2 = "Q-Token[: |]*(\\w*?)[\\s,]";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        Pattern r2 = Pattern.compile(pattern2);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(text);
        Matcher m2 = r2.matcher(text);
        if (m.find()&&m2.find()){
            String guid = m.group(1);
            String token = m2.group(1);
            String[] txt = new String[2];
            txt[0] = guid;
            txt[1] = token;
            return txt;
        }
        else return null;

    }
    //上传配置
    public void showDialog(final NewConfig newConfig){
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonObject.put("Time",newConfig.getTime());
                            jsonObject.put("Guid",newConfig.getGuid());
                            jsonObject.put("Token",newConfig.getToken());
                            HttpURLConnection con=null;
                            String path="http://" + getString(R.string.host) + "/KingCardServices/create_config.php?id=1";
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
                                out.write((jsonObject.toString()).getBytes("utf-8"));
                                out.flush();
                                out.close();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                String lines;
                                StringBuffer sbf = new StringBuffer();
                                while ((lines = reader.readLine()) != null) {
                                    lines = new String(lines.getBytes(), "utf-8");
                                    sbf.append(lines);
                                }
//                                System.out.println("返回来的报文："+sbf.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            tools.mes("上传失败");
                        };
                    }
                }
        ).start();
    }
    //写入
    public void write(String path, CharSequence config){
        if(config != old){
            try {
                tools.savaFileToSD(path,config.toString());
                Toast.makeText(GetPacket.this, "写入成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(GetPacket.this, "写入失败", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(GetPacket.this, "请先获取配置信息", Toast.LENGTH_SHORT).show();

    }
    //唤醒
    public void open(String name){
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(name);
            if (intent != null) {
                intent.putExtra("type", "110");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else Toast.makeText(getApplicationContext(), "打开失败：未安装应用", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "无法打开", Toast.LENGTH_SHORT).show();
        }
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
//        float beginY = e1.getY();
//        float endY = e2.getY();

        if(beginX-endX>minMove&&Math.abs(velocityX)>minVelocity){   //左滑
//            Toast.makeText(this,velocityX+"左滑",Toast.LENGTH_SHORT).show();
        }else if(endX-beginX>minMove&&Math.abs(velocityX)>minVelocity){   //右滑
            this.finish();
//            Toast.makeText(this,velocityX+"右滑",Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}
