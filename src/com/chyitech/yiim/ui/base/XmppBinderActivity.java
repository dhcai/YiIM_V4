package com.chyitech.yiim.ui.base;

import android.os.Bundle;
import android.view.WindowManager;

import com.chyitech.yiim.app.YiIMApplication;
import com.chyitech.yiim.sdk.api.YiXmppListener;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.ikantech.support.ui.YiUIBaseActivity;

public abstract class XmppBinderActivity extends YiUIBaseActivity implements
		YiXmppListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// auto hide keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * 将结果post到UI线程中去
	 */
	@Override
	public void onXmppResonpse(final YiXmppResult result) {
		// TODO Auto-generated method stub
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				onUIXmppResponse(result);
			}
		});
	}

	protected abstract void onUIXmppResponse(YiXmppResult result);

	protected YiIMApplication getYiIMApplication() {
		return (YiIMApplication) getApplication();
	}

	
}
