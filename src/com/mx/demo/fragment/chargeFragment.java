package com.mx.demo.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mx.cttic.card.CtticCard.DeviceType;
import com.mx.cttic.card.CtticTradeResult;
import com.mx.cttic.load.ApplyOrderResult;
import com.mx.cttic.load.CtticLoad;
import com.mx.cttic.load.CtticLoad.MXApplyOrderCallBack;
import com.mx.cttic.load.CtticLoad.MXLoadCallBack;
import com.mx.data.AppBus;
import com.mx.demo.GlobalData;
import com.mx.demo.GlobalData.action;
import com.mx.demo.R;
import com.mx.demo.view.MoneyGridView;
import com.mx.httpclient.ArteryPreClient;
import com.mx.httpclient.ArteryPreTrans;
import com.mx.pay.MXPay;
import com.mx.pay.MXPay.MXPayCallBack;
import com.mx.pay.PayResult;
import com.mx.util.MXBaseUtil;
import com.mx.util.MXLog;
import com.mx.util.PayOrder;
import com.mx.util.exception.MXParameterNullException;

public class chargeFragment extends Fragment {
	final static String TAG = "charge";

	private MoneyGridAdapter gridAdapter;
	private List<String> gridList = new ArrayList<String>();
	private MoneyGridView money_grid;

	private Button chargeButton;

	private String chargeAmountString;

	GlobalData data;

	private Handler mHandler;

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
		View view = inflater.inflate(R.layout.frag_charge, null);
		gridList.clear();
		gridList.add("5");
		gridList.add("10");
		gridList.add("20");
		gridList.add("50");

		gridList.add("100");
		gridList.add("200");
		gridList.add("30");
		gridList.add("500");

