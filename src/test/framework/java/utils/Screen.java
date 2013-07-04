package test.framework.java.utils;

public class Screen {

	public static void takeShot(String path) {
		RootCmd.execRootCmd("screenshot " + path);
	}
	
	public static void takeCapture(String path) {
		RootCmd.execRootCmd("screencap " + path);
	}
}
