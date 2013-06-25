package test.framework.java.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import android.view.View;

import com.baidu.tcpdump.MainActivity;

public class ScreenRecorder {

	

	public void start(View button) {
		RootCmd.execRootCmd("echo > /sdcard/zhangxin/event.txt");
		RootCmd.execRootCmd("geteee -t -q >> /sdcard/zhangxin/event.txt &");
	}

	public void stop(View button) {
		RootCmd.execRootCmd("killall geteee");
		RootCmd.execRootCmd("killall geteee");
		RootCmd.execRootCmd("killall geteee");
	}

	public void playback(View button) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<String> playback = FileUtils.readLines(new File(
						"/sdcard/zhangxin/event.txt"));
				String line = null;
				String[] item = null;
				int len = playback.size();

				List<Event> event = new ArrayList<Event>();
				for (int i = 2; i < len; i++) {
					line = playback.get(i);
					line.trim();
					item = line.split(" ");

					Event e = new Event();
					e.time = Double.parseDouble(item[0]);
					e.event = item[1];
					e.type = item[2];
					e.code = item[3];
					e.value = item[4];
					event.add(e);
				}

				len = event.size();

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

					int lasttime = -1;
					int time = -1;

					Event e = event.get(0);
					
					int b = Integer.parseInt("0035", 16);
					
					lasttime = (int) (e.time * 1000);
					output.write("sendevent " + e.event + " " + Integer.parseInt(e.type, 16) + " "
							+ Integer.parseInt(e.code, 16) + " " + Integer.parseInt(e.value, 16) + "\n");
					output.flush();

					//快进/慢放控制变量，单位（毫秒）： + ：慢放， - ：快进
					int delta = 0;
					
					for (int i = 1; i < len; i++) {						

						e = event.get(i);
						time = (int) (e.time * 1000);
						Thread.sleep(time - lasttime + delta);

						output.write("sendevent " + e.event + " " + Integer.parseInt(e.type, 16)
								+ " " + Integer.parseInt(e.code, 16) + " " + Integer.parseInt(e.value, 16) + "\n");
						output.flush();
						lasttime = time;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();

	}

	class Event {
		double time;
		String event;
		String type;
		String code;
		String value;
	}

	// 转化字符串为十六进制编码
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}
}
