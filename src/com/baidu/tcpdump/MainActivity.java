package com.baidu.tcpdump;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import test.framework.java.utils.Device;
import test.framework.java.utils.FileUtils;
import test.framework.java.utils.PushUtility;
import test.framework.java.utils.RootCmd;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.GestureDetector;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final String root = "/sdcard/zhangxin/result";
	
	public static String resultPath = "";
	
	private TextView mLogView;

	private WakeLock wl;
	
	private Handler mHandler = new Handler();

	@Override
	protected void onResume() {
		super.onResume();
		wl.acquire();
	}

	@Override
	protected void onDestroy() {
		wl.release();
		wl = null;
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
			wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, "tcpdump");
		}
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

		new Thread(new Run()).start();

	}

	public void tcpdump(View v) {

		// SystemKey.back(getApplicationContext());
		Intent service = new Intent();
		service.putExtra("dump", true);
		service.setClass(this, TcpdumpService.class);
		startService(service);
	}

	public void stopTcpdump(View v) {
		final View view = v;
		new Thread(new Runnable() {

			@Override
			public void run() {
				RootCmd.execRootCmd("busybox killall tcpdump");
				log("停止所有tcpdump进程...");
				
				traffic(true);
				log("流量统计...");
				
				powerMonitor();
				log("电量统计...");
				// stopPower();
				
				runOnUiThread(new Runnable() {
					public void run() {
						if (view != null) {
							Log.i(TAG, "停止测试！");
							log("停止测试！！！");
						}
					}
				});
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

				mLogView.setText(mLogView.getText().toString() + log + "\n");
				//
				// mLogView.scrollTo(0, mLogView.getBottom());
			}
		});
	}

	private class Run implements Runnable {
		public void run() {
			// check root permission
			if (PushUtility.isRooted(getApplicationContext())) {
				log("具有root权限的设备！");
			} else {
				log("设备没有root！！！尝试获取临时root权限...");
				log("获取成功后， 请重新运行【一键测试】");
				log("root过程约持续60秒！");
				runOnUiThread(new Runnable() {
					public void run() {
						root(null);
					}
				});

				return;
			}

			AssetManager assetMgr = getAssets();

			// ArrayList<String> b = RootCmd.execRootCmd("busybox");
			// if (!b.isEmpty() // 存在busybox， 则不安装
			// && TextUtils.equals(b.get(0).toLowerCase(), "busybox")) {
			//
			// log("设备中有busybox！");
			// } else {
			// log("安装busybox...");
			//
			// try {
			// String path = "/sdcard/zhangxin/busybox/busybox";
			// FileUtils.copyFile(assetMgr.open("busybox"), path);
			//
			// // remount /system
			// // "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system"
			// RootCmd.execRootCmd("busybox mount -o remount,rw /system");
			//
			// // cp busybox /system/bin
			// RootCmd.execRootCmd("cat " + path + " > "
			// + "/system/bin/busybox");
			// RootCmd.execRootCmd("chmod 777 /system/bin/busybox");
			//
			// RootCmd.execRootCmd("busybox --install /system/bin");
			//
			// log("安装busybox成功！");
			// } catch (IOException e1) {
			// 
			// e1.printStackTrace();
			// }
			// }

			// 1.第一步：安装tcpdump
			ArrayList<String> r = RootCmd.execRootCmd("ls /data/local/tcpdump");
			if (r.isEmpty()) {
				// 0.清除tcpdump
				log("kill all processes of tcpdump");
				stopTcpdump(null);

				log("开始安装tcpdump...");

				InputStream is = null;
				try {
					is = assetMgr.open("tcpdump");
				} catch (IOException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}

				if (is != null) {
					// 1.1.拷贝到sdcard里
					String dstPath = "/sdcard/zhangxin/tcpdump/tcpdump";
					FileUtils.copyFile(is, dstPath);

					// 1.2.root权限copy到/data/local下, 使用cat命令, 防止有些手机没有cp命令
					RootCmd.execRootCmd("cat " + dstPath + " > "
							+ "/data/local/tcpdump");
					RootCmd.execRootCmd("chmod 777 /data/local/tcpdump");
					log("安装tcpdump成功！");
				} else {
					Log.e(TAG, "testSuit: copy file failed!");

					log("安装tcpdump失败！无法测试");
					return;
				}
			} else {

			}

			// 2.安装 JPush、个推、BPush（5分钟和10分钟2个版本）， 共4个版本

			// 2.1先卸载所有
			log("开始卸载所有JPush、个推、baidupush应用...");
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

			// 2.2.1copy apks to sdcard
			log("开始安装JPush、个推、baidupush...");
			String[] apkToInstall = { "JPush.apk", "GexinSdkDemoActivity.apk",
					"PushDemo.apk" };
//			ArrayList<InputStream> apkPath = new ArrayList<InputStream>();
			String dstParent = "/sdcard/zhangxin/apk/";

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

			// 创建结果收集目录
			initResultPath();
			log("创建结果收集目录：" + resultPath);
			
//			final String resultPath = "/sdcard/zhangxin/result";
			RootCmd.execRootCmd("busybox mkdir -p " + resultPath);

			// 收集设备信息
			RootCmd.execRootCmd("getprop > " + resultPath + "device_info.txt");

			// dump
			log("启动tcpdump...");
			tcpdump(null);

			// 2.3启动

			// 启动 个推
			log("开始启动个推...");
			RootCmd.execRootCmd("am start -n com.igexin.demo/.GexinSdkDemoActivity");
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
			// Intent intent = new Intent(Intent.ACTION_MAIN);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
			// intent.addCategory(Intent.CATEGORY_HOME);
			// startActivity(intent);

			RootCmd.execRootCmd("am start -n com.baidu.tcpdump/com.baidu.tcpdump.MainActivity");

			// close AssetManager
			// assetMgr.close();

			// 检查socket是否存在
			log("等待建立长连接");
			sleep(10);
			ArrayList<String> netstat = RootCmd
					.execRootCmd("busybox netstat -anpt | busybox awk '/5287|5224|5225|3000/'");

			boolean isAllConnected = true;
			if (netstat.isEmpty()) {
				log("错误！长连接均未建立！！！");
				isAllConnected = false;
			} else {
				boolean bpush = false;
				boolean jpush = false;
				boolean getui = false;

				for (String item : netstat) {
					if (item.contains(":5287")) {
						if (item.contains("ESTABLISHED")) {
							bpush = true;
						}
					}

					if (item.contains(":3000")) {
						if (item.contains("ESTABLISHED")) {
							jpush = true;
						}
					}

					if (item.contains(":5224") || item.contains(":5225")) {
						if (item.contains("ESTABLISHED")) {
							getui = true;
						}
					}
				}

				if (bpush) {
					log("baidupush长连接建立成功！");
				} else {
					log("错误！baidupush长连接未建立！");
				}

				if (jpush) {
					log("jpush长连接建立成功！");
				} else {
					log("错误！jpush长连接未建立！");
				}

				if (getui) {
					log("个推长连接建立成功！");
				} else {
					log("错误！个推长连接未建立！");
				}

				isAllConnected = bpush && jpush && getui;
			}

			if (!isAllConnected) {
				log("有未建立长连接的应用！\n请根据提示或者【查询长连接】打开对应应用重新绑定！\n确保长连接都存在， 然后点击【统计流量、电量】");
				return;
			}

			// 流量统计
			log("开始统计流量...");
			traffic(false);

			// 电量统计
			log("开始统计电量...");
			power();

			log("测试套件启动完成！");
			// busybox netstat -anpt | egrep "5287|3000|5224|5225"
			// //（5287百度、3000Jpush、5224 5225个推）

			// back
			// SystemKey.back(getApplicationContext());

			// Intent intent = new Intent();
			// intent.setClass(this, MainActivity.class);
			// startActivity(intent);

			// RootCmd.execRootCmd("am start -n com.baidu.tcpdump/com.baidu.tcpdump.MainActivity");

			// RootCmd.execRootCmd("am instrument -w -e class com.baidu.tcpdump.test.NotepadActivityTest#testBack com.baidu.tcpdump.test/android.test.InstrumentationTestRunner");

			// 2.5返回home
			// Intent intent = new Intent(Intent.ACTION_MAIN);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
			// intent.addCategory(Intent.CATEGORY_HOME);
			// this.startActivity(intent);

			// Intent jpush = new Intent();
			// jpush.setClassName(this, "com.example.jpushdemo.MainActivity");
			// startActivity(jpush);
		}

		private void initResultPath() {		
		    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		    String dir = dateFormat.format(new Date());
			resultPath = root + "/" + dir + "/";
		}

	}

	private void power() {

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

	private void stopPower() {
		RootCmd.execRootCmd("am force-stop edu.umich.PowerTutor");
	}

	private void powerMonitor() {
		String[] pkgs = { "com.baidu.push.example", "zhangxin.push",
				"com.igexin.demo" };

		RootCmd.execRootCmd("am startservice -a com.baidu.action.statistics.POWER");

		for (String p : pkgs) {
			RootCmd.execRootCmd("am startservice -a com.baidu.action.statistics.POWER -e package "
					+ p);
		}

		// logcat -d | grep 'I/PowerUsage' | awk -F ':' '{print $5}' | awk
		// '{print $1}' | awk -F 'mAh' '{print $1}'
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				final String powerPath = resultPath + "power.txt";
				RootCmd.execRootCmd("logcat -d -v time -s PowerUsage:I >> " + powerPath);
				RootCmd.execRootCmd("date >> " + powerPath);
			}
		}, 2000);
		
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
			token = " > ";
		}

		String dstBpush = resultPath + "traffic_bdpush.log";
		String dstJpush = resultPath + "traffic_jpush.log";
		String dstGetui = resultPath + "traffic_getui.log";
		ArrayList<String> a = null;
		a = RootCmd.execRootCmd("cat " + bpush);
		a = RootCmd.execRootCmd(a.get(0));
		for (String i : a) {
			i = i.replace(";", ",");
			RootCmd.execRootCmd("echo " + i + "';'" + token + dstBpush);
		}
		RootCmd.execRootCmd("date >> " + dstBpush);

		a = RootCmd.execRootCmd("cat " + jpush);
		a = RootCmd.execRootCmd(a.get(0));
		for (String i : a) {
			i = i.replace(";", ",");
			RootCmd.execRootCmd("echo " + i + " ';' " + token + dstJpush);
		}
		RootCmd.execRootCmd("date >> " + dstJpush);

		a = RootCmd.execRootCmd("cat " + getui);
		a = RootCmd.execRootCmd(a.get(0));
		for (String i : a) {
			i = i.replace(";", ",");
			RootCmd.execRootCmd("echo " + i + " ';' " + token + dstGetui);
		}
		RootCmd.execRootCmd("date >> " + dstGetui);
	}

	public void isConnected(View v) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ArrayList<String> netstat = RootCmd
						.execRootCmd("busybox netstat -anpt | busybox awk '/5287|5224|5225|3000/'");

				String content = "";

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
							}
						}

						if (item.contains(":3000")) {
							if (item.contains("ESTABLISHED")) {
								jpush = true;
							}
						}

						if (item.contains(":5224") || item.contains(":5225")) {
							if (item.contains("ESTABLISHED")) {
								getui = true;
							}
						}
					}

					if (bpush) {
						content += "baidupush长连接已建立成功！" + "\n";
					} else {
						content += "错误！baidupush长连接未建立！" + "\n";
					}

					if (jpush) {
						content += "jpush长连接已建立成功！" + "\n";
					} else {
						content += "错误！jpush长连接未建立！" + "\n";
					}

					if (getui) {
						content += "个推长连接已建立成功！" + "\n";
					} else {
						content += "错误！个推长连接未建立！" + "\n";
					}
				}

				final String log = content;
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), log, Toast.LENGTH_SHORT).show();
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
				// 流量统计
				log("开始统计流量...");
				traffic(false);

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
		Uri content_url = Uri
				.parse("http://pan.baidu.com/share/link?shareid=2445760335&uk=1208163734#dir/path=%2Fpushtest");
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