package test.framework.java.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.LocalServerSocket;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.SyncStateContract.Constants;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


/**
 * 工具类
 */

public final class PushUtility {
    
    /** log tag name. */
    private static final String TAG = "Utility";
//    /** MoPlusService类名 */
//    private static final String MOPLUS_CLASS_NAME = "MoPlusService";

    /**
     * 构造
     */
    private PushUtility() {
        
    }
    
    /**
     * 是否已经是root用户
     * @param context Context
     * @return true表示是，false表示不是
     */
    public static boolean isRooted(Context context) {
        File sufilebin = new File("/data/data/root");
        try {
            sufilebin.createNewFile();
            if (sufilebin.exists()) {
                sufilebin.delete();
            }
            return true;
        } catch (IOException e) {
//            if (getPacakgeInfo(context, "com.noshufou.android.su") != null
//                    || getPacakgeInfo(context, "com.miui.uac") != null) {
//                return true;
//            }
            
            ArrayList<String> result = RootCmd.execRootCmd("");
                        
            if (result.isEmpty()) {
            	return true;
            }
            
            return false;
        }

    }
    
    /**
     * 获取packageName 关联的PacakgeInfo
     * 
     * @param context
     *            Context
     * @param packageName
     *            应用包名
     * @return PackageInfo
     */
    public static PackageInfo getPacakgeInfo(Context context, String packageName) {
        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
                return pi;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 
     * 获取指定的sticky broadcast intent
     * 
     * @param ctx
     *            上下文
     * @param action
     *            Sticky broadcast对应的Action
     * @return Intent
     */
    public static Intent getStickyIntent(Context ctx, String action) {
        Intent relt = null;

        BroadcastReceiver stickyRcv = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // Do nothing
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        relt = ctx.registerReceiver(stickyRcv, filter);
        ctx.unregisterReceiver(stickyRcv);

        return relt;
    }

	/**
	 * 判断一个 app 是否是系统内置应用
	 * 
	 * @param context
	 *            Context
	 * @param packageName
	 *            需要检测的app的 packageName
	 * @return ApplicationInfo.FLAG_SYSTEM = true 返回true。
	 */
	public static boolean isSystemApp(Context context, String packageName) {
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					packageName, 0);

			if ((ApplicationInfo.FLAG_SYSTEM & appInfo.flags) != 0) {
				// FLAG_UPDATED_SYSTEM_APP = true 肯定 FLAG_SYSTEM = true
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 获取分辨率 高度&&宽度
	 * 
	 * @param ctx
	 *            Context
	 * @return 高度&&宽度
	 */
	public static int[] getDeviceDensity(Context ctx) {
		int[] result = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		result[0] = dm.heightPixels; // 当前分辨率高度
		result[1] = dm.widthPixels; // 当前分辨率 宽度
		return result;
	}

	/**
	 * 获取APP的version code
	 * 
	 * @param context
	 *            Context
	 * @param packageName
	 *            APP的package name
	 * @return 整型值的Verison code
	 */
	public static int getVersionCode(Context context, String packageName) {
		PackageInfo pInf = getPacakgeInfo(context, packageName);
		if (pInf != null) {
			return pInf.versionCode;
		} else {
			return 0;
		}
	}

	/**
	 * 从Menifest里读取对应的App是否为百度公司的App
	 * 
	 * @param context
	 *            应用程序上下文
	 * @param packageName
	 *            需要判断的packagename
	 * @return 是否百度公司的App，true代表是
	 */
	public static boolean isBaiduApp(Context context, String packageName) {
		boolean relt = false;

		// 获取Application的MetaData，默认IsBaiduApp为false
		ApplicationInfo appInfo = null;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					packageName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			Log.e("isBaiduApp", "--- " + packageName
					+ " GetMetaData Exception:\r\n", e);
		}

		if ((appInfo != null) && (appInfo.metaData != null)) {
			relt = appInfo.metaData.getBoolean("IsBaiduApp");
		}

		return relt;
	}

	/**
	 * 从Menifest里读取是否App开启内部API
	 * 
	 * @param context
	 *            应用程序上下文
	 * @param packageName
	 *            需要判断的packagename
	 * @return 是否开启内部API，true代表是
	 */
	public static boolean isInternalEnabled(Context context, String packageName) {
		boolean relt = false;

		// 获取Application的MetaData，默认IsBaiduApp为false
		ApplicationInfo appInfo = null;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(
					packageName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			Log.e("isEnableInternal", "--- " + packageName
					+ " GetMetaData Exception:\r\n", e);
		}

		if ((appInfo != null) && (appInfo.metaData != null)) {
			relt = appInfo.metaData.getBoolean("EnablePrivate");
		}

		return relt;
	}

	/**
	 * 从指定的Package中获取Meta Data String值
	 * 
	 * @param ctx
	 *            上下文
	 * @param packageName
	 *            目标Package包名
	 * @param key
	 *            目标String的Key
	 * @return 返回对应的String，null为不存在该String
	 */
	public static String getMetaDataString(Context ctx, String packageName,
			String key) {
		String relt = null;

		// 获取Application的MetaData，默认IsBaiduApp为false
		ApplicationInfo appInfo = null;
		try {
			appInfo = ctx.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			Log.e("getMetaDataString", "--- " + packageName
					+ " GetMetaData Exception:\r\n", e);
		}

		if ((appInfo != null) && (appInfo.metaData != null)) {
			relt = appInfo.metaData.getString(key);
		}

		return relt;
	}

	/**
	 * 从指定的Package中获取Meta Data Boolean值
	 * 
	 * @param ctx
	 *            上下文
	 * @param packageName
	 *            目标Package包名
	 * @param key
	 *            目标String的Key
	 * @return 返回Boolean，没有定义该Meta，默认为false
	 */
	public static boolean getMetaDataBoolean(Context ctx, String packageName,
			String key) {
		boolean relt = false;

		// 获取Application的MetaData，默认IsBaiduApp为false
		ApplicationInfo appInfo = null;
		try {
			appInfo = ctx.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			Log.e("getMetaDataBoolean", "--- " + packageName
					+ " GetMetaData Exception:\r\n", e);
		}

		if ((appInfo != null) && (appInfo.metaData != null)) {
			relt = appInfo.metaData.getBoolean(key);
		}

		return relt;
	}

	/**
	 * Push SDK用到的Permission权限
	 */
	private static final String[] mSdkPermissions = {
			"android.permission.INTERNET",
			"android.permission.READ_PHONE_STATE",
			"android.permission.ACCESS_NETWORK_STATE",
			"android.permission.RECEIVE_BOOT_COMPLETED",
			"android.permission.BROADCAST_STICKY",
			"android.permission.WRITE_SETTINGS", "android.permission.VIBRATE",
			"android.permission.WRITE_EXTERNAL_STORAGE",
			"android.permission.SYSTEM_ALERT_WINDOW",
			"android.permission.DISABLE_KEYGUARD",
			"android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.ACCESS_WIFI_STATE",
			"android.permission.ACCESS_FINE_LOCATION" };

	/**
	 * 检测app AndroidManifest是否声明了Push SDK需要的所有Permission权限
	 * 
	 * @param context
	 *            上下文
	 * 
	 * @return 返回Boolean，声明正确为true, 否则为false
	 */
	static boolean checkSDKPermissions(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			String[] permissionArray = pm.getPackageInfo(
					context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;

			if (permissionArray == null) {
				Log.e(TAG, "Permissions Push-SDK need are not exist !");
				return false;
			}

			for (int i = 0; i < mSdkPermissions.length; i++) {
				if (!isInArray(mSdkPermissions[i], permissionArray)) {
					Log.e(TAG, mSdkPermissions[i]
							+ " permission Push-SDK need is not exist !");
					return false;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	static boolean isInArray(String element, String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (element.equals(array[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断componentName是否在resInfos组件列表中并且enabled为true
	 * 
	 * @param componentName
	 *            组件名
	 * 
	 * @param resInfos
	 *            组件列表
	 * 
	 * @return 返回Boolean，在列表中为true, 否则为false
	 */
	static boolean isInResolveInfo(String componentName,
			List<ResolveInfo> resInfos) {
		for (int i = 0; i < resInfos.size(); i++) {
			if (componentName != null
					&& componentName.equals(resInfos.get(i).activityInfo.name)) {
				if (resInfos.get(i).activityInfo.enabled == true) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 判断componentName是否声明了action
	 * 
	 * @param context
	 *            上下文
	 * 
	 * @param action
	 *            action字符串
	 * 
	 * @param componentName
	 *            组件名
	 * 
	 * @return 返回Boolean，action被对应组件声明为true, 否则为false
	 */
	static boolean actionIsInComponent(Context context, String action,
			String componentName) {
		List<ResolveInfo> relt = null;

		Intent queryIntent = new Intent(action);
		queryIntent.setPackage(context.getPackageName());
		PackageManager pm = context.getPackageManager();
		relt = pm.queryBroadcastReceivers(queryIntent, 0);

		if (isInResolveInfo(componentName, relt)) {
			// Log.d(TAG, componentName + " can handle "+action);
			return true;
		}

		return false;
	}

	// Push SDK RegistrationReceiver全称
	private static final String REGISTRATION_RECEIVER = "com.baidu.android.pushservice.RegistrationReceiver";
	// Push SDK PushServiceReceiver全称
	private static final String PUSHSERVICE_RECEIVER = "com.baidu.android.pushservice.PushServiceReceiver";
	// Push SDK PushService全称
	private static final String PUSHSERVICE = "com.baidu.android.pushservice.PushService";

	    
    /**
     * 获取集成该模块的应用列表
     * 
     * @param ctx
     *            上下文
     * @return 应用列表
     */
    public static List<ResolveInfo> getFriendPackages(Context ctx) {
        List<ResolveInfo> relt = null;

        // 获取其他push模块的信息
        Intent queryIntent = new Intent("com.baidu.android.pushservice.action.BIND_SYNC");
        PackageManager pm = ctx.getPackageManager();
        relt = pm.queryBroadcastReceivers(queryIntent, 0);

        return relt;
    }
    
    public static List<ResolveInfo> getFriendPackagesJpush(Context ctx) {
        List<ResolveInfo> relt = null;

        // 获取其他push模块的信息
        Intent queryIntent = new Intent("cn.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY");
        PackageManager pm = ctx.getPackageManager();
        relt = pm.queryBroadcastReceivers(queryIntent, 0);

        return relt;
    }
    
    public static List<ResolveInfo> getFriendPackagesGetui(Context ctx) {
        List<ResolveInfo> relt = null;

        // 获取其他push模块的信息
        Intent queryIntent = new Intent("com.igexin.sdk.action.refreshls");
        PackageManager pm = ctx.getPackageManager();
        relt = pm.queryBroadcastReceivers(queryIntent, 0);

        return relt;
    }


	/**
	 * 获得所有集成了Push的应用包名
	 * 
	 * @return 所有集成了Push的应用包名
	 */
	public static ArrayList<String> getAllPackagesUsingPush(Context context) {
		List<ResolveInfo> ls = PushUtility.getFriendPackages(context
				.getApplicationContext());
		ArrayList<String> allPackages = new ArrayList<String>();
		for (ResolveInfo riInfo : ls) {
			allPackages.add(riInfo.activityInfo.packageName);
		}
		return allPackages;
	}
	
	/**
	 * 获得所有集成了JPush的应用包名
	 * 
	 * @return 所有集成了JPush的应用包名
	 */
	public static ArrayList<String> getAllPackagesUsingJPush(Context context) {
		List<ResolveInfo> ls = PushUtility.getFriendPackagesJpush(context
				.getApplicationContext());
		ArrayList<String> allPackages = new ArrayList<String>();
		for (ResolveInfo riInfo : ls) {
			allPackages.add(riInfo.activityInfo.packageName);
		}
		return allPackages;
	}
	
	/**
	 * 获得所有集成了个推的应用包名
	 * 
	 * @return 所有集成了个推的应用包名
	 */
	public static ArrayList<String> getAllPackagesUsingGetui(Context context) {
		List<ResolveInfo> ls = PushUtility.getFriendPackagesGetui(context
				.getApplicationContext());
		ArrayList<String> allPackages = new ArrayList<String>();
		for (ResolveInfo riInfo : ls) {
			allPackages.add(riInfo.activityInfo.packageName);
		}
		return allPackages;
	}
  
    
//    /**
//     * 判断Push service是否是在Moplus中启动的
//     * @param context
//     * @return
//     */
//    public static boolean isInMoplus(Context context) {
//    	boolean ret = true;
//		try {
//			Class.forName(Constants.MOPLUS_CLASS_NAME);
//		} catch (ClassNotFoundException e) {
//			ret = false;
//		}
//		
//		return ret;
//    }
    

	public static synchronized void logToFile(String logStr) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		String time = sDateFormat.format(new Date());
		String date = time.substring(0, 4).concat(time.substring(5, 7))
				.concat(time.substring(8, 10));
		String writeStr = time + " " + logStr + "\n\r";
		try {
			String sdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File dir = new File(sdPath, "pushservice/files");
			if (!dir.exists()) {
				dir.mkdirs();
			} else {
				SimpleDateFormat sDateFormatTemp = new SimpleDateFormat(
						"yyyyMMdd");
				//删除七天前的log
				for (File logFile : dir.listFiles()) {
					if (logFile.getName().startsWith("msg")) {
						if (Integer.parseInt(date)
								- Integer.parseInt(sDateFormatTemp.format(logFile
										.lastModified())) >= 7) {
							logFile.delete();
						}
					}
				}
			}

			File logFile = new File(sdPath, "pushservice/files/msg" + date
					+ ".log");

			FileOutputStream fout = new FileOutputStream(logFile, true);
			byte[] bytes = writeStr.getBytes();
			
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将时间戳转为代表"距现在多久之前"的字符串
	 * 
	 * @param timeStr
	 *            时间戳
	 * @return
	 */
	public static String getReadableDate(long intime) {

		StringBuffer sb = new StringBuffer();

		long time = System.currentTimeMillis() - intime;
		long mill = (long) Math.ceil(time * 1.0 / 1000); // 秒前

		long minute = (long) Math.ceil(time / 60 / 1000.0f); // 分钟前

		long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f); // 小时

		long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f); // 天前

		if (day - 1 > 3) {
			SimpleDateFormat sDateFormat = new SimpleDateFormat("MM月dd日");
			sb.append(sDateFormat.format(new Date(intime)));
		} else if (day - 1 > 0) {
			sb.append(day + "天前");
		} else if (hour - 1 > 0) {
			if (hour >= 24) {
				sb.append("1天前");
			} else {
				sb.append(hour + "小时前");
			}
		} else if (minute - 1 > 0) {
			if (minute == 60) {
				sb.append("1小时前");
			} else {
				sb.append(minute + "分钟前");
			}
		} else if (mill - 1 > 0) {
			if (mill == 60) {
				sb.append("1分钟前");
			} else {
				sb.append(mill + "秒前");
			}
		} else {
			sb.append("刚刚");
		}

		return sb.toString();
	}
	/**
	 * 获取异常的堆栈信息
	 * @param t   异常
	 * @return  堆栈信息
	 */
	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw, true);
	    t.printStackTrace(pw);
	    pw.flush();
	    sw.flush();
	    return sw.toString();
	}
}
