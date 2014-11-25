package com.chyitech.yiim.adapter;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.EmotionManager;
import com.chyitech.yiim.entity.YiMessage;
import com.chyitech.yiim.entity.YiMessage.MsgType;
import com.chyitech.yiim.gif.GifEmotionUtils;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.sdk.provider.YiMessageColumns;
import com.chyitech.yiim.util.DateUtils;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.util.YiUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChatRecordAdapter extends CursorAdapter {
	private LayoutInflater mInflater;
	private Context mContext;

	private Map<String, SoftReference<YiXmppVCard>> mVcards;
	private Map<String, SoftReference<Bitmap>> mCache;

	private GifEmotionUtils mGifEmotionUtils;

	private View.OnClickListener mOnImageClickListener;
	private View.OnClickListener mOnAudioClickListener;

	private Handler mHandler = new Handler();

	public ChatRecordAdapter(Context context, Cursor c) {
		super(context, c, false);
		// TODO Auto-generated constructor stub
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mVcards = new HashMap<String, SoftReference<YiXmppVCard>>();
		mCache = new HashMap<String, SoftReference<Bitmap>>();

		EmotionManager.initializeEmoji(mContext);
		EmotionManager.initializeClassicalEmoji(mContext);

		mGifEmotionUtils = new GifEmotionUtils(mContext,
				EmotionManager.getClassicalEmotions(),
				EmotionManager.getClassicalEmotionDescs(), R.drawable.face);
	}

	public void setOnImageClickListener(View.OnClickListener listener) {
		mOnImageClickListener = listener;
	}

	public void setOnAudioClickListener(View.OnClickListener listener) {
		mOnAudioClickListener = listener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = (ViewHolder) view.getTag();

		try {
			String uu = cursor.getString(cursor
					.getColumnIndex(YiMessageColumns.SENDER));
			if (StringUtils.isRoomJid(uu)) {
				uu = StringUtils.getJidResouce(uu) + "@"
						+ YiIMSDK.getInstance().getServerName();
			} else {
				uu = StringUtils.escapeUserResource(uu);
			}
			final String sender = uu;

			final String dateString = DateUtils.format(
					mContext,
					new Date(cursor.getLong(cursor
							.getColumnIndex(YiMessageColumns.CREATE_DATE))));
			final boolean isReceipt = cursor.getInt(cursor
					.getColumnIndex(YiMessageColumns.RECEIPT)) == 1;

			if (isReceipt) {
				viewHolder.text1.setText(StringUtils.escapeUserHost(sender)
						+ " " + dateString + " "
						+ mContext.getString(R.string.str_received));
			} else {
				viewHolder.text1.setText(StringUtils.escapeUserHost(sender)
						+ " " + dateString);
			}

			final TextView textView = viewHolder.text1;
			SoftReference<YiXmppVCard> softReference = mVcards.get(sender);
			if (softReference != null && softReference.get() != null) {
				YiXmppVCard vcard = softReference.get();
				if (isReceipt) {
					viewHolder.text1.setText(vcard.displayName() + " "
							+ dateString + " "
							+ mContext.getString(R.string.str_received));
				} else {
					viewHolder.text1.setText(vcard.displayName() + " "
							+ dateString);
				}
			} else {
				final YiXmppVCard vCard = new YiXmppVCard();
				vCard.load(mContext, sender, false, true,
						new YiXmppVCardListener() {
							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								mVcards.put(sender,
										new SoftReference<YiXmppVCard>(vCard));
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										if (isReceipt) {
											textView.setText(vCard
													.displayName()
													+ " "
													+ dateString
													+ " "
													+ mContext
															.getString(R.string.str_received));
										} else {
											textView.setText(vCard
													.displayName()
													+ " "
													+ dateString);
										}
									}
								});
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub

							}
						});
			}

			YiMessage message = YiMessage.fromString(cursor.getString(cursor
					.getColumnIndex(YiMessageColumns.CONTENT)));

			if (message.getType().equals(MsgType.SYSTEM)) {
				viewHolder.text1.setText(dateString);
			}

			dealMsg(message, viewHolder.text2, viewHolder.imageView);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void dealMsg(YiMessage message, TextView textView,
			ImageView imageView) {
		MsgType msgType = message.getType();
		String content = message.getBody();

		textView.setOnClickListener(null);
		textView.setTag(null);
		textView.setText("");
		imageView.setVisibility(View.GONE);
		textView.setVisibility(View.VISIBLE);

		if (MsgType.PLANTEXT.equals(msgType)) {
			// 处理表情
			// v1.setText(content);
			mGifEmotionUtils.setSpannableText(textView, content, mHandler);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
					null);
		} else if (MsgType.BIG_EMOTION.equals(msgType)) {// 处理大表情
			int emotionId = EmotionManager.getEmotionResourceId(content);
			if (emotionId != -1) {
				mGifEmotionUtils.setSpannableText(textView, "", mHandler);
				textView.setCompoundDrawablesWithIntrinsicBounds(mContext
						.getResources().getDrawable(emotionId), null, null,
						null);
			}
		} else if (MsgType.IMAGE.equals(msgType)) { // 处理图片信息
			String status = message.getParams().get("status");

			try {
				int width = Integer.parseInt(message.getParams().get("width"));
				int height = Integer
						.parseInt(message.getParams().get("height"));

				int small_width = 0;
				int small_height = Integer.parseInt(message.getParams().get(
						"small_height"));

				float scale = 1.0F;

				int w = 0;
				int h = 0;
				if (small_width > 0 && width > small_width) {
					scale = small_width / width;
				} else if (small_height > 0 && height > small_height) {
					scale = small_height * 1.0F / height;
				}

				if (height * scale > 180) {
					h = 180;
				} else {
					h = (int) (height * scale);
				}

				w = (int) (width * 1.0F / height * h);

				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView
						.getLayoutParams();
				params.width = w;
				params.height = h;
				imageView.setLayoutParams(params);
			} catch (Exception e) {
				// TODO: handle exception
			}

			if (!YiUtils.isStringInvalid(status) && "sending".equals(status)) {
				String uri = message.getParams().get("uri");

				if (!YiUtils.isStringInvalid(uri)) {
					ImageLoader.getInstance().displayImage(uri, imageView);
				}
			} else if (!YiUtils.isStringInvalid(status)
					&& "error".equals(status)) {
				String uri = message.getParams().get("uri");

				if (!YiUtils.isStringInvalid(uri)) {
					ImageLoader.getInstance().displayImage(uri, imageView);
				}
			} else {
				String uri = message.getParams().get("small_url");
				if (YiUtils.isStringInvalid(uri)) {
					uri = message.getBody();
				}

				if (!YiUtils.isStringInvalid(uri)) {
					ImageLoader.getInstance().displayImage(uri, imageView);
				}
			}

			imageView.setTag(message);
			imageView.setOnClickListener(mOnImageClickListener);
			textView.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
		} else if (MsgType.AUDIO.equals(msgType)) { // 处理语音信息
			String filePath = content;

			mGifEmotionUtils.setSpannableText(textView, "", mHandler);

			int duration = -1;
			try {
				duration = Integer.valueOf(message.getParams().get(
						"audio_duration"));

				mGifEmotionUtils.setSpannableText(textView,
						String.format("%d\"", duration / 1000), mHandler);
			} catch (Exception e) {
				// TODO: handle exception
			}

			textView.setCompoundDrawablesWithIntrinsicBounds(
					mContext.getResources().getDrawable(
							R.drawable.chatto_voice_playing_r), null, null,
					null);
			textView.setTag(filePath);
			textView.setOnClickListener(mOnAudioClickListener);
		} else if (MsgType.SYSTEM.equals(msgType)) {
			mGifEmotionUtils.setSpannableText(textView, content, mHandler);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
					null);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View convertView = mInflater.inflate(R.layout.chat_record_item, null);

		ViewHolder holder = new ViewHolder();

		holder.text1 = (TextView) convertView.findViewById(R.id.text1);
		holder.text2 = (TextView) convertView.findViewById(R.id.text2);
		holder.imageView = (ImageView) convertView.findViewById(R.id.image);

		convertView.setTag(holder);

		return convertView;
	}

	private class ViewHolder {
		TextView text1;
		TextView text2;
		ImageView imageView;
	}
}
