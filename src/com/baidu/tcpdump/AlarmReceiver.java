package com.baidu.tcpdump;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	private static final String TAG = AlarmReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		// try {
		// Intent newIntent = new Intent(context, Phase2.class);
		// newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// context.startActivity(newIntent);
		// } catch (Exception e) {
		// Toast.makeText(context,
		// "There was an error somewhere, but we still received an alarm",
		// Toast.LENGTH_SHORT).show();
		// e.printStackTrace();
		//
		// }
		
		Log.i(TAG, "alarmreceiver enter!!!!!!!!!!");
		String action = intent.getAction();
		if (!action.equals("com.baidu.tcpdump.Action.rootalarm")) {
			return;
		}
//		Toast.makeText(
//				context,
//				"There was an error somewhere, but we still received an alarm",
//				Toast.LENGTH_SHORT).show();
		
		try {
			Log.i(TAG, "alarmreceiver~~~~~~~~~~~~~~~~~~~");
			Intent service = new Intent(context, RootService.class);
//			service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(service);
		} catch (Exception e) {
			Toast.makeText(
					context,
					"There was an error somewhere, but we still received an alarm",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();

		} 
	}

}
