package com.chyitech.yiim.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;

import com.chyitech.yiim.R;
import com.chyitech.yiim.app.YiIMApplication;
import com.chyitech.yiim.common.NotificationManager;
import com.chyitech.yiim.common.YiIMConfig;
import com.chyitech.yiim.common.YiIMConstant;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.entity.YiMessage;
import com.chyitech.yiim.entity.YiMessage.MsgType;
import com.chyitech.yiim.sdk.api.YiConntectionDelegate;
import com.chyitech.yiim.sdk.api.YiConversationDelegate;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiMessageDelegate;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppListener;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.service.YiXmppBinder;
import com.chyitech.yiim.sdk.service.YiXmppService;
import com.chyitech.yiim.util.DistanceUtils;
import com.chyitech.yiim.util.FileUtils;
import com.chyitech.yiim.util.YiQiqiuUtils;
import com.ikantech.support.proxy.YiDialogProxy;
import com.ikantech.support.service.YiLocalService;
import com.ikantech.support.util.YiLog;
import com.ikantech.support.util.YiUtils;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.CallRet;
import com.qiniu.rs.UploadCallRet;

public class BackgroundServer extends YiXmppService implements YiXmppListener {
	protected static final int ONE_SECOND = 1000;
	protected static final int MSG_AUTO_LOGIN = 0x01;
	protected static final int MSG_TIP = 0x02;

	public static final String MSG_SOUND_OFFICE = "msg_office";
	public static final String MSG_SOUND_CHORD = "msg_chord";
	public static final String MSG_SOUND_TRITONE = "msg_tritone";

	private BackgroundBinder mBinder;

	private MsgTipReceiver mMsgTipReceiver;
	//消息提示音
	private SoundPool mSoundPool;
	private Map<String, Integer> mSoundIds;

	private Vibrator mVibrator;

	private YiDialogProxy mDialogProxy;
	private NotificationManager mNotificationManager;

	// 用于判断当前是否处于锁屏状态
	private KeyguardManager mKeyguardManager;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_AUTO_LOGIN:
				// 如果不需要自动登录，则不执行
				YiUserInfo userInfo = YiUserInfo
						.getUserInfo(BackgroundServer.this);

				// 如果最后一次停止进程，非用户主动退出，则调度自动登录，注自动登录调度是必须满足自动登录要求，服务才会真正执行
				// 所以用户退出时，记得将这个标识置为true;
				YiIMConfig config = YiIMConfig
						.getInstance(BackgroundServer.this);

				if (userInfo == null || config.isExited()) {
					return;
				}

