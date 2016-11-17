package com.mx.demo.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mx.data.AppBus;
import com.mx.data.BusJsonData;
import com.mx.demo.R;
import com.squareup.otto.Subscribe;

public class progressDialogFragment extends DialogFragment {
	private TextView msgTextView;
	private View view;
	public String msg = "progress_msg";

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
		view = inflater.inflate(R.layout.progress_dialog, container);

		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		getDialog().setCanceledOnTouchOutside(false);

		msgTextView = (TextView) view.findViewById(R.id.id_tv_loadingmsg);

		msgTextView.setText(getArguments().getString(msg));

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		// 注册到bus事件总线中
		System.out.println("=AppBus= register");
		AppBus.getInstance().register(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		System.out.println("=AppBus= unregister");
		AppBus.getInstance().unregister(this);
	}

	@Subscribe
	public void setContent(String data) {
		System.out.println("====" + data);
		msgTextView.setText(data);
	}
}
