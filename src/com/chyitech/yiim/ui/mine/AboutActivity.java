package com.chyitech.yiim.ui.mine;

import android.os.Bundle;
import android.os.Message;
import android.webkit.WebView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.ui.base.CustomTitleActivity;

public class AboutActivity extends CustomTitleActivity {
	private WebView mAbout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_about);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mAbout = (WebView) findViewById(R.id.about);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		//mAbout.getSettings().setJavaScriptEnabled(true);
		mAbout.loadUrl("file:///android_asset/about.html");
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}
}
