package test.framework.java.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.util.Log;

public final class ShellCommandInvoke {
	private ShellCommandInvoke() {}
	
	/**
	 * 执行linux shell命令
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static final ArrayList<String> shell(String shellCmd) throws IOException, InterruptedException {

		Process process = Runtime.getRuntime().exec(shellCmd);
		InputStreamReader is = new InputStreamReader(process.getInputStream());
		LineNumberReader input = new LineNumberReader(is);
		
		ArrayList<String> result = new ArrayList<String>();
		
		
		String output = null;
		
		while ((output = input.readLine()) != null) {
			result.add(output);
		}
		
		int exitValue = -1;
		
		try {
			exitValue = process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (exitValue == 0) {
			System.out.println("Command succeed! : " + shellCmd);
		} else {
			System.out.println("Command failed! : " + shellCmd);
		}
		
		//clear
		process.waitFor();
		input.close();
		
		return result;
	}
	
	
	/**
	 * 执行linux shell命令(root权限)
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static final ArrayList<String> shellOfRoot(String shellCmd) throws IOException, InterruptedException {

		Process process = Runtime.getRuntime().exec("su");
		
		process.waitFor();
		
		InputStreamReader is = new InputStreamReader(process.getInputStream());
		LineNumberReader input = new LineNumberReader(is);
		ArrayList<String> result = new ArrayList<String>();
		
		OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream());
		osw.append(shellCmd);
		osw.flush();
		
		String output = null;
		
//		while ((output = input.readLine()) != null) {
//			result.add(output);
//		}
		
		int exitValue = -1;
		
//		try {
//			exitValue = process.waitFor();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		if (exitValue == 0) {
			System.out.println("Command succeed! : " + shellCmd);
		} else {
			System.out.println("Command failed! : " + shellCmd);
		}
		
		//clear
		process.waitFor();
		input.close();
		osw.close();
		
		return result;
	}
}
