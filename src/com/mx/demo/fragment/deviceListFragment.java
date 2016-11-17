package com.mx.demo.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cmo.mx.ble.MXBleManager;

import com.mx.blelib.ShellReader;
import com.mx.demo.GlobalData;
import com.mx.demo.R;
import com.mx.demo.GlobalData.BLETYPE;
import com.mx.demo.adapter.CommonAdapter;
import com.cttic.se.CtticReader;
import com.cttic.se.TimeoutException;
import com.mx.util.MXBaseUtil;
import com.mx.util.MXLog;

public class deviceListFragment extends Fragment {
	final static String TAG = "device_list";

	private ListView mListView;
	private macListAdapter mAdapter;
	private List<DeviceInfo> macList = new ArrayList<DeviceInfo>();

	private Button refresh;
	private Handler mHandler;

	CtticReader mReader;

	GlobalData data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		data = GlobalData.getInstance(getActivity().getApplicationContext());

		MXBleManager manager = MXBleManager.getInstance(getActivity().getApplicationContext());
		data.setgBleManager(manager);

		mHandler = new Handler();

		mAdapter = new macListAdapter(getActivity().getApplicationContext(), macList);

		boolean initBle = data.getgBleManager().initBLE();

		MXLog.i(TAG, "initBle," + initBle);

		starScan();
	}

	private void starScan() {
		data.getgBleManager().scanLeDevice(new LeScanCallback() {

			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				// TODO Auto-generated method stub

				for (DeviceInfo vInfo : macList) {
					if (vInfo.mac.equals(device.getAddress()))
						return;
				}

				DeviceInfo info = new DeviceInfo();
				info.mac = device.getAddress();

				String IDString = "ID:";

				String[] macStrings = device.getAddress().toUpperCase().split(":");
				int length = macStrings.length;
				IDString = IDString + macStrings[length - 2] + macStrings[length - 1];

				info.name = device.getName() + " " + IDString;

				info.device = device;

				MXLog.i(TAG, info.name);
				MXLog.i(TAG, info.mac);

				macList.add(info);

				mHandler.post(new Runnable() {

					@Override
					public void run() {

						mAdapter.notifyDataSetChanged();
					}
				});

			}
		}, 10000);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_device_ble_list, null);

		mListView = (ListView) view.findViewById(R.id.ble_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

				Bundle args = new Bundle();
				String message = "连接中";
				args.putString(data.getProgress().msg, message);
				data.getProgress().setArguments(args);
				data.getProgress().show(getFragmentManager(), "progressDialog");

				mReader = ShellReader.getInstance(getActivity().getApplicationContext());

				int index_list = index;

				data.setgBleReader(mReader, BLETYPE.SHELL);
				data.setBound(true, macList.get(index_list).mac);

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						SystemClock.sleep(2000);
						data.getProgress().dismiss();
						getActivity().getFragmentManager().popBackStack();
					}
				});
				thread.start();

			}
		});

		refresh = (Button) view.findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				macList.clear();
				mAdapter.notifyDataSetChanged();
				// scan
				starScan();
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

	class DeviceInfo {
		public String mac;
		public String name;
		public BluetoothDevice device;

		DeviceInfo() {
		}
	}

	class macListAdapter extends CommonAdapter<DeviceInfo> {
		public class ViewHolder {
			public TextView mac;
			public TextView name;
		}

		ViewHolder holder;

		public macListAdapter(Context context, List<DeviceInfo> list) {
			super(context, list);
		}

		@Override
		protected View noConvertView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.list_item_ble_list, parent, false);
			holder = new ViewHolder();
			holder.mac = (TextView) convertView.findViewById(R.id.list_device_mac);
			holder.name = (TextView) convertView.findViewById(R.id.list_device_name);
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		protected View hasConvertView(int position, View convertView, ViewGroup parent) {
			holder = (ViewHolder) convertView.getTag();
			return convertView;
		}

		@Override
		protected View initConvertView(int position, View convertView, ViewGroup parent) {
			holder.mac.setText(list.get(position).mac);
			holder.name.setText(list.get(position).name);
			return convertView;
		}

	}
}
