package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.*;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;

public class Reward extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reward);
        SharedPreferences sp = getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        //自定义壁纸
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layoutreward);
        if (sp.getString("backpath",null)!= null){
            String uri = sp.getString("backpath",null);
            Drawable drawable= (Drawable) Drawable.createFromPath(uri);
            linearLayout.setBackground(drawable);
        }else {
            linearLayout.setBackgroundResource(R.mipmap.tree);
        }
        Button pay = (Button)findViewById(R.id.pay);
        Button wechat = (Button)findViewById(R.id.wechat);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(Reward.this);
                if (hasInstalledAlipayClient) {
                    AlipayDonate.startAlipayClient(Reward.this, "fkx05119erkcmohkyhuub10");
                }
            }
        });
        wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream weixinQrIs = getResources().openRawResource(R.raw.wechat);
                String qrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AndroidDonateSample" + File.separator +
                        "wechat.png";
                WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
                WeiXinDonate.donateViaWeiXin(Reward.this, qrPath);
                Toast.makeText(getApplicationContext(), "已保存收款码，请点击右上角选择照片", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
