package test.framework.java.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class SystemKey {

	private static final String TAG = SystemKey.class.getSimpleName();
	
	public static void back(Context context) {
		
		AssetManager assetMgr = context.getAssets();
		InputStream is = null;
		try {
			is = assetMgr.open("back");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (is != null) {
			
			// 1.1.拷贝到sdcard里
			String dstPath = "/sdcard/zhangxin/systemkey/back";
			FileUtils.copyFile(is, dstPath);
			
			// 1.2.back key
			ArrayList<String> result = RootCmd.execRootCmd("ls /dev/input/");
			
			for (String item : result) {
				RootCmd.execRootCmd("cat " + dstPath + " > "
						+ "/dev/input/" + item);
			}
			
		} else {
			Log.e(TAG, "back: back key failed!");
		}
	}
}
