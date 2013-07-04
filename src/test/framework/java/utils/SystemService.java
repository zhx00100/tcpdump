package test.framework.java.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.PowerManager;

public class SystemService {

    /**
     * obtain ActivityManagerService
     */
    public static final ActivityManager getActivityManager(Context context) {
        return (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    /**
     * obtain PackageManagerService
     */
    public static final PackageManager getPackageManager(Context context) {
        return (PackageManager) context.getPackageManager();
    }
    
    /**
     * obtain PowerManagerService
     */
    public static final PowerManager getPowerManager(Context context) {
        return (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }
    
    /**
     * obtain AlarmManagerService
     */
    public static final AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
    
    /**
     * obtain ConnectivityManagerService
     */
    public static final ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    
    /**
     * obtain NotificationManagerService
     */
    public static final NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
