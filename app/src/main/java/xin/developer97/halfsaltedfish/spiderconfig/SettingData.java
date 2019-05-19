package xin.developer97.halfsaltedfish.spiderconfig;

import java.io.Serializable;

public class SettingData implements Serializable {
    private String packgeName;
    private String path;
    private String ip;
    private String autotime;
    private Boolean autoOpen,thenOpen,screenOff,changeOpen,doset;

    public SettingData(String packgeName, String path, String ip,String autotime,Boolean autoOpen,Boolean thenOpen,Boolean screenOff,Boolean changeOpen) {
        this.packgeName = packgeName;
        this.path = path;
        this.ip = ip;
        this.autotime = autotime;
        this.autoOpen = autoOpen;
        this.thenOpen = thenOpen;
        this.screenOff = screenOff;
        this.changeOpen = changeOpen;
    }

    public SettingData() {
    }


}
