package test.framework.java.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.Build;
import android.util.Log;

public class ForceStopProcess {

	//Logcat TAG
	private static final String TAG = ForceStopProcess.class.getSimpleName();

	//entry main();
	public static void main(String[] args) {

		new ForceStopProcess().run(args);

	}

	//instance method;
	public void run(final String[] args) {
		
		System.out.println("dalvikvm invocation!");

		Log.i(TAG, "dalvikvm invoke");

		Log.i(TAG, Arrays.toString(args));

		try {
			// int myUid = Process.myUid();
			// int myPid = Process.myPid();
			// int appId = UserHandle.getAppId(myUid);
			// int userId = UserHandle.getUserId(myUid);
			//
			// Log.i(TAG, "myUid=" + myUid + "; myPid=" + myPid + "; appId=" +
			// appId + "; userId=" + userId);
			// invoke forceStopPackage

			Log.i(TAG, "invoke forceStopPackage");
			String packageName = "com.baidu.push.example";
			IActivityManager iam = ActivityManagerNative.getDefault();
//			Method m = IActivityManager.class.getDeclaredMethod(
//					"forceStopPackage", String.class);
//			m.invoke(iam, "com.baidu.push.example");
			if (Build.VERSION.SDK_INT < 16) {
				iam.forceStopPackage(packageName);
			} else {
				iam.forceStopPackage(packageName, -1);
			}

			Class<?> cls = Class.forName("android.app.IActivityManager");
			Method[] ms = cls.getDeclaredMethods();
			for (Method mm : ms) {
//				Log.i(TAG, mm.toGenericString());
			}

			Log.i(TAG, "end invoke forceStopPackage");

			Log.i(TAG, ActivityManagerNative.getDefault().toString());
		} catch (Exception e) {

			Log.i(TAG, "error!");
			Log.i(TAG, Log.getStackTraceString(e));
		}
	}
}
