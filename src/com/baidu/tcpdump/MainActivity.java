package com.baidu.tcpdump;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import test.framework.java.utils.Device;
import test.framework.java.utils.FileUtils;
import test.framework.java.utils.Network;
import test.framework.java.utils.PushUtility;
import test.framework.java.utils.RootCmd;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String SHARED_URL = "http://pan.baidu.com/share/link?shareid=998428214&uk=1208163734";

	private static final String root = "/sdcard/zhangxin/result";

	public static String resultPath = "";

	private static String logPath = "";

	private TextView mLogView;

	private WakeLock wl;

	private Handler mHandler = new Handler();

	public long mStartTime;

	@Override
	protected void onResume() {
		super.onResume();
		// Network.getNetworkType(Network.checkNetworkType(getApplicationContext()));
		// Network.
	}

	@Override
	protected void onDestroy() {
		if (wl != null) {
			wl.release();
			wl = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("log", mLogView.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLogView = (TextView) findViewById(R.id.log);
		mLogView.setMovementMethod(ScrollingMovementMethod.getInstance());

		if (savedInstanceState != null)
			mLogView.setText(savedInstanceState.getString("log"));

		if (wl == null) {

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, "tcpdump");
			wl.acquire();
		}
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "onNewIntent:" + intent.toString());

		setIntent(intent);

		String cmd = intent.getStringExtra("cmd");
		if (TextUtils.equals(cmd, "stop_tcpdump")) {
			stopTcpdump(null);
		}

		String root = intent.getStringExtra("rooted");
		if (root != null) {
			log("rooted == " + root);
			if (root.equals("false")) {
				log("root 失败， 无法root，请重启手机！");
			}
		}
	}

	public void testSuit(View v) {

		mLogView.setText("");
		new Thread(new Run()).start();

	}

	public void tcpdump(View v) {

		// SystemKey.back(getApplicationContext());
		Intent service = new Intent();
		service.putExtra("dump", true);
		service.setClass(this, TcpdumpService.class);
		startService(service);
	}

	private void killTcpdump() {
		RootCmd.execRootCmd("busybox killall tcpdump");
		RootCmd.execRootCmd("busybox killall tcpdump");
		log("停止所有tcpdump进程...");
	}
	
	public void stopTcpdump(View v) {
		killTcpdump();
		
		new Thread(new Runnable() {

			@Override
			public void run() {

				log("流量统计...");
				traffic(true);

				log("电量统计...");
				powerMonitor();
				// stopPower();

				// if (view != null) {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String stopTime = dateFormat.format(new Date());
				long stopTimeInMils = System.currentTimeMillis();

				// Calendar c = Calendar.getInstance();
				// // c.setTimeZone(TimeZone.getTimeZone("GTM"));
				// c.setTimeInMillis(stopTimeInMils - mStartTime);
				//
				// DateFormat dateFormat2 = new
				// SimpleDateFormat("yyyy年mm月dd天HH小时mm分ss秒");
				// dateFormat2.setTimeZone(TimeZone.getTimeZone("GTM+00:00"));
				// String testTime = dateFormat2.format(c.getTime());
				long testTime = (stopTimeInMils - mStartTime) / 1000;
				long hour = testTime / 3600;
				long min = testTime % 3600 / 60;
				long second = testTime % 60;

				String s = String.format("%1$d时%2$02d分%3$02d秒", hour, min,
						second);

				Log.i(TAG, "停止测试！");
				log("停止测试, 并写入logfile文件！！！\n停止测试时间：" + stopTime + "\n测试持续时间："
						+ s);

				FileUtils.logToFile(mLogView.getText().toString(), logPath);
				// }

			}
		}).start();
	}

	private void sleep(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
	}

	private void log(final String log) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				mLogView.append(log + "\n");
				// mLogView.setText(mLogView.getText().toString() + log + "\n");
				//
				// mLogView.scrollTo(0, mLogView.getBottom());

				int height = mLogView.getHeight();
				int scrollY = mLogView.getScrollY();
				int lineHeight = mLogView.getLineHeight();
				int lineCount = mLogView.getLineCount();// 总行数
				/**
				 * textView不可见内容的高度，可以理解为偏移位移
				 */
