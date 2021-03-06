package com.mx.demo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.mx.cttic.card.CtticCard;
import com.mx.cttic.card.CtticCard.DeviceType;
import com.mx.cttic.card.CtticCard.QueryCardCallBack;
import com.mx.cttic.card.CtticCard.QueryPbocCardCallBack;
import com.mx.cttic.card.CtticCardInfo;
import com.mx.cttic.card.CtticTradeResult;
import com.mx.cttic.card.PbocCardInfo;
import com.mx.cttic.load.CtticLoad;
import com.mx.cttic.load.CtticLoad.MXLoadCallBack;
import com.mx.data.AppBus;
import com.mx.demo.GlobalData.action;
import com.mx.demo.fragment.BondDeviceFragment;
import com.mx.demo.fragment.LeftFragment;
import com.mx.demo.fragment.NFCDeviceFragment;
import com.mx.demo.fragment.UnBondDeviceFragment;
import com.mx.nfclib.NFCReader;
import com.mx.util.MXLog;
import com.mx.util.PayOrder;

public class MainActivity extends SlidingFragmentActivity implements OnClickListener {
	private final static String TAG = "main";

	private ImageView topButton;
	private Fragment mContent;
	private TextView topTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initSlidingMenu(savedInstanceState);

		topButton = (ImageView) findViewById(R.id.topButton);
		topButton.setOnClickListener(this);
		topTextView = (TextView) findViewById(R.id.topTv);
	}

	private void initSlidingMenu(Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mContent = getFragmentManager().getFragment(savedInstanceState, "mContent");
		}

		final GlobalData data = GlobalData.getInstance(this.getApplicationContext());
		if (data.isNFCMethod()) {
			if (mContent == null) {
				mContent = new NFCDeviceFragment();
			}
		} else if (!data.isBound()) {
			if (mContent == null) {
				mContent = new UnBondDeviceFragment();
			}
		} else {
			if (mContent == null) {
				mContent = new BondDeviceFragment();
			}
		}

		getFragmentManager().beginTransaction().replace(R.id.content_frame, mContent).commit();
		getSlidingMenu().showContent();

		setBehindContentView(R.layout.menu_frame_left);
		getFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftFragment()).commit();

		SlidingMenu sm = getSlidingMenu();

		sm.setMode(SlidingMenu.LEFT);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(null);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindScrollScale(0.0f);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchConent(Fragment fragment, String title) {
		mContent = fragment;
		getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
		topTextView.setText(title);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topButton:
			toggle();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		NFCReader.enableForegroundDispatch(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		NFCReader.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (NFCReader.initNfcEnvironment(intent) == 0) {
			MXLog.i(TAG, "init nfc environment success");

			final GlobalData data = GlobalData.getInstance(this);
			if (data.getCurrent_action() == action.QUERY_PBOC) {
				CtticCard.getInstance(DeviceType.NFC).requestQueryPbocCard(new QueryPbocCardCallBack() {

					@Override
					public void onReceiveQueryPBCard(int code, PbocCardInfo pbCardInfo) {
						// TODO Auto-generated method stub
						MXLog.i(TAG, "code is " + code);
						if (code != 0) {

						} else {
							MXLog.i(TAG, pbCardInfo.toString());

							final PbocCardInfo info = pbCardInfo;

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
			} else if (data.getCurrent_action() == action.QUERY_EDEP) {
				CtticCard.getInstance(DeviceType.NFC).requestQueryCard(new QueryCardCallBack() {

					@Override
					public void onReceiveQueryCard(int code, CtticCardInfo ctticCardInfo) {
						// TODO Auto-generated method stub
						MXLog.i(TAG, "code is " + code);
						if (code != 0) {
							
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
			} else if (data.getCurrent_action() == action.LOAD_EDEP) {

				PayOrder order = data.getOrder();
				CtticLoad.getInstance().doCtticLoad(order, DeviceType.NFC, CtticLoad.LOAD_TYPE.LOCAL,
						new MXLoadCallBack() {

							@Override
							public void onReceiveCallBack(CtticTradeResult result) {
								// TODO Auto-generated method stub
								data.getProgress().dismiss();
								MXLog.i(TAG, result.toString());
							}

							@Override
							public void onMessageOut(String msg) {
								// TODO Auto-generated method stub
								MXLog.i(TAG, "msg is " + msg);
							}
						});
			}
		} else {
			MXLog.i(TAG, "init nfc environment failed");
		}
	}
}
