package io.d0ublew.apk_loader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DFile {
    public static String TAG = DFile.class.getSimpleName();

    public static void createDir(String dirPath) {
        new File(dirPath).mkdirs();
        DLog.i(TAG, dirPath + " directory created");
    }

    public static boolean extract(InputStream in, int size, String outputFilePath) {
        DLog.i(TAG, "extract(): " + outputFilePath + " size: " + size);
        try {
            byte[] buf = new byte[size];
            int offset = 0;
            do {
                offset = in.read(buf, offset, size - offset);
            } while (offset > 0);
            FileOutputStream out = new FileOutputStream(outputFilePath);
            out.write(buf);
            out.close();
        } catch (IOException e) {
            DLog.e(TAG, "extract(): " + e);
            return false;
        }
        DLog.i(TAG, "Successful extract(): " + outputFilePath);
        return true;
    }

    public static boolean extractLibs(String zipFilePath, String outputDir) {
        try {
            ZipFile zf = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> e = zf.entries();
            boolean result = true;
            while (e.hasMoreElements() && result) {
                ZipEntry ze = e.nextElement();
                String name = ze.getName();
                if (name.startsWith("lib/arm64-v8a/") && name.endsWith(".so")) {
                    DLog.i(TAG, "Found " + ze.getName());
                    InputStream in = zf.getInputStream(ze);
                    result = extract(in, (int) ze.getSize(), outputDir + "/" + new File(ze.getName()).getName());
                    in.close();
                }
            }
            zf.close();
            return result;
        } catch (IOException e) {
            DLog.e(TAG, "Failed extractLibs(): " + e);
            return false;
        }
    }

    public static boolean extractDex(String zipFilePath, String outputDir) {
        try {
            ZipFile zf = new ZipFile(zipFilePath);
            ZipEntry ze = zf.getEntry("classes.dex");
            if (ze == null) {
                DLog.e(TAG, "classes.dex not found");
                return false;
            }
            InputStream in = zf.getInputStream(ze);
            boolean result = extract(in, (int) ze.getSize(), outputDir + "/" + ze.getName());
            in.close();
            zf.close();
            return result;
        } catch (IOException e) {
            DLog.e(TAG, "extractDex(): " + e.getMessage());
            return false;
        }
    }
}
