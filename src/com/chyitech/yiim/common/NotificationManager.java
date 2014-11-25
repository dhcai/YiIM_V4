package com.chyitech.yiim.common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.ui.MainActivity;

public class NotificationManager {
	private static final int NOTIFICATION_ID = 0x01;

	private Context mContext;
	private android.app.NotificationManager mNotificationManager;

	public NotificationManager(Context context,
			android.app.NotificationManager notificationManager) {
		mNotificationManager = notificationManager;
		mContext = context;
	}

	public void remove() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	public void update() {
		int unread = YiIMSDK.getInstance().totalUnReadByType(0)
				+ YiIMSDK.getInstance().totalUnReadByType(1);

		String subMsg = mContext.getString(R.string.str_unread_msg_tip, unread);

		if (Build.VERSION.SDK_INT >= 11) {
			updateNotification(subMsg, System.currentTimeMillis());
		} else {

			Notification notification = new Notification(
					R.drawable.ic_launcher, "", System.currentTimeMillis());
			PendingIntent contentIndent = PendingIntent.getActivity(mContext,
					0, new Intent(mContext, MainActivity.class),
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(mContext,
					mContext.getString(R.string.app_name), subMsg,
					contentIndent);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	@SuppressLint("NewApi")
	private void updateNotification(final String subMsg, long time) {
		final Notification.Builder builder = new Notification.Builder(mContext);
		builder.setSmallIcon(R.drawable.notify_icon);

		// 去掉时间
		// builder.setWhen(0);

		builder.setContentText(subMsg);
		builder.setTicker(mContext.getString(R.string.app_name));
		builder.setContentTitle(mContext.getString(R.string.app_name));

		notifi(builder.getNotification());
	}

	@SuppressLint("NewApi")
	private void notifi(Notification.Builder builder) {
		builder.setLargeIcon(BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.ic_launcher));
		notifi(builder.getNotification());
	}

	private void notifi(Notification notification) {
		Intent notificationIntent = new Intent(mContext, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		notification.contentIntent = contentIntent;

		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
}
