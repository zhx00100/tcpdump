package test.framework.java.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SIM {
	// 第一种方法:  
	// 获取手机的IMSI码,并判断是中国移动\中国联通\中国电信
	/**
	 *  获取SIM卡的IMSI码  
	 *          * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile
	 *  Subscriber Identification Number）是区别移动用户的标志，  
	 *          * 储存在SIM卡中，可用于区别移动用户的有效信息
	 * 。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，  
	 *          * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，  
	 *          * 用于识别移动客户所归属的移动网络
	 * ，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。  
	 *          * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可           
	 */

	public static void getOperator(Context context) {
		// TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		if (imsi == null) {
			// return null;
		}

		if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
			// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号           //中国移动  
			//         
		} else if (imsi.startsWith("46001")) {// 中国联通
		} else if (imsi.startsWith("46003")) {// 中国电信          

		}

	}

	public static void getOperator2(Context context) {
		// 第二种方法  
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = telManager.getSimOperator();
		if (operator != null) {

			if (operator.equals("46000") || operator.equals("46002")) {// 中国移动
			} else if (operator.equals("46001")) {// 中国联通
			} else if (operator.equals("46003")) {// 中国电信
			}
		}
	}
	
	public static String getOperator3(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String operator = telManager.getSimOperatorName();
		
		return operator;
	}
}
