package com.chyitech.yiim.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.YiIMConfig;
import com.chyitech.yiim.common.YiIMConstant;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.proxy.XmppErrorTipProxy;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.ikantech.support.util.YiLog;
import com.ikantech.support.util.YiPrefsKeeper;

/**
 * 登录界面
 * 
 * @author saint
 * 
 */
public class LoginActivity extends CustomTitleActivity {
	private EditText mUserNameEditText;
	private EditText mPasswdEditText;
	private CheckBox mRememberPwdCheckBox;
	private CheckBox mAutoLoginCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_login);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
		cancelProgressDialog();
		switch (result.what) {
		case XMPP_CONNECT:
			// 连接服务器失败
			if (!result.success()) {
				showMsgDialog(getString(R.string.err_connecting_server_failed),
						getString(R.string.str_ok));
			} else {
				YiIMSDK.getInstance()
						.login(mUserNameEditText.getText().toString().trim(),
								mPasswdEditText.getText().toString().trim(),
								true, this);
				showProgressDialog(R.string.str_logining);
			}
			break;
		case XMPP_LOGIN:
			if (!result.success()) {
				// 先由错误提示代理处理
				if (!XmppErrorTipProxy.handle(this, this, result.error)) {
					YiLog.getInstance().e("login failed: %s", result.obj);
					showMsgDialog(getString(R.string.err_login_failed),
							getString(R.string.str_ok));
					YiIMSDK.getInstance().disconect(null);
				}
			} else {//登录成功
				YiUserInfo info = YiUserInfo.getUserInfo(this,
						mUserNameEditText.getText().toString().trim());
				info.setUserName(mUserNameEditText.getText().toString().trim());
				//如果要记住密码
				if (mRememberPwdCheckBox.isChecked()) {
					info.setPasswd(mPasswdEditText.getText().toString().trim());
				} else {
					//否则清除可能已记住的密码
					info.setPasswd("");
				}
				info.enableAutoLogin(mAutoLoginCheckBox.isChecked());
				info.enableRememberPasswd(mRememberPwdCheckBox.isChecked());
				// 设置为活跃用户
				info.active(this);
				// 持久化用户信息
				info.persist(this);

				YiIMConfig config = YiIMConfig.getInstance(this);
				// 设置用户主动退出的标识为false，用于下次自动登录，或服务被回收重启时的自动登录
				config.setExited(false);
				// 持久化基本配置
				YiPrefsKeeper.write(this, config);

				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mUserNameEditText = (EditText) findViewById(R.id.login_user_edit);
		mPasswdEditText = (EditText) findViewById(R.id.login_passwd_edit);
		mRememberPwdCheckBox = (CheckBox) findViewById(R.id.login_remember_pwd);
		mAutoLoginCheckBox = (CheckBox) findViewById(R.id.login_auto_login);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		setTitleBarLeftBtnText(getString(R.string.str_exit));
		setTitleBarRightBtnText(getString(R.string.str_register));

		// 读取持久化的用户信息
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo != null) {
			if (!isStringInvalid(userInfo.getUserName())) {
				mUserNameEditText.setText(userInfo.getUserName());
			}

			if (!isStringInvalid(userInfo.getPasswd())) {
				mPasswdEditText.setText(userInfo.getPasswd());
			}

			mRememberPwdCheckBox.setChecked(userInfo.rememberPasswdEnabled());
			mAutoLoginCheckBox.setChecked(userInfo.autoLoginEnabled());
		}
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mAutoLoginCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							mRememberPwdCheckBox.setChecked(true);
						}
					}
				});
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	public void onForgetPasswdClick(View view) {

	}

	@Override
	public void onTitleBarLeftBtnClick(View view) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().disconect(null);
		super.onTitleBarLeftBtnClick(view);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().disconect(null);
		super.onBackPressed();
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);
	}

	public void onLoginClick(View view) {
		if (isStringInvalid(mUserNameEditText.getText().toString().trim())) {
			showMsgDialog(getString(R.string.err_empty_user_name),
					getString(R.string.str_ok));
			return;
		}

		// 判断用户名的合法性
		if (!mUserNameEditText.getText().toString().trim()
				.matches(YiIMConstant.USER_NAME_REGEXP)) {
			showMsgDialog(getString(R.string.err_illegal_username));
			return;
		}

		if (isStringInvalid(mPasswdEditText.getText().toString().trim())) {
			showMsgDialog(getString(R.string.err_empty_passwd),
					getString(R.string.str_ok));
			return;
		}

		// 启动服务器
		YiIMSDK.getInstance().connect(this);
		showProgressDialog(R.string.str_connecting_server);
	}
}
