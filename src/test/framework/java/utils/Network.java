package test.framework.java.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import static test.framework.java.utils.Network.MobileType.*;

public class Network {

	private static final String TAG = Network.class.getSimpleName();

	/** Called when the activity is first created. */

	public static class APN {

		public static final Uri PREFERRED_APN_URI = Uri
				.parse("content://telephony/carriers/preferapn");

		public static final String CTWAP = "ctwap";
		public static final String CTNET = "ctnet";
		public static final String CMWAP = "cmwap";
		public static final String CMNET = "cmnet";
		public static final String NET_3G = "3gnet";
		public static final String WAP_3G = "3gwap";
		public static final String UNIWAP = "uniwap";
		public static final String UNINET = "uninet";
	}

	public static class MobileType {
		public static final int TYPE_CT_WAP = 5;
		public static final int TYPE_CT_NET = 6;
		public static final int TYPE_CT_WAP_2G = 7;
		public static final int TYPE_CT_NET_2G = 8;

		public static final int TYPE_CM_WAP = 9;
		public static final int TYPE_CM_NET = 10;
		public static final int TYPE_CM_WAP_2G = 11;
		public static final int TYPE_CM_NET_2G = 12;

		public static final int TYPE_CU_WAP = 13;
		public static final int TYPE_CU_NET = 14;
		public static final int TYPE_CU_WAP_2G = 15;
		public static final int TYPE_CU_NET_2G = 16;

		public static final int TYPE_OTHER = 17;

		/** 没有网络 */
		public static final int TYPE_NET_WORK_DISABLED = 0;

		/** wifi网络 */
		public static final int TYPE_WIFI = 4;

		/** 移动网络 */
		public static final int TYPE_MOBILE = 3;

		/** 未知错误 */
		public static final int TYPE_ERROR = -1;

	}

	public static String getNetworkType(int checkNetworkType) {
		final String networkType;
		
		switch (checkNetworkType) {
		case TYPE_WIFI:
			Log.i(TAG, "================wifi");
			networkType = "wifi";
			break;
		case TYPE_NET_WORK_DISABLED:
			Log.i(TAG, "================no network");
			networkType = "no network";
			break;
		case TYPE_CT_WAP:
			Log.i(TAG, "================ctwap");
			networkType = "ctwap";
			break;
		case TYPE_CT_WAP_2G:
			Log.i(TAG, "================ctwap_2g");
			networkType = "ctwap_2g";
			break;
		case TYPE_CT_NET:
			Log.i(TAG, "================ctnet");
			networkType = "ctnet";
			break;
		case TYPE_CT_NET_2G:
			Log.i(TAG, "================ctnet_2g");
			networkType = "ctnet_2g";
			break;
		case TYPE_CM_WAP:
			Log.i(TAG, "================cmwap");
			networkType = "cmwap";
			break;
		case TYPE_CM_WAP_2G:
			Log.i(TAG, "================cmwap_2g");
			networkType = "cmwap_2g";
			break;
		case TYPE_CM_NET:
			Log.i(TAG, "================cmnet");
			networkType = "cmnet";
			break;
		case TYPE_CM_NET_2G:
			Log.i(TAG, "================cmnet_2g");
			networkType = "cmnet_2g";
			break;
		case TYPE_CU_NET:
			Log.i(TAG, "================cunet");
			networkType = "cunet";
			break;
		case TYPE_CU_NET_2G:
			Log.i(TAG, "================cunet_2g");
			networkType = "cunet_2g";
			break;
		case TYPE_CU_WAP:
			Log.i(TAG, "================cuwap");
			networkType = "cuwap";
			break;
		case TYPE_CU_WAP_2G:
			Log.i(TAG, "================cuwap_2g");
			networkType = "Wifi";
			break;
		case TYPE_OTHER:
			Log.i(TAG, "================other");
			networkType = "other";
			break;
		default:
			networkType = "未识别的APN";
			break;
		}
		
		return networkType;
	}

	public static boolean isConnected(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mobNetInfoActivity = connectivityManager
				.getActiveNetworkInfo();

		if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
			return false;
		}
		