				//如果已连接至服务器，则执行登录流程
				if (YiIMSDK.getInstance().connected()) {
					if (!YiIMSDK.getInstance().authed()) {
						autoLogin();
					}
				} else {//连接服务器
					YiIMSDK.getInstance().connect(BackgroundServer.this);
				}
				break;
			case MSG_TIP:
				msgTipProcess((String) msg.obj);
				break;
			default:
				break;
			}
		}

	};

	public Handler getHandler() {
		return mHandler;
	}

	public void scheduleAutoLogin() {
		getHandler().removeMessages(MSG_AUTO_LOGIN);
		getHandler().sendEmptyMessageDelayed(MSG_AUTO_LOGIN, ONE_SECOND);
	}

	//自动登录
	private synchronized void autoLogin() {
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo != null && userInfo.rememberPasswdEnabled()
				&& !YiUtils.isStringInvalid(userInfo.getUserName())
				&& !YiUtils.isStringInvalid(userInfo.getPasswd())) {
			YiIMSDK.getInstance().login(userInfo.getUserName(),
					userInfo.getPasswd(), true, this);
		}
	}

	@Override
	public void onXmppResonpse(YiXmppResult result) {
		// TODO Auto-generated method stub
		switch (result.what) {
		case XMPP_CONNECT:
			if (!result.success()) {
				YiLog.getInstance().e("start xmpp service failed.");

				// 5-15秒内进行重连
				int randomR = new Random().nextInt(11) + 5;

				mHandler.removeMessages(MSG_AUTO_LOGIN);
				mHandler.sendEmptyMessageDelayed(MSG_AUTO_LOGIN, randomR
						* ONE_SECOND);
			} else {
				if (!YiIMSDK.getInstance().authed()) {
					autoLogin();
				}
			}
			break;
		case XMPP_LOGIN:
			if (!result.success()) {
				YiLog.getInstance().e("login failed: %s", result.obj);

				// 5-15秒内进行重连
				int randomR = new Random().nextInt(11) + 5;

				mHandler.removeMessages(MSG_AUTO_LOGIN);
				mHandler.sendEmptyMessageDelayed(MSG_AUTO_LOGIN, randomR
						* ONE_SECOND);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mBinder = new BackgroundBinder(this);
		mDialogProxy = new YiDialogProxy(this);
		mNotificationManager = new NotificationManager(
				this,
				(android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE));

		mMsgTipReceiver = new MsgTipReceiver();
		IntentFilter msgTipFilter = new IntentFilter();
		msgTipFilter.addAction(YiXmppConstant.NOTIFICATION_ON_MESSAGE_RECEIVED);
		registerReceiver(mMsgTipReceiver, msgTipFilter);

		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		mSoundIds = new HashMap<String, Integer>();
		mSoundIds.put(MSG_SOUND_OFFICE, mSoundPool.load(this, R.raw.office, 1));
		mSoundIds.put(MSG_SOUND_CHORD, mSoundPool.load(this, R.raw.chrod, 2));
		mSoundIds.put(MSG_SOUND_TRITONE,
				mSoundPool.load(this, R.raw.tritone, 3));

		mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public static class BackgroundBinder extends YiXmppBinder {
		private double mLastLBSLng;
		private double mLastLBSLat;

		public BackgroundBinder(YiLocalService s) {
			super(s);
			// TODO Auto-generated constructor stub
			YiIMSDK.getInstance().setMessageDelegate(new MessageDelegate(s));
			YiIMSDK.getInstance().setConversationDelegate(
					new ConversationDelegate(s));
			YiIMSDK.getInstance().setConntectionDelegate(
					new YiConntectionDelegate() {
						@Override
						public void onDisconnect() {
							// TODO Auto-generated method stub
							mLastLBSLat = 0;
							mLastLBSLng = 0;
						}

						@Override
						public void onConnect() {
							// TODO Auto-generated method stub
							//已登录
							if (YiIMSDK.getInstance().authed()) {
								// 更新LBS
								((YiIMApplication) getYiIMService()
										.getApplication()).mLocationClient
										.stop();
								((YiIMApplication) getYiIMService()
										.getApplication()).mLocationClient
										.start();
							}
						}

						@Override
						public void onConflict() {
							// TODO Auto-generated method stub
							((BackgroundServer) getYiIMService())
									.showSystemMsgDialog(
											getYiIMService().getString(
													R.string.str_relogin_tip),
											getYiIMService().getString(
													R.string.str_relogin),
											getYiIMService().getString(
													R.string.str_logout),
											new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													// TODO Auto-generated
													// method stub
													//重新登录
													YiIMSDK.getInstance()
															.reLogin(true);
												}
											}, new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													// TODO Auto-generated
													// method stub
													//注销当前账号
													YiIMSDK.getInstance()
															.reLogin(false);

													YiUtils.broadcast(
															getYiIMService(),
															YiIMConstant.NOTIFICATION_EXIT,
															null);
												}
											});
						}
					});
		}

		public void scheduleAutoLogin() {
			if (mServiceHandler.get() != null) {
				((BackgroundServer) mServiceHandler.get()).scheduleAutoLogin();
			}
		}

		@Override
		public void sendFile(final String receiver, final Uri uri, final int w,
				final int h) {
			// TODO Auto-generated method stub
			execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						//生成七牛Key
						String key = YiQiqiuUtils.generateKey();

						YiMessage message = new YiMessage();
						message.setType(MsgType.IMAGE);
						message.addParam("uri", uri.toString());
						//生成图片Url
						message.setBody(YiQiqiuUtils.generateDownloadUrl(key,
								false));
						//生成缩略图Url
						message.addParam("small_url",
								YiQiqiuUtils.generateDownloadUrl(key, true));
						message.addParam("small_height", "200");
						message.addParam("width", "" + w);
						message.addParam("height", "" + h);
						message.addParam("status", "sending");
						message.addParam("percent", "0");

						//将消息插入到本地数据库，必须等图片上传完成后，才真正发送图片
						YiIMSDK.getInstance().insertMessageToLocal(key,
								message.toString(), receiver);

						//上传图片
						YiQiqiuUtils.upload(getYiIMService(),
								YiQiqiuUtils.generateUploadToken(), key, uri,
								null, new QiqiuCallback(key, uri, message));
					} catch (Exception e) {
						// TODO: handle exception
						YiLog.getInstance().e(e, "send image failed");
					}
				}
			});
		}

		/**
		 * 重新发送消息
		 */
		@Override
		public void resend(String msgId, String jid) {
			// TODO Auto-generated method stub
			YiMessage message = YiMessage.fromString(YiIMSDK.getInstance()
					.messageFromLocal(msgId));
			String status = message.getParams().get("status");
			//如果是图片类，如果是图片上传失败，则重新上传
			if (message.getType() == MsgType.IMAGE && "error".equals(status)) {
				resendFile(message, msgId);
			} else {
				//重新发送消息
				long delay = 5000;
				if(message.getType() == MsgType.AUDIO) {
					delay = Long.valueOf(message.getParams().get("audio_duration"));
				}
				YiIMSDK.getInstance().resendMessage(msgId, message.toString(),
						jid, delay);
			}
		}

		protected void resendFile(final YiMessage message, final String msgId) {
			execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						message.addParam("status", "sending");
						message.addParam("percent", "0");
						String uri = message.getParams().get("uri");

						YiQiqiuUtils.upload(getYiIMService(), YiQiqiuUtils
								.generateUploadToken(), msgId, Uri.parse(uri),
								null, new QiqiuCallback(msgId, Uri.parse(uri),
										message));
					} catch (Exception e) {
						// TODO: handle exception
						YiLog.getInstance().e(e, "resend image failed");
					}
				}
			});
		}

		@Override
		public void updateNotification() {
			//更新notification
			if (mServiceHandler.get() != null) {
				((BackgroundServer) mServiceHandler.get()).updateNotification();
			}
		}

		@Override
		public void updateLBS(double lng, double lat) {
			// TODO Auto-generated method stub
			double dis = DistanceUtils.getDistance(lat, lng, mLastLBSLat,
					mLastLBSLng);
			//如果距离大于500M
			if (dis > 500) {
				mLastLBSLat = lat;
				mLastLBSLng = lng;
				super.updateLBS(lng, lat);
			}
		}
	}

	private static class MessageDelegate implements YiMessageDelegate {
		private Context mContext;

		public MessageDelegate(Context context) {
			mContext = context;
		}

		@Override
		public String prepareInsertMessage(String msg, String sender,
				String receiver) {
			// TODO Auto-generated method stub
			YiMessage message = YiMessage.fromString(msg);

			if (message.getType().equals(YiMessage.MsgType.AUDIO)) {//如果是语音短消息
				try {
					//将语音短消息保存到文件
					String filename = FileUtils.getInstance().storeAudioFile(
							YiIMSDK.getInstance().getCurrentUserName(),
							message.getBody());
					message.setBody(filename);
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else if (message.getType().equals(YiMessage.MsgType.SYSTEM)) {
				//如果是系统消息，则将其替换为本地字符串
				if (YiXmppConstant.MESSAGE_SUBSCRIBED.equals(msg)) {
					message.setBody(mContext.getString(R.string.str_subscribed));
				} else if (YiXmppConstant.MESSAGE_UNSUBSCRIPTION.equals(msg)) {
					message.setBody(mContext
							.getString(R.string.str_unsubscription));
				} else if (YiXmppConstant.MESSAGE_ROOM_SUBSCRIBED.equals(msg)) {
					message.setBody(mContext
							.getString(R.string.str_room_subscribed));
				} else if (YiXmppConstant.MESSAGE_ROOM_UNSUBSCRIPTION
						.equals(msg)) {
					message.setBody(mContext
							.getString(R.string.str_room_unsubscription));
				}
			}

			return message.toString();
		}

		@Override
		public void prepareRemoveMessage(String msgId, String msg,
				String sender, String receiver) {
			// TODO Auto-generated method stub
			YiMessage message = YiMessage.fromString(msg);
			//如果是语音短消息，则删除本地缓存的文件
			if (message.getType().equals(YiMessage.MsgType.AUDIO)) {
				File file = new File(message.getBody());
				if (file.exists()) {
					file.delete();
				}
			}
		}
	}

	private static class ConversationDelegate implements YiConversationDelegate {
		private Context mContext;

		public ConversationDelegate(Context context) {
			mContext = context;
		}

		@Override
		public String prepareUpdateConversation(String subMsg, int type,
				String sender) {
			// TODO Auto-generated method stub
			YiMessage message = YiMessage.fromString(subMsg);
			if (message.getType().equals(YiMessage.MsgType.AUDIO)) {
				message.setBody(mContext.getString(R.string.str_msg_type_audio));
			} else if (message.getType().equals(YiMessage.MsgType.BIG_EMOTION)) {
				message.setBody(mContext
						.getString(R.string.str_msg_type_big_emoji));
			} else if (message.getType().equals(YiMessage.MsgType.IMAGE)) {
				message.setBody(mContext.getString(R.string.str_msg_type_image));
			}
			return message.getBody();
		}

	}

	private void msgTipProcess(String sender) {
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;

		boolean tip = userInfo.msgTipEnabled();
		//如果是锁屏状态
		if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
			//如果需要提醒
			tip = userInfo.keyguardMsgTipEnabled();
		}

		if (tip) {
			if (sender.contains("conference")) {
				if (userInfo.msgTipRoomAudioEnable()) {
					mSoundPool.play(mSoundIds.get(userInfo.getMsgTipSound()),
							1, 1, 0, 0, 1);
				}

				if (userInfo.msgTipRoomVibratorEnable()) {
					mVibrator.vibrate(new long[] { 0, 200, 100, 200 }, -1);
				}
			} else {
				if (userInfo.msgTipRosterAudioEnabled()) {
					mSoundPool.play(mSoundIds.get(userInfo.getMsgTipSound()),
							1, 1, 0, 0, 1);
				}

				if (userInfo.msgTipRosterVibratorEnabled()) {
					mVibrator.vibrate(new long[] { 0, 200, 100, 200 }, -1);
				}
			}
		}
	}

	//更新notification
	private void updateNotification() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> cn = am.getRunningTasks(1);
		RunningTaskInfo taskInfo = cn.get(0);
		String name = taskInfo.topActivity.getPackageName();
		String name1 = getClass().getPackage().getName();
		if (!YiUtils.isStringInvalid(name) && !name1.contains(name)) {
			mNotificationManager.update();
		} else {
			mNotificationManager.remove();
		}
	}

	private void showSystemMsgDialog(final String detials,
			final String leftLab, final String rightLab,
			final OnClickListener leftListener,
			final OnClickListener rightListener) {
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mDialogProxy.showMsgDialogTitle();
				mDialogProxy.setMsgDialogTitle(R.string.app_name);

				if (!YiUtils.isStringInvalid(detials)) {
					mDialogProxy.showMsgDialogDetailMsg();
					mDialogProxy.setMsgDialogDetailMsg(detials);
				} else {
					mDialogProxy.hideMsgDialogDetailMsg();
				}

				if (!YiUtils.isStringInvalid(leftLab)) {
					mDialogProxy.showMsgDialogBtnLeft();
					mDialogProxy.setMsgDialogBtnLeftText(leftLab);
				} else {
					mDialogProxy.hideMsgDialogBtnLeft();
				}

				if (!YiUtils.isStringInvalid(rightLab)) {
					mDialogProxy.showMsgDialogBtnRight();
					mDialogProxy.setMsgDialogBtnRightText(rightLab);
				} else {
					mDialogProxy.hideMsgDialogBtnRight();
				}

				mDialogProxy.setMsgDialogCanceledOnTouchOutside(false);
				mDialogProxy.setMsgDialogCancelable(false);
				mDialogProxy.setMsgDialogBtnLeftClickListener(leftListener);
				mDialogProxy.setMsgDilaogBtnRightClickListener(rightListener);
				mDialogProxy.setMsgDialogIsSystemDialog();
				mDialogProxy.showMsgDialog();
			}
		});
	}

	private class MsgTipReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					YiXmppConstant.NOTIFICATION_ON_MESSAGE_RECEIVED)) {
				String sender = intent.getStringExtra("sender");
				if (((YiIMSDK.defaultCore().getActiveJid() == null || !sender
						.contains(YiIMSDK.defaultCore().getActiveJid())) && !sender
						.contains(YiIMSDK.defaultCore().getCurrentUserName()))
						|| (mKeyguardManager.inKeyguardRestrictedInputMode())) {
					getHandler().removeMessages(MSG_TIP);
					Message msg = getHandler().obtainMessage(MSG_TIP, sender);
					getHandler().sendMessageDelayed(msg, 200);
				}

				updateNotification();
			}
		}
	}

	/**
	 * 七牛SDK上传回调
	 * @author saint
	 *
	 */
	private static class QiqiuCallback extends CallBack {
		private String mKey;
		private YiMessage mMessage;
		private Uri mUri;

		public QiqiuCallback(String key, Uri uri, YiMessage message) {
			mKey = key;
			mMessage = message;
			mUri = uri;
		}

		@Override
		public void onProcess(long current, long total) {
			// TODO Auto-generated method stub
			//上传进度更新
			YiLog.getInstance().i("image[%s] onProcess %d %d", mKey, current,
					total);
			int percent = (int) (current * 100.0F / total);
			int old_percent = Integer.valueOf(mMessage.getParams().get(
					"percent"));
			if (percent - old_percent > 5) {
				mMessage.addParam("percent", "" + percent);
				YiIMSDK.getInstance().updateMessageToLocal(mKey,
						mMessage.toString());
			}
		}

		@Override
		public void onSuccess(UploadCallRet ret) {
			// TODO Auto-generated method stub
			//上传成功
			YiLog.getInstance().i("image[%s] onSuccess", mKey);
			mMessage.getParams().remove("status");
			mMessage.getParams().remove("percent");
			mMessage.getParams().remove("uri");
			if (YiIMSDK.getInstance().updateMessageToLocal(mKey,
					mMessage.toString())) {
				YiIMSDK.getInstance().sendMessageFromLocal(mKey);
				if (mUri.toString().contains("yiim/dcim")) {
					File file = new File(mUri.getPath());
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}

		@Override
		public void onFailure(CallRet ret) {
			// TODO Auto-generated method stub
			//上传失败
			YiLog.getInstance().i("image[%s] onFailure", mKey);
			mMessage.addParam("status", "error");
			YiIMSDK.getInstance().updateMessageToLocal(mKey,
					mMessage.toString());
		}
	}
}
