package com.mx.data;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class AppBus extends Bus {
	private static AppBus bus;

	private final Handler handler = new Handler(Looper.getMainLooper());

	@Override
	public void post(final Object event) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			super.post(event);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					AppBus.super.post(event);
				}
			});
		}
	}

	public static AppBus getInstance() {
		if (bus == null) {
			bus = new AppBus();
		}
		return bus;
	}
}
