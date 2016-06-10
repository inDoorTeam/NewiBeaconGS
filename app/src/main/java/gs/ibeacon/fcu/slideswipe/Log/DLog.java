package gs.ibeacon.fcu.slideswipe.Log;

import android.util.Log;

/**
 * Created by bing on 2016/6/9.
 */
public class DLog {
    private static final String TAG = "iBeaconGS";

    public static void d(String classname, String msg){
        Log.d(TAG, ">>> " + classname + " >>> " + msg);
    }
    public static void w(String msg){
        Log.w(TAG, ">>> " + msg);
    }
}