//				int maxY = (mLogView.getLineCount() * mLogView.getLineHeight()
//						+ mLogView.getPaddingTop() + mLogView
//							.getPaddingBottom()) - mLogView.getHeight();

				double viewCount = Math.floor(height / lineHeight);// 可见区域最大显示多少行
				if (lineCount > viewCount) {// 总行数大于可见区域显示的行数时则滚动

					mLogView.scrollTo(0,
							(int) (lineHeight * (lineCount - viewCount)));

				} else if (scrollY != 0) {

					mLogView.scrollTo(0, 0);
				}

			}
		});
	}

	private boolean ensureDeviceRooted() {
		if (Device.isRooted()) {
			log("具有root权限的设备！");
			return true;
		} else {
			log("设备没有root或者在权限管理器里被拒绝授予root了！！！\n请点击【一键root】尝试获取临时root权限或在权限管理器授予root权限...");
			return false;
		}
	}
	
	private boolean ensureNetworkConnected() {
		if (Network.isConnected(getApplicationContext())) {
			String c = "网络已连接! 网络制式：" + Network.getType(getApplicationContext());
			log(c);
			return true;
		} else {
			log("网络未连接, 请联网后测试!");
			
			// 打开网络设置activity
			Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
//			ComponentName cm = new ComponentName("com.android.phone", "com.android.phone.Settings");
//			intent.setComponent(cm);
			// intent.setAction("android.intent.action.VIEW");
			startActivity(intent);

			return false;
		}
	}
	
	private boolean installBusybox() {
		try {
			String file = "busybox";
			FileUtils.SaveIncludedFileIntoFilesFolder(R.raw.busybox, file, getApplicationContext());
			String busybox = FileUtils.getFilesDir(getApplicationContext()).toString() + "/" + file;
			// 重新挂载/system分区使分区可写
			RootCmd.execRootCmd(busybox + " mount -o remount,rw /system");
			// copy busybox 到 /system/bin/busybox
			RootCmd.execRootCmd(busybox + " cat " + busybox + " > /system/bin/" + file);
			
			RootCmd.execRootCmd(busybox + " chmod 777 /system/bin/" + file);

//			RootCmd.execRootCmd("busybox --install /system/bin");

//			log("还原/system分区只读属性");
			RootCmd.execRootCmd("busybox mount -o remount,ro /system");
			log("安装busybox成功！");
			
			return true;
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			log("安装busybox失败！");
			return false;
		}
	}
	
	/**
	 * busybox 路径： /data/data/com.baidu.tcpdump/files/busybox
	 * @return
	 */
	private boolean isBusyboxExists() {
		File busybox = new File(FileUtils.getFilesDir(getApplicationContext()), "busybox");
		return busybox.exists();
	}
	
	private boolean ensureBusyboxInstalled() {
		if (!isBusyboxExists()) {
			return installBusybox();
		}
		
		return true;
	}
	
	private boolean isTcpdumpExists() {
		File busybox = new File(FileUtils.getFilesDir(getApplicationContext()), "tcpdump");
		return busybox.exists();
	}
	
	private boolean ensureTcpdumpInstalled() {
		if (!isTcpdumpExists()) {
			return installTcpdump();
		}
		
		return true;
	}
	
	private boolean installTcpdump() {
		try {
			String file = "tcpdump";
			FileUtils.SaveIncludedFileIntoFilesFolder(R.raw.tcpdump, file, getApplicationContext());
			String tcpdump = FileUtils.getFilesDir(getApplicationContext()).toString() + "/" + file;
			// 重新挂载/system分区使分区可写
			RootCmd.execRootCmd("busybox mount -o remount,rw /system");
			// copy busybox 到 /system/bin/busybox
			RootCmd.execRootCmd("busybox cat " + tcpdump + " > /system/bin/" + file);
			
			RootCmd.execRootCmd("busybox chmod 777 /system/bin/" + file);

//			log("还原/system分区只读属性");
			RootCmd.execRootCmd("busybox mount -o remount,ro /system");
			log("安装tcpdump成功！");
			
			return true;
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			log("安装tcpdump失败！无法测试");
			return false;
		}
	}
	
	private void uninstallAllApks() {
		log("卸载所有JPush、个推、baidupush应用...");
		ArrayList<String> pushes = PushUtility
				.getAllPackagesUsingPush(getApplicationContext());
		ArrayList<String> jpushes = PushUtility
				.getAllPackagesUsingJPush(getApplicationContext());
		ArrayList<String> getuies = PushUtility
				.getAllPackagesUsingGetui(getApplicationContext());

		ArrayList<String> all = new ArrayList<String>();
		all.addAll(pushes);
		all.addAll(jpushes);
		all.addAll(getuies);

		for (String apk : all) {
			RootCmd.execRootCmd("pm uninstall " + apk);
		}
		log("卸载完成!");
		
	}
	
	private void installAllApks() {
		log("开始安装JPush、个推、baidupush...");
		String[] apkToInstall = { "JPush.apk", "GexinSdkDemoActivity.apk",
				"PushDemo.apk" };
		// ArrayList<InputStream> apkPath = new ArrayList<InputStream>();
		String dstParent = "/sdcard/zhangxin/apk/";

		AssetManager assetMgr = getAssets();
		for (String apk : apkToInstall) {
			try {
				FileUtils.copyFile(assetMgr.open(apk), dstParent + apk);
			} catch (IOException e) {
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}

		// 2.2.2安装
		for (String apk : apkToInstall) {
			RootCmd.execRootCmd("pm install " + dstParent + apk);
		}
		log("安装完成!");
	}
	
	private void getDeviceInfo() {
		RootCmd.execRootCmd("getprop > " + resultPath + "device_info.txt");
		String c = "Network type: " + Network.getType(getApplicationContext());
		RootCmd.execRootCmd("echo >> " + resultPath + "device_info.txt");
		RootCmd.execRootCmd("echo " + c + " >> " + resultPath + "device_info.txt");
	}
	
	private void launchAllApps() {
		List<String> res = null;
		// 启动 个推
		log("开始启动个推...");
		res = RootCmd.execRootCmd("am start -n com.igexin.demo/.GexinSdkDemoActivity");

		sleep(20);
		RootCmd.execRootCmd("am start -n com.baidu.tcpdump/com.baidu.tcpdump.MainActivity");

		// 启动 百度
		log("开始启动baidupush demo...");
		RootCmd.execRootCmd("am start -n com.baidu.push.example/com.baidu.push.example.PushDemoActivity");
		sleep(10);

		// 启动 jpush
		log("开始启动jpush...");
		RootCmd.execRootCmd("am start -n zhangxin.push/com.example.jpushdemo.MainActivity");
		sleep(5);
		RootCmd.execRootCmd("am start -n com.baidu.tcpdump/com.baidu.tcpdump.MainActivity");

		RootCmd.execRootCmd("am start -n com.baidu.tcpdump/com.baidu.tcpdump.MainActivity");
	}
	
	private boolean ensureSocketConnected() {
		ArrayList<String> netstat = RootCmd
				.execRootCmd("busybox netstat -anpt | busybox awk '/5287|5224|5225|3000/'");

		boolean isAllConnected = true;
		boolean bpush = false;
		boolean jpush = false;
		boolean getui = false;

		int bpushConnectCount = 0;
		int jpushConnectCount = 0;
		int getuiConnectCount = 0;

		if (netstat.isEmpty()) {
			log("错误！长连接均未建立！！！");
			isAllConnected = false;
		} else {

			for (String item : netstat) {
				if (item.contains(":5287")) {
					if (item.contains("ESTABLISHED")) {
						bpush = true;
						bpushConnectCount++;
					}
				}

				if (item.contains(":3000")) {
					if (item.contains("ESTABLISHED")) {
						jpush = true;
						jpushConnectCount++;
					}
				}

				if (item.contains(":5224") || item.contains(":5225")) {
					if (item.contains("ESTABLISHED")) {
						getui = true;
						getuiConnectCount++;
					}
				}
			}

			if (bpush) {
				log("baidupush长连接建立成功！连接数 = " + bpushConnectCount);
			} else {
				log("错误！baidupush长连接未建立！");
			}

			if (jpush) {
				log("jpush长连接建立成功！连接数 = " + jpushConnectCount);
			} else {
				log("错误！jpush长连接未建立！");
			}

			if (getui) {
				log("个推长连接建立成功！连接数 = " + getuiConnectCount);
			} else {
				log("错误！个推长连接未建立！");
			}

			isAllConnected = bpush && jpush && getui;
		}

		if (!isAllConnected) {
			log("有未建立长连接的应用！\n请根据提示或者【查询长连接】打开对应应用重新绑定！\n确保长连接都存在， 然后点击【统计流量、电量】");
			return false;
		}

		if (bpushConnectCount == 1 && jpushConnectCount == 1
				&& getuiConnectCount == 1) {

		} else {
			log("长连接数目不正确, 这可能是bug！\n请点击重运行【一键测试】！\n确保长连接都==1!");
			clearResultPath();
			return false;
		}
		
		return true;
	}
	
	private class Run implements Runnable {
		public void run() {

			// 打印并记录测试开始时间
			mStartTime = System.currentTimeMillis();
			log("开始测试!!!\n开始时间：" + getDate());
						
			// 第1步：检查设备是否具有root权限， 要有su文件
			if (!ensureDeviceRooted()) {
				return;
			}

			// 第2步：检查网络是否可用
			if (!ensureNetworkConnected()) {
				return;
			}
			
			// 第3步：覆盖安装busybox 从 res/raw/busybox文件 到 /data/data/com.baidu.tcpdump/files/busybox -> /system/bin/busybox
			if (!ensureBusyboxInstalled()) {
				return;
			}
			
			// 第4步：覆盖安装tcpdump 从 res/raw/tcpdump文件 到 /data/data/com.baidu.tcpdump/files/tcpdump -> /system/bin/tcpdump
			if (!ensureTcpdumpInstalled()) {
				return;
			}
			
			// 第5步：kill all tcpdump processes
			killTcpdump();
			log("kill all processes of tcpdump");

			// 第6步：删除2个目录
			removeCacheDirs();
			log("删除/sdcard/zhangxin/apk & traffic目录");

			// 第7步：安装 JPush、个推、BPush（5分钟和10分钟2个版本）， 共4个版本
			// 1.卸载所有竞品
			uninstallAllApks();

			// 2.安装所有竞品
			installAllApks();

			// 第8步：创建结果收集目录
			initResultPath();
			log("创建结果收集目录：" + resultPath);
			initLogPath();
			log("创建log.txt，收集打印日志");

			// 第9步：收集设备信息
			getDeviceInfo();
			
			// 第10步：dump
			log("启动tcpdump...");
			tcpdump(null);

			// 第11步：启动所有竞品
			launchAllApps();

			// 第12步：确保竞品长连接存在且连接数 == 1
			log("等待建立长连接");
			sleep(10);
			if (!ensureSocketConnected()) {
				return;
			}

			// 第13步：开始统计流量&电量
			trafficAndPower(null);
		}
	}
	
	private String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String startTime = dateFormat.format(new Date());
		return startTime;
	}

	private void initResultPath() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String dir = dateFormat.format(new Date());
		resultPath = root + "/" + dir + "/";

		RootCmd.execRootCmd("busybox mkdir -p " + resultPath);
	}

	private void clearResultPath() {
		RootCmd.execCmd("busybox rm -rf " + resultPath);
	}

	private void initLogPath() {
		logPath = resultPath + "log.txt";
	}
	
	private void power() {
		// 处于睡眠态
		// KeyguardManager kgMgr = (KeyguardManager)
		// getSystemService(Context.KEYGUARD_SERVICE);
		//
		// boolean showing = kgMgr.inKeyguardRestrictedInputMode();
		// 安装PowerTutor
		AssetManager assetMgr = getAssets();

		String path = "/sdcard/zhangxin/apk/PowerTutor.apk";
		try {
			FileUtils.copyFile(assetMgr.open("PowerTutor.apk"), path);
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}

		RootCmd.execRootCmd("pm uninstall edu.umich.PowerTutor");
		RootCmd.execRootCmd("pm install -r " + path);

		powerMonitor();
	}

	private void removeCacheDirs() {
		RootCmd.execRootCmd("busybox rm -rf /sdcard/zhangxin/apk");
		RootCmd.execRootCmd("busybox rm -rf /sdcard/zhangxin/traffic");
	}

	private void stopPower() {
		RootCmd.execRootCmd("am force-stop edu.umich.PowerTutor");
	}

	private void powerMonitor() {
		String[] pkgs = { "com.baidu.push.example", "zhangxin.push",
				"com.igexin.demo" };

		RootCmd.execRootCmd("am startservice -a com.baidu.action.statistics.POWER");
		sleep(2);

		// clear logcat
		RootCmd.execRootCmd("logcat -c");

		for (String p : pkgs) {
			RootCmd.execRootCmd("am startservice -a com.baidu.action.statistics.POWER -e package "
					+ p);
			sleep(1);
		}

		// logcat -d | grep 'I/PowerUsage' | awk -F ':' '{print $5}' | awk
		// '{print $1}' | awk -F 'mAh' '{print $1}'

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				final String powerPath = resultPath + "power.txt";

				RootCmd.execRootCmd("echo [-----------------开始电量统计！--------------] >> " + powerPath);
				RootCmd.execRootCmd("date >> " + powerPath);
				RootCmd.execRootCmd("logcat -d -v time -s PowerUsage:I >> "	+ powerPath);

			}
		}, 1000);

	}

	private void traffic(boolean isAppend) {

		AssetManager assetMgr = getAssets();

		String bpush = "/sdcard/zhangxin/traffic/traffic_bdpush";
		String jpush = "/sdcard/zhangxin/traffic/traffic_jpush";
		String getui = "/sdcard/zhangxin/traffic/traffic_getui";
		try {
			FileUtils.copyFile(assetMgr.open("traffic_bdpush"), bpush);
			FileUtils.copyFile(assetMgr.open("traffic_jpush"), jpush);
			FileUtils.copyFile(assetMgr.open("traffic_getui"), getui);
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}

		final String token;
		if (isAppend) {
			token = " >> ";
		} else {
			token = " >> ";
		}

		String dstBpush = resultPath + "traffic_bdpush.log";
		String dstJpush = resultPath + "traffic_jpush.log";
		String dstGetui = resultPath + "traffic_getui.log";

		try {
			RootCmd.execRootCmd("date >> " + dstBpush);
			ArrayList<String> a = null;
			a = RootCmd.execRootCmd("cat " + bpush);
			a = RootCmd.execRootCmd(a.get(0));
			for (String i : a) {
				i = i.replace(";", ",");
				RootCmd.execRootCmd("echo " + i + "';'" + token + dstBpush);
			}
			RootCmd.execRootCmd("echo >> " + dstBpush);

			RootCmd.execRootCmd("date >> " + dstJpush);
			a = RootCmd.execRootCmd("cat " + jpush);
			a = RootCmd.execRootCmd(a.get(0));
			for (String i : a) {
				i = i.replace(";", ",");
				RootCmd.execRootCmd("echo " + i + " ';' " + token + dstJpush);
			}
			RootCmd.execRootCmd("echo >> " + dstJpush);

			RootCmd.execRootCmd("date >> " + dstGetui);
			a = RootCmd.execRootCmd("cat " + getui);
			a = RootCmd.execRootCmd(a.get(0));
			for (String i : a) {
				i = i.replace(";", ",");
				RootCmd.execRootCmd("echo " + i + " ';' " + token + dstGetui);
			}
			RootCmd.execRootCmd("echo >> " + dstGetui);
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
	}

	public void isConnected(View v) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<String> netstat = RootCmd
						.execRootCmd("busybox netstat -anpt | busybox awk '/5287|5224|5225|3000/'");

				String content = "";

				int bpushConnectCount = 0;
				int jpushConnectCount = 0;
				int getuiConnectCount = 0;

				if (netstat.isEmpty()) {
					content = "错误！长连接均未建立！！！";
				} else {
					boolean bpush = false;
					boolean jpush = false;
					boolean getui = false;

					for (String item : netstat) {
						if (item.contains(":5287")) {
							if (item.contains("ESTABLISHED")) {
								bpush = true;
								bpushConnectCount++;
							}
						}

						if (item.contains(":3000")) {
							if (item.contains("ESTABLISHED")) {
								jpush = true;
								jpushConnectCount++;
							}
						}

						if (item.contains(":5224") || item.contains(":5225")) {
							if (item.contains("ESTABLISHED")) {
								getui = true;
								getuiConnectCount++;
							}
						}
					}

					if (bpush) {
						content += "baidupush长连接已建立成功！连接数 = "
								+ bpushConnectCount + "\n";
					} else {
						content += "错误！baidupush长连接未建立！" + "\n";
					}

					if (jpush) {
						content += "jpush长连接已建立成功！连接数 = " + jpushConnectCount
								+ "\n";
					} else {
						content += "错误！jpush长连接未建立！" + "\n";
					}

					if (getui) {
						content += "个推长连接已建立成功！连接数 = " + getuiConnectCount
								+ "\n";
					} else {
						content += "错误！个推长连接未建立！" + "\n";
					}
				}

				final String log = content;
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), log,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).start();
	}

	public void root(View v) {
		Device device = new Device(getApplicationContext());
		device.dostuff();
	}

	public void trafficAndPower(View v) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// mLogView.setText("");
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String startTime = dateFormat.format(new Date());
				mStartTime = System.currentTimeMillis();

				log("开始流量、电量统计!!!\n开始时间：" + startTime);

				// 流量统计
				List<String> t = RootCmd
						.execRootCmd("ls /proc/net/xt_qtaguid/stats");
				if (t == null || t.isEmpty()) {
					log("/proc/net/xt_qtaguid/stats文件不存在， 无法使用系统级统计流量，采用tcpdump抓包分析统计流量!");
				} else {
					log("开始统计流量...");
					traffic(false);
				}

				// 电量统计
				log("开始统计电量...");
				power();

				log("测试套件启动完成！");
			}
		}).start();

	}

	public void update(View v) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(SHARED_URL);
		intent.setData(content_url);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();

		// 2.5返回home
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
		intent.addCategory(Intent.CATEGORY_HOME);
		this.startActivity(intent);
	}

}
