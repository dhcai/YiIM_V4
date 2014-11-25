package com.chyitech.yiim.ui.contact;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.chyitech.yiim.common.ViewImageDialog;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.ui.ChatActivity;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.ui.base.MsgRecordActivity;
import com.chyitech.yiim.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiUtils;

public class UserInfoActivity extends CustomTitleFragmentActivity implements
		YiXmppVCardListener, OnCheckedChangeListener, ActionSheetListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;
	private static final int MSG_UPDATE = 0x02;

	private String mJid;
	private String mWhichActivity;
	private YiXmppVCard mVCard;

	private PullToRefreshScrollView mPullToRefreshScrollView;

	private NativeReceiver mNativeReceiver;

	private ImageView mAvatarImageView;
	private TextView mNickNameTextView;
	private TextView mAccountTextView;

	private TextView mDistrictTextView;
	private TextView mGenderTextView;
	private TextView mMemoTextView;
	private TextView mSignTextView;

	private View mRosterRootView;
	private CheckSwitchButton mBlockMsgSwitch;
	private CheckSwitchButton mReceiptRequestSwitch;
	private CheckSwitchButton mReceiptResponseSwitch;

	private boolean mInited = false;

	private ViewImageDialog mImageDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_userinfo);
		super.onCreate(savedInstanceState);

		initViews();
		initDatas();
		installListeners();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			mPullToRefreshScrollView.onRefreshComplete();
			updateVCard();
			updateSwitch();
			mInited = true;
			break;
		case MSG_UPDATE:
			loadVCard(false);
			break;
		default:
			break;
		}
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.userinfo_rootview);

		mAvatarImageView = (ImageView) findViewById(R.id.userinfo_avatar);
		mNickNameTextView = (TextView) findViewById(R.id.userinfo_nickname);
		mAccountTextView = (TextView) findViewById(R.id.userinfo_account);

		mDistrictTextView = (TextView) findViewById(R.id.userinfo_district);
		mGenderTextView = (TextView) findViewById(R.id.userinfo_gender);
		mMemoTextView = (TextView) findViewById(R.id.userinfo_memo);
		mSignTextView = (TextView) findViewById(R.id.userinfo_sign);

		mRosterRootView = findViewById(R.id.userinfo_roster_rootview);
		mBlockMsgSwitch = (CheckSwitchButton) findViewById(R.id.userinfo_block_msg);
		mReceiptRequestSwitch = (CheckSwitchButton) findViewById(R.id.userinfo_msg_receipt_request);
		mReceiptResponseSwitch = (CheckSwitchButton) findViewById(R.id.userinfo_msg_receipt_response);

	}

	protected void initDatas() {
		// TODO Auto-generated method stub
		setTheme(R.style.ActionSheetStyleIOS7);

		mWhichActivity = getIntent().getStringExtra("which");
		mJid = getIntent().getStringExtra("jid");
		if (!YiIMSDK.getInstance().getCurrentUserJid().equals(mJid)) {
			setTitleBarRightImageBtnSrc(R.drawable.btn_title_menu_selector);
		}

		mVCard = new YiXmppVCard();
	}

	protected void installListeners() {
		// TODO Auto-generated method stub
		mBlockMsgSwitch.setOnCheckedChangeListener(this);
		mReceiptRequestSwitch.setOnCheckedChangeListener(this);
		mReceiptResponseSwitch.setOnCheckedChangeListener(this);

		mPullToRefreshScrollView
				.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ScrollView> refreshView) {
						// TODO Auto-generated method stub
						// 清除图片缓存
						YiAsyncImageLoader.removeMemoryCache(mJid);
						if (mImageDialog != null) {
							mImageDialog.clearCache();
						}

						loadVCard(true);
					}
				});

		mNativeReceiver = new NativeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(YiXmppConstant.NOTIFICATION_ON_ROSTER_UPDATED);
		registerReceiver(mNativeReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mNativeReceiver);
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		loadVCard(false);
	}

	public void onAvatarClick(View v) {
		File file = new File(YiXmppVCard.getAvatarPathByJid(mJid));
		if (file.exists()) {
			if (mImageDialog == null) {
				mImageDialog = new ViewImageDialog(this,
						R.style.ImageViewDialog);
			}
			mImageDialog.setBitmapPath(Uri.fromFile(file).toString());
			mImageDialog.show();
		}
	}

	protected void loadVCard(boolean forceNetwork) {
		mVCard.load(this, mJid, forceNetwork, true, this);
	}

	protected void updateSwitch() {
		mBlockMsgSwitch.setChecked(
				YiIMSDK.getInstance().isBlockedMessage(mJid), false);
		mReceiptRequestSwitch.setChecked(YiIMSDK.getInstance()
				.requestMessageReceipt(mJid), false);
		mReceiptResponseSwitch.setChecked(YiIMSDK.getInstance()
				.responseMessageReceipt(mJid), false);
	}

	protected void updateVCard() {
		if (!mVCard.isExist()) {
			return;
		}

		if (!mVCard.isRoster()
				&& ChatActivity.class.getSimpleName().equals(mWhichActivity)
				&& mInited
				&& !mJid.equals(YiIMSDK.getInstance().getCurrentUserJid())) {
			setResult(Activity.RESULT_OK);
			finish();
		}

		YiAsyncImageLoader.loadBitmapFromStore(mJid,
				new YiImageLoaderListener() {
					@Override
					public void onImageLoaded(String url, Bitmap bitmap) {
						// TODO Auto-generated method stub
						mAvatarImageView.setImageBitmap(bitmap);
					}
				});

		if (!YiUtils.isStringInvalid(mVCard.getNickname())) {
			mNickNameTextView.setText(mVCard.getNickname());
		} else {
			mNickNameTextView.setText(StringUtils.escapeUserHost(mJid));
		}

		mAccountTextView.setText(getString(R.string.str_account_format,
				StringUtils.escapeUserHost(mJid)));

		if (!YiUtils.isStringInvalid(mVCard.getGender())) {
			if (YiXmppConstant.MALE.equals(mVCard.getGender())
					|| "m".equals(mVCard.getGender())) {
				mGenderTextView.setText(getString(R.string.str_male));
			} else {
				mGenderTextView.setText(getString(R.string.str_female));
			}
		}

		StringBuilder builder = new StringBuilder();
		if (!YiUtils.isStringInvalid(mVCard.getCountry())) {
			builder.append(mVCard.getCountry());
		}

		if (!YiUtils.isStringInvalid(mVCard.getProvince())) {
			builder.append(" ");
			builder.append(mVCard.getProvince());
		}

		if (!YiUtils.isStringInvalid(mVCard.getAddress())) {
			builder.append(" ");
			builder.append(mVCard.getAddress());
		}
		mDistrictTextView.setText(builder.toString());

		if (!YiUtils.isStringInvalid(mVCard.getSign())) {
			mSignTextView.setText(mVCard.getSign());
		}

		if (!YiUtils.isStringInvalid(mVCard.getMemo())) {
			mMemoTextView.setText(mVCard.getMemo());
		}

		if (mVCard.isRoster()
				&& !YiIMSDK.getInstance().getCurrentUserJid().equals(mJid)) {
			mRosterRootView.setVisibility(View.VISIBLE);
		} else {
			mRosterRootView.setVisibility(View.GONE);
		}
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView == mBlockMsgSwitch) {
			if (mBlockMsgSwitch.isChecked()) {
				YiIMSDK.getInstance().blockMessage(mJid);
			} else {
				YiIMSDK.getInstance().cancelBlockMessage(mJid);
			}
		} else if (buttonView == mReceiptRequestSwitch) {
			YiIMSDK.getInstance().setMessageReceipt(mJid,
					mReceiptRequestSwitch.isChecked(),
					mReceiptResponseSwitch.isChecked());
		} else if (buttonView == mReceiptResponseSwitch) {
			YiIMSDK.getInstance().setMessageReceipt(mJid,
					mReceiptRequestSwitch.isChecked(),
					mReceiptResponseSwitch.isChecked());
		}
	}

	public void onModifyGroupClick(View v) {
		Intent intent = new Intent(UserInfoActivity.this,
				ContactGroupActivity.class);
		intent.putExtra("jid", mJid);
		intent.putExtra("mode", "modify");
		intent.putExtra("groupName", mVCard.getGroupName());
		startActivity(intent);
	}

	public void onViewMsgRecordClick(View v) {
		Intent intent = new Intent(UserInfoActivity.this,
				MsgRecordActivity.class);
		intent.putExtra("jid", mJid);
		startActivity(intent);
	}

	@Override
	public void onTitleBarRightImgBtnClick(View view) {
		// TODO Auto-generated method stub
		showActionSheet();
	}

	protected void showActionSheet() {
		if (mVCard.isRoster()) {
			if (mVCard.verified()) {
				ActionSheet
						.createBuilder(this, getSupportFragmentManager())
						.setCancelButtonTitle(getString(R.string.str_cancel))
						.setOtherButtonTitles(
								getString(R.string.str_delete_friend),
								getString(R.string.str_set_memo))
						.setCancelableOnTouchOutside(true).setListener(this)
						.show();
			} else {
				ActionSheet
						.createBuilder(this, getSupportFragmentManager())
						.setCancelButtonTitle(getString(R.string.str_cancel))
						.setOtherButtonTitles(
								getString(R.string.str_delete_friend),
								getString(R.string.str_set_memo),
								getString(R.string.str_re_subscript))
						.setCancelableOnTouchOutside(true).setListener(this)
						.show();
			}
		} else {
			ActionSheet
					.createBuilder(this, getSupportFragmentManager())
					.setCancelButtonTitle(getString(R.string.str_cancel))
					.setOtherButtonTitles(getString(R.string.str_add_as_friend))
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
		if (mVCard.isRoster()) {
			switch (index) {
			case 0:
				showMsgDialog(
						null,
						getString(R.string.str_delete_friend_tip,
								mVCard.displayName()),
						getString(R.string.str_ok),
						getString(R.string.str_cancel),
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								YiIMSDK.getInstance().removeFriend(mJid);
							}
						}, null);
				break;
			case 1: {
				Intent intent = new Intent(UserInfoActivity.this,
						SetMemoActivity.class);
				intent.putExtra("jid", mJid);
				startActivity(intent);
				break;
			}
			case 2:
				if (!mVCard.verified()) {
					YiIMSDK.getInstance().reRequestAuthorization(mJid);
				}
				break;
			default:
				break;
			}
		} else {
			if (index == 0) {
				YiIMSDK.getInstance().addFriend(mJid, null, null);
			}
		}
	}

	private class NativeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					YiXmppConstant.NOTIFICATION_ON_ROSTER_UPDATED)) {
				getHandler().removeMessages(MSG_UPDATE);
				getHandler().sendEmptyMessageDelayed(MSG_UPDATE, 200);
			}
		}
	}
}
