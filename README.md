What is it?
-----------
CoreLib是一个工具核心库。
* 网络库，直接JSON的对象化Request和Response。
* 大文件下载
* 文件IO工具类
* Log系统，支持动态Log开关，Log会保存在本地。
* 自定义线程池。
* SingleInstance管理。

Sample工程:
-----------
https://github.com/MichaelSun/CorelibSample


集成方式
----------

AndroidMenifest.xml加入一下权限：
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />

corelib是lib工程，直接导入。

初始化代码加入App，需要在所有的初始化之前：

    public class MyApplication extends Application {

        @Override public void onCreate() {        
            /**
            * true: open Log
            * flase: close Log
            * /
            CoreConfig.init(getApplicationContext(), false);
        }
        ...
    }

网络
----------
使用标注方式定义API的java bean request，所有的返回数据都会直接解析成Object Response，目前支持JSON。
所有的网络分成*Request*和*Response*，所有的API Request继承自**RequestBase**，所有的API Response继承自**ResponseBase**

Log系统
----------
使用CoreConfig.init(getApplicationContext(), true)启动log之后，Log会存储在*/scard/.your_package_name/debug_log.txt*。

如果要在Logcat中过滤Log，使用**adb -d shell DebugLog:d *:s**

使用方式:

    CoreConfig.LOG(msg);
    CoreConfig.LOG(tag, msg);
    CoreConfig.LOG(msg, Throwable);

Proguard配置
-----------
    -keep class com.michael.corelib.** {*;}

    -keep class * extends com.michael.corelib.internet.core.RequestBase { *; }
    -keep class * extends com.michael.corelib.internet.core.ResponseBase { *; }


