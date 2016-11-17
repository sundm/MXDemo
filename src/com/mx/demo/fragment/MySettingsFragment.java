package com.mx.demo.fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import cmo.mx.ble.MXBleManager;

import com.mx.demo.GlobalData;
import com.mx.demo.R;
import com.mx.util.MXLog;
import com.mx.util.SwitchButton;

public class MySettingsFragment extends Fragment {
	private SwitchButton switch_nfc_btn;
	final String tag = "setting_fragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_settings, null);
		final GlobalData data = GlobalData.getInstance(getActivity().getApplicationContext());

		switch_nfc_btn = (SwitchButton) view.findViewById(R.id.switch_nfc);
		switch_nfc_btn.setChecked(data.isNFCMethod());
		switch_nfc_btn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				MXLog.i(tag, "nfc_switch," + isChecked);
				if (isChecked) {

				} else {
					MXBleManager manager = MXBleManager.getInstance(getActivity().getApplicationContext());
					boolean initBle = manager.initBLE();
					boolean isEnable = manager.isEnabled();
					MXLog.i(tag, "initBle," + initBle);
					MXLog.i(tag, "isEnable," + isEnable);
//					manager.scanLeDevice(new LeScanCallback() {
//						
//						@Override
//						public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//							// TODO Auto-generated method stub
//							MXLog.i(tag, "device name:" + device.getName());
//							MXLog.i(tag, "device address:" + device.getAddress());
//						}
//					}, 10000);
				}

				data.setNFCMethod(isChecked);

			}
		});

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
