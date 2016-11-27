package com.mx.demo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cttic.se.CtticReader;
import com.mx.demo.GlobalData;
import com.mx.demo.MainActivity;
import com.mx.demo.R;

public class NFCDeviceFragment extends Fragment {
	private Button onSetMethodBtn;

	final private static String TAG = "NFCDeviceFragment";

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
		View view = null;

		view = inflater.inflate(R.layout.frag_device_nfc, null);
		onSetMethodBtn = (Button) view.findViewById(R.id.setMethod);
		onSetMethodBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Fragment newContent = null;
				String title = null;

				newContent = new MySettingsFragment();
				title = getString(R.string.settings);

				if (getActivity() == null) {
					return;
				}
				if (getActivity() instanceof MainActivity) {
					MainActivity fca = (MainActivity) getActivity();
					fca.switchConent(newContent, title);
				}

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
