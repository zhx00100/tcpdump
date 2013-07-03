package test.framework.java.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileUtils {

	private static final String TAG = FileUtils.class.getSimpleName();
	
	public static void copyFile(InputStream src, String dst) {
		InputStream is = src;
		OutputStream os = null;
		try {
			File f = new File(dst);
									
			if (!f.exists() && !f.isDirectory()) {
				
				if (!f.getParentFile().exists()) {

					f.getParentFile().mkdirs();

				}
				
				os = new FileOutputStream(f);
				
				int readLen = -1;
				byte[] buffer = new byte[1024];
				
				while((readLen = is.read(buffer)) != -1){    
					os.write(buffer, 0, readLen);    
	            }
				
				os.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
     * 获取默认server地址。 如果sdcard存在appsearch.cfg文件，则从中获取地址。格式如下：
     * server=http://m.baidu.com
     * 
     */
    private static void getTestConfig() {
        
        File file = new File(Environment.getExternalStorageDirectory(), "pushservice.cfg");
        
        if (file.exists()) {
            
            Properties prop = new Properties();
            FileInputStream fis = null;
            
            try {
                fis = new FileInputStream(file);
                prop.load(fis);
                // http server
                String tempHttpServer = prop.getProperty("http_server");
                if (tempHttpServer != null && tempHttpServer.length() > 0) {
//                    sHttpServer = tempHttpServer;
                }
                
                // socket server
                String tempSocketServer = prop.getProperty("socket_server");
                if (tempSocketServer != null && tempSocketServer.length() > 0) {
//                    sSocketServer = tempSocketServer;
                }
                
                // socket server port
                String tempSocketServerPort = prop.getProperty("socket_server_port");
                if (tempSocketServerPort != null && tempSocketServerPort.length() > 0) {
//                    sSocketServerPort = Integer.parseInt(tempSocketServerPort);
                }

                // config server
                String tempConfigServer = prop.getProperty("config_server");
                if (tempConfigServer != null && tempConfigServer.length() > 0) {
//                    sConfigServer = tempConfigServer;
                }

//                // 长连接时间间隔
//                String tempSocketInterval = prop.getProperty("socket_interval");
//                if (tempSocketInterval != null && tempSocketInterval.length() > 0) {
//                    sSocketInterval = Integer.parseInt(tempSocketInterval) * DateUtils.MINUTE_IN_MILLIS;
//                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static synchronized void logToFile(String logStr, String path) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		String time = sDateFormat.format(new Date());
		String date = time.substring(0, 4).concat(time.substring(5, 7))
				.concat(time.substring(8, 10));
		String writeStr = time + " " + logStr + "\n\r";
		try {
//			String sdPath = Environment.getExternalStorageDirectory()
//					.getAbsolutePath();
//			File dir = new File(sdPath, "pushservice/files");
//			if (!dir.exists()) {
//				File parentDir = new File(dir.getParent());
//				if (!parentDir.exists()) {
//					parentDir.mkdirs();
//				}
//				
//			} else {
//				SimpleDateFormat sDateFormatTemp = new SimpleDateFormat(
//						"yyyyMMdd");
//				//删除七天前的log
//				for (File logFile : dir.listFiles()) {
//					if (logFile.getName().startsWith("msg")) {
//						if (Integer.parseInt(date)
//								- Integer.parseInt(sDateFormatTemp.format(logFile
//										.lastModified())) >= 7) {
//							logFile.delete();
//						}
//					}
//				}
//			}

//			File logFile = new File(sdPath, "pushservice/files/msg" + date
//					+ ".log");

			// added
			File logFile = new File(path);
			
			FileOutputStream fout = new FileOutputStream(logFile, true);
			byte[] bytes = writeStr.getBytes();
			
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**
     * 行读取文件， 并保存到list中
     * @param file
     * @return
     */
    public static List<String> readLine(File file) {
        LineNumberReader input = null;
        ArrayList<String> result = new ArrayList<String>();
        
        try {
            input = new LineNumberReader(new FileReader(file));
            
            String line = null;
            
            while ((line = input.readLine()) != null) {
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return result;
    }
}
