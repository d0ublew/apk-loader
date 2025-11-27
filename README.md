# APK Loader

Generate `classes.dex` file

```sh
javac -classpath ${ANDROID_SDK_ROOT}/platforms/android-${ANDROID_SDK_VERSION}/android.jar -d out/ ./app/src/main/java/**/*.java
d8 out/**/*.class --output out/
```
