package com.chyitech.yiim.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.YiIMConfig;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.ui.LoginActivity;
import com.chyitech.yiim.ui.SettingsActivity;
import com.chyitech.yiim.ui.mine.AboutActivity;
import com.chyitech.yiim.ui.mine.PrivacyListActivity;
import com.chyitech.yiim.ui.mine.UserInfoSetActivity;
import com.ikantech.support.util.YiPrefsKeeper;
import com.ikantech.support.widget.YiFragment;

/**
 * 我TAB
 * @author saint
 *
 */
public class MineFragment extends YiFragment implements View.OnClickListener {
	private View mRootView;
	private View mTabPersionalInfoView;
	private View mTabPrivacyView;
	private View mTabSettingsView;
	private View mTabAboutView;
	private View mTabExitView;
	private View mTabLogoutView;

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = inflater.inflate(R.layout.frag_tab_mine, null);

		mTabPersionalInfoView = mRootView
				.findViewById(R.id.tab_mine_persional_info);
		mTabPrivacyView = mRootView.findViewById(R.id.tab_mine_privacy);
		mTabSettingsView = mRootView.findViewById(R.id.tab_mine_settings);
		mTabAboutView = mRootView.findViewById(R.id.tab_mine_about);
		mTabExitView = mRootView.findViewById(R.id.tab_mine_exit);
		mTabLogoutView = mRootView.findViewById(R.id.tab_mine_logout);

		mTabPersionalInfoView.setOnClickListener(this);
		mTabPrivacyView.setOnClickListener(this);
		mTabSettingsView.setOnClickListener(this);
		mTabAboutView.setOnClickListener(this);
		mTabExitView.setOnClickListener(this);
		mTabLogoutView.setOnClickListener(this);

		return mRootView;
	}

	public void onExitBtnClick() {
		YiIMConfig config = YiIMConfig.getInstance(getActivity());
		// 设置退出标识
		config.setExited(true);
		// 持久化
		YiPrefsKeeper.write(getActivity(), config);

		// 停止Xmpp服务
		YiIMSDK.getInstance().disconect(null);

		getActivity().finish();
	}

	public void onLogOutBtnClick() {
		YiUserInfo userInfo = YiUserInfo.getUserInfo(getActivity());
		// 设置自动登录标志为false
		userInfo.enableAutoLogin(false);
		// 持久化
		userInfo.persist(getActivity());

		// 跳转至登录界面
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivity(intent);

		onExitBtnClick();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mTabExitView) {
			showMsgDialog(null, getString(R.string.str_exit_confirm),
					getString(R.string.str_ok), getString(R.string.str_cancel),
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							onExitBtnClick();
						}
					}, null);
		} else if (v == mTabLogoutView) {
			onLogOutBtnClick();
		} else if (v == mTabSettingsView) {
			Intent intent = new Intent(getActivity(), SettingsActivity.class);
			startActivity(intent);
		} else if (v == mTabPersionalInfoView) {
			Intent intent = new Intent(getActivity(), UserInfoSetActivity.class);
			startActivity(intent);
		} else if (v == mTabPrivacyView) {
			Intent intent = new Intent(getActivity(), PrivacyListActivity.class);
			startActivity(intent);
		}else if (v == mTabAboutView) {
			Intent intent = new Intent(getActivity(), AboutActivity.class);
			startActivity(intent);
		}
	}
}
