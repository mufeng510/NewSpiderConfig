package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewConfig {
    private Context context;

    public String time;
    public String ip;
    public String guid;
    public String token;
    //获取配置
    public NewConfig(Context context,String time,String guid,String token) {
        this.time = time;
        SharedPreferences sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        this.ip = sp.getString("ip","157.255.173.185");
        this.guid = decryptGuid(guid);
        this.token = decryptToken(token);
    }
    //生成配置
    public NewConfig(Context context,String guid, String token) {
        this.context = context;
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        this.time =ft.format(dNow);
        SharedPreferences sp = context.getSharedPreferences("mysetting.txt", Context.MODE_PRIVATE);
        this.ip = sp.getString("ip","157.255.173.185");
        this.guid = guid;
        this.token = token;
    }

    public String getConfig(){
        String config = String.format("listen_port=65080;\n" +
                "worker_proc=1;\n" +
                "daemon=on;\n" +
                "uid=3004;\n" +
                "mode=wap;\n" +
                "\n" +
                "http_ip=%s;\n" +
                "http_port=8090;\n" +
                "http_del=\"Host,X-Online-Host\";\n" +
                "http_first=\"[M] http://[H][U] [V]\\r\\n\n" +
                "Q-GUID:%s\\r\\n\n" +
                "Q-Token:%s\n" +
                "\n" +
                "\\r\\n\n" +
                "Host: [H]\\r\\n\n" +
                "Connection: Keep-Alive\\r\\n\";\n" +
                "\n" +
                "https_connect=on;\n" +
                "https_ip=%s;\n" +
                "https_port=8091;\n" +
                "https_del=\"Host,X-Online-Host\";\n" +
                "https_first=\"[M] [H] [V]\\r\\n\n" +
                "Host: [H]\\r\\n\n" +
                "Proxy-Connection: keep-alive\\r\\n\n" +
                "Q-GUID:%s\\r\\n\n" +
                "Q-Token:%s\n" +
                "\\r\\n\";\n" +
                "\n" +
                "dns_tcp=http;\n" +
                "dns_listen_port=65053;\n" +
                "dns_url=\"119.29.29.29\";\n",this.ip,this.guid,this.token,this.ip,this.guid,this.token);
        return config;
    }

//    public void setIp(String ip) {
//        this.ip = ip;
//    }
//
//    public void setGuid(String guid) {
//        this.guid = guid;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
//
    public String getTime() {
        return time;
    }
//
//    public String getIp() {
//        return ip;
//    }
//
//    public String getGuid() {
//        return guid;
//    }
//
//    public String getToken() {
//        return token;
//    }

    private char password(int num){
        switch (num){
            case 0:
                return 'a';
            case 1:
                return 'b';
            case 2:
                return 'c';
            case 3:
                return 'd';
            case 4:
                return 'e';
            case 5:
                return 'f';
            case 6:
                return '0';
            case 7:
                return '1';
            case 8:
                return '2';
            case 9:
                return '4';
        }
        return '8';
    }
    //加密GUID
    public String encryptionGuid(){
        return guid.replace(password((int)time.charAt(18)), '*');
    }
    //解密GUID
    public String decryptGuid(String decryptGuid){
        return decryptGuid.replace('*',password((int)time.charAt(18)));
    }
    //加密Token
    public String encryptionToken(){
        return token.replace(password((int)time.charAt(17)),'*');
    }
    //解密Token
    public String decryptToken(String decryptToken){
        return decryptToken.replace('*',password((int)time.charAt(17)));
    }
}
