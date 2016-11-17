package com.mx.demo.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.mx.cttic.card.PbocCardInfo;
import com.mx.data.AppBus;
import com.mx.demo.GlobalData;
import com.mx.demo.GlobalData.action;
import com.mx.demo.R;
import com.mx.demo.adapter.CommonAdapter;
import com.mx.util.MXBaseUtil;
import com.mx.util.MXLog;
import com.squareup.otto.Subscribe;

public class pbocFragment extends Fragment {

	final static String TAG = "pboc";
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
		View view = inflater.inflate(R.layout.frag_pboc, null);

		loadBtn = (Button) view.findViewById(R.id.pbocLoadBtn);

		loadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		readBtn = (Button) view.findViewById(R.id.pbocFreshBtn);
		readBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				data = GlobalData.getInstance(getActivity().getApplicationContext());
				data.setCurrent_action(action.QUERY_PBOC);

				if (data.isNFCMethod()) {
					if (data.getProgress().isAdded()) {
						data.getProgress().getDialog().show();
						return;
					}
					Bundle args = new Bundle();
					args.putString(data.getProgress().msg, "请将卡片靠近NFC");

					data.getProgress().setArguments(args);
					data.getProgress().show(getFragmentManager(), "progressDialog");

					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							SystemClock.sleep(2000);
							AppBus.getInstance().post("查询中");
						}
					});
					thread.start();

				} else {

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
		data.setCurrent_action(action.QUERY_PBOC);

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
		// 娉ㄥ�����bus浜�浠舵�荤嚎涓�
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
	public void setContent(PbocCardInfo cardInfo) {
		MXLog.i(TAG, cardInfo.toString());
		logList.clear();
		panString = cardInfo.getPan();
		panTextView.setText(panString);
		amountString = MXBaseUtil.stringMoneyTrans(cardInfo.getBalance(), 10);
		balanceTextView.setText(amountString);
		typeTextView.setText(typeString);

		List<Map<String, String>> list = cardInfo.getListCardECRecords();

		for (Map<String, String> iteMap : list) {
			LogInfo info = new LogInfo();
			String dateString = iteMap.containsKey("9A") ? iteMap.get("9A") : "";
			String timeString = iteMap.containsKey("9F21") ? iteMap.get("9F21") : "";
			if (dateString.isEmpty() || timeString.isEmpty()) {
				info.dateString = "";
			} else {
				SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
				java.util.Date date;
				try {
					date = format.parse(dateString + timeString);
					SimpleDateFormat format1 = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
					info.dateString = format1.format(date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			String amountString = MXBaseUtil.stringMoneyTrans(iteMap.containsKey("9F02") ? iteMap.get("9F02") : "0000",
					10);
			info.amountString = amountString;
			String typeString = iteMap.containsKey("9C") ? iteMap.get("9C") : "";
			if (typeString.equals("60")) {
				info.typeString = "���瀛�";
			} else if (typeString.equals("00")) {
				info.typeString = "娑�璐�";
			} else {
				info.typeString = "��朵��";
			}

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
