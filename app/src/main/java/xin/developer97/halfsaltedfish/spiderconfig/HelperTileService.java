package xin.developer97.halfsaltedfish.spiderconfig;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HelperTileService extends TileService {
    Intent intent_service;
    Tools tools;
    @Override
    public void onClick() {
        super.onClick();
        tools = Tools.getTools();
        Log.d("TileService", "onClick()");
        //获取自定义的Tile.
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        Log.d("TileService", "Tile state: " + tile.getState());
        switch (tile.getState()) {
            case Tile.STATE_ACTIVE:
                //当前状态是开，设置状态为关闭.
                tile.setState(Tile.STATE_INACTIVE);
                //更新快速设置面板上的图块的颜色，状态为关.
                tile.updateTile();
                //do close somethings.
                stopService(intent_service);
                tools.mes("已退出配置助手");
                Log.d("TileService","停止服务");
                break;
            case Tile.STATE_UNAVAILABLE:
                break;
            case Tile.STATE_INACTIVE:
                //当前状态是关，设置状态为开.
                tile.setState(Tile.STATE_ACTIVE);
                //更新快速设置面板上的图块的颜色，状态为开.
                tile.updateTile();
                //do open somethings.
                startService(intent_service);
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    if(!tools.isNotificationEnabled()){
                                        Log.d("TileService","未开启通知权限");
                                        tools.mes("务必开启通知权限才能使用快捷工具");
                                        tools.isHasNotifications();
                                    }else
                                        Log.d("TileService","开启了通知权限");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).start();
                Log.d("TileService","启动服务");
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent_service = new Intent(this, MyService.class);
    }
}