		gridAdapter = new MoneyGridAdapter(getActivity().getApplicationContext(), gridList);
		money_grid = (MoneyGridView) view.findViewById(R.id.amountGridView);
		money_grid.setAdapter(gridAdapter);
		money_grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				gridAdapter.bindInt(position);
				MXLog.i(TAG, "select is " + position);
				String con = gridAdapter.getItem(position);
				chargeAmountString = MXBaseUtil.toMoneyString(con, 8);
				MXLog.i(TAG, "chargeAmountString is " + chargeAmountString);

			}
		});

		chargeButton = (Button) view.findViewById(R.id.chargeBtn);
		chargeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				data = GlobalData.getInstance(getActivity().getApplicationContext());

				final String amount = chargeAmountString;
				final String deviceId = "12345678";
				final String userId = "88888888";

				Bundle args = new Bundle();
				args.putString(data.getProgress().msg, "申请订单");
				data.getProgress().setArguments(args);
				data.getProgress().show(getFragmentManager(), "订单");

				ArteryPreTrans.ClientMode mode = ArteryPreTrans.ClientMode.TEST;
				try {
					ArteryPreClient.getInstance().initEnvironment(getActivity(), mode);

					CtticLoad.getInstance().applyCMBPayOrder(amount, deviceId, userId, new MXApplyOrderCallBack() {

						@Override
						public void onReceiveCallBack(ApplyOrderResult result) {
							MXLog.i(TAG, result.toString());

							data.getProgress().dismiss();

							if (result.getResultCode() != 0) {
								Toast.makeText(getActivity(), result.getResultDesc(), Toast.LENGTH_LONG).show();
								return;
							}

							final PayOrder order = result.getOrder();
							MXLog.i(TAG, order.getOrderInfo());

							MXPay.getInstance().doPay(order, getActivity(), new MXPayCallBack() {
								@Override
								public void onReceiveCallBack(PayResult result) {
									// TODO
									MXLog.i(TAG, result.toString());

									if (result.getResultCode() != 0) {
										Toast.makeText(getActivity(), result.getResultDesc(), Toast.LENGTH_LONG).show();
										return;
									}

									data.setOrder(order);
									data.setCurrent_action(action.LOAD_EDEP);

									if (data.isNFCMethod()) {
										mHandler.postDelayed(new Runnable() {

											@Override
											public void run() {
												Bundle args = new Bundle();
												args.putString(data.getProgress().msg, "请将卡片靠近NFC完成充值");
												data.getProgress().setArguments(args);
												data.getProgress().show(getFragmentManager(), "充值");
											}
										}, 1000);
									} else {

										mHandler.postDelayed(new Runnable() {

											@Override
											public void run() {
												Bundle args = new Bundle();
												args.putString(data.getProgress().msg, "开始充值");
												data.getProgress().setArguments(args);
												data.getProgress().show(getFragmentManager(), "充值");
											}
										}, 500);

										mHandler.postDelayed(new Runnable() {

											@Override
											public void run() {
												CtticLoad.getInstance().registerCtticReader(data.getgBleReader());
												CtticLoad.getInstance().doCtticLoad(order, DeviceType.BLE,
														CtticLoad.LOAD_TYPE.INTERFLOW, new MXLoadCallBack() {

															@Override
															public void onReceiveCallBack(CtticTradeResult result) {
																data.getProgress().dismiss();

																MXLog.i(TAG, result.toString());

																if (0 == result.getResultCode()) {
																	getActivity().runOnUiThread(new Runnable() {

																		@Override
																		public void run() {
																			Toast.makeText(
																					getActivity()
																							.getApplicationContext(),
																					"充值成功", Toast.LENGTH_LONG).show();
																		}
																	});
																}
																else {
																	final String msg = result.getErrInfo();
																	getActivity().runOnUiThread(new Runnable() {

																		@Override
																		public void run() {
																			Toast.makeText(
																					getActivity()
																							.getApplicationContext(),
																					msg, Toast.LENGTH_LONG).show();
																		}
																	});
																}
															}

															@Override
															public void onMessageOut(final String msg) {
																MXLog.i(TAG, "msg is " + msg);

																// getActivity().runOnUiThread(new
																// Runnable() {
																// @Override
																// public void
																// run() {
																// AppBus.getInstance().post(msg);
																// }
																// });

															}
														});
											}
										}, 1000);

									}

								}
							});
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MXParameterNullException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mHandler = new Handler();

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

	@Override
	public void onStart() {
		super.onStart();
		MXLog.i(TAG, "=AppBus= register");
		AppBus.getInstance().register(this);

	}

	@Override
	public void onResume() {
		super.onResume();

		MXLog.i(TAG, "onResume");

	}

	@Override
	public void onPause() {
		super.onPause();

		MXLog.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		MXLog.i(TAG, "=AppBus= unregister");
		AppBus.getInstance().unregister(this);
	}

	public class MoneyGridAdapter extends BaseAdapter {
		private int selete_posi = -1;
		private Context context;
		private List<String> list;

		public MoneyGridAdapter(Context context, List<String> list) {
			super();
			this.context = context;
			this.list = list;
		}

		public void bindInt(int posi) {
			selete_posi = posi;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public String getItem(int arg0) {
			if (list.size() > arg0) {
				return list.get(arg0);
			}
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View converview, ViewGroup arg2) {
			MoneyHold hold;
			if (converview == null) {
				converview = LayoutInflater.from(context).inflate(R.layout.ada_money_lay, null);
				hold = new MoneyHold();
				hold.ada_money_tex = (TextView) converview.findViewById(R.id.ada_money_tex);
				converview.setTag(hold);
			} else {
				hold = (MoneyHold) converview.getTag();
			}
			if (!TextUtils.isEmpty(getItem(position))) {
				if (position == 5) {
					hold.ada_money_tex.setText(getItem(position));
				} else {
					hold.ada_money_tex.setText(getItem(position) + "元");
				}
			}
			if (position != selete_posi) {
				hold.ada_money_tex.setBackgroundResource(R.drawable.rmb_normal);
				hold.ada_money_tex.setTextColor(Color.parseColor("#666666"));
			} else {
				hold.ada_money_tex.setTextColor(Color.WHITE);
				hold.ada_money_tex.setBackgroundResource(R.drawable.rmb_passwed);
			}
			return converview;
		}

		private class MoneyHold {
			TextView ada_money_tex;
		}
	}
}
