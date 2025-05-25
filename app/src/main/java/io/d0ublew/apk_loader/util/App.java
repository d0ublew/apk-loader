package io.d0ublew.apk_loader.util;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class App {
    public static final String TAG = App.class.getSimpleName();

    public static Context createAppContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThreadObj = currentActivityThreadMethod.invoke(null);

            Field boundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
            boundApplicationField.setAccessible(true);
            Object mBoundApplication = boundApplicationField.get(activityThreadObj);   // AppBindData

            Field infoField = Objects.requireNonNull(mBoundApplication).getClass().getDeclaredField("info");   // info
            infoField.setAccessible(true);
            Object loadedApkObj = infoField.get(mBoundApplication);  // LoadedApk

            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            Method createAppContextMethod = contextImplClass.getDeclaredMethod("createAppContext", activityThreadClass, loadedApkObj.getClass());
            createAppContextMethod.setAccessible(true);

            Object context = createAppContextMethod.invoke(null, activityThreadObj, loadedApkObj);
            if (context instanceof Context)
                return (Context) context;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | NoSuchFieldException e) {
            DLog.e(TAG, "createAppContext(): " + e.getMessage());
        }
        return null;
    }
}
