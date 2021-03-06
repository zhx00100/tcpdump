/*
Copyright (C) 2011 The University of Michigan

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Please send inquiries to powertutor@umich.edu
 */

package edu.umich.PowerTutor.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import edu.umich.PowerTutor.R;
import edu.umich.PowerTutor.ui.PowerTop;
import edu.umich.PowerTutor.ui.UMLogger;
import edu.umich.PowerTutor.util.BatteryStats;
import edu.umich.PowerTutor.util.Counter;
import edu.umich.PowerTutor.util.SystemInfo;

public class UMLoggerService extends Service {
	private static final String TAG = "UMLoggerService";

	private static final int NOTIFICATION_ID = 1;
	private static final int NOTIFICATION_ID_LETTER = 2;

	private Thread estimatorThread;
	private PowerEstimator powerEstimator;

	private Notification notification;

	private NotificationManager notificationManager;
	private TelephonyManager phoneManager;
	
	//added by zhangxin11
	private WakeLock mWl;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		powerEstimator = new PowerEstimator(this);

		/* Register to receive phone state messages. */
		phoneManager = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		phoneManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE
				| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
				| PhoneStateListener.LISTEN_SERVICE_STATE
				| PhoneStateListener.LISTEN_SIGNAL_STRENGTH);

		/* Register to receive airplane mode and battery low messages. */
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		registerReceiver(broadcastIntentReceiver, filter);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//added by zhangxin11
		
		if (mWl == null) {
			mWl = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PowerStatistics");
			mWl.acquire();
			Log.i(TAG, "mWl.acquire()");
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// android.os.Debug.startMethodTracing("pt.trace");

		if (intent.getBooleanExtra("stop", false)) {
			stopSelf();
			return;
		} else if (estimatorThread != null) {
			return;
		}
		showNotification();
		estimatorThread = new Thread(powerEstimator);
		estimatorThread.start();
	}

	// added by zhangxin11@baidu.com
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		
		String pkgName = intent.getStringExtra("package");
		String mask = intent.getStringExtra("mask");
		if (pkgName != null) {pkgName = pkgName.trim();
			printPowerByPkgName(pkgName, mask);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private String TAG_1 = "PowerUsage";
	/**
	 * 按package name获得并打印耗电量
	 * 
	 * @param pkgName
	 *            要获取耗电量的package name
	 * @param topIgnoreMask
	 *            过滤 LED/CPU/WIFI/3G/RADIO/SENSOR...
	 */
	private void printPowerByPkgName(String pkgName, String mask) {

		int topIgnoreMask = new TopIgnoreMaskFactory().build(mask);

		System.out.println(String.format("Package: %1$s; Mask:%2$s", pkgName,
				mask));

//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(this);

		// get total energy
		int keyId = PowerTop.KEY_TOTAL_ENERGY;// prefs.getInt("topKeyId",
												// PowerTop.KEY_TOTAL_ENERGY);

		try {
			byte[] rawUidInfo = mBinder
					.getUidInfo(Counter.WINDOW_TOTAL// all
							/*
							 * prefs.getInt("topWindowType",
							 * Counter.WINDOW_TOTAL)
							 */, mBinder.getNoUidMask() | /*
														 * prefs.getInt(
														 * "topIgnoreMask", 0)
														 */topIgnoreMask);

			if (rawUidInfo != null) {

				UidInfo[] uidInfos = (UidInfo[]) new ObjectInputStream(
						new ByteArrayInputStream(rawUidInfo)).readObject();

				double total = 0;

				for (UidInfo uidInfo : uidInfos) {

					if (uidInfo.uid == SystemInfo.AID_ALL) {
						continue;
					}

					switch (keyId) {
					case PowerTop.KEY_CURRENT_POWER:
						uidInfo.key = uidInfo.currentPower;
						uidInfo.unit = "W";
						break;
					case PowerTop.KEY_AVERAGE_POWER:
						uidInfo.key = uidInfo.totalEnergy
								/ (uidInfo.runtime == 0 ? 1 : uidInfo.runtime);
						uidInfo.unit = "W";
						break;
					case PowerTop.KEY_TOTAL_ENERGY:
						uidInfo.key = uidInfo.totalEnergy;
						uidInfo.unit = "J";
						break;
					default:
						uidInfo.key = uidInfo.currentPower;
						uidInfo.unit = "W";
					}

					total += uidInfo.key;
				}

				if (total == 0) {
					total = 1;
				}

				for (UidInfo uidInfo : uidInfos) {
					uidInfo.percentage = 100.0 * uidInfo.key / total;
				}

				Arrays.sort(uidInfos);

				PackageManager pm = getPackageManager();
				pkgName = pkgName.trim();
				for (UidInfo item : uidInfos) {
					// percentage < 0.1 的过滤
					// if (uidInfos[i].uid == SystemInfo.AID_ALL
					// || uidInfos[i].percentage < PowerTop.HIDE_UID_THRESHOLD)
					// {
					// continue;
					// }
					if (TextUtils.equals(pm.getNameForUid(item.uid), pkgName)) {

				        DecimalFormat localDecimalFormat1 = new DecimalFormat("#.##%");
				        double d9 = item.percentage / 100.0D;
				        String percentage = localDecimalFormat1.format(d9);
				        DecimalFormat localDecimalFormat2 = new DecimalFormat("#.##");
				        double d10 = item.key / 1000.0D;
				        String j = localDecimalFormat2.format(d10);
				        DecimalFormat localDecimalFormat3 = new DecimalFormat("#.##");
				        double d11 = item.key * 7.499999999999999E-005D;
				        String mAh = localDecimalFormat3.format(d11);
				        long secs = (long)Math.round(item.runtime);
				        String time = String.format("[%1$d:%2$02d:%3$02d]", secs / 60 / 60, (secs / 60) % 60, secs % 60);
				        
				        if (topIgnoreMask == TopIgnoreMaskFactory.IGNORE_MASK_ALL) {
				        	mask = "ALL:[ ";
				        	for (String i : mBinder.getComponents()) {
				        		mask += i + ", ";
				        	}
				        	mask = mask.substring(0, mask.length() - 2);
				        	mask += " ] ";
				        }
				        
				        String log = "[==package: " + pkgName + "==] mask: " + mask + " percentage: " + percentage + " power: " + mAh + "mAh (" + j + "J)" + " time: " + time;
				        
				        Log.i(TAG_1, log);
				        
				        long[] totals = mBinder.getTotals(item.uid, Counter.WINDOW_TOTAL);
				        String[] components = mBinder.getComponents();
				        
				        int length = totals.length;
				        
				        StringBuilder sb = new StringBuilder();
				        
				        double totalJ = 0;
				        double totalmAh = 0;
				        
				        for (int index = 0; index < length; index++) {
				        	double J = totals[index] / 1000.0D;
				        	double mah = totals[index] * 7.499999999999999E-005D;
				        	totalJ += J;
				        	totalmAh += mah;
				        	sb.append(components[index]).append(":").append(localDecimalFormat2.format(J)).append("J").append("---");
				        	sb.append(localDecimalFormat2.format(mah)).append("mAh").append("\n");
				        }
				        
						Log.i(TAG_1, sb.append("total:").append(localDecimalFormat2.format(totalmAh)).append("mAh").append(String.format(" (%1$sJ)", localDecimalFormat2.format(totalJ))).append("\n").toString());
						
						break;
					}

				}

			}
		} catch (IOException e) {
			Log.i(TAG_1, Log.getStackTraceString(e));
		} catch (RemoteException e) {
			Log.i(TAG_1, Log.getStackTraceString(e));
		} catch (ClassNotFoundException e) {
			Log.i(TAG_1, Log.getStackTraceString(e));
		} catch (ClassCastException e) {
			Log.i(TAG_1, Log.getStackTraceString(e));
		} catch (Exception e) {
			Log.i(TAG_1, Log.getStackTraceString(e));
		}

	}

	private final class TopIgnoreMaskFactory {
		// public String
		public static final String MASK_SPLIT = ",";

		// display all
		public static final int IGNORE_MASK_ALL = 0;// prefs.getInt("topIgnoreMask", 0);
		
		public int build(String mask) {
			
			if (TextUtils.isEmpty(mask)) {
				return IGNORE_MASK_ALL;
			}
			
			int resultMask = ~IGNORE_MASK_ALL;// | 1 << ;
			
			try {
				String[] ignoreMasks = TextUtils.split(mask.toUpperCase(), MASK_SPLIT);
				ArrayList<String> temp = new ArrayList<String>();
				for (String item : ignoreMasks) {
					if (TextUtils.isEmpty(item)) {
						continue;
					}

					temp.add(item.trim());
				}

				List<String> components = Arrays.asList(mBinder.getComponents());
				int noUidMask = mBinder.getNoUidMask();
				
				int index = -1;
//				resultMask = IGNORE_MASK_ALL | 1 << components.size();
				for (String item : temp) {
					if ((index = components.indexOf(item)) == -1) {
						continue;
					}

					if ((noUidMask & 1 << index) != 0) {
						continue;
					}

//					if ((IGNORE_MASK_ALL & 1 << index) == 0) {
//
//					} else {
						resultMask = resultMask & ~(1 << index);
//					}

				}

			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.i(TAG_1, Log.getStackTraceString(e));
			} catch (Exception e) {
				Log.i(TAG_1, Log.getStackTraceString(e));
			}

//			if (resultMask == -1) {
//				resultMask = IGNORE_MASK_ALL;
//			}
			
			return resultMask;
		}
	}

	@Override
	public void onDestroy() {
		//added by zhangxin11
		Log.i(TAG, "onDestroy");
		
		if (mWl != null) {
			mWl.release();
			Log.i(TAG, "mWl.release();");	
		}
		
		// android.os.Debug.stopMethodTracing();
		if (estimatorThread != null) {
			estimatorThread.interrupt();
			while (estimatorThread.isAlive()) {
				try {
					estimatorThread.join();
				} catch (InterruptedException e) {
				}
			}
		}
		unregisterReceiver(broadcastIntentReceiver);

		/*
		 * See comments in showNotification() for why we are using reflection
		 * here.
		 */
		boolean foregroundSet = false;
		try {
			Method stopForeground = getClass().getMethod("stopForeground",
					boolean.class);
			stopForeground.invoke(this, true);
			foregroundSet = true;
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchMethodException e) {
		}
		if (!foregroundSet) {
			setForeground(false);
			notificationManager.cancel(NOTIFICATION_ID);
		}

		super.onDestroy();
	};

	/** This function is to construct the real-time updating notification */
	public void showNotification() {
		int icon = R.drawable.level;

		// icon from resources
		CharSequence tickerText = "PowerTutor"; // ticker-text
		long when = System.currentTimeMillis(); // notification time
		Context context = getApplicationContext(); // application Context
		CharSequence contentTitle = "PowerTutor"; // expanded message title
		CharSequence contentText = ""; // expanded message text

		Intent notificationIntent = new Intent(this, UMLogger.class);
		notificationIntent.putExtra("isFromIcon", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		/*
		 * the next two lines initialize the Notification, using the
		 * configurations above.
		 */
		notification = new Notification(icon, tickerText, when);
		notification.iconLevel = 2;
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		/*
		 * We need to set the service to run in the foreground so that system
		 * won't try to destroy the power logging service except in the most
		 * critical situations (which should be fairly rare). Due to differences
		 * in apis across versions of android we have to use reflection. The
		 * newer api simultaneously sets an app to be in the foreground while
		 * adding a notification icon so services can't 'hide' in the
		 * foreground. In the new api the old call, setForeground, does nothing.
		 * See: http://developer.android.com/reference/android/app/Service.html#
		 * startForeground%28int,%20android.app.Notification%29
		 */
		boolean foregroundSet = false;
		try {
			Method startForeground = getClass().getMethod("startForeground",
					int.class, Notification.class);
			startForeground.invoke(this, NOTIFICATION_ID, notification);
			foregroundSet = true;
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchMethodException e) {
		}
		if (!foregroundSet) {
			setForeground(true);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	/*
	 * This function is to update the notification in real time. This function
	 * is apparently fairly expensive cpu wise. Updating once a second caused a
	 * 8% cpu utilization penalty.
	 */
	public void updateNotification(int level, double totalPower) {
		notification.icon = R.drawable.level;
		notification.iconLevel = level;

		// If we know how much charge the battery has left we'll override the
		// normal icon with one that indicates how much time the user can expect
		// left.
		BatteryStats bst = BatteryStats.getInstance();
		if (bst.hasCharge() && bst.hasVoltage()) {
			double charge = bst.getCharge();
			double volt = bst.getVoltage();
			if (charge > 0 && volt > 0) {
				notification.icon = R.drawable.time;

				double minutes = charge * volt / (totalPower / 1000) / 60;
				if (minutes < 55) {
					notification.iconLevel = 1 + (int) Math.max(0,
							Math.round(minutes / 10.0) - 1);
				} else {
					notification.iconLevel = (int) Math.min(13,
							6 + Math.max(0, Math.round(minutes / 60.0) - 1));
				}
			}
		}

		CharSequence contentTitle = "PowerTutor";
		CharSequence contentText = "Total Power: "
				+ (int) Math.round(totalPower) + " mW";

		/*
		 * When the user selects the notification the tab view for global power
		 * usage will appear.
		 */
		Intent notificationIntent = new Intent(this, UMLogger.class);
		notificationIntent.putExtra("isFromIcon", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, contentTitle, contentText,
				contentIntent);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private final ICounterService.Stub mBinder = new ICounterService.Stub() {
		public String[] getComponents() {
			return powerEstimator.getComponents();
		}

		public int[] getComponentsMaxPower() {
			return powerEstimator.getComponentsMaxPower();
		}

		public int getNoUidMask() {
			return powerEstimator.getNoUidMask();
		}

		public int[] getComponentHistory(int count, int componentId, int uid) {
			return powerEstimator.getComponentHistory(count, componentId, uid,
					-1);
		}

		public long[] getTotals(int uid, int windowType) {
			return powerEstimator.getTotals(uid, windowType);
		}

		public long getRuntime(int uid, int windowType) {
			return powerEstimator.getRuntime(uid, windowType);
		}

		public long[] getMeans(int uid, int windowType) {
			return powerEstimator.getMeans(uid, windowType);
		}

		public byte[] getUidInfo(int windowType, int ignoreMask) {
			UidInfo[] infos = powerEstimator.getUidInfo(windowType, ignoreMask);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				new ObjectOutputStream(output).writeObject(infos);
			} catch (IOException e) {
				return null;
			}
			for (UidInfo info : infos) {
				info.recycle();
			}
			return output.toByteArray();
		}

		public long getUidExtra(String name, int uid) {
			return powerEstimator.getUidExtra(name, uid);
		}
	};

	BroadcastReceiver broadcastIntentReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				Bundle extra = intent.getExtras();
				try {
					if ((Boolean) extra.get("state")) {
						powerEstimator.writeToLog("airplane-mode on\n");
					} else {
						powerEstimator.writeToLog("airplane-mode off\n");
					}
				} catch (ClassCastException e) {
					// Some people apparently are having this problem. I'm not
					// really
					// sure why this should happen.
					Log.w(TAG, "Couldn't determine airplane mode state");
				}
			} else if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
				powerEstimator.writeToLog("battery low\n");
			} else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				powerEstimator.writeToLog("battery-change "
						+ intent.getIntExtra("plugged", -1) + " "
						+ intent.getIntExtra("level", -1) + "/"
						+ intent.getIntExtra("scale", -1) + " "
						+ intent.getIntExtra("voltage", -1)
						+ intent.getIntExtra("temperature", -1) + "\n");
				powerEstimator.plug(intent.getIntExtra("plugged", -1) != 0);
			} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)
					|| intent.getAction()
							.equals(Intent.ACTION_PACKAGE_REPLACED)) {
				// A package has either been removed or its metadata has changed
				// and we
				// need to clear the cache of metadata for that app.
				SystemInfo.getInstance().voidUidCache(
						intent.getIntExtra(Intent.EXTRA_UID, -1));
			}
		};
	};

	PhoneStateListener phoneListener = new PhoneStateListener() {
		public void onServiceStateChanged(ServiceState serviceState) {
			switch (serviceState.getState()) {
			case ServiceState.STATE_EMERGENCY_ONLY:
				powerEstimator.writeToLog("phone-service emergency-only\n");
				break;
			case ServiceState.STATE_IN_SERVICE:
				powerEstimator.writeToLog("phone-service in-service\n");
				switch (phoneManager.getNetworkType()) {
				case (TelephonyManager.NETWORK_TYPE_EDGE):
					powerEstimator.writeToLog("phone-network edge\n");
					break;
				case (TelephonyManager.NETWORK_TYPE_GPRS):
					powerEstimator.writeToLog("phone-network GPRS\n");
					break;
				case 8:
					powerEstimator.writeToLog("phone-network HSDPA\n");
					break;
				case (TelephonyManager.NETWORK_TYPE_UMTS):
					powerEstimator.writeToLog("phone-network UMTS\n");
					break;
				default:
					powerEstimator.writeToLog("phone-network "
							+ phoneManager.getNetworkType() + "\n");
				}
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				powerEstimator.writeToLog("phone-service out-of-service\n");
				break;
			case ServiceState.STATE_POWER_OFF:
				powerEstimator.writeToLog("phone-service power-off\n");
				break;
			}
		}

		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				powerEstimator.writeToLog("phone-call idle\n");
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				powerEstimator.writeToLog("phone-call off-hook\n");
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				powerEstimator.writeToLog("phone-call ringing\n");
				break;
			}
		}

		public void onDataConnectionStateChanged(int state) {
			switch (state) {
			case TelephonyManager.DATA_DISCONNECTED:
				powerEstimator.writeToLog("data disconnected\n");
				break;
			case TelephonyManager.DATA_CONNECTING:
				powerEstimator.writeToLog("data connecting\n");
				break;
			case TelephonyManager.DATA_CONNECTED:
				powerEstimator.writeToLog("data connected\n");
				break;
			case TelephonyManager.DATA_SUSPENDED:
				powerEstimator.writeToLog("data suspended\n");
				break;
			}
		}

		public void onSignalStrengthChanged(int asu) {
			powerEstimator.writeToLog("signal " + asu + "\n");
		}
	};
}
