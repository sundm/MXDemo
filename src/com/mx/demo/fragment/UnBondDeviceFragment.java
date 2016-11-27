package com.mx.demo.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.mx.demo.GlobalData;
import com.mx.demo.R;

public class UnBondDeviceFragment extends Fragment {

	private Button onBoundBtn;

	final private static String TAG = "UnBondDeviceFragment";

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

		view = inflater.inflate(R.layout.frag_device_bound_main, null);
		onBoundBtn = (Button) view.findViewById(R.id.setBound);
		onBoundBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String title = getString(R.string.blelist);

				FragmentTransaction ft2 = getFragmentManager().beginTransaction();
				ft2.replace(R.id.content_frame, new deviceListFragment(), title);
				ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft2.addToBackStack(null);
				ft2.commit();
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

	@Override
	public void onResume() {
		super.onResume();
		final GlobalData data = GlobalData.getInstance(getActivity().getApplicationContext());
		if (data.isBound()) {
			String title = getString(R.string.device);
			FragmentTransaction ft2 = getFragmentManager().beginTransaction();
			ft2.replace(R.id.content_frame, new BondDeviceFragment(), title);
			ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft2.addToBackStack(null);
			ft2.commit();
		}
	}
}
