package com.chyitech.yiim.ui.mine;

import android.os.Bundle;
import android.os.Message;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.CheckSwitchButton;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.ui.base.CustomTitleActivity;

public class MsgReceiptActivity extends CustomTitleActivity implements
		OnCheckedChangeListener {
	private CheckSwitchButton mMsgReceiptSwitch;
	private CheckSwitchButton mMsgReceiptRequestSwitch;
	private CheckSwitchButton mMsgReceiptResponseSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_msg_receipt);
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
		mMsgReceiptSwitch = (CheckSwitchButton) findViewById(R.id.msg_receipt_switch_all);
		mMsgReceiptRequestSwitch = (CheckSwitchButton) findViewById(R.id.msg_receipt_switch_request);
		mMsgReceiptResponseSwitch = (CheckSwitchButton) findViewById(R.id.msg_receipt_switch_response);

	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo != null) {
			mMsgReceiptSwitch.setChecked(userInfo.msgReceiptEnabled());
			mMsgReceiptRequestSwitch.setChecked(userInfo
					.msgReceiptRequestEnabled());
			mMsgReceiptResponseSwitch.setChecked(userInfo
					.msgReceiptResponseEnabled());
		}
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mMsgReceiptSwitch.setOnCheckedChangeListener(this);
		mMsgReceiptRequestSwitch.setOnCheckedChangeListener(this);
		mMsgReceiptResponseSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;
		
		if (buttonView == mMsgReceiptSwitch) {
			userInfo.enableMsgReceipt(mMsgReceiptSwitch.isChecked());
			YiIMSDK.getInstance().setMessageReceipt(mMsgReceiptSwitch.isChecked());
		} else if (buttonView == mMsgReceiptRequestSwitch) {
			userInfo.enableMsgReceiptRequest(mMsgReceiptRequestSwitch.isChecked());
			YiIMSDK.getInstance().setMessageReceiptRequest(mMsgReceiptRequestSwitch.isChecked());
		} else if (buttonView == mMsgReceiptResponseSwitch) {
			userInfo.enableMsgReceiptResponse(mMsgReceiptResponseSwitch.isChecked());
			YiIMSDK.getInstance().setMessageReceiptResponse(mMsgReceiptResponseSwitch.isChecked());
		}
		
		userInfo.persist(this);
	}
}
