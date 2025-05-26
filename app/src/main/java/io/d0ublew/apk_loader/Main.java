package io.d0ublew.apk_loader;

import io.d0ublew.apk_loader.util.App;
import io.d0ublew.apk_loader.util.DFile;
import io.d0ublew.apk_loader.util.DLog;
import io.d0ublew.apk_loader.util.DReflection;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final String TAG = Main.class.getSimpleName();

    public static String tobeLoadedApk;

    public static String entryClass;

    public static Context appContext;

    public Main(String apk, String clazz) {
        DLog.i(TAG, "constructor called");
        Context context = App.createAppContext();
        if (context == null) {
            DLog.e(TAG, "createAppContext() failed");
            return;
        }
        appContext = context;
        tobeLoadedApk = apk;
        entryClass = clazz;
        DLog.i(TAG, "tobeLoadedApk: " + tobeLoadedApk);
        DLog.i(TAG, "entryClass: " + entryClass);
        init(context);
    }

    public static void init(Context context) {
        DLog.i(TAG, "init(context)");
        String appPrivateDir = context.getFilesDir().getAbsolutePath();
        DLog.i(TAG, appPrivateDir);
        File apkLoaderDir = new File(appPrivateDir + "/apk_loader");
        String apkLoaderLibsDir = apkLoaderDir + "/libs";
        DFile.createDir(apkLoaderLibsDir);
        // if (!DFile.extractDex(tobeLoadedApk, apkLoaderDir.getAbsolutePath())) {
        //     DLog.e(TAG, "Failed to extract dex");
        //     return;
        // }
        if (!DFile.extractLibs(tobeLoadedApk, apkLoaderLibsDir)) {
            DLog.e(TAG, "Failed to extract libs");
            return;
        }

        ClassLoader apkClassLoader = context.getClassLoader();
        DLog.i(TAG, "Before apkClassLoader: " + apkClassLoader);
        try {
            Field pathListField = DReflection.findField(apkClassLoader, "pathList");
            Object dexPathList = pathListField.get(apkClassLoader);
            assert dexPathList != null;
            ArrayList<File> newNativeLibs = new ArrayList<>();
            newNativeLibs.add(new File(apkLoaderLibsDir));
            Field oriNativeLibsField = DReflection.findField(dexPathList, "nativeLibraryDirectories");
            ArrayList<File> oriNativeLibsList = (ArrayList<File>) oriNativeLibsField.get(dexPathList);
            if (oriNativeLibsList == null) {
                oriNativeLibsField.set(dexPathList, newNativeLibs);
            } else {
                oriNativeLibsList.addAll(newNativeLibs);
            }

            Method makePathElementsNativeMethod = DReflection.findMethod(dexPathList, "makePathElements", List.class);
            makePathElementsNativeMethod.setAccessible(true);
            Object[] nativeLibsArr = (Object[]) makePathElementsNativeMethod.invoke(dexPathList, newNativeLibs);
            assert nativeLibsArr != null;
            DReflection.expandFieldArray(dexPathList, "nativeLibraryPathElements", nativeLibsArr);

            ArrayList<File> newDexPathList = new ArrayList<>();
            newDexPathList.add(new File(tobeLoadedApk));
            Method makePathElementsMethod = DReflection.findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
            makePathElementsMethod.setAccessible(true);
            Object[] dexArr = (Object[]) makePathElementsMethod.invoke(dexPathList, newDexPathList, null, null);
            assert dexArr != null;
            DReflection.expandFieldArray(dexPathList, "dexElements", dexArr);

//            Method findLibraryMethod = DReflection.findMethod(apkClassLoader, "findLibrary", String.class);
//            findLibraryMethod.setAccessible(true);
//            String out = (String) findLibraryMethod.invoke(apkClassLoader, "pine");
//            DLog.i(TAG, "findLibrary(): " + out);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            DLog.e(TAG, "Failed to modify original classLoader: " + e);
            return;
        }
        DLog.i(TAG, "After apkClassLoader: " + apkClassLoader);

        try {
            Class<?> clazz = apkClassLoader.loadClass(entryClass);
            Constructor<?> ctor = clazz.getConstructor(Context.class);
            ctor.newInstance(context);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            DLog.e(TAG, "Failed to load dynamic stuff: " + e);
            return;
        }
    }
}
