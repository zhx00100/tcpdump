package com.baidu.tcpdump;

import java.io.IOException;

import test.framework.java.utils.RootCmd;
import test.framework.java.utils.ShellCommandInvoke;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TcpdumpService extends Service {

	private static final String TAG = TcpdumpService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		//
		if (intent == null) {
			intent = new Intent();
			Log.i(TAG, "onStartCommand: intent == null");
		}

		if (intent.getBooleanExtra("dump", false)) {
			new Thread(new Runnable() {

				@Override
				public void run() {

					dump();

				}
			}).start();
		}

		return START_STICKY;
	}

	private void dump() {

		// RootCmd.execRootCmd("mkdir -p /sdcard/zhangxin/tcpdump/aaa");

		final String tcpdump = "/data/local/tcpdump";
		
		final String cmd = "%1s -i any -w " + MainActivity.resultPath + "/%2s %3s &";
		
		new Thread() {

			@Override
			public void run() {
				RootCmd.execRootCmd(String.format(cmd, tcpdump, "jpush.pcap", "port 3000"));
			}
		}.start();

		new Thread() {

			@Override
			public void run() {
				RootCmd.execRootCmd(String.format(cmd, tcpdump, "getui.pcap", "port 5224 or port 5225"));
			}
		}.start();

		new Thread() {

			@Override
			public void run() {
				RootCmd.execRootCmd(String.format(cmd, tcpdump, "bpush.pcap", "port 5287"));
			}
		}.start();
		
		new Thread() {

			@Override
			public void run() {
				RootCmd.execRootCmd(String.format(cmd, tcpdump, "all.pcap", ""));
			}
			
		}.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
