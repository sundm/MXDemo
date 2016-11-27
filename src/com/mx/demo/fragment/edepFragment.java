package com.mx.demo.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mx.cttic.card.CtticCard;
import com.mx.cttic.card.CtticCard.DeviceType;
import com.mx.cttic.card.CtticCard.QueryCardCallBack;
import com.mx.cttic.card.CtticCardInfo;
import com.mx.data.AppBus;
import com.mx.demo.GlobalData;
import com.mx.demo.GlobalData.action;
import com.mx.demo.MainActivity;
import com.mx.demo.R;
import com.mx.demo.adapter.CommonAdapter;
import com.mx.util.MXBaseUtil;
import com.mx.util.MXLog;
import com.mx.util.edep.EDEPLog;
import com.mx.util.edep.EdepUtil;
import com.squareup.otto.Subscribe;

public class edepFragment extends Fragment {

	final static String TAG = "edep";
	private ListView mListView;
	private logListAdapter mAdapter;
	private List<LogInfo> logList = new ArrayList<LogInfo>();

	private Button loadBtn;
	private Button readBtn;
	private TextView panTextView;
	private TextView typeTextView;
	private TextView balanceTextView;

	private String panString = "";
	private String amountString = "";
	private String typeString = "";

	GlobalData data;

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
		View view = inflater.inflate(R.layout.frag_edep, null);

		loadBtn = (Button) view.findViewById(R.id.pbocLoadBtn);

		loadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Fragment newContent = null;
				String title = null;

				newContent = new chargeFragment();
				title = getString(R.string.charge);

				if (getActivity() == null) {
					return;
				}
				if (getActivity() instanceof MainActivity) {
					MainActivity fca = (MainActivity) getActivity();
					fca.switchConent(newContent, title);
				}
			}
		});

		readBtn = (Button) view.findViewById(R.id.pbocFreshBtn);
		readBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				data = GlobalData.getInstance(getActivity().getApplicationContext());
				data.setCurrent_action(action.QUERY_EDEP);

				if (data.getProgress().isAdded()) {
					data.getProgress().getDialog().show();
					return;
				}

				if (data.isNFCMethod()) {

					Bundle args = new Bundle();
					args.putString(data.getProgress().msg, "请将卡片靠近NFC");

					data.getProgress().setArguments(args);
					data.getProgress().show(getFragmentManager(), "progressDialog");

					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							SystemClock.sleep(2000);
							AppBus.getInstance().post("正在查询");
						}
					});
					thread.start();

				} else {
					Bundle args = new Bundle();
					args.putString(data.getProgress().msg, "蓝牙通讯中");

					data.getProgress().setArguments(args);
					data.getProgress().show(getFragmentManager(), "progressDialog");
					
					CtticCard.getInstance(DeviceType.BLE).registerCtticReader(data.getgBleReader());
					
					CtticCard.getInstance(DeviceType.BLE).requestQueryCard(new QueryCardCallBack() {

						@Override
						public void onReceiveQueryCard(int code, CtticCardInfo ctticCardInfo) {
							// TODO Auto-generated method stub
							MXLog.i(TAG, "code is " + code);
							if (code != 0) {
								data.getProgress().dismiss();
								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												"查询失败请重试", Toast.LENGTH_LONG).show();
									}
								});
							} else {
								MXLog.i(TAG, ctticCardInfo.toString());

								final CtticCardInfo info = ctticCardInfo;

								Thread thread = new Thread(new Runnable() {
									@Override
									public void run() {
										SystemClock.sleep(500);
										AppBus.getInstance().post(info);
									}
								});
								thread.start();

							}

						}

					});
				}
			}
		});

		mAdapter = new logListAdapter(getActivity().getApplicationContext(), logList);

		mListView = (ListView) view.findViewById(R.id.log_pboc_list);
		mListView.setAdapter(mAdapter);

		panTextView = (TextView) view.findViewById(R.id.text_pan_view);
		panTextView.setText(panString);
		typeTextView = (TextView) view.findViewById(R.id.text_type_view);
		typeTextView.setText(typeString);
		balanceTextView = (TextView) view.findViewById(R.id.text_balance_view);
		balanceTextView.setText(amountString);

		data = GlobalData.getInstance(getActivity().getApplicationContext());
		data.setCurrent_action(action.QUERY_EDEP);

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

	@Subscribe
	public void setContent(CtticCardInfo cardInfo) {
		MXLog.i(TAG, cardInfo.toString());
		
		logList.clear();
		panString = cardInfo.getCardId();
		typeString = cardInfo.getType();
		panTextView.setText(panString);
		amountString = MXBaseUtil.stringMoneyTrans(cardInfo.getUseBalance(), 16);
		balanceTextView.setText(amountString);
		typeTextView.setText(typeString);

		List<String> list = cardInfo.getListCardEPRecords();

		for (String logItem : list) {
			LogInfo info = new LogInfo();

			EDEPLog log = EdepUtil.paseLog(logItem);

			if (log.getTradeNO() == 0)
				continue;

			info.amountString = log.getTradeAmount();
			info.dateString = log.getTradeDate();
			info.typeString = log.getTradeType();

			logList.add(info);
		}

		mAdapter.notifyDataSetChanged();

		if (data.getProgress().getDialog() != null && data.getProgress().getDialog().isShowing())
			data.getProgress().getDialog().dismiss();
	}

	class LogInfo {
		public String amountString;
		public String typeString;
		public String dateString;

		LogInfo() {
		}
	}

	class logListAdapter extends CommonAdapter<LogInfo> {
		public class ViewHolder {
			public TextView amountView;
			public TextView typeView;
			public TextView dateView;
		}

		ViewHolder holder;

		public logListAdapter(Context context, List<LogInfo> list) {
			super(context, list);
		}

		@Override
		protected View noConvertView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.list_item_log_list, parent, false);
			holder = new ViewHolder();
			holder.amountView = (TextView) convertView.findViewById(R.id.log_amount);
			holder.typeView = (TextView) convertView.findViewById(R.id.log_type);
			holder.dateView = (TextView) convertView.findViewById(R.id.log_date);
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
			holder.amountView.setText(list.get(position).amountString);
			holder.dateView.setText(list.get(position).dateString);
			holder.typeView.setText(list.get(position).typeString);
			return convertView;
		}
	}
}
