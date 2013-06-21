package test.framework.java.utils;

import jackpal.androidterm.Exec;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.baidu.tcpdump.R;
import com.baidu.tcpdump.RootService;

public class Device {
	
	private static final String TAG = Device.class.getSimpleName();
	
	private Context mContext;
	
	public Device(Context context) {
		mContext = context;
	}
	
	/**
	 * 是否是root的设备
	 */
	public static boolean isRooted() {
		
		boolean isRooted = false;
		
		try {
			final VirtualTerminal vt = new VirtualTerminal();
			
			vt.open();
			
			VirtualTerminal.VTCommandResult r = vt.runCommand("id");
			
			if (r.success()) {
				// Rooted device
				isRooted = true;
				
			}
			
			vt.close();
			
			return isRooted;
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		
		return isRooted;
	}
	
	/**
	 * 获取临时root权限
	 */
	public void rootForTemp() {
		
	}
	
	public interface temprootlistener {
		public void onTempRootCompleted(boolean tempRooted);
	}
	
	public void dotemproot(final temprootlistener listener) {
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		
		final WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "deviceroot");
		wl.acquire();
		
		Log.i(TAG, "Starting temp root");

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);
		
		Log.i(TAG, "Got processid: " + processId[0]);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);

		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						//saystuff(str);
						Log.i(TAG, str);
						if (str.contains("finished checked")) {
							if (listener != null) {
								listener.onTempRootCompleted(true);
							}
							Log.i(TAG, "root success!");
							
							
//							Toast.makeText(mContext, "root success!", 500).show();
							Log.i(TAG, "Temporary root applied! You are now rooted until your next reboot.");
							break;
						}
					} catch (Exception e) {
						Log.i(TAG, Log.getStackTraceString(e));
						if (listener != null) {
							listener.onTempRootCompleted(false);
						}
					}
				}
				wl.release();
			}
		}.start();

		try {
			write(out, "id");
			try {
				SaveIncludedZippedFileIntoFilesFolder(R.raw.busybox, "busybox", mContext);
				SaveIncludedZippedFileIntoFilesFolder(R.raw.su, "su", mContext);
				SaveIncludedZippedFileIntoFilesFolder(R.raw.superuser, "SuperUser.apk", mContext);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			write(out, "chmod 777 " + getFilesDir() + "/busybox");
			write(out, getFilesDir() + "/busybox killall rageagainstthecage");
			write(out, getFilesDir() + "/busybox killall rageagainstthecage");
			write(out, getFilesDir() + "/busybox rm "+getFilesDir()+"/temproot.ext");
			write(out, getFilesDir() + "/busybox rm -rf "+getFilesDir()+"/bin");
			write(out, getFilesDir() + "/busybox cp -rp /system/bin "+getFilesDir());
			write(out, getFilesDir() + "/busybox dd if=/dev/zero of="+getFilesDir()+"/temproot.ext bs=1M count=15");
			write(out, getFilesDir() + "/busybox mknod /dev/loop9 b 7 9");
			write(out, getFilesDir() + "/busybox losetup /dev/loop9 "+getFilesDir()+"/temproot.ext");
			write(out, getFilesDir() + "/busybox mkfs.ext2 /dev/loop9");
			write(out, getFilesDir() + "/busybox mount -t ext2 /dev/loop9 /system/bin");
			write(out, getFilesDir() + "/busybox cp -rp "+getFilesDir()+"/bin/* /system/bin/");
			write(out, getFilesDir() + "/busybox cp "+getFilesDir()+"/su /system/bin");
			write(out, getFilesDir() + "/busybox cp "+getFilesDir()+"/busybox /system/bin");
			write(out, getFilesDir() + "/busybox chown 0 /system/bin/su");
			write(out, getFilesDir() + "/busybox chown 0 /system/bin/busybox");
			write(out, getFilesDir() + "/busybox chmod 4755 /system/bin/su");
			write(out, getFilesDir() + "/busybox chmod 755 /system/bin/busybox");
			write(out, "pm install -r "+getFilesDir()+"/SuperUser.apk");
			write(out, "checkvar=checked");
			write(out, "echo finished $checkvar");
			
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
	}
	
	private File getFilesDir() {
		return mContext.getFilesDir();
	}
	
	private void write(FileOutputStream out, String command) throws IOException {
		command += "\n";
		out.write(command.getBytes());
		out.flush();
	}
	public static void SaveIncludedFileIntoFilesFolder(int resourceid, String filename, Context ApplicationContext) throws Exception {
		InputStream is = ApplicationContext.getResources().openRawResource(resourceid);
		FileOutputStream fos = ApplicationContext.openFileOutput(filename, Context.MODE_WORLD_READABLE);
		byte[] bytebuf = new byte[1024];
		int read;
		while ((read = is.read(bytebuf)) >= 0) {
			fos.write(bytebuf, 0, read);
		}
		is.close();
		fos.getChannel().force(true);
		fos.flush();
		fos.close();
	}
	
	public static void SaveIncludedZippedFileIntoFilesFolder(int resourceid, String filename, Context ApplicationContext) throws Exception {
		InputStream is = ApplicationContext.getResources().openRawResource(resourceid);
		FileOutputStream fos = ApplicationContext.openFileOutput(filename, Context.MODE_WORLD_READABLE);
		GZIPInputStream gzis = new GZIPInputStream(is);
		byte[] bytebuf = new byte[1024];
		int read;
		while ((read = gzis.read(bytebuf)) >= 0) {
			fos.write(bytebuf, 0, read);
		}
		gzis.close();
		fos.getChannel().force(true);
		fos.flush();
		fos.close();
	}
	
	
	public void dostuff() {
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "z4root");
		wl.acquire();

		Log.i(TAG, "Saving required file...");
		try {
			SaveIncludedFileIntoFilesFolder(R.raw.rageagainstthecage, "rageagainstthecage", mContext);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);
		Log.i(TAG, "Got processid: " + processId[0]);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);

		// final int[] processId_t = new int[1];
		// final FileDescriptor fd_t = Exec.createSubprocess("/system/bin/sh",
		// "-", null, processId_t);
		// Log.i("AAA", "Got processid_t: " + processId_t[0]);
		//
		// final FileOutputStream out_t = new FileOutputStream(fd_t);
		// final FileInputStream in_t = new FileInputStream(fd_t);
		
		
