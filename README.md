# APK Loader

1. Generate `classes.dex` file
    ```sh
    javac -classpath ${ANDROID_SDK_ROOT}/platforms/android-${ANDROID_SDK_VERSION}/android.jar -d out/ ./app/src/main/java/**/*.java
    d8 out/**/*.class --output out/
    ```

2. Generate C header file
    ```sh
    python3 bin2header.py --name apk_loader_dex ./out/classes.dex ./out/apk_loader.h
    ```
