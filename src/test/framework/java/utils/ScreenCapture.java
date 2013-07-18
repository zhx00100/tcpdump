package test.framework.java.utils;

import java.util.ArrayList;

import com.baidu.tcpdump.MainActivity;

public class ScreenCapture {

	public static void takeShot(String path) {
		RootCmd.execRootCmd("screenshot " + path);
	}
	
	public static void takeCapture(String path) {
		RootCmd.execRootCmd("screencap " + path);
	}
	
	public static boolean hasScreencap() {
		ArrayList<String> ret = RootCmd.execRootCmd(MainActivity.mBusybox + " which screencap");
		
		return ret != null && !ret.isEmpty() && ret.get(0).contains("screencap");
	}
}
