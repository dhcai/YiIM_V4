package com.chyitech.yiim.ui.mine;

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

public class ModifyNicknameActivity extends CustomTitleActivity implements
		YiXmppVCardListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;

	private EditText mNickname;
	private YiXmppVCard mVCard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_modify_nickname);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			if (!isStringInvalid(mVCard.getNickname())) {
				mNickname.setText(mVCard.getNickname());
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		if (isStringInvalid(mNickname.getText())) {
			showMsgDialog(this.getString(R.string.str_empty_nickname));
			return;
		}

		if (!mNickname.getText().toString().equals(mVCard.getNickname())) {
			mVCard.setNickname(mNickname.getText().toString());
			mVCard.store(new YiXmppVCardListener() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					ModifyNicknameActivity.this.finish();
				}

				@Override
				public void onFailed() {
					// TODO Auto-generated method stub
					showMsgDialog(getString(R.string.str_save_nickname_failed));
				}
			});
		} else {
			finish();
		}
	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mNickname = (EditText) findViewById(R.id.nickname);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		setTitleBarRightBtnText(this.getString(R.string.str_done));
		mVCard = new YiXmppVCard();
		mVCard.load(this, YiIMSDK.getInstance().getCurrentUserJid(), false,
				true, this);
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
