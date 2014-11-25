package com.chyitech.yiim.ui.room;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ActionSheet;
import com.chyitech.yiim.common.ActionSheet.ActionSheetListener;
import com.chyitech.yiim.common.CheckSwitchButton;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppListener;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppCmd;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppError;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.ui.ChatActivity;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.ui.base.MsgRecordActivity;
import com.chyitech.yiim.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.ikantech.support.util.YiUtils;

public class RoomInfoActivity extends CustomTitleFragmentActivity implements
		OnCheckedChangeListener, YiXmppRoomInfoListener, ActionSheetListener,
		YiXmppListener {
	private static final int MSG_LOAD_COMPELETE = 0x01;
	private static final int MSG_UPDATE = 0x02;

	private String mJid;
	private String mWhichActivity;

	private PullToRefreshScrollView mPullToRefreshScrollView;
	private ImageView mAvatarImageView;
	private TextView mNicknameTextView;
	private TextView mAccountTextView;

	private TextView mSignTextView;
	private TextView mWelcomeTextView;

	private View mRosterRootView;
	private CheckSwitchButton mBlockMsgSwitch;
	private CheckSwitchButton mAutoJoinSwitch;

	private View mAutojoinRootView;
	
	private View mInviteView;

	private YiXmppRoomInfo mRoomInfo;

	private NativeReceiver mNativeReceiver;

	private boolean mInited = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_roominfo);
		super.onCreate(savedInstanceState);

		initViews();
		initDatas();
		installListeners();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_COMPELETE:
			mPullToRefreshScrollView.onRefreshComplete();
			updateRoomInfo();
			updateSwitch();
			mInited = true;
			break;
		case MSG_UPDATE:
			loadRoomInfo(false);
			break;
		default:
			break;
		}
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.roominfo_rootview);
		mAvatarImageView = (ImageView) findViewById(R.id.roominfo_avatar);
		mNicknameTextView = (TextView) findViewById(R.id.roominfo_nickname);
		mAccountTextView = (TextView) findViewById(R.id.roominfo_account);

		mSignTextView = (TextView) findViewById(R.id.roominfo_sign);
		mWelcomeTextView = (TextView) findViewById(R.id.roominfo_welcome);

		mAutoJoinSwitch = (CheckSwitchButton) findViewById(R.id.roominfo_autojoin);
		mBlockMsgSwitch = (CheckSwitchButton) findViewById(R.id.roominfo_block_msg);

		mRosterRootView = findViewById(R.id.roominfo_roster_rootview);
		mAutojoinRootView = findViewById(R.id.roominfo_joined_rootview);
		
		mInviteView = findViewById(R.id.roominfo_invite_friend);
	}

	protected void initDatas() {
		// TODO Auto-generated method stub
		setTheme(R.style.ActionSheetStyleIOS7);

		mWhichActivity = getIntent().getStringExtra("which");
		mJid = getIntent().getStringExtra("jid");
		setTitleBarRightImageBtnSrc(R.drawable.btn_title_menu_selector);

		mRoomInfo = new YiXmppRoomInfo();
		updateSwitch();
	}

	protected void installListeners() {
		// TODO Auto-generated method stub
		mPullToRefreshScrollView
				.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ScrollView> refreshView) {
						// TODO Auto-generated method stub
						loadRoomInfo(true);
					}
				});

		mBlockMsgSwitch.setOnCheckedChangeListener(this);
		mAutoJoinSwitch.setOnCheckedChangeListener(this);

		mNativeReceiver = new NativeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(YiXmppConstant.NOTIFICATION_ON_ROOM_UPDATED);
		registerReceiver(mNativeReceiver, intentFilter);
	}

	protected void uninstallListeners() {
		// TODO Auto-generated method stub
		unregisterReceiver(mNativeReceiver);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		uninstallListeners();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		loadRoomInfo(false);
	}

	protected void loadRoomInfo(boolean loadNetwork) {
		mRoomInfo.load(this, mJid, loadNetwork, true, this);
	}

	protected void updateSwitch() {
		mAutoJoinSwitch.setChecked(YiIMSDK.getInstance().roomAutoJoin(mJid),
				false);
		mBlockMsgSwitch.setChecked(
				YiIMSDK.getInstance().isBlockedMessage(mJid), false);
	}

	protected void updateRoomInfo() {
		if (!mRoomInfo.isExist()) {
			return;
		}

		if (!mRoomInfo.isRoster()
				&& ChatActivity.class.getSimpleName().equals(mWhichActivity)
				&& mInited) {
			setResult(Activity.RESULT_OK);
			finish();
		}

		if (!YiUtils.isStringInvalid(mRoomInfo.getName())) {
			mNicknameTextView.setText(mRoomInfo.getName());
		} else {
			mNicknameTextView.setText(StringUtils.escapeUserHost(mJid));
		}

		mAccountTextView.setText(getString(R.string.str_account_format,
				StringUtils.escapeUserHost(mJid)));

		if (!YiUtils.isStringInvalid(mRoomInfo.getSign())) {
			mSignTextView.setText(mRoomInfo.getSign());
		}

		if (!YiUtils.isStringInvalid(mRoomInfo.getWelcome())) {
			mWelcomeTextView.setText(mRoomInfo.getWelcome());
		}

		RoomIcon icon = RoomIcon.eval(mRoomInfo.getIcon());
		mAvatarImageView.setImageResource(icon.getResId());

		if (mRoomInfo.isRoster()) {
			mRosterRootView.setVisibility(View.VISIBLE);
		} else {
			mRosterRootView.setVisibility(View.GONE);
		}

		if (YiIMSDK.getInstance().roomJoinedByJid(mJid)) {
			mAutojoinRootView.setVisibility(View.VISIBLE);
			if(YiIMSDK.getInstance().isRoomAdmin(mJid) || 
					YiIMSDK.getInstance().isRoomOwner(mJid)) {
				mInviteView.setVisibility(View.VISIBLE);
			}else {
				mInviteView.setVisibility(View.GONE);
			}
		} else {
			mAutojoinRootView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView == mAutoJoinSwitch) {
			YiIMSDK.getInstance().setRoomAutoJoin(mAutoJoinSwitch.isChecked(),
					mJid);
		} else if (buttonView == mBlockMsgSwitch) {
			if (mBlockMsgSwitch.isChecked()) {
				YiIMSDK.getInstance().blockMessage(mJid);
			} else {
				YiIMSDK.getInstance().cancelBlockMessage(mJid);
			}
		}
	}

	public void onModifyGroupClick(View v) {
		Intent intent = new Intent(RoomInfoActivity.this,
				RoomGroupActivity.class);
		intent.putExtra("jid", mJid);
		intent.putExtra("mode", "modify");
		intent.putExtra("groupName", mRoomInfo.getGroupName());
		startActivity(intent);
	}

	public void onViewMsgRecordClick(View v) {
		Intent intent = new Intent(RoomInfoActivity.this,
				MsgRecordActivity.class);
		intent.putExtra("jid", mJid);
		startActivity(intent);
	}

	public void onInviteFriendClick(View v) {
		Intent intent = new Intent(RoomInfoActivity.this,
				InviteFriendActivity.class);
		intent.putExtra("jid", mJid);
		intent.putExtra("which", RoomInfoActivity.class.getSimpleName());
		startActivity(intent);
	}

	public void onViewOnlineMemberClick(View v) {
		Intent intent = new Intent(RoomInfoActivity.this,
				RoomMemberActivity.class);
		intent.putExtra("jid", mJid);
		startActivity(intent);
	}

	@Override
	public void onSuccess() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_COMPELETE);
	}

	@Override
	public void onFailed() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_COMPELETE);
	}

	@Override
	public void onTitleBarRightImgBtnClick(View view) {
		// TODO Auto-generated method stub
		showActionSheet();
	}

	protected void showActionSheet() {
		String firstBtn = null;
		if (mRoomInfo.isRoster()) {
			if (YiIMSDK.getInstance().isRoomOwner(mJid)) {
				firstBtn = getString(R.string.str_destroy_room);
			} else {
				firstBtn = getString(R.string.str_exit_room);
			}
		} else {
			firstBtn = getString(R.string.str_into_room);
		}

		if (YiIMSDK.getInstance().roomJoinedByJid(mJid)) {
			if (YiIMSDK.getInstance().isRoomOwner(mJid)) {
				ActionSheet
						.createBuilder(this, getSupportFragmentManager())
						.setCancelButtonTitle(getString(R.string.str_cancel))
						.setOtherButtonTitles(firstBtn,
								getString(R.string.str_unjoin_room),
								getString(R.string.str_modify_roominfo))
						.setCancelableOnTouchOutside(true).setListener(this)
						.show();
			} else {
				ActionSheet
						.createBuilder(this, getSupportFragmentManager())
						.setCancelButtonTitle(getString(R.string.str_cancel))
						.setOtherButtonTitles(firstBtn,
								getString(R.string.str_unjoin_room))
						.setCancelableOnTouchOutside(true).setListener(this)
						.show();
			}
		} else if (mRoomInfo.isRoster()) {
			ActionSheet
					.createBuilder(this, getSupportFragmentManager())
					.setCancelButtonTitle(getString(R.string.str_cancel))
					.setOtherButtonTitles(firstBtn,
							getString(R.string.str_join_room))
					.setCancelableOnTouchOutside(true).setListener(this).show();
		} else {
			ActionSheet.createBuilder(this, getSupportFragmentManager())
					.setCancelButtonTitle(getString(R.string.str_cancel))
					.setOtherButtonTitles(firstBtn)
					.setCancelableOnTouchOutside(true).setListener(this).show();
		}
	}

	@Override
	public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOtherButtonClick(ActionSheet actionSheet, int index) {
		// TODO Auto-generated method stub
		switch (index) {
		case 0:
			if (mRoomInfo.isRoster()) {
				if (YiIMSDK.getInstance().isRoomOwner(mJid)) {
					YiIMSDK.getInstance().destroyRoom(mJid, this);
				} else {
					YiIMSDK.getInstance().removeRoom(mJid);
				}
			} else {
				YiIMSDK.getInstance().registerInRoom(mJid);
			}
			break;
		case 1:
			if (mRoomInfo.isRoster()) {
				if (YiIMSDK.getInstance().roomJoinedByJid(mJid)) {
					YiIMSDK.getInstance().leaveRoom(mJid);
					updateRoomInfo();
				} else {
					YiIMSDK.getInstance().joinRoom(mRoomInfo, this);
				}
			}
			break;
		case 2:
			if (YiIMSDK.getInstance().isRoomOwner(mJid)
					&& YiIMSDK.getInstance().roomJoinedByJid(mJid)) {
				Intent intent = new Intent(RoomInfoActivity.this,
						ConfigRoomActivity.class);
				intent.putExtra("jid", mJid);
				startActivity(intent);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onXmppResonpse(final YiXmppResult result) {
		// TODO Auto-generated method stub
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				onUIXmppResonpse(result);
			}
		});
	}

	protected void onUIXmppResonpse(YiXmppResult result) {
		if (result.what.equals(YiXmppCmd.XMPP_JOIN_ROOM)) {
			if (result.error.equals(YiXmppError.XMPP_ERR_ITEM_NOT_FOUND)) {
				showMsgDialog(R.string.err_room_not_exist);
				return;
			}
			loadRoomInfo(false);
		} else if (result.what.equals(YiXmppCmd.XMPP_DESTROY_ROOM)) {
			if (result.error.equals(YiXmppError.XMPP_ERR_CONFLICT)) {
				showMsgDialog(R.string.err_unjoin_send_msg_tip);
				return;
			}
		}
	}

	private class NativeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					YiXmppConstant.NOTIFICATION_ON_ROOM_UPDATED)) {
				getHandler().removeMessages(MSG_UPDATE);
				getHandler().sendEmptyMessageDelayed(MSG_UPDATE, 200);
			}
		}
	}
}
