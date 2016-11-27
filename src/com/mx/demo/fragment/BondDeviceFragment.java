package com.mx.demo.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cttic.se.ConnectException;
import com.cttic.se.CtticReader;
import com.cttic.se.TimeoutException;
import com.mx.blelib.ShellReader;
import com.mx.demo.CustomDialog;
import com.mx.demo.GlobalData;
import com.mx.demo.GlobalData.BLETYPE;
import com.mx.demo.R;
import com.mx.util.MXBaseUtil;
import com.mx.util.MXLog;

public class BondDeviceFragment extends Fragment {
	private Button unboundBtn;
	private Button updateBtn;

	private CtticReader mReader;

	private TextView macTextView;
	private TextView batteryTextView;
	private TextView versionTextView;

	final private static String TAG = "deviceFragment";

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
		final GlobalData data = GlobalData.getInstance(getActivity().getApplicationContext());

		View view = null;

		view = inflater.inflate(R.layout.frag_device, null);

		macTextView = (TextView) view.findViewById(R.id.text_mac_view);
		batteryTextView = (TextView) view.findViewById(R.id.text_bettery_view);
		versionTextView = (TextView) view.findViewById(R.id.text_version_view);

		unboundBtn = (Button) view.findViewById(R.id.unboundBtn);
		unboundBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
				builder.setMessage("是否解绑当前设备?");
				builder.setTitle("解绑");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						data.setBound(false, null);
						mReader.close();
						dialog.dismiss();

						String title = getString(R.string.device);

						FragmentTransaction ft2 = getFragmentManager().beginTransaction();
						ft2.replace(R.id.content_frame, new UnBondDeviceFragment(), title);
						ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
						ft2.addToBackStack(null);
						ft2.commit();
					}
				});

				builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
			}
		});

		updateBtn = (Button) view.findViewById(R.id.updateBtn);
		updateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
				builder.setMessage("是否进行固件升级?");
				builder.setTitle("固件升级");
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Bundle args = new Bundle();

						args.putString(data.getProgress().msg, "正在进行固件升级");
						data.getProgress().setArguments(args);
						data.getProgress().show(getFragmentManager(), "progressDialog");
					}
				});

				builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
			}
		});

		Bundle args = new Bundle();

		args.putString(data.getProgress().msg, "正在连接绑定设备");
		data.getProgress().setArguments(args);
		data.getProgress().show(getFragmentManager(), "progressDialog");

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (data.getBleType() == BLETYPE.SHELL) {
					if (data.getgBleReader() == null) {
						mReader = ShellReader.getInstance(getActivity().getApplicationContext());
						data.setgBleReader(mReader, BLETYPE.SHELL);

					} else {
						mReader = (ShellReader) data.getgBleReader();
					}

					final String boundMacString = data.boundMac();

					ShellReader shellReader = (ShellReader) mReader;
					if (!shellReader.isOpened()) {

						if (boundMacString.isEmpty()) {

							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									data.getProgress().dismiss();

									Toast.makeText(getActivity().getApplicationContext(), "mac地址获取失败",
											Toast.LENGTH_LONG).show();
								}
							});

						} else {
							try {
								byte[] resp = shellReader.open(boundMacString, 10, null);
								if (0 != MXBaseUtil.bytes2Int(resp)) {
									getActivity().runOnUiThread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method
											// stub
											data.getProgress().dismiss();

											Toast.makeText(getActivity().getApplicationContext(), "连接失败",
													Toast.LENGTH_LONG).show();
										}
									});
								} else {
									try {
										final String battery = shellReader.getBatteryLevel();
										MXLog.i(TAG, "battery is " + battery);

										if (battery == null || battery.isEmpty()) {
											getActivity().runOnUiThread(new Runnable() {

												@Override
												public void run() {
													Toast.makeText(getActivity().getApplicationContext(), "获取电量失败",
															Toast.LENGTH_LONG).show();
												}
											});
										} else {
											getActivity().runOnUiThread(new Runnable() {

												@Override
												public void run() {
													// TODO Auto-generated
													// method stub
													data.getProgress().dismiss();
													macTextView.setText(boundMacString);
													batteryTextView.setText(battery + "%");
													Toast.makeText(getActivity().getApplicationContext(), "获取电量成功",
															Toast.LENGTH_LONG).show();
												}
											});
										}

									} catch (ConnectException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} catch (TimeoutException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						try {
							final String battery = shellReader.getBatteryLevel();
							MXLog.i(TAG, "battery is " + battery);

							if (battery == null || battery.isEmpty()) {
								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										data.getProgress().dismiss();
										Toast.makeText(getActivity().getApplicationContext(), "获取电量失败",
												Toast.LENGTH_LONG).show();
									}
								});
							} else {
								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated
										// method stub
										data.getProgress().dismiss();
										macTextView.setText(boundMacString);
										batteryTextView.setText(battery + "%");
										Toast.makeText(getActivity().getApplicationContext(), "连接成功", Toast.LENGTH_LONG)
												.show();
									}
								});
							}

						} catch (ConnectException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}
		});
		thread.start();

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
