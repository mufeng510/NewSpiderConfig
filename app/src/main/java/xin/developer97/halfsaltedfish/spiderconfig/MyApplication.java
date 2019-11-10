package xin.developer97.halfsaltedfish.spiderconfig;

import android.app.Application;

import com.hjq.toast.ToastUtils;

public class MyApplication extends Application {
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        ToastUtils.init(this);
    }

    public static MyApplication getInstance(){
        return myApplication;
    }
}
