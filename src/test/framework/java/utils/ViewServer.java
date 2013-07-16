package test.framework.java.utils;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

public class ViewServer {

	private IWindowManager mWm;
	
	public static void main(String[] args) {
		System.out.println("ViewServer!main!");
		new ViewServer().run(args);
	}
	
	private void run(final String[] args) {
		System.out.println("ViewServer!");
		mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
		start();
	}
	
	public void start() {
		try {
			mWm.startViewServer(12321);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("mWm.startViewServer(12321)!:error!");
		}
		System.out.println("mWm.startViewServer(12321)!");
		
//		try {
//			Socket socket = new Socket(InetAddress.getLocalHost(), 12345);
//			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
//			out.write("DUMP -1");
//			out.newLine();
//			out.flush();
//			String line = null;
//			int maxCount = 0;
//			while ( (maxCount < 4) && ((line = in.readLine()) != null) ) {
//				maxCount++;
//				System.out.println(line + "\n");
//			}
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