		return true;
	}
	
	public static int isWifiOrMobile(Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mobNetInfoActivity = connectivityManager
				.getActiveNetworkInfo();

		if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
			// 注意一：
			// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
			// 但是有些电信机器，仍可以正常联网，
			// 所以当成net网络处理依然尝试连接网络。
			// （然后在socket中捕捉异常，进行二次判断与用户提示）。
			return TYPE_NET_WORK_DISABLED;
		}

		// NetworkInfo不为null开始判断是网络类型
		int netType = mobNetInfoActivity.getType();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			// wifi net处理
			return TYPE_WIFI;
		} else if (netType == ConnectivityManager.TYPE_MOBILE) {
			return TYPE_MOBILE;
		}

		return TYPE_ERROR;
	}

	/***
	 * 判断Network具体类型（联通移动wap，电信wap，其他net）
	 * 
	 * */
	public static int checkNetworkType(Context context) {
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo mobNetInfoActivity = connectivityManager
					.getActiveNetworkInfo();

			String typeName = mobNetInfoActivity.getTypeName();
			Log.i(TAG, typeName);

			if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
				// 注意一：
				// NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
				// 但是有些电信机器，仍可以正常联网，
				// 所以当成net网络处理依然尝试连接网络。
				// （然后在socket中捕捉异常，进行二次判断与用户提示）。
				return TYPE_NET_WORK_DISABLED;
			} else {
				// NetworkInfo不为null开始判断是网络类型
				int netType = mobNetInfoActivity.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					// wifi net处理
					return TYPE_WIFI;
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					// 注意二：
					// 判断是否电信wap:
					// 不要通过getExtraInfo获取接入点名称来判断类型，
					// 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
					// 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
					// 所以可以通过这个进行判断！

					boolean is3G = isFastMobileNetwork(context);

					final Cursor c = context.getContentResolver().query(
							APN.PREFERRED_APN_URI, null, null, null, null);
					if (c != null) {
						c.moveToFirst();
						final String user = c.getString(c
								.getColumnIndex("user"));
						if (!TextUtils.isEmpty(user)) {
							if (user.startsWith(APN.CTWAP)) {
								return is3G ? TYPE_CT_WAP : TYPE_CT_WAP_2G;
							} else if (user.startsWith(APN.CTNET)) {
								return is3G ? TYPE_CT_NET : TYPE_CT_NET_2G;
							}
						}
					}
					c.close();

					// 注意三：
					// 判断是移动联通wap:
					// 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip
					// 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在
					// 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...
					// 所以采用getExtraInfo获取接入点名字进行判断

					String netMode = mobNetInfoActivity.getExtraInfo();
					Log.i("", "==================netmode:" + netMode);
					if (netMode != null) {
						// 通过apn名称判断是否是联通和移动wap
						netMode = netMode.toLowerCase();

						if (netMode.equals(APN.CMWAP)) {
							return is3G ? TYPE_CM_WAP : TYPE_CM_WAP_2G;
						} else if (netMode.equals(APN.CMNET)) {
							return is3G ? TYPE_CM_NET : TYPE_CM_NET_2G;
						} else if (netMode.equals(APN.NET_3G)
								|| netMode.equals(APN.UNINET)) {
							return is3G ? TYPE_CU_NET : TYPE_CU_NET_2G;
						} else if (netMode.equals(APN.WAP_3G)
								|| netMode.equals(APN.UNIWAP)) {
							return is3G ? TYPE_CU_WAP : TYPE_CU_WAP_2G;
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return TYPE_OTHER;
		}

		return TYPE_OTHER;

	}

	public static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return true; // ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return true; // ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return true; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true; // ~ 10+ Mbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;

		}
	}
	
	/**
	 * 打开/关闭 wifi
	 */
	public static void enableWifi(boolean enable) {
		if (enable) {
			openWifi();
		} else {
			closeWifi();
		}
	}
	
	/**
	 * 打开/关闭 移动数据
	 */
	public static void enableData(boolean enable) {
		if (enable) {
			openData();
		} else {
			closeData();
		}
	}
	
	/**
	 * 打开/关闭 网络连接
	 */
	public static void enableNetwork(boolean enable) {
		if (enable) {
			enableWifi(true);
			enableData(true);
		} else {
			enableWifi(false);
			enableData(false);
		}
	}
	
	/**
	 * 设置wifi优于移动数据
	 */
	public static void setWifiPrefer() {
//		Settings.Secure.putInt(cr, name, value)
		RootCmd.execRootCmd("svc wifi prefer");	
	}
	
	/**
	 * 设置移动数据优于wifi
	 */
	public static void setDataPrefer() {
		RootCmd.execRootCmd("svc data prefer");	
	}
	
	private static void openWifi() {
		RootCmd.execRootCmd("svc wifi enable");
	}
	
	private static void closeWifi() {
		RootCmd.execRootCmd("svc wifi disable");
	}
	
	private static void openData() {
		RootCmd.execRootCmd("svc data enable");
	}
	
	private static void closeData() {
		RootCmd.execRootCmd("svc data disable");
	}

}
