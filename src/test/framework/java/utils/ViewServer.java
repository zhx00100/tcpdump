package test.framework.java.utils;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

public class ViewServer{

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
			mWm.startViewServer(12345);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("mWm.startViewServer(4939)!:error!");
		}
		System.out.println("mWm.startViewServer(4939)!");
	}
}
