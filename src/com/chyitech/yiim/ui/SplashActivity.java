package com.chyitech.yiim.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.chyitech.yiim.R;
import com.chyitech.yiim.app.YiIMApplication;
import com.chyitech.yiim.common.YiIMConfig;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiIMSDK;

/**
 * 欢迎界面
 * @author saint
 *
 */
public class SplashActivity extends Activity {
	private static final int MSG_LOADING_TIMEOUT = 0x00;
	private static final int LOADING_DELAYED = 2000;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_LOADING_TIMEOUT:
				suggestLaunch();
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		YiIMApplication application = (YiIMApplication) this.getApplication();
		//如果APP是开机后第一次启动，则展示欢迎界面
		if (application.isFirstLaunch()) {
			application.setFirstLaunch(false);
			setContentView(R.layout.activity_splash);
			mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIMEOUT,
					LOADING_DELAYED);
		} else {
			//否则跳转至登录或主界面
			suggestLaunch();
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().updateNotification();
		super.onStart();
	}

	private void suggestLaunch() {
		Intent intent = null;

		//如果已登录
		if (YiIMSDK.getInstance().authed()) {
			intent = new Intent(SplashActivity.this, MainActivity.class);
		} else {
			intent = new Intent(SplashActivity.this, LoginActivity.class);

			YiIMConfig config = YiIMConfig.getInstance(this);
			YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
			//如果用户上一次未退出登录，或用户已勾选自动登录
			if (userInfo != null
					&& (!config.isExited() || userInfo.shouldAutoLogin())) {
				intent = new Intent(SplashActivity.this, MainActivity.class);
			}
		}

		startActivity(intent);
		overridePendingTransition(0, R.anim.splash_activity_anim);
		finish();
	}

}
