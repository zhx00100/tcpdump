package test.framework.java.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

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
}
