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

public class ModifyRegionActivity extends CustomTitleActivity implements
		YiXmppVCardListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;

	private EditText mCountry;
	private EditText mProvinc;

	private YiXmppVCard mVCard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_modify_region);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			if (!isStringInvalid(mVCard.getCountry())) {
				mCountry.setText(mVCard.getCountry());
			}

			if (!isStringInvalid(mVCard.getProvince())) {
				mProvinc.setText(mVCard.getProvince());
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		if (mCountry.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.str_empty_country));
			return;
		}

		if (mProvinc.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.str_empty_province));
			return;
		}

		mVCard.setCountry(mCountry.getText().toString());
		mVCard.setProvince(mProvinc.getText().toString());
		mVCard.store(new YiXmppVCardListener() {
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				ModifyRegionActivity.this.finish();
			}

			@Override
			public void onFailed() {
				// TODO Auto-generated method stub
				showMsgDialog(getString(R.string.str_save_region_failed));
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
		mCountry = (EditText) findViewById(R.id.country);
		mProvinc = (EditText) findViewById(R.id.province);
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
