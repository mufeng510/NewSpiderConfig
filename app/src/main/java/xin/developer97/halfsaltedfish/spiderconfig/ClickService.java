package xin.developer97.halfsaltedfish.spiderconfig;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

/**
 * Created by minggo on 16/5/30.
 */
public class ClickService extends AccessibilityService {

    private ClickReceiver receiver;
    private static ClickService service;
    static String foregroundPackageName = "";
    public static boolean isForegroundPkgViaDetectionService(String packageName) {
        return packageName.equals(ClickService.foregroundPackageName);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //接收事件,如触发了通知栏变化、界面变化等
        Log.i("mService", "AccessibilityEvent按钮点击变化");
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            /*
             * 如果 与 DetectionService 相同进程，直接比较 foregroundPackageName 的值即可
             * 如果在不同进程，可以利用 Intent 或 bind service 进行通信
             */
            foregroundPackageName = event.getPackageName().toString();

            /*
             * 基于以下还可以做很多事情，比如判断当前界面是否是 Activity，是否系统应用等，
             * 与主题无关就不再展开。
             */
            ComponentName cName = new ComponentName(event.getPackageName().toString(),
                    event.getClassName().toString());
        }
        Log.i("mService",foregroundPackageName);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        Log.i("mService", "按钮点击变化");

        //接收按键事件
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {
        Log.i("mService", "授权中断");
        //服务中断，如授权关闭或者将服务杀死
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("mService", "service授权成功");
        service = this;
        //连接服务后,一般是在授权成功后会接收到
        if (receiver == null) {
            receiver = new ClickReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("auto.click");
            registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    //执行返回
    public void performBack() {
        Log.i("mService", "执行返回");
        try {

        }catch (Exception e){

        }
        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    //执行点击
    private void performClick(String resourceId) {

        Log.i("mService","点击执行");
        try {
            AccessibilityNodeInfo nodeInfo = this.getRootInActiveWindow();
            AccessibilityNodeInfo targetNode = null;
            //通过名字获取
            //targetNode = findNodeInfosByText(nodeInfo,"广告");
            //通过id获取
            targetNode = findNodeInfosById(nodeInfo,"com.cqyapp.tinyproxy:id/menu_item_switch");
            if (targetNode.isClickable()) {
                targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }catch (Exception e){

        }

    }

    //执行点击
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void performClick(String resourceId) {
//
//        Log.i("mService","点击执行");
//        try {
//            List<AccessibilityWindowInfo> windows = getWindows();
//            for (int i=0;i<windows.size();i++){
//                AccessibilityNodeInfo nodeInfo = windows.get(i).getRoot();
//                AccessibilityNodeInfo targetNode = null;
//                targetNode = findNodeInfosById(nodeInfo,"com.cqyapp.tinyproxy:id/"+resourceId);
//                if (targetNode.isClickable()) {
//                    targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//        }catch (Exception e){
//
//        }
//    }


    //通过id查找
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if(list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    //通过文本查找
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 判断当前服务是否正在运行
     * */
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    public static boolean isRunning() {
//        if(service == null) {
//            return false;
//        }
//        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
//        AccessibilityServiceInfo info = service.getServiceInfo();
//        if(info == null) {
//            return false;
//        }
//        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
//        Iterator<AccessibilityServiceInfo> iterator = list.iterator();
//
//        boolean isConnect = false;
//        while (iterator.hasNext()) {
//            AccessibilityServiceInfo i = iterator.next();
//            if(i.getId().equals(info.getId())) {
//                isConnect = true;
//                break;
//            }
//        }
//        if(!isConnect) {
//            return false;
//        }
//        return true;
//    }

    public class ClickReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int i = intent.getIntExtra("flag", 0);
            Log.i("mService","广播flag="+i);
            if (i == 1) {
                String resourceid = intent.getStringExtra("id");
                performClick(resourceid);
            }

        }

    }


}
