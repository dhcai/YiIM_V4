package com.chyitech.yiim.ui.contact;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.ui.base.CustomTitleActivity;

public class SetMemoActivity extends CustomTitleActivity implements
		YiXmppVCardListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;

	private EditText mEditText;
	private YiXmppVCard mVCard;
	private String mJid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_set_memo);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			updateVCard();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mEditText = (EditText) findViewById(R.id.set_memo_edittext);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mJid = getIntent().getStringExtra("jid");

		mVCard = new YiXmppVCard();
		loadVCard(false);
		setTitleBarRightBtnText(getString(R.string.str_save));
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().setMemo(mJid, mEditText.getText().toString());
		finish();
	}

	protected void loadVCard(boolean forceNetwork) {
		mVCard.load(this, mJid, forceNetwork, true, this);
	}

	protected void updateVCard() {
		if (!mVCard.isExist()) {
			return;
		}
		mEditText.setText(mVCard.getMemo());
	}

	@Override
	public void onSuccess() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_VCARD_COMPELETE);
	}

	@Override
	public void onFailed() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_VCARD_COMPELETE);
	}
}
