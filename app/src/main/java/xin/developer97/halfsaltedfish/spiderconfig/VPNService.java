package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class VPNService extends VpnService implements Runnable {

    private ParcelFileDescriptor descriptor;
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private Thread vpnThread;
    private int mtuSize = 1500;
    private boolean isRun;
    private static final String TAG = "VPNService";


    Tools tools = Tools.getTools();

    public VPNService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vpnThread = new Thread(this, "VPNService");
        vpnThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void run() {
        try {
            // 开启连接后初始化VPN参数并建立连接
            establishVPN();
            // 开始读写操作
            startStream();
        } catch (Exception e) {
        } finally {
            stop();
        }
    }

    //建立vpn
    private void establishVPN() {
        Builder builder = new Builder();
        builder.setMtu(mtuSize);
        builder.addAddress("10.1.10.1", 32);
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("8.8.8.8");
        builder.setSession(getString(R.string.app_name));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 添加需要拦截的应用
            // 如果应用未安装时 这里会拋出未找到应用的异常。
            try {
                builder.addAllowedApplication("com.tencent.mtt");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                tools.mes("请安装QQ浏览器");
            }
        } else {
            tools.mes("当前版本的android 不支持vpnservice");
        }
        // 调用方法建立连接，并把返回的包描述符实例赋值给全局变量
        descriptor = builder.establish();
        isRun = true;
    }

    /**
     * 开始读写
     *
     * @throws Exception
     */
    private void startStream() throws Exception {
        // 根据描述符创建文件文件读取流
        inputStream = new FileInputStream(descriptor.getFileDescriptor());
        // 根据描述符创建文件文件写入流
        outputStream = new FileOutputStream(descriptor.getFileDescriptor());
        // 创建一个用来保存数据报的缓存区，其大小应与Builder设置的缓存区大小相同。
        byte[] bytes = new byte[mtuSize];

        int size = 0;

        while (isRun) {
            // 循环读取拦截到的数据  每读一条就是一个IP数据报。
            // 这里读取到的数据报是本地程序需要发到网络上的数据报。
            // 拦截到的数据报需要通过安全通道进行写出到远程主机
            size = inputStream.read(bytes);
            // 如果读取的数据长度为0说明还没有数据报被拦截。需要循环等待有数据进来
            if (size <= 0) {
                Thread.sleep(20);
                continue;
            } else {

                Packet packet = null;
                try {
                    ByteBuffer buf = ByteBuffer.wrap(bytes);
                    packet = new Packet(buf, false);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String sIp = null;
                if (packet.ip4Header.destinationAddress != null) {
                    sIp = packet.ip4Header.destinationAddress.getHostAddress();
                }


                if (packet.isTCP()) {

                    Log.i(TAG, "tcp address:" + packet.ip4Header.sourceAddress.getHostAddress() + "tcp port:"
                            + packet.tcpHeader.sourcePort + " des:" + sIp + " des port:" + packet.tcpHeader.destinationPort);
                    try {
                        if (packet.tcpHeader.destinationPort == 8090){
                            Log.i("8090",packet.toString());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else if (packet.isPing()) {
//                    Log.w(TAG, packet.ip4Header.toString());
                } else {
//                    Log.w(TAG, "Unknown packet type");
//                    Log.w(TAG, packet.ip4Header.toString());
                }
            }
//            System.out.println(new String(bytes, 0, size));

            // 这里写入的数据报是远程主机返回的数据报
            // 由连接到远程主机的通道读取获得
            outputStream.write(bytes, 0, size);

            // 读取到数据报后发给通道处理并发送到真实网络
            outputStream.flush();
            // 到此VPN的功能大体就介绍结束了。
        }
    }

    // 关闭数据流 并结束服务。
    public synchronized void stop() {

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            } finally {
                inputStream = null;
            }
        }

        if (descriptor != null) {
            try {
                descriptor.close();
            } catch (IOException e) {
            } finally {
                descriptor = null;
            }
        }

        stopSelf();
    }
}
