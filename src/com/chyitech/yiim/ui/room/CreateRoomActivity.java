package com.chyitech.yiim.ui.room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppCmd;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppError;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.ikantech.support.util.YiUtils;

public class CreateRoomActivity extends CustomTitleActivity {
	private static final int REQ_INVITE = 0x01;

	private ImageButton mIcon1;
	private ImageButton mIcon2;
	private ImageButton mIcon3;
	private ImageButton mIcon4;

	private RoomIcon mSelectedIcon = RoomIcon.DEFAULIT_4;
	private ImageButton mLastSelectedIcon = null;

	private EditText mRoomNameEditText;
	private EditText mRoomSignEditText;
	private EditText mRoomWelcomeEditText;

	private String[] mSelectedFriends;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_create_room);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQ_INVITE && resultCode == RESULT_OK) {
			mSelectedFriends = data.getStringArrayExtra("friends");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
		cancelProgressDialog();
		if (result.what.equals(YiXmppCmd.XMPP_CREATE_ROOM)) {
			if (result.success()) {
				if (mSelectedFriends != null) {
					for (String friend : mSelectedFriends) {
						if (!YiUtils.isStringInvalid(friend)) {
							YiIMSDK.getInstance().inviteRoomMemberByName(
									mRoomNameEditText.getText().toString()
											.trim(), friend);
						}
					}
				}
				showMsgDialog(getString(R.string.str_room_create_success),
						getString(R.string.str_ok), new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								finish();
							}
						});
			} else if (result.error
					.equals(YiXmppError.XMPP_ERR_ITEM_ALREADY_EXIST)) {
				showMsgDialog(R.string.err_create_room_already_exist);
			} else {
				showMsgDialog(R.string.err_create_room_failed);
			}
		}
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mIcon1 = (ImageButton) findViewById(R.id.create_room_icon1);
		mIcon1.setSelected(false);
		mIcon2 = (ImageButton) findViewById(R.id.create_room_icon2);
		mIcon2.setSelected(true);
		mIcon3 = (ImageButton) findViewById(R.id.create_room_icon3);
		mIcon3.setSelected(true);
		mIcon4 = (ImageButton) findViewById(R.id.create_room_icon4);
		mIcon4.setSelected(true);

		mLastSelectedIcon = mIcon1;

		mRoomNameEditText = (EditText) findViewById(R.id.create_room_room_name);
		mRoomSignEditText = (EditText) findViewById(R.id.create_room_room_sign);
		mRoomWelcomeEditText = (EditText) findViewById(R.id.create_room_room_welcome);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		setTitleBarRightBtnText(getString(R.string.str_create));
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
		if (isStringInvalid(mRoomNameEditText.getText().toString().trim())) {
			showMsgDialog(R.string.err_empty_room_name);
			return;
		}

		if (isStringInvalid(mRoomSignEditText.getText().toString().trim())) {
			showMsgDialog(R.string.err_empty_room_sign);
			return;
		}

		if (isStringInvalid(mRoomWelcomeEditText.getText().toString().trim())) {
			showMsgDialog(R.string.err_empty_room_welcome);
			return;
		}

		try {
			String name = mRoomNameEditText.getText().toString().trim();

			YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
			roomInfo.setName(name);
			roomInfo.setSign(mRoomSignEditText.getText().toString().trim());
			roomInfo.setIcon(mSelectedIcon.toString());
			roomInfo.setWelcome(mRoomWelcomeEditText.getText().toString()
					.trim());
			YiIMSDK.getInstance().createRoom(roomInfo, this);
			showProgressDialog(R.string.str_room_creating);
		} catch (Exception e) {
		}
	}

	public void onInviteFriendClick(View v) {
		Intent intent = new Intent(CreateRoomActivity.this,
				InviteFriendActivity.class);
		startActivityForResult(intent, REQ_INVITE);
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
