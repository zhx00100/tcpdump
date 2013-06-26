package test.framework.java.utils;

import android.os.Environment;
import android.text.TextUtils;

public class SDCard {

	// Linux
	// 1:吧你的卡插在读卡器里面
	// 2：进入LINUX系统 敲入 fdisk -l 查看U盘盘符 一般是Sda1
	// 3:mount -t vfat /dev/sda1 /mntcd /mnt ls

	// android 挂载/卸载 SDCard的命令
	// 1.mount命令查看sdcard的挂载路径， 并记录例如： /dev/block/loop0 /mnt/sdcard vfat
	// rw,relatime,uid=1000,gid=1015,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro
	// 0 0
	// 2.卸载sdcard： busybox umount -l /mnt/sdcard ： -l参数，延迟绑定， 防止sdcard处在busy状态
	// 3.挂载sdcard： busybox mount -t vfat /dev/block/loop0 /mnt/sdcard

	/**
	 * 检测sdcard是否可用
	 */
	public boolean isSDCardAvailable() {
		return TextUtils.equals(Environment.getExternalStorageState(),
				Environment.MEDIA_MOUNTED);
	}

}
