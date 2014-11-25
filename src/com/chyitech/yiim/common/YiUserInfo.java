package com.chyitech.yiim.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import com.chyitech.yiim.sdk.core.YiCore;
import com.chyitech.yiim.sdk.util.YiParamsExt;
import com.ikantech.support.util.YiPrefsKeeper;
import com.ikantech.support.util.YiPrefsKeeper.YiPrefsKeepable;
import com.ikantech.support.util.YiUtils;

public class YiUserInfo extends YiParamsExt implements YiPrefsKeepable {
	private static final String USERINFO_PREFS_NAME = "yiim_us_";

	// 用户名
	private String mUserName;
	// 密码
	private String mPasswd;
	// 启用记住密码
	private boolean mEnableRememberPasswd;
	// 启用自动登录
	private boolean mEnableAutoLogin;

	// 启用消息提醒
	private boolean mEnableMsgTip;
	// 启用好友消息到来时进行声音提示
	private boolean mEnableRosterMsgTipAudio;
	// 启用好友消息到来时进行振动提示
	private boolean mEnableRosterMsgTipVibrator;
	// 启用房间消息到来时进行声音提示
	private boolean mEnableRoomMsgTipAudio;
	// 启用房间消息到来时进行振动提示
	private boolean mEnableRoomMsgTipVibrator;

	private String mMsgTipSound;

	// 启用消息回执
	private boolean mEnableMsgReceipt;
	// 启用请求消息回执
	private boolean mEnableMsgReceiptRequest;
	// 启用响应消息回执
	private boolean mEnableMsgReceiptResponse;

	// 启用房间自动出席
	private boolean mEnableAutoJoin;

	// 用户是否是第一次登录，此标识用于当用户第一次登录时做一些必要的初始化工作
	private boolean mIsFirstLogin;

	// 处于锁屏时，是否提醒
	private boolean mIsKeyguardTip;

	private static YiUserInfo mUserInfo = null;

	private YiUserInfo(String username) {
		mUserName = username;
		mPasswd = null;
	}

