package com.chyitech.yiim.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.CheckSwitchButton;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.chyitech.yiim.ui.mine.MsgReceiptActivity;
import com.chyitech.yiim.ui.mine.MsgTipActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 设置界面
 * @author saint
 *
 */
public class SettingsActivity extends CustomTitleActivity implements OnCheckedChangeListener {
	private CheckSwitchButton mAutoJoinSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
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
		mAutoJoinSwitch = (CheckSwitchButton)findViewById(R.id.settings_room_autojoin);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if(userInfo != null) {
			mAutoJoinSwitch.setChecked(userInfo.autoJoinEnabled(), false);
		}
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mAutoJoinSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	public void onMsgReceiptClick(View v) {
		Intent intent = new Intent(this, MsgReceiptActivity.class);
		startActivity(intent);
	}
	
	public void onMsgTipClick(View v) {
		Intent intent = new Intent(this, MsgTipActivity.class);
		startActivity(intent);
	}
	
	//清除所有聊天记录
	public void onClearChatRecord(View v) {
		showMsgDialog(null, getString(R.string.str_clear_msg_confirm),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						YiIMSDK.getInstance().removeAllMessage();
					}
				}, null);
	}
	
	//清除所有的消息列表
	public void onClearSessionRecord(View v) {
		showMsgDialog(null, getString(R.string.str_clear_msg_conversation_confirm),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						YiIMSDK.getInstance().removeAllConversation();
					}
				}, null);
	}
	
	//清除图片缓存
	public void onClearImageCache(View v) {
		showMsgDialog(null, getString(R.string.str_clear_msg_imagecache_confirm),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						ImageLoader.getInstance().clearDiskCache();
						ImageLoader.getInstance().clearMemoryCache();
					}
				}, null);
	}
	
	//清除VCard缓存
	public void onClearAllUsersInfo(View v) {
		showMsgDialog(null, getString(R.string.str_clear_msg_userinfo_confirm),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						YiXmppVCard.clearVCardCache(SettingsActivity.this);
					}
				}, null);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(buttonView == mAutoJoinSwitch) {
			YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
			if(userInfo != null) {
				userInfo.enableAutoJoin(mAutoJoinSwitch.isChecked());
				YiIMSDK.getInstance().setAutoJoinRoom(mAutoJoinSwitch.isChecked());
				userInfo.persist(this);
			}
		}
	}
}
