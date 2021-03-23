package com.bodtec.module.dahuaplayer;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.bodtec.module.dahuaplayer.act.DahuaCloudPlayAct;


/**
 * hujx 3/15/21
 */
public class BodtecDahuaHelper {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("dslalien");
        System.loadLibrary("dsl");
        System.loadLibrary("CommonSDK");
        System.loadLibrary("DSSMobileSDK");
        System.loadLibrary("PlatformSDK");
        System.loadLibrary("DSSCloudStream");
    }

    private static class Instance {
        static BodtecDahuaHelper instance = new BodtecDahuaHelper();
    }

    /**
     * 单一实例
     */
    public static BodtecDahuaHelper getInstance() {
        return BodtecDahuaHelper.Instance.instance;
    }

    public void open(Context context, String token, String snCode) {
        if (null == context || TextUtils.isEmpty(token) || TextUtils.isEmpty(snCode)) {
            Toast.makeText(context, "参数错误，播放失败！", Toast.LENGTH_SHORT).show();
        } else {
            DahuaCloudPlayAct.open(context, token, snCode);
        }
    }

}
