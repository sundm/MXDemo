package com.mx.demo;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import cmo.mx.ble.MXBleManager;

import com.mx.demo.fragment.progressDialogFragment;
import com.cttic.se.CtticReader;
import com.mx.util.MXLog;
import com.mx.util.PayOrder;

public class GlobalData extends Application {
	private static progressDialogFragment progressDialogFragment;

	private static SharedPreferences sharedPreferences;
	private static Editor editor;

	final static String TAG = "global";
	final static String NFC_METHOD = "nfc_method";
	final static String DEVICE_BOUND = "device_bound";
	final static String DEVICE_MAC = "device_mac";
	final static String DEVICE_TYPE = "device_type";

	private static GlobalData self = null;

	private MXBleManager gBleManager;

	private CtticReader gBleReader;

	public static final String BLE_SYN_SUCCESS = "com.ble.connectsuc";
	public static final String BLE_CONNECT_LOST = "com.ble.connectlost";
	public static final String BLE_SERVICE_CANCLE = "service_cancle";

	public static final String SERVICE_WATCH = "com.mx.demo";

	public final static String MAIN_SERVICE = "com.mx.demo.service.Bleservice";

	public static enum action {
		NONE, QUERY_PBOC, QUERY_EDEP, LOAD_PBOC, LOAD_EDEP;
	}

	public static enum BLETYPE {
		SHELL, BOND;
	}

	private action current_action = action.NONE;

	private boolean isNeedFresh;

	private PayOrder order;

	public static GlobalData getInstance(Context context) {
		if (self == null) {
			self = new GlobalData();
		}
		return self;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		self = this;

		progressDialogFragment = new progressDialogFragment();

		sharedPreferences = getSharedPreferences("mx_config", Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();

		// String processName = getProcessName(this,
		// android.os.Process.myPid());
		// if (processName != null) {
		// boolean defaultProcess =
		// processName.equals(GlobalData.SERVICE_WATCH);
		// if (defaultProcess) {
		// MXLog.i(TAG, "寮�濮�娉ㄥ��������:" + processName);
		// registerServices();
		// }
		// }

	}

	// private void registerServices() {
	// Intent intent = new Intent(GlobalData.this, Bleservice.class);
	// startService(intent);
	// }

	public boolean isNFCMethod() {
		return sharedPreferences == null ? false : sharedPreferences.getBoolean(NFC_METHOD, false);
	}

	public void setNFCMethod(boolean isNFC) {
		editor.putBoolean(NFC_METHOD, isNFC);
		editor.commit();
	}

	public void setBound(boolean isBound, String mac) {
		editor.putBoolean(DEVICE_BOUND, isBound);

		editor.putString(DEVICE_MAC, mac);

		editor.commit();
	}

	public boolean isBound() {
		return sharedPreferences == null ? false : sharedPreferences.getBoolean(DEVICE_BOUND, false);
	}

	public String boundMac() {
		return sharedPreferences == null ? "" : sharedPreferences.getString(DEVICE_MAC, "");
	}

	public progressDialogFragment getProgress() {

		return progressDialogFragment;
	}

	public action getCurrent_action() {
		return current_action;
	}

	public void setCurrent_action(action action) {
		current_action = action;
	}

	public boolean isNeedFresh() {
		return isNeedFresh;
	}

	public void setNeedFresh(boolean isNeedFresh) {
		this.isNeedFresh = isNeedFresh;
	}

	public PayOrder getOrder() {
		return order;
	}

	public void setOrder(PayOrder order) {
		this.order = order;
	}

	public MXBleManager getgBleManager() {
		return gBleManager;
	}

	public void setgBleManager(MXBleManager gBleManager) {
		this.gBleManager = gBleManager;
	}

	public CtticReader getgBleReader() {
		return gBleReader;
	}

	public BLETYPE getBleType() {
		String type = sharedPreferences == null ? "" : sharedPreferences.getString(DEVICE_TYPE, "");
		if (type.isEmpty()) {
			return null;
		} else {
			MXLog.i(TAG, "device type is " + type);
			if (type.equals("SHELL")) {
				return BLETYPE.SHELL;
			}

			if (type.equals("BOND")) {
				return BLETYPE.BOND;
			}

		}
		return null;
	}

	public void setgBleReader(CtticReader gBleReader, BLETYPE type) {
		this.gBleReader = gBleReader;

		editor.putString(DEVICE_TYPE, type.toString());

		editor.commit();
	}

	/**
	 * ��ㄦ�ュ�ゆ�������℃�����杩�琛�. // * @param context
	 * 
	 * @param className
	 *            ��ゆ����������″��瀛�
	 * @return true ��ㄨ��琛� false 涓���ㄨ��琛�
	 */
	public boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(500);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().toString().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}
}
