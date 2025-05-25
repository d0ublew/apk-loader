package io.d0ublew.apk_loader.util;

public class DLog {
    private static boolean enableLog = true;
    private static String TAG = "d0ublew";

    public static void d(String tag, String msg) {
        if (enableLog) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (enableLog) {
            android.util.Log.v(tag, String.format("[%s] %s", TAG, msg));
        }
    }

    public static void w(String tag, String msg) {
        if (enableLog) {
            android.util.Log.w(tag, String.format("[%s] %s", TAG, msg));
        }
    }

    public static void i(String tag, String msg) {
        if (enableLog) {
            android.util.Log.i(tag, String.format("[%s] %s", TAG, msg));
        }
    }

    public static void e(String tag, String msg) {
        if (enableLog) {
            android.util.Log.e(tag, String.format("[%s] %s", TAG, msg));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (enableLog) {
            android.util.Log.e(tag, String.format("[%s] %s", TAG, msg), tr);
        }
    }
}
