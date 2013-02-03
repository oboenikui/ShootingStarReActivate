package com.oboenikui.shootingstarreactivate;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;

public class Modify {
    @TargetApi(9)
    public static void modHttp(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy); 
        }
    }
}
