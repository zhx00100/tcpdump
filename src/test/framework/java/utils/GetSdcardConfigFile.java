package test.framework.java.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import android.os.Environment;
import android.text.format.DateUtils;


public class GetSdcardConfigFile {

	/**
     * 获取默认server地址。 如果sdcard存在appsearch.cfg文件，则从中获取地址。格式如下：
     * server=http://m.baidu.com
     * 
     */
    private static void getTestConfig() {
        
        File file = new File(Environment.getExternalStorageDirectory(), "pushservice.cfg");
//        
//        if (file.exists()) {
//            
//            Properties prop = new Properties();
//            FileInputStream fis = null;
//            
//            try {
//                fis = new FileInputStream(file);
//                prop.load(fis);
//                // http server
//                String tempHttpServer = prop.getProperty("http_server");
//                if (tempHttpServer != null && tempHttpServer.length() > 0) {
//                    sHttpServer = tempHttpServer;
//                }
//                
//                // socket server
//                String tempSocketServer = prop.getProperty("socket_server");
//                if (tempSocketServer != null && tempSocketServer.length() > 0) {
//                    sSocketServer = tempSocketServer;
//                }
//                
//                // socket server port
//                String tempSocketServerPort = prop.getProperty("socket_server_port");
//                if (tempSocketServerPort != null && tempSocketServerPort.length() > 0) {
//                    sSocketServerPort = Integer.parseInt(tempSocketServerPort);
//                }
//
//                // config server
//                String tempConfigServer = prop.getProperty("config_server");
//                if (tempConfigServer != null && tempConfigServer.length() > 0) {
//                    sConfigServer = tempConfigServer;
//                }
//
//                // 长连接时间间隔
//                String tempSocketInterval = prop.getProperty("socket_interval");
//                if (tempSocketInterval != null && tempSocketInterval.length() > 0) {
//                    sSocketInterval = Integer.parseInt(tempSocketInterval) * DateUtils.MINUTE_IN_MILLIS;
//                }
//            } catch (Exception e) {
//                //e.printStackTrace();
//                if (Constants.isDebugMode()) {
//                    System.out.println(e.getMessage());
//                }
//            } finally {
//                if (fis != null) {
//                    try {
//                        fis.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }
    
    void t() throws Exception {
    	throw new Exception();
    }
}
