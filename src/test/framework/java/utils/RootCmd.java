package test.framework.java.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import android.util.Log;

public final class RootCmd {

	private static final String TAG = "RootCmd";
	private static boolean mHaveRoot = false;

	// 判断机器Android是否已经root，即是否获取root权限
	public static boolean haveRoot() {
		if (!mHaveRoot) {
			int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
			if (ret != -1) {
				Log.i(TAG, "have root!");
				mHaveRoot = true;
			} else {
				Log.i(TAG, "not root!");
			}
		} else {
			Log.i(TAG, "mHaveRoot = true, have root!");
		}
		return mHaveRoot;
	}

	/**
	 * 执行命令 并输出结果
	 * 
	 * @param cmd
	 * @return if null， no su command, if su only， the result is empty.
	 */
	public static ArrayList<String> execRootCmd(String cmd) {
		
		ArrayList<String> result = new ArrayList<String>();
		
		String error = "no su";
		
//		DataOutputStream dos = null;
//		DataInputStream dis = null;

		Writer osw = null;
		BufferedWriter output = null;
		
		Reader isr = null;
		BufferedReader input = null;
		
		try {
			Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统
														// 有su命令
			InputStream is = p.getInputStream();
			OutputStream os = p.getOutputStream();
			
			osw = new OutputStreamWriter(os);
			output = new BufferedWriter(osw);
			
//			dos = new DataOutputStream(os);
//			dis = new DataInputStream(is);

			Log.i(TAG, "execRootCmd: " + cmd);
			output.write(cmd + "\n");
			output.flush();
			output.write("exit\n");
			output.flush();
			
			String line = null;
			
			isr = new InputStreamReader(is);
			input = new LineNumberReader(isr);
			
			while ((line = input.readLine()) != null) {
				Log.d(TAG, "execRootCmd: " + line);
				result.add(line);
			}

			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			result.add(error);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
		}
		
		return result;
	}

	/**
	 * 执行命令但不关注结果输出
	 * @param cmd
	 * @return
	 */
	public static int execRootCmdSilent(String cmd) {
		int result = -1;
		DataOutputStream dos = null;

		try {
			Process p = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(p.getOutputStream());

			Log.i(TAG, "execRootCmdSilent: " + cmd);
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			p.waitFor();
			result = p.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * 在用戶权限下执行shell命令
	 * @param cmd
	 * @return
	 */
	public static ArrayList<String> execCmd(String cmd) {
		
		ArrayList<String> result = new ArrayList<String>();
		
		String error = "no su";
		
//		DataOutputStream dos = null;
		DataInputStream dis = null;

		try {
			Process p = Runtime.getRuntime().exec(cmd);// 经过Root处理的android系统
														// 有su命令
//			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());

			Log.i(TAG, "execCmd: " + cmd);
//			dos.writeBytes(cmd + "\n");
//			dos.flush();
//			dos.writeBytes("exit\n");
//			dos.flush();
			String line = null;
			while ((line = dis.readLine()) != null) {
				Log.d(TAG, "execCmd: " + line);
				result.add(line);
			}

			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			result.add(error);
		} finally {
//			if (dos != null) {
//				try {
//					dos.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
}
