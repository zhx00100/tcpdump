package android.app;

import android.os.IInterface;

public interface IActivityManager extends IInterface {
	public abstract void forceStopPackage(final String packageName);
	
	//sdk > 16
	public abstract void forceStopPackage(final String packageName, int userId);
}
