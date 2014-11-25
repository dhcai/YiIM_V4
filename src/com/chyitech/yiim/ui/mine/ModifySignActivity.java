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

public class ModifySignActivity extends CustomTitleActivity implements
		YiXmppVCardListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;

	private EditText mSign;

	private YiXmppVCard mVCard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_modify_sign);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			if (!isStringInvalid(mVCard.getSign())) {
				mSign.setText(mVCard.getSign());
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		if (mSign.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.str_empty_nickname));
			return;
		}

		mVCard.setSign(mSign.getText().toString());
		mVCard.store(new YiXmppVCardListener() {
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ModifySignActivity.this.finish();
			}

			@Override
			public void onFailed() {
				// TODO Auto-generated method stub
				showMsgDialog(getString(R.string.str_save_sign_failed));
			}
		});
	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mSign = (EditText) findViewById(R.id.sign);
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
