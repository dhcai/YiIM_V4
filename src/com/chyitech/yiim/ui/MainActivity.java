package com.chyitech.yiim.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ActionSheet;
import com.chyitech.yiim.common.ActionSheet.ActionSheetListener;
import com.chyitech.yiim.common.YiIMConfig;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.fragment.ContactFragment;
import com.chyitech.yiim.fragment.ConversationFragment;
import com.chyitech.yiim.fragment.MineFragment;
import com.chyitech.yiim.fragment.NearByFragment;
import com.chyitech.yiim.fragment.RoomFragment;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.ui.contact.ContactGroupActivity;
import com.chyitech.yiim.ui.contact.FindFriendActivity;
import com.chyitech.yiim.ui.room.CreateRoomActivity;
import com.chyitech.yiim.ui.room.FindRoomActivity;
import com.chyitech.yiim.ui.room.RoomGroupActivity;
import com.ikantech.support.util.YiLog;
import com.ikantech.support.util.YiPrefsKeeper;

public class MainActivity extends CustomTitleFragmentActivity implements
		ActionSheetListener {
	private OnAuthedBroadcastReceiver mAuthedBroadcastReceiver;
	private OnUnReadMsgBroadcastReceiver mUnReadMsgBroadcastReceiver;
	private FragmentManager mFragmentManager;
	private RadioGroup mRadioGroup;
	private Fragment mContent = null;

	private ConversationFragment mConversationFragment = null;
	private RoomFragment mRoomFragment = null;
	private ContactFragment mContactFragment = null;
	private NearByFragment mNearByFragment = null;
	private MineFragment mMineFragment = null;

	private RadioButton mConversationBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		super.onCreate(savedInstanceState);

		initViews();
		initDatas();
		installListeners();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		uninstallListeners();
		super.onDestroy();
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mFragmentManager = getSupportFragmentManager();
		mRadioGroup = (RadioGroup) findViewById(R.id.main_tab);
		mConversationBtn = (RadioButton) findViewById(R.id.main_tab_conversation);
		mRadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						FragmentTransaction transaction = mFragmentManager
								.beginTransaction();
						Fragment fragment = null;
						switch (checkedId) {
						case R.id.main_tab_conversation:
							if (mConversationFragment == null) {
								mConversationFragment = new ConversationFragment();
							}
							fragment = mConversationFragment;
							setTitle(getString(R.string.str_conversation));
							setTitleBarRightImageBtnSrc(-1);
							break;
						case R.id.main_tab_room:
							if (mRoomFragment == null) {
								mRoomFragment = new RoomFragment();
							}
							fragment = mRoomFragment;
							setTitle(getString(R.string.str_room));
							setTitleBarRightImageBtnSrc(R.drawable.btn_title_menu_selector);
							break;
						case R.id.main_tab_friend:
							if (mContactFragment == null) {
								mContactFragment = new ContactFragment();
							}
							fragment = mContactFragment;
							setTitle(getString(R.string.str_friend));
							setTitleBarRightImageBtnSrc(R.drawable.btn_title_menu_selector);
							break;
						case R.id.main_tab_nearby:
							if (mNearByFragment == null) {
								mNearByFragment = new NearByFragment();
							}
							fragment = mNearByFragment;
							setTitle(getString(R.string.str_nearby));
							setTitleBarRightImageBtnSrc(-1);
							break;
						case R.id.main_tab_mine:
							if (mMineFragment == null) {
								mMineFragment = new MineFragment();
							}
							fragment = mMineFragment;
							setTitle(getString(R.string.str_mine));
							setTitleBarRightImageBtnSrc(-1);
							break;
						default:
							break;
						}

						if (mContent != fragment) {
							if (!fragment.isAdded()) { // 先判断是否被add过
								if (mContent != null) {
									transaction.hide(mContent);
								}
								transaction.add(R.id.main_content, fragment)
										.commit(); // 隐藏当前的fragment，add下一个到Activity中
							} else {
								if (mContent != null) {
									transaction.hide(mContent);
								}
								transaction.show(fragment).commit(); // 隐藏当前的fragment，显示下一个
							}
							mContent = fragment;
						}
					}
				});
		mRadioGroup.check(R.id.main_tab_conversation);
	}

	protected void initDatas() {
		hideTitleBarLeftBtn();
		setTheme(R.style.ActionSheetStyleIOS7);
		YiIMConfig config = YiIMConfig.getInstance(this);
		// 设置退出标识
		config.setExited(false);
		// 持久化
		YiPrefsKeeper.write(this, config);

		if (YiIMSDK.getInstance().authed()) {
			onAuthed();
		} else {
			YiIMSDK.getInstance().scheduleAutoLogin();
		}
	}

	protected void installListeners() {
		// TODO Auto-generated method stub
		// 注册认证广播监听
		mAuthedBroadcastReceiver = new OnAuthedBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(YiXmppConstant.NOTIFICATION_ON_AUTHED);
		registerReceiver(mAuthedBroadcastReceiver, intentFilter);

		mUnReadMsgBroadcastReceiver = new OnUnReadMsgBroadcastReceiver();
		IntentFilter intentFilter1 = new IntentFilter();
		intentFilter1
				.addAction(YiXmppConstant.NOTIFICATION_ON_UNREAD_MSG_UPDATED);
		registerReceiver(mUnReadMsgBroadcastReceiver, intentFilter1);
	}

	protected void uninstallListeners() {
		// TODO Auto-generated method stub
		unregisterReceiver(mAuthedBroadcastReceiver);
		unregisterReceiver(mUnReadMsgBroadcastReceiver);
	}

	protected void onAuthed() {
		// TODO Auto-generated method stub
		YiLog.getInstance().i("onAuthed");
		updateUnReadMsg();
	}

	@Override
	public void onTitleBarRightImgBtnClick(View view) {
		if (mContent == null) {
			return;
		}

		if (mContent.equals(mRoomFragment)) {
			ActionSheet
					.createBuilder(this, getSupportFragmentManager())
					.setCancelButtonTitle(getString(R.string.str_cancel))
					.setOtherButtonTitles(getString(R.string.str_search_room),
							getString(R.string.str_create_room),
							getString(R.string.str_group_manager))
					.setCancelableOnTouchOutside(true).setListener(this).show();
		} else if (mContent.equals(mContactFragment)) {
			ActionSheet
					.createBuilder(this, getSupportFragmentManager())
					.setCancelButtonTitle(getString(R.string.str_cancel))
					.setOtherButtonTitles(
							getString(R.string.str_search_friend),
							getString(R.string.str_group_manager))
					.setCancelableOnTouchOutside(true).setListener(this).show();
		}
	}

	private class OnAuthedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					onAuthed();
				}
			});
		}
	}

	private class OnUnReadMsgBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					updateUnReadMsg();
				}
			});
		}
	}

	private void updateUnReadMsg() {
		int total = YiIMSDK.getInstance().totalUnReadByType(
				YiXmppConstant.CONVERSATION_TYPE_RECORD)
				+ YiIMSDK.getInstance().totalUnReadByType(
						YiXmppConstant.CONVERSATION_TYPE_REQUEST);
		if (total > 0) {
			mConversationBtn.setCompoundDrawablesWithIntrinsicBounds(
					null,
					getResources().getDrawable(
							R.drawable.btn_tab_chat_unread_selector), null,
					null);
		} else {
			mConversationBtn.setCompoundDrawablesWithIntrinsicBounds(

			null, getResources().getDrawable(R.drawable.btn_tab_chat_selector),
					null, null);
		}
	}

	@Override
	public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onExitRequest() {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		// 设置自动登录标志为false
		userInfo.enableAutoLogin(false);
		// 持久化
		userInfo.persist(this);

		// 跳转至登录界面
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);

		YiIMConfig config = YiIMConfig.getInstance(this);
		// 设置退出标识
		config.setExited(true);
		// 持久化
		YiPrefsKeeper.write(this, config);

		// 停止Xmpp服务
		YiIMSDK.getInstance().disconect(null);

		finish();
	}

	@Override
	public void onOtherButtonClick(ActionSheet actionSheet, int index) {
		// TODO Auto-generated method stub
		if (mContent == mContactFragment) {
			Intent intent = null;
			switch (index) {
			case 0:
				intent = new Intent(MainActivity.this, FindFriendActivity.class);
				break;
			case 1:
				intent = new Intent(MainActivity.this,
						ContactGroupActivity.class);
				break;
			default:
				break;
			}
			if (intent != null) {
				startActivity(intent);
			}
		} else if (mContent == mRoomFragment) {
			Intent intent = null;
			switch (index) {
			case 0:
				intent = new Intent(MainActivity.this, FindRoomActivity.class);
				break;
			case 1:
				intent = new Intent(MainActivity.this, CreateRoomActivity.class);
				break;
			case 2:
				intent = new Intent(MainActivity.this, RoomGroupActivity.class);
				break;
			default:
				break;
			}
			if (intent != null) {
				startActivity(intent);
			}
		}
	}
}
