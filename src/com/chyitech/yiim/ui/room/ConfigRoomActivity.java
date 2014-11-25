package com.chyitech.yiim.ui.room;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.ui.base.CustomTitleActivity;

public class ConfigRoomActivity extends CustomTitleActivity {
	private ImageButton mIcon1;
	private ImageButton mIcon2;
	private ImageButton mIcon3;
	private ImageButton mIcon4;

	private RoomIcon mSelectedIcon = RoomIcon.DEFAULIT_4;
	private ImageButton mLastSelectedIcon = null;

	private EditText mRoomSignEditText;
	private EditText mRoomWelcomeEditText;

	private String mJid;
	private YiXmppRoomInfo mRoomInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_config_room);
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
		mIcon1 = (ImageButton) findViewById(R.id.config_room_icon1);
		mIcon1.setSelected(false);
		mIcon2 = (ImageButton) findViewById(R.id.config_room_icon2);
		mIcon2.setSelected(true);
		mIcon3 = (ImageButton) findViewById(R.id.config_room_icon3);
		mIcon3.setSelected(true);
		mIcon4 = (ImageButton) findViewById(R.id.config_room_icon4);
		mIcon4.setSelected(true);

		mLastSelectedIcon = mIcon1;

		mRoomSignEditText = (EditText) findViewById(R.id.config_room_room_sign);
		mRoomWelcomeEditText = (EditText) findViewById(R.id.config_room_room_welcome);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		setTitleBarRightBtnText(getString(R.string.str_save));
		mJid = getIntent().getStringExtra("jid");

		mRoomInfo = new YiXmppRoomInfo();
		mRoomInfo.load(this, mJid, false, true, new YiXmppRoomInfoListener() {

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				getHandler().post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mRoomSignEditText.setText(mRoomInfo.getSign());
						mRoomWelcomeEditText.setText(mRoomInfo.getWelcome());

						RoomIcon icon = RoomIcon.eval(mRoomInfo.getIcon());
						if (icon.equals(RoomIcon.DEFAULIT_1)) {
							onIconBtnClick(mIcon2);
						} else if (icon.equals(RoomIcon.DEFAULIT_2)) {
							onIconBtnClick(mIcon3);
						} else if (icon.equals(RoomIcon.DEFAULIT_3)) {
							onIconBtnClick(mIcon4);
						} else if (icon.equals(RoomIcon.DEFAULIT_4)) {
							onIconBtnClick(mIcon1);
						}
					}
				});
			}

			@Override
			public void onFailed() {
				// TODO Auto-generated method stub
				ConfigRoomActivity.this.finish();
			}
		});
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
		if (isStringInvalid(mRoomSignEditText.getText().toString().trim())) {
			showMsgDialog(R.string.err_empty_room_sign);
			return;
		}

		if (isStringInvalid(mRoomWelcomeEditText.getText().toString().trim())) {
			showMsgDialog(R.string.err_empty_room_welcome);
			return;
		}

		try {

			mRoomInfo.setSign(mRoomSignEditText.getText().toString().trim());
			mRoomInfo.setIcon(mSelectedIcon.toString());
			mRoomInfo.setWelcome(mRoomWelcomeEditText.getText().toString()
					.trim());
			mRoomInfo.store(this, new YiXmppRoomInfoListener() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					ConfigRoomActivity.this.finish();
				}

				@Override
				public void onFailed() {
					// TODO Auto-generated method stub
					showMsgDialog(R.string.err_config_room_failed);
				}
			});
		} catch (Exception e) {
		}
	}

	public void onIconBtnClick(View v) {
		if (mIcon1 == v) {
			mLastSelectedIcon.setSelected(true);
			mIcon1.setSelected(false);
			mLastSelectedIcon = mIcon1;
			mSelectedIcon = RoomIcon.DEFAULIT_4;
		} else if (mIcon2 == v) {
			mLastSelectedIcon.setSelected(true);
			mIcon2.setSelected(false);
			mLastSelectedIcon = mIcon2;
			mSelectedIcon = RoomIcon.DEFAULIT_1;
		} else if (mIcon3 == v) {
			mLastSelectedIcon.setSelected(true);
			mIcon3.setSelected(false);
			mLastSelectedIcon = mIcon3;
			mSelectedIcon = RoomIcon.DEFAULIT_2;
		} else if (mIcon4 == v) {
			mLastSelectedIcon.setSelected(true);
			mIcon4.setSelected(false);
			mLastSelectedIcon = mIcon4;
			mSelectedIcon = RoomIcon.DEFAULIT_3;
		}
	}

}
