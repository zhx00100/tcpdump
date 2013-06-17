package com.baidu.tcpdump;

import test.framework.java.utils.Device;
import test.framework.java.utils.Device.temprootlistener;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.PowerManager.WakeLock;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class RootService extends Service {

	private static final String TAG = RootService.class.getSimpleName();
	
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
		// TODO Auto-generated method stub
		Log.i(TAG, "onstart");
		dotemproot();
		return super.onStartCommand(intent, flags, startId);
	}

	private void dotemproot() {
		Log.i(TAG, "dotemproot");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "z4root");
		
		Toast.makeText(RootService.this, "尝试获取临时root权限，持续约20秒!请不要做其他操作！", Toast.LENGTH_LONG).show();
		
		Log.i(TAG, "dotemproot1");
		
		final Handler handler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Log.i(TAG, "handleMessage");
				if (msg.what == 1) {
					Log.i(TAG, "handleMessage what == 1, rooted==" + msg.obj);
					Intent activity = new Intent();
					activity.setClass(getApplicationContext(), MainActivity.class);
					activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.putExtra("rooted", msg.obj.toString());
					startActivity(activity);
					Toast.makeText(RootService.this, "rooted == " + msg.obj + "!", Toast.LENGTH_LONG).show();
				}
					
				return false;
			}
		});
		
		Log.i(TAG, "dotemproot sleep 5s");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "dotemproot week");
		// TODO Auto-generated method stub
		Device device = new Device(getApplicationContext());
		device.dotemproot(new temprootlistener() {
			
			@Override
			public void onTempRootCompleted(boolean rooted) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onTempRootCompleted");
				Message msg = Message.obtain();
				msg.what = 1;
				msg.obj = rooted;
				handler.sendMessage(msg);
			}
		});
//		device.dopermroot(null);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
