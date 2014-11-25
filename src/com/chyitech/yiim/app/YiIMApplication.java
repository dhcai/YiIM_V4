package com.chyitech.yiim.app;

import java.util.HashMap;
import java.util.Map;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.chyitech.yiim.R;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppLog;
import com.chyitech.yiim.service.BackgroundServer.BackgroundBinder;
import com.ikantech.support.app.YiApplication;
import com.ikantech.support.cache.YiStoreCache;
import com.ikantech.support.common.YiCrashHandler;
import com.ikantech.support.util.YiBase64;
import com.ikantech.support.util.YiFileUtils;
import com.ikantech.support.util.YiUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class YiIMApplication extends YiApplication {
	// Xmpp Jid资源
	public static final String JID_RES = "Android";

	// Xmpp服务器地址
	//public static final String IP = "115.28.150.238";
	public static final String IP = "121.42.15.91";
	// public static final String IP = "192.168.0.104";
	// public static final String IP = "60.190.203.43";
	// Xmpp服务器端口
	public static final int PORT = 5222;

	//是否为第一次登录
	private boolean mIsFirstLaunch = true;

	private NativeServiceConnection mServiceConnection;

	//百度定位相关
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;

	//获取当前进程名称
	String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
				.getRunningAppProcesses()) {
			if (appProcess.pid == pid) {

				return appProcess.processName;
			}
		}
		return null;
	}

	@Override
	protected void initialize() {
		if (getCurProcessName(this).contains(":remote")) {
			return;
		}

		super.initialize();

		// 日志开关，false表示关闭
		YiXmppLog.getInstance().setEnableLogDebug(true);
		YiXmppLog.getInstance().setEnableLogError(true);
		YiXmppLog.getInstance().setEnableLogInfo(true);
		YiXmppLog.getInstance().setEnableLogWarn(true);
		YiXmppLog.getInstance().setEnableLogVerbose(true);

		// 初始化Base64字符顺序，用于语音短消息的编码
		YiBase64.setChars("AB678wxCDEf34ghiFGUVWXYZabcdejIJKLPQRklmnopqMNO5rstHSTuvyz0129_-");

		// 初始化crash收集存储位置
		YiCrashHandler.setLogPath(YiFileUtils.getStorePath() + "yiim/crash/");
		// 初始化缓存存储位置
		YiStoreCache.IMAGE_CACHE_DIR = "yiim/";

		YiUserInfo userInfo = YiUserInfo.getUserInfo(YiIMApplication.this);
		if (userInfo != null) {//将配置设置到SDK中
			YiIMSDK.getInstance().setCurrentUserName(userInfo.getUserName());
			YiIMSDK.getInstance().setMessageReceipt(
					userInfo.msgReceiptEnabled());
			YiIMSDK.getInstance().setMessageReceiptRequest(
					userInfo.msgReceiptRequestEnabled());
			YiIMSDK.getInstance().setMessageReceiptResponse(
					userInfo.msgReceiptResponseEnabled());
			YiIMSDK.getInstance().setAutoJoinRoom(userInfo.autoJoinEnabled());
		}

		// 配置异步加载图片库
		DisplayImageOptions op = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.no_pic)
				.showImageOnFail(R.drawable.no_pic)
				.showImageOnLoading(R.drawable.no_pic).cacheOnDisk(true)
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(op).build();
		ImageLoader.getInstance().init(config);

		// 绑定YiIMService
		mServiceConnection = new NativeServiceConnection();
		Intent xmppServiceIntent = new Intent(
				YiXmppConstant.XMPP_SERVER_ACTION_NAME);
		startService(xmppServiceIntent);
		bindService(xmppServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		//初始化LBS
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(false);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(false);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
	}

	@Override
	public void onTerminate() {
		unbindService(mServiceConnection);
		super.onTerminate();
	}

	// 是否打开程序崩溃收集程序
	@Override
	protected boolean openCrashHandler() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isFirstLaunch() {
		return mIsFirstLaunch;
	}

	public void setFirstLaunch(boolean isFirstLaunch) {
		mIsFirstLaunch = isFirstLaunch;
	}

	private class NativeServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// config service before assignment.
			YiIMSDK.getInstance().setBackgroundService(service);
			YiIMSDK.getInstance().setHost(IP);
			YiIMSDK.getInstance().setServerName("chyitech.com");
			// YiIMSDK.getInstance().setServerName("192.168.43.136");
			YiIMSDK.getInstance().setPort(PORT);
			YiIMSDK.getInstance().setResource(JID_RES);

			//调度自动登录
			((BackgroundBinder) service).scheduleAutoLogin();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			Log.i("BaiduLocationApiDem", sb.toString());

			//如果LBS信息获取成功
			if (location.getLocType() == 61 || location.getLocType() == 65
					|| location.getLocType() == 66
					|| location.getLocType() == 161) {
				//更新自身及自己创建的群组LBS信息
				YiIMSDK.getInstance().updateAllLBS(location.getLongitude(),
						location.getLatitude());

				Map<String, String> params = new HashMap<String, String>();
				params.put("longitude", String.valueOf(location.getLongitude()));
				params.put("latitude", String.valueOf(location.getLatitude()));
				YiUtils.broadcast(getApplicationContext(),
						"com.chyitech.yiim.LBS_UPDATE", params);
			}

			mLocationClient.stop();
		}
	}
}
