package com.example.myappli;

import android.content.Context;

/**
 * Description:
 * Detail:
 * Create Time: 2020/3/11
 *
 * @author kallen
 * @version 1.0
 * @see ...
 * History:
 * @since Since
 */
public class AppContainer {

    private static final String TAG = AppContainer.class.getSimpleName();
    private static Context sContext;
//    private static BaiduTtsService ttsService;
//    public static final String TAG_CALL_CONTENT_OBSERVER = "tag_call_content_observer";
//
//    public static boolean isInitTTS = false;
//    public static Elder elder;

    public static void put(Context context) {
        sContext = context.getApplicationContext();
//        ToastWrapper.get().init(sContext);
//        elder = new Elder();
//        elder.setPhone(SharePrefrenceUtils.getString(Elder.TAG_ELDER_PHONE, ""));
//        elder.setNickname(SharePrefrenceUtils.getString(Elder.TAG_ELDER_NICKNAME, ""));
//        elder.setUrl(SharePrefrenceUtils.getString(Elder.TAG_ELDER_URL, ""));
//        elder.setSex(SharePrefrenceUtils.getInt(Elder.TAG_ELDER_SEX, 2));
//        elder.setAddress(SharePrefrenceUtils.getString(Elder.TAG_ELDER_ADDRESS, ""));
//        elder.setToken(SharePrefrenceUtils.getString(Elder.TAG_ELDER_TOKEN, ""));
//        elder.setLogin(SharePrefrenceUtils.getBoolean(Elder.TAG_ELDER_IS_LOGIN, false));
    }

//    /**
//     * 启动上传地理位置的服务(30分钟一次)
//     */
//    public static void startUpLoaPositionService() {
//        if (isUserLogin()) {
//            CMICLogger.d(TAG,"启动上传地理位置的服务");
//            Intent intent = new Intent(sContext, UploadPositionService.class);
//            intent.putExtra(UploadPositionService.SERVICE_COMMAND, UploadPositionService.LOGIN_ACCOUNT);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                sContext.startForegroundService(intent);
//            } else {
//                sContext.startService(intent);
//            }
//        }
//    }
//
//    public static boolean isFinish = false;
//
//    public static boolean isUserLogin() {
//        return elder != null && elder.isLogin();
//    }
//
//    /**
//     * 初始化短信库
//     */
//    public static void initLibUpdateCtrl() {
//        ThreadTool.get().runOnWorker(new Runnable() {
//            @Override
//            public void run() {
//                //初始化短信判断库
//                if (sContext instanceof Application) {
//                    LibUpdateCtrl.initDate((Application) sContext, true, Constant.BASE_SMS_FILTER_URL, "2");
//                } else {
//                    LogUtils.debug("AppContainer", "初始化短信判断库...失败！！！");
//                }
//            }
//        });
//    }
//
//
//    private static boolean isInitCall = false;
//
//
//
    public static Context app() {
        return sContext;
    }
//
//
//    private AppContainer() {
//    }
//
//    private static boolean isMainProcess(Context sContext) {
//        int pid = android.os.Process.myPid();
//        ActivityManager manager = (ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE);
//        if (null != manager) {
//            List<ActivityManager.RunningAppProcessInfo> appProcesses = manager.getRunningAppProcesses();
//            if (appProcesses == null) {
//                return false;
//            }
//            for (ActivityManager.RunningAppProcessInfo process : appProcesses) {
//                if (process.pid == pid) {
//                    return sContext.getPackageName().equals(process.processName);
//                }
//            }
//        }
//        return false;
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private static boolean isInitTTSTTS(Context context) {
//        final Context app = context.getApplicationContext();
//        ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
//        assert cm != null;
//        Network[] networks = cm.getAllNetworks();
//        if (isInitTTS) {
//            return true;
//        } else {
//            if (networks.length > 0) {
//                initTTS();
//            } else if (!NETWORK_RECEIVER.isReg) {
//                NETWORK_RECEIVER.isReg = true;
//                IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//                app.registerReceiver(NETWORK_RECEIVER, networkFilter);
//            }
//        }
//        return false;
//    }
//
//
//    private final static NetworkReceiver NETWORK_RECEIVER = new NetworkReceiver();
//
//    private static class NetworkReceiver extends BroadcastReceiver {
//        boolean isReg = false;
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//                if (isInitTTSTTS(context)) {
//                    context.getApplicationContext().unregisterReceiver(this);
//                }
//            }
//        }
//    }


}