//		final int[] processId1 = new int[1];
//		final FileDescriptor fd1 = Exec.createSubprocess("/system/bin/sh", "-", null, processId1);
//		Log.i("AAA", "Got processid: " + processId1[0]);
//
//		final FileOutputStream out1 = new FileOutputStream(fd1);
//		final FileInputStream in1 = new FileInputStream(fd1);
		
		
		
		
		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				// byte[] mBuffer_t = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						Log.i(TAG, str);
						if (str.contains("Forked")) {
							Log.i(TAG, "FORKED FOUND!");
							Log.i(TAG, "Forking completed");

//							Intent intent = new Intent(mContext, AlarmReceiver.class);
//							intent.setAction("com.baidu.tcpdump.Action.rootalarm");
//							PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, 0);
//
//							// Get the AlarmManager service
//							AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//
//							// for (int i=5;i<120;i+=15) {
//							Calendar cal = Calendar.getInstance();
//							cal.add(Calendar.SECOND, 5);
//							am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
							
							Log.i(TAG, "start rootservice in device");
							Intent intent = new Intent(mContext, RootService.class);
							mContext.startService(intent);
//							new Thread(new Runnable() {
//								
//								@Override
//								public void run() {
									// TODO Auto-generated method stub
//									try {
//										Runtime.getRuntime().exec("am startservice -a com.baidu.tcpdump.Action.rootservice");
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										Log.e(TAG, Log.getStackTraceString(e));
//									}
									
									


									

//									try {
////										write(out1, "am startservice -a com.baidu.tcpdump.Action.rootservice");
//										out1.write("am startservice -a com.baidu.tcpdump.Action.rootservice".getBytes());
//										out1.flush();
//									} catch (Exception ex) {
//										Log.i(TAG, Log.getStackTraceString(ex));
//									}
//									
//									RootCmd.execCmd("am startservice -a com.baidu.tcpdump.Action.rootservice");
//								}
//							}).start();
							
							
							// }

							// Get the AlarmManager service

							Log.i(TAG, "Aquiring root shell...");
							wl.release();
							Thread.sleep(20000);
//							finish();
							return;
						}
						if (str.contains("Cannot find adb")) {
							String warning = "In order for this to work, USB debugging must be enabled. The settings page will open when you press OK. Please enable USB debugging, and then retry.";
							Log.i(TAG, warning);
							Toast.makeText(mContext, warning, Toast.LENGTH_LONG).show();
							
							try {
								mContext.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
							} catch (Exception e) {
								try {
									mContext.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
								} catch (Exception e2) {
									//showDialog(SHOW_SETTINGS_ERROR_DIALOG);
									Log.i(TAG, "SHOW_SETTINGS_ERROR_DIALOG");
									return;
								}
							}
//							finish();
							
							return;
						}
					} catch (Exception e) {
						read = -1;
						e.printStackTrace();
					}
				}
			};
		}.start();

		try {
			String command = "chmod 777 " + getFilesDir() + "/rageagainstthecage\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/rageagainstthecage\n";
			out.write(command.getBytes());
			out.flush();
			Log.i(TAG, "Running exploit in order to obtain root access...");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public interface permrootlistener {
		public void onPermRootCompleted(boolean permRooted);
	}
	public void dopermroot(final permrootlistener listener) {
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "z4root");
		wl.acquire();
		Log.i("AAA", "Starting");

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);
		Log.i("AAA", "Got processid: " + processId[0]);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);


		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						//saystuff(str);
						Log.i("AAA", str);
					} catch (Exception ex) {
						
					}
				}
				wl.release();
			}
		}.start();

		try {
			String command = "id\n";
			out.write(command.getBytes());
			out.flush();
			try {
				SaveIncludedZippedFileIntoFilesFolder(R.raw.busybox, "busybox", mContext);
				SaveIncludedZippedFileIntoFilesFolder(R.raw.su, "su", mContext);
				SaveIncludedZippedFileIntoFilesFolder(R.raw.superuser, "SuperUser.apk", mContext);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			command = "chmod 777 " + getFilesDir() + "/busybox\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/busybox mount -o remount,rw /system\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/busybox cp "+getFilesDir()+"/su /system/bin/\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/busybox cp "+getFilesDir()+"/SuperUser.apk /system/app\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/busybox cp "+getFilesDir()+"/busybox /system/bin/\n";
			out.write(command.getBytes());
			out.flush();
			command = "chown root.root /system/bin/busybox\nchmod 755 /system/bin/busybox\n";
			out.write(command.getBytes());
			out.flush();
			command = "chown root.root /system/bin/su\n";
			out.write(command.getBytes());
			out.flush();
			command = getFilesDir() + "/busybox chmod 6755 /system/bin/su\n";
			out.write(command.getBytes());
			out.flush();
			command = "chown root.root /system/app/SuperUser.apk\nchmod 755 /system/app/SuperUser.apk\n";
			out.write(command.getBytes());
			out.flush();
			command = "rm "+getFilesDir()+"/busybox\n";
			out.write(command.getBytes());
			out.flush();
			command = "rm "+getFilesDir()+"/su\n";
			out.write(command.getBytes());
			out.flush();
			command = "rm "+getFilesDir()+"/SuperUser.apk\n";
			out.write(command.getBytes());
			out.flush();
			command = "rm "+getFilesDir()+"/rageagainstthecage\n";
			out.write(command.getBytes());
			out.flush();
			command = "echo \"reboot now!\"\n";
			Log.i(TAG, "Rebooting...");
			out.write(command.getBytes());
			out.flush();		
			Thread.sleep(3000);
			command = "sync\nsync\n";
			out.write(command.getBytes());
			out.flush();
			command = "reboot\n";
			out.write(command.getBytes());
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void dounroot() {
		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "z4root");
		wl.acquire();
		Log.i("AAA", "Starting");

		final int[] processId = new int[1];
		final FileDescriptor fd = Exec.createSubprocess("/system/bin/sh", "-", null, processId);
		Log.i("AAA", "Got processid: " + processId[0]);

		final FileOutputStream out = new FileOutputStream(fd);
		final FileInputStream in = new FileInputStream(fd);


		new Thread() {
			public void run() {
				byte[] mBuffer = new byte[4096];
				int read = 0;
				while (read >= 0) {
					try {
						read = in.read(mBuffer);
						String str = new String(mBuffer, 0, read);
						//saystuff(str);
						Log.i("AAA", str);
					} catch (Exception ex) {
						
					}
				}
				wl.release();
			}
		}.start();

		try {
			write(out, "id");
			try {
				SaveIncludedZippedFileIntoFilesFolder(R.raw.busybox, "busybox", mContext);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			write(out, "chmod 777 " + getFilesDir() + "/busybox");
			write(out, getFilesDir() + "/busybox mount -o remount,rw /system");
			write(out, getFilesDir() + "/busybox rm /system/bin/su");
			write(out, getFilesDir() + "/busybox rm /system/xbin/su");
			write(out, getFilesDir() + "/busybox rm /system/bin/busybox");
			write(out, getFilesDir() + "/busybox rm /system/xbin/busybox");
			write(out, getFilesDir() + "/busybox rm /system/app/SuperUser.apk");
			write(out, "echo \"reboot now!\"");
			Log.i(TAG, "Rebooting...");
			Thread.sleep(3000);
			write(out, "sync\nsync");
			write(out, "reboot");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
