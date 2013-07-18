package test.framework.java.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Adb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("main.");
		new Adb().run(args);
	}

	private void run(String[] cmd) {
		System.out.println("run in Adb.java");
		boolean connected = false;
		try {
			Socket socket = new Socket(InetAddress.getLocalHost(), 5037);
			connected = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (connected) {
			System.out.println("Connect to adb, success!");
		} else {
			System.out.println("error!!!");
			
		}
	}
}
