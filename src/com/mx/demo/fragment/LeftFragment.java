package com.mx.demo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mx.demo.GlobalData;
import com.mx.demo.MainActivity;
import com.mx.demo.R;

public class LeftFragment extends Fragment implements OnClickListener {
	private TextView deviceTextView;
	private TextView pbocTextView;
	private TextView edepTextView;
	private TextView settingsTextView;

	private Fragment deviceFragment;
	private Fragment pbocFragment;
	private Fragment edepFragment;
	private Fragment settingFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_menu, null);
		findViews(view);

		return view;
	}

	public void findViews(View view) {
		deviceTextView = (TextView) view.findViewById(R.id.tvDevice);
		pbocTextView = (TextView) view.findViewById(R.id.tvPboc);
		edepTextView = (TextView) view.findViewById(R.id.tvEdep);
		settingsTextView = (TextView) view.findViewById(R.id.tvMySettings);

		deviceTextView.setOnClickListener(this);
		pbocTextView.setOnClickListener(this);
		edepTextView.setOnClickListener(this);
		settingsTextView.setOnClickListener(this);

		deviceTextView.setText(deviceTextView.getText() + "——未连接");
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
	public void onClick(View v) {
		String title = null;
		switch (v.getId()) {
		case R.id.tvDevice: {
			if (deviceFragment == null) {
				deviceFragment = new deviceFragment();
			}
			title = getString(R.string.device);
			switchFragment(deviceFragment, title);
			break;
		}
		case R.id.tvPboc: {
			if (pbocFragment == null) {
				pbocFragment = new pbocFragment();
			}

			title = getString(R.string.pboc);

			switchFragment(pbocFragment, title);
			break;
		}
		case R.id.tvEdep: {
			if (edepFragment == null) {
				edepFragment = new edepFragment();
			}

			title = getString(R.string.edep);
			switchFragment(edepFragment, title);
			break;
		}
		case R.id.tvMySettings: {
			if (settingFragment == null) {
				settingFragment = new MySettingsFragment();
			}
			title = getString(R.string.settings);
			switchFragment(settingFragment, title);
			break;
		}
		default:
			break;
		}

	}

	/**
	 * 切换fragment
	 * 
	 * @param fragment
	 */
	private void switchFragment(Fragment fragment, String title) {
		if (getActivity() == null) {
			return;
		}
		if (getActivity() instanceof MainActivity) {
			MainActivity fca = (MainActivity) getActivity();
			fca.switchConent(fragment, title);
		}
	}

}
