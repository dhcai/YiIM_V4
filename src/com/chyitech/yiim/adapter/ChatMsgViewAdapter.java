package com.chyitech.yiim.adapter;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.EmotionManager;
import com.chyitech.yiim.entity.YiMessage;
import com.chyitech.yiim.entity.YiMessage.MsgType;
import com.chyitech.yiim.gif.GifEmotionUtils;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.sdk.provider.YiMessageColumns;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiLog;
import com.ikantech.support.util.YiUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChatMsgViewAdapter extends CursorAdapter {
	private static final int MSG_TIME_SHOW_DELAY = 120000;

	private LayoutInflater mInflater;
	private String mUser;
	private EmotionManager mEmotionManager;
	private Context mContext;
	private boolean mIsMultChat;

	private GifEmotionUtils mGifEmotionUtils;

	private View.OnClickListener mOnImageClickListener;
	private View.OnClickListener mOnAudioClickListener;
	private View.OnClickListener mOnVideoClickListener;
	private View.OnClickListener mResendClickListener;

	private Map<String, SoftReference<YiXmppVCard>> mVcards;
	private Map<String, SoftReference<Bitmap>> mCache;

	private Handler mHandler = new Handler();

	public View.OnClickListener getOnVideoClickListener() {
		return mOnVideoClickListener;
	}

	public void setOnVideoClickListener(
			View.OnClickListener mOnVideoClickListener) {
		this.mOnVideoClickListener = mOnVideoClickListener;
	}

	public void setResendClickListener(View.OnClickListener listener) {
		mResendClickListener = listener;
	}

	public ChatMsgViewAdapter(Context context, GifEmotionUtils gifEmotionUtils,
			Cursor c, String currentUser, EmotionManager emotionManager) {
		super(context, c, true);
		// TODO Auto-generated constructor stub
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mUser = currentUser;
		mEmotionManager = emotionManager;

		mGifEmotionUtils = gifEmotionUtils;

		mVcards = new HashMap<String, SoftReference<YiXmppVCard>>();
		mCache = new HashMap<String, SoftReference<Bitmap>>();
		mIsMultChat = false;
	}

	public void setOnImageClickListener(View.OnClickListener listener) {
		mOnImageClickListener = listener;
	}

	public void setOnAudioClickListener(View.OnClickListener listener) {
		mOnAudioClickListener = listener;
	}

	public void setIsMultiChat(boolean v) {
		mIsMultChat = v;
	}

	private int transfer(int position) {
		return getCount() - position - 1;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.getDropDownView(transfer(position), convertView, parent);
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		position = transfer(position);
		if (position < 0 || position > getCursor().getCount() - 1) {
			return null;
		}
		return super.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(transfer(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.getView(transfer(position), convertView, parent);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = (ViewHolder) arg0.getTag();
		if (viewHolder != null) {
			String uu = arg2.getString(arg2
					.getColumnIndex(YiMessageColumns.SENDER));
			if (StringUtils.isRoomJid(uu)) {
				uu = StringUtils.getJidResouce(uu) + "@"
						+ YiIMSDK.getInstance().getServerName();
			} else {
				uu = StringUtils.escapeUserResource(uu);
			}
			final String sender = uu;
			boolean isComMsg = !sender.startsWith(mUser);

			long date = arg2.getLong(arg2
					.getColumnIndex(YiMessageColumns.CREATE_DATE));
			viewHolder.systemTip.setText(DateFormat.format(
					"yyyy-MM-dd kk:mm:ss", new Date(date)));

			viewHolder.systemTip.setVisibility(View.GONE);
			viewHolder.systemContent.setVisibility(View.GONE);

			int position = arg2.getPosition();

			Cursor cursor = (Cursor) getItem(arg2.getCount()
					- arg2.getPosition() - 2);
			if (cursor != null) {
				long date2 = cursor.getLong(cursor
						.getColumnIndex(YiMessageColumns.CREATE_DATE));
				if (Math.abs(date2 - date) > MSG_TIME_SHOW_DELAY) {
					viewHolder.systemTip.setVisibility(View.VISIBLE);
				}
			}

			arg2.moveToPosition(position);

			if (arg2.getPosition() == arg2.getCount() - 1) {
				viewHolder.systemTip.setVisibility(View.VISIBLE);
			}

			String content = arg2.getString(arg2
					.getColumnIndex(YiMessageColumns.CONTENT));
			YiLog.getInstance().i("msg %s", content);
			YiMessage message = null;
			try {
				message = YiMessage.fromString(content);
			} catch (Exception e) {
				// TODO: handle exception
				YiLog.getInstance().e(e, "create YiIMMessage failed. %s",
						content);
				return;
			}

			if (!mIsMultChat) {
				viewHolder.leftUserName.setVisibility(View.GONE);
				viewHolder.rightUserName.setVisibility(View.GONE);
			}

			TextView userTextView = null;
			ImageView userImageView = null;
			// 如果消息是对方发给我的
			if (isComMsg) {
				viewHolder.rightView.setVisibility(View.GONE);
				viewHolder.leftView.setVisibility(View.VISIBLE);

				userTextView = viewHolder.leftUserName;
				userImageView = viewHolder.leftHead;
				dealMsg(arg2.getString(arg2
						.getColumnIndex(YiMessageColumns.MSG_ID)),
						viewHolder.leftContent, viewHolder.leftImageRoot,
						viewHolder.leftImage, viewHolder.leftImageProgress,
						viewHolder.leftImageMask, viewHolder.leftAddon,
						message, isComMsg, 2, viewHolder);
			} else {
				viewHolder.leftView.setVisibility(View.GONE);
				viewHolder.rightView.setVisibility(View.VISIBLE);

				userTextView = viewHolder.rightUserName;
				userImageView = viewHolder.rightHead;

				dealMsg(arg2.getString(arg2
						.getColumnIndex(YiMessageColumns.MSG_ID)),
						viewHolder.rightContent, viewHolder.rightImageRoot,
						viewHolder.rightImage, viewHolder.rightImageProgress,
						viewHolder.rightImageMask, viewHolder.rightAddon,
						message, isComMsg, arg2.getInt(arg2
								.getColumnIndex(YiMessageColumns.RECEIPT)),
						viewHolder);
			}

			userTextView.setText(StringUtils.escapeUserHost(sender));

			final TextView textView = userTextView;
			SoftReference<YiXmppVCard> softReference = mVcards.get(sender);
			if (softReference != null && softReference.get() != null) {
				YiXmppVCard vcard = softReference.get();
				userTextView.setText(vcard.displayName());
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
										textView.setText(vCard.displayName());
									}
								});
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub

							}
						});
			}

			final ImageView imageView = userImageView;
			if (!sender.equals(imageView.getTag())) {
				imageView.setImageResource(R.drawable.mini_avatar_shadow);
				imageView.setTag(sender);
				YiAsyncImageLoader.loadBitmapFromStore(sender,
						new YiImageLoaderListener() {

							@Override
							public void onImageLoaded(String url, Bitmap bitmap) {
								// TODO Auto-generated method stub
								imageView.setImageBitmap(bitmap);
							}
						});
			}

			if (message.getType().equals(MsgType.SYSTEM)) {
				viewHolder.leftView.setVisibility(View.GONE);
				viewHolder.rightView.setVisibility(View.GONE);
				viewHolder.systemContent.setText(message.getBody());
				viewHolder.systemContent.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dealMsg(String id, TextView contentView, View imageRoot,
			final ImageView imageView, TextView imageProgress, View imageMask,
			ImageView addon, YiMessage message, boolean isComMsg,
			int isReceipted, ViewHolder holder) {
		String content = message.getBody();
		MsgType msgType = message.getType();

		contentView.setOnClickListener(null);
		contentView.setTag(null);
		addon.setVisibility(View.GONE);
		contentView.setVisibility(View.GONE);
		imageRoot.setVisibility(View.GONE);

		imageView.setTag(null);
		addon.setTag(null);
		addon.setVisibility(View.GONE);
		addon.setOnClickListener(null);

		holder.rightProgress.setVisibility(View.GONE);

		if (isReceipted == YiXmppConstant.MSG_STATUS_RECEIVED) {
			addon.setVisibility(View.VISIBLE);
			addon.setImageResource(R.drawable.msg_receipt);
		} else if (isReceipted == YiXmppConstant.MSG_STATUS_ERROR) { // 发送失败
			addon.setVisibility(View.VISIBLE);
			addon.setTag(id);
			addon.setImageResource(R.drawable.btn_style_resend);
			addon.setOnClickListener(mResendClickListener);
		} else if (isReceipted == YiXmppConstant.MSG_STATUS_SENDING
				&& !isComMsg) {
			holder.rightProgress.setVisibility(View.VISIBLE);
		}

		if (MsgType.PLANTEXT.equals(msgType)) {
			// 处理表情
			// v1.setText(content);
			mGifEmotionUtils.setSpannableText(contentView, content, mHandler);
			contentView.setVisibility(View.VISIBLE);
			contentView.setCompoundDrawablesWithIntrinsicBounds(null, null,
					null, null);
		} else if (MsgType.BIG_EMOTION.equals(msgType)) {// 处理大表情
			int emotionId = EmotionManager.getEmotionResourceId(content);
			if (emotionId != -1) {
				mGifEmotionUtils.setSpannableText(contentView, "", mHandler);
				contentView.setVisibility(View.VISIBLE);
				contentView.setCompoundDrawablesWithIntrinsicBounds(mContext
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

				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView
						.getLayoutParams();
				params.width = w;
				params.height = h;
				imageView.setLayoutParams(params);
			} catch (Exception e) {
				// TODO: handle exception
			}

			if (!YiUtils.isStringInvalid(status) && "sending".equals(status)) {
				imageProgress.setVisibility(View.VISIBLE);
				imageMask.setVisibility(View.VISIBLE);
				int pr = 0;
				try {
					pr = Integer.parseInt(message.getParams().get("percent"));
				} catch (Exception e) {
					// TODO: handle exception
				}
				imageProgress.setText(pr + "%");

				String uri = message.getParams().get("uri");

				if (!YiUtils.isStringInvalid(uri)
						&& !uri.equals(imageRoot.getTag())) {
					ImageLoader.getInstance().displayImage(uri, imageView);
					imageRoot.setTag(uri);
				}
			} else if (!YiUtils.isStringInvalid(status)
					&& "error".equals(status)) {
				imageProgress.setVisibility(View.GONE);
				imageMask.setVisibility(View.GONE);
				addon.setVisibility(View.VISIBLE);
				addon.setTag(id);
				addon.setImageResource(R.drawable.btn_style_resend);
				addon.setOnClickListener(mResendClickListener);
				holder.rightProgress.setVisibility(View.GONE);

				String uri = message.getParams().get("uri");

				if (!YiUtils.isStringInvalid(uri)
						&& !uri.equals(imageRoot.getTag())) {
					ImageLoader.getInstance().displayImage(uri, imageView);
					imageRoot.setTag(uri);
				}
			} else {
				imageProgress.setVisibility(View.GONE);
				imageMask.setVisibility(View.GONE);

				String uri = message.getParams().get("small_url");
				if (YiUtils.isStringInvalid(uri)) {
					uri = message.getBody();
				}

				if (!YiUtils.isStringInvalid(uri)
						&& !uri.equals(imageRoot.getTag())) {
					ImageLoader.getInstance().displayImage(uri, imageView);
					imageRoot.setTag(uri);
				}
			}

			imageRoot.setVisibility(View.VISIBLE);
			imageView.setTag(message);
			imageView.setOnClickListener(mOnImageClickListener);
		} else if (MsgType.AUDIO.equals(msgType)) { // 处理语音信息
			String filePath = content;

			contentView.setVisibility(View.VISIBLE);

			mGifEmotionUtils.setSpannableText(contentView, "", mHandler);

			int duration = -1;
			try {
				duration = Integer.valueOf(message.getParams().get(
						"audio_duration"));

				mGifEmotionUtils.setSpannableText(contentView,
						String.format("%d\"", duration / 1000), mHandler);
			} catch (Exception e) {
				// TODO: handle exception
			}

			if (isComMsg) {
				contentView.setCompoundDrawablesWithIntrinsicBounds(
						mContext.getResources().getDrawable(
								R.drawable.chatfrom_voice_playing), null, null,
						null);
			} else {
				contentView.setCompoundDrawablesWithIntrinsicBounds(
						null,
						null,
						mContext.getResources().getDrawable(
								R.drawable.chatto_voice_playing), null);
			}
			contentView.setTag(filePath);
			contentView.setOnClickListener(mOnAudioClickListener);
		} else if (MsgType.VIDEO.equals(msgType)) { // 处理视频信息
			String filePath = message.getBody();
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageDrawable(mContext.getResources().getDrawable(
					R.drawable.chatfrom_voice_playing));
			imageView.setTag(filePath);
			imageView.setOnClickListener(mOnVideoClickListener);
		}
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View convertView = mInflater.inflate(R.layout.chatting_item_msg_text,
				null);

		ViewHolder viewHolder = new ViewHolder();

		viewHolder.systemTip = (TextView) convertView
				.findViewById(R.id.chatting_item_system);
		viewHolder.systemContent = (TextView) convertView
				.findViewById(R.id.chatting_item_system_content);

		viewHolder.leftView = convertView.findViewById(R.id.chatting_item_left);
		viewHolder.leftHead = (ImageView) convertView
				.findViewById(R.id.chatting_item_left_userhead);
		viewHolder.leftUserName = (TextView) convertView
				.findViewById(R.id.chatting_item_left_username);
		viewHolder.leftAddon = (ImageView) convertView
				.findViewById(R.id.chatting_item_left_addon);
		viewHolder.leftContent = (TextView) convertView
				.findViewById(R.id.chatting_item_left_content);
		viewHolder.leftImage = (ImageView) convertView
				.findViewById(R.id.content_left_image);
		viewHolder.leftImageRoot = convertView
				.findViewById(R.id.content_left_image_root);
		viewHolder.leftImageProgress = (TextView) convertView
				.findViewById(R.id.content_left_image_progress);
		viewHolder.leftImageMask = convertView
				.findViewById(R.id.content_left_image_mask);

		viewHolder.rightView = convertView
				.findViewById(R.id.chatting_item_right);
		viewHolder.rightHead = (ImageView) convertView
				.findViewById(R.id.chatting_item_right_userhead);
		viewHolder.rightUserName = (TextView) convertView
				.findViewById(R.id.chatting_item_right_username);
		viewHolder.rightAddon = (ImageView) convertView
				.findViewById(R.id.chatting_item_right_addon);
		viewHolder.rightContent = (TextView) convertView
				.findViewById(R.id.chatting_item_right_content);
		viewHolder.rightImage = (ImageView) convertView
				.findViewById(R.id.content_right_image);
		viewHolder.rightImageRoot = convertView
				.findViewById(R.id.content_right_image_root);
		viewHolder.rightImageProgress = (TextView) convertView
				.findViewById(R.id.content_right_image_progress);
		viewHolder.rightProgress = convertView
				.findViewById(R.id.chatting_item_right_progress);
		viewHolder.rightImageMask = convertView
				.findViewById(R.id.content_right_image_mask);

		convertView.setTag(viewHolder);
		return convertView;
	}

	private class ViewHolder {
		TextView systemTip;

		TextView systemContent;

		View leftView;
		TextView leftUserName;
		TextView leftContent;
		ImageView leftAddon;
		View leftImageRoot;
		TextView leftImageProgress;
		View leftImageMask;
		ImageView leftImage;
		ImageView leftHead;

		View rightView;
		ImageView rightHead;
		ImageView rightAddon;
		TextView rightUserName;
		TextView rightContent;
		View rightImageRoot;
		TextView rightImageProgress;
		View rightImageMask;
		View rightProgress;
		ImageView rightImage;
	}
}