	@Override
	public void save(Editor editor) {
		try {
			editor.putString("username", mUserName);
			editor.putBoolean("remember_pwd", mEnableRememberPasswd);
			editor.putBoolean("auto_login", mEnableAutoLogin);
			editor.putBoolean("msg_tip", mEnableMsgTip);
			editor.putBoolean("msg_tip_roster_audio", mEnableRosterMsgTipAudio);
			editor.putBoolean("msg_tip_roster_vibrator",
					mEnableRosterMsgTipVibrator);
			editor.putBoolean("msg_tip_room_audio", mEnableRoomMsgTipAudio);
			editor.putBoolean("msg_tip_room_vibrator",
					mEnableRoomMsgTipVibrator);
			editor.putString("msg_tip_sound", mMsgTipSound);
			editor.putBoolean("msg_receipt", mEnableMsgReceipt);
			editor.putBoolean("msg_receipt_request", mEnableMsgReceiptRequest);
			editor.putBoolean("msg_receipt_response", mEnableMsgReceiptResponse);
			editor.putBoolean("msg_autojoin", mEnableAutoJoin);
			editor.putBoolean("is_first_login", mIsFirstLogin);
			editor.putBoolean("keyguard_tip", mIsKeyguardTip);
			editor.putString("params", paramsToJson());
			if (mEnableRememberPasswd) {
				editor.putString("passwd", Base64.encodeToString(
						mPasswd.getBytes(), Base64.DEFAULT));
			} else {
				editor.putString("passwd", "");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void restore(SharedPreferences preferences) {
		try {
			mUserName = preferences.getString("username", null);
			mPasswd = preferences.getString("passwd", null);
			mEnableRememberPasswd = preferences.getBoolean("remember_pwd",
					false);
			mEnableMsgTip = preferences.getBoolean("msg_tip", true);
			mEnableRosterMsgTipAudio = preferences.getBoolean(
					"msg_tip_roster_audio", true);
			mEnableRosterMsgTipVibrator = preferences.getBoolean(
					"msg_tip_roster_vibrator", false);
			mEnableRoomMsgTipAudio = preferences.getBoolean(
					"msg_tip_room_audio", true);
			mEnableRoomMsgTipVibrator = preferences.getBoolean(
					"msg_tip_room_vibrator", false);
			mMsgTipSound = preferences.getString("msg_tip_sound", "msg_office");

			mEnableMsgReceipt = preferences.getBoolean("msg_receipt", false);
			mIsKeyguardTip = preferences.getBoolean("keyguard_tip", true);
			mEnableMsgReceiptRequest = preferences.getBoolean(
					"msg_receipt_request", true);
			mEnableMsgReceiptResponse = preferences.getBoolean(
					"msg_receipt_response", true);

			mEnableAutoJoin = preferences.getBoolean("msg_autojoin", true);

			mEnableAutoLogin = preferences.getBoolean("auto_login", false);
			mIsFirstLogin = preferences.getBoolean("is_first_login", true);
			if (!YiUtils.isStringInvalid(mPasswd)) {
				mPasswd = new String(Base64.decode(mPasswd, Base64.DEFAULT));
			}

			String param = preferences.getString("params", null);
			if (!YiUtils.isStringInvalid(param)) {
				paramsFromJson(param);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public String getPrefsName() {
		return USERINFO_PREFS_NAME + mUserName;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String username) {
		this.mUserName = username;
	}

	public String getPasswd() {
		return mPasswd;
	}

	public void setPasswd(String passwd) {
		this.mPasswd = passwd;
	}

	public boolean rememberPasswdEnabled() {
		return mEnableRememberPasswd;
	}

	public void enableRememberPasswd(boolean rememberPasswd) {
		this.mEnableRememberPasswd = rememberPasswd;
	}

	public boolean autoLoginEnabled() {
		return mEnableAutoLogin;
	}

	public void enableAutoLogin(boolean autoLogin) {
		this.mEnableAutoLogin = autoLogin;
	}

	public boolean autoJoinEnabled() {
		return mEnableAutoJoin;
	}

	public void enableAutoJoin(boolean v) {
		this.mEnableAutoJoin = v;
	}

	public boolean msgTipEnabled() {
		return mEnableMsgTip;
	}

	public void enableMsgTip(boolean msgTip) {
		this.mEnableMsgTip = msgTip;
	}

	public boolean keyguardMsgTipEnabled() {
		return mIsKeyguardTip;
	}

	public void enableKeyguardMsgTip(boolean v) {
		this.mIsKeyguardTip = v;
	}

	public boolean msgTipRosterAudioEnabled() {
		return mEnableRosterMsgTipAudio;
	}

	public void enableMsgTipRosterAudio(boolean msgTipRosterAudio) {
		this.mEnableRosterMsgTipAudio = msgTipRosterAudio;
	}

	public boolean msgTipRosterVibratorEnabled() {
		return mEnableRosterMsgTipVibrator;
	}

	public void enableMsgTipRosterVibrator(boolean msgTipRosterVibrator) {
		this.mEnableRosterMsgTipVibrator = msgTipRosterVibrator;
	}

	public boolean msgTipRoomAudioEnable() {
		return mEnableRoomMsgTipAudio;
	}

	public void enableMsgRoomAudio(boolean msgTipRoomAudio) {
		this.mEnableRoomMsgTipAudio = msgTipRoomAudio;
	}

	public boolean msgTipRoomVibratorEnable() {
		return mEnableRoomMsgTipVibrator;
	}

	public void enableMsgRoomVibratorEnable(boolean msgTipRoomVibrator) {
		this.mEnableRoomMsgTipVibrator = msgTipRoomVibrator;
	}

	public String getMsgTipSound() {
		return mMsgTipSound;
	}

	public void setMsgTipSound(String msgTipSound) {
		this.mMsgTipSound = msgTipSound;
	}

	public boolean msgReceiptEnabled() {
		return mEnableMsgReceipt;
	}

	public void enableMsgReceipt(boolean v) {
		this.mEnableMsgReceipt = v;
	}

	public boolean msgReceiptRequestEnabled() {
		return mEnableMsgReceiptRequest;
	}

	public void enableMsgReceiptRequest(boolean v) {
		this.mEnableMsgReceiptRequest = v;
	}

	public boolean msgReceiptResponseEnabled() {
		return mEnableMsgReceiptRequest;
	}

	public void enableMsgReceiptResponse(boolean v) {
		this.mEnableMsgReceiptResponse = v;
	}

	public boolean isFirstLogin() {
		return mIsFirstLogin;
	}

	public void setFirstLogin(boolean isFirstLogin) {
		this.mIsFirstLogin = isFirstLogin;
	}

	public String getJid() {
		return mUserName + "@" + YiCore.getServerName();
	}

	public String getJidWithResource() {
		return mUserName + "@" + YiCore.getServerName() + "/"
				+ YiCore.getJidResource();
	}

	/**
	 * 是否需要自动登录
	 * 
	 * @return true if need
	 */
	public boolean shouldAutoLogin() {
		return mEnableAutoLogin && mEnableRememberPasswd
				&& !YiUtils.isStringInvalid(mUserName)
				&& !YiUtils.isStringInvalid(mPasswd);
	}

	protected static YiUserInfo getUserInfo(Context context, boolean force) {
		YiPrefsKeeper.read(context, YiUserInfoList.getInstance());
		if (YiUserInfoList.getInstance().getUser() != null
				&& (mUserInfo == null || force)) {
			mUserInfo = new YiUserInfo(YiUserInfoList.getInstance().getUser());
			YiPrefsKeeper.read(context, mUserInfo);
		}
		return mUserInfo;
	}

	/**
	 * 获取当前用户的基本信息
	 * 
	 * @param context
	 * @return
	 */
	public static YiUserInfo getUserInfo(Context context) {
		return getUserInfo(context, false);
	}

	/**
	 * 获取具体用户的信息
	 * 
	 * @param context
	 * @param user
	 * @return
	 */
	public static YiUserInfo getUserInfo(Context context, String user) {
		YiUserInfo info = new YiUserInfo(user);
		YiPrefsKeeper.read(context, info);
		return info;
	}

	/**
	 * 将当前用户设置为活跃用户，这样下次调用getUserInfo的时候，获取的就当前用户的信息了。
	 * 
	 * @param context
	 */
	public void active(Context context) {
		YiUserInfoList.getInstance().setUser(getUserName());
		YiPrefsKeeper.write(context, YiUserInfoList.getInstance());
		mUserInfo = this;
	}

	/**
	 * 持久保存
	 * 
	 * @param context
	 */
	public void persist(Context context) {
		YiPrefsKeeper.write(context, this);
	}
}
