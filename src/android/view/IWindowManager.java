package android.view;

import android.os.IBinder;
import android.os.RemoteException;

public interface IWindowManager {
	boolean startViewServer(int port) throws RemoteException;
	
	class Stub {
		public static IWindowManager asInterface(IBinder i) {
			return null;
		}
	}
}