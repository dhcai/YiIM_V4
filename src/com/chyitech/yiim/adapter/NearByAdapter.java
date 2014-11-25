package com.chyitech.yiim.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.sdk.util.YiParamsExt;
import com.chyitech.yiim.util.DateUtils;
import com.chyitech.yiim.util.DistanceUtils;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiUtils;

public class NearByAdapter extends BaseAdapter {
	private Context mContext;
	private Handler mHandler;

	private List<YiParamsExt> mDatas;

	private double mLng;
	private double mLat;

	public NearByAdapter(Context context, List<YiParamsExt> datas,
			Handler handler) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mHandler = handler;
		mDatas = datas;
	}

	public double getLng() {
		return mLng;
	}

	public void setLng(double lng) {
		this.mLng = lng;
	}

	public double getLat() {
		return mLat;
	}

	public void setLat(double lat) {
		this.mLat = lat;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (position < 0 || position > (mDatas.size() - 1)) {
			return null;
		}
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder1 = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.nearby_item, null);
			holder1 = new ViewHolder();
			holder1.mRootView = (RelativeLayout) convertView
					.findViewById(R.id.tab_chats_rootview);
			holder1.mIconView = (ImageView) convertView
					.findViewById(R.id.tab_chats_head);
			holder1.mMsgView = (TextView) convertView
					.findViewById(R.id.tab_chats_msg);
			holder1.mSubMsgView = (TextView) convertView
					.findViewById(R.id.tab_chats_sub_msg);
			holder1.mDateView = (TextView) convertView
					.findViewById(R.id.tab_chats_date);
			holder1.mActiveView = (TextView) convertView
					.findViewById(R.id.tab_chats_active);

			convertView.setTag(holder1);
		} else {
			holder1 = (ViewHolder) convertView.getTag();
		}
		final ViewHolder holder = holder1;

		YiParamsExt ext = (YiParamsExt) getItem(position);
		if (ext == null) {
			return convertView;
		}

		final ImageView imageView = holder.mIconView;
		final String user = ext.getParam("jid");

		holder.mMsgView.setText("");
		holder.mSubMsgView.setText("");

		if (StringUtils.isRoomJid(user)) {
			holder.mIconView.setTag(user);

			final YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
			roomInfo.load(mContext, user, false, true,
					new YiXmppRoomInfoListener() {
						@Override
						public void onSuccess() {
							// TODO Auto-generated method stub
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									holder.mMsgView.setText(roomInfo.getName());

									if (YiUtils.isStringInvalid(roomInfo
											.getName())) {
										holder.mMsgView.setText(roomInfo
												.getJid()
												.replaceAll("@.+$", ""));
									} else {
										holder.mMsgView.setText(roomInfo
												.getName());
									}

									holder.mSubMsgView.setText(roomInfo
											.getSign());

									RoomIcon icon = RoomIcon.eval(roomInfo
											.getIcon());
									holder.mIconView.setImageResource(icon
											.getResId());
								}
							});
						}

						@Override
						public void onFailed() {
							// TODO Auto-generated method stub
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									holder.mMsgView.setText(user.replaceAll(
											"@.+$", ""));
								}
							});
						}
					});
		} else {
			String tag = (String) holder.mIconView.getTag();
			if (!user.equals(tag)) {
				holder.mIconView
						.setImageResource(R.drawable.mini_avatar_shadow);
				holder.mIconView.setTag(user);
				YiAsyncImageLoader.loadBitmapFromStore(user,
						new YiImageLoaderListener() {
							@Override
							public void onImageLoaded(String url, Bitmap bitmap) {
								// TODO Auto-generated method stub
								imageView.setImageBitmap(bitmap);
							}
						});
			}

			final YiXmppVCard vcard = new YiXmppVCard();
			vcard.load(mContext, user, false, true, new YiXmppVCardListener() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					// 加载显示名
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							holder.mMsgView.setText(vcard.displayName());
							holder.mSubMsgView.setText(vcard.getSign());
						}
					});
				}

				@Override
				public void onFailed() {
					// TODO Auto-generated method stub
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							holder.mMsgView.setText(user.replaceAll("@.+$", ""));
						}
					});
				}
			});
		}

		Date msg_date = StringUtils.parseXmppTime(ext.getParam("lastupdatetime"));
		if (msg_date != null) {
			holder.mActiveView.setText(DateUtils.format(mContext, msg_date));
		} else {
			holder.mActiveView.setText("");
		}

		double dis = DistanceUtils.getDistance(mLat, mLng,
				Double.parseDouble(ext.getParam("latitude")),
				Double.parseDouble(ext.getParam("longitude")));
		if (dis > 1000) {
			holder.mDateView.setText(String.format("%.2fkm", dis / 1000.0f));
		} else {
			holder.mDateView.setText(String.format("%dm", (int) dis));
		}

		return convertView;
	}

	private class ViewHolder {
		RelativeLayout mRootView;

		ImageView mIconView;
		TextView mMsgView;
		TextView mSubMsgView;
		TextView mDateView;
		TextView mActiveView;

		ImageView mRatioImageView;
	}
}
