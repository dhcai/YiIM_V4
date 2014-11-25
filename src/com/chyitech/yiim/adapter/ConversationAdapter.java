package com.chyitech.yiim.adapter;

import java.sql.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ListItemDeleteView;
import com.chyitech.yiim.common.SwipeListViewAdapter;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.sdk.provider.YiConversationColumns;
import com.chyitech.yiim.util.DateUtils;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiLog;
import com.ikantech.support.util.YiUtils;

public class ConversationAdapter extends CursorAdapter implements
		SwipeListViewAdapter {
	private Context mContext;
	private Handler mHandler;
	private View.OnClickListener mOnDeleteClickListener = null;
	private OnItemClickListener mItemClickListener;
	private boolean mVisible;

	public ConversationAdapter(Context context, Cursor cursor, Handler handler) {
		// TODO Auto-generated constructor stub
		super(context, cursor, true);
		mContext = context;
		mHandler = handler;
	}

	public View.OnClickListener getOnDeleteClickListener() {
		return mOnDeleteClickListener;
	}

	public void setOnDeleteClickListener(
			View.OnClickListener onDeleteClickListener) {
		this.mOnDeleteClickListener = onDeleteClickListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = super.getView(position, convertView, parent);
		view.setTag(R.id.key_id1, Integer.valueOf(position));
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();

		if (holder == null || cursor == null) {
			return;
		}

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (getItemClickListener() != null) {
					int p = (Integer) v.getTag(R.id.key_id1);
					getItemClickListener().onItemClick(null, null, p, 0);
				}
			}
		});

		final ImageView imageView = holder.mIconView;

		final int type = cursor.getInt(cursor
				.getColumnIndex(YiConversationColumns.MSG_TYPE));

		String uu = cursor.getString(cursor
				.getColumnIndex(YiConversationColumns.JID));

		holder.mDeleteBtn.setTag(R.id.key_id1, uu);
		holder.mDeleteBtn.setTag(R.id.key_id2, type);
		holder.mDeleteBtn.setOnClickListener(mOnDeleteClickListener);

		final String user = StringUtils.escapeUserResource(uu).split(":")[0];
		final boolean reg = uu.contains(":register:");
		YiLog.getInstance().i("con uu: %s", uu);

		if (mVisible) {
//			ListItemDeleteView dv = (ListItemDeleteView) view;
//			dv.beginScroll(300);
			view.scrollTo(0, 0);
		}

		holder.mSubMsgView.setText(cursor.getString(cursor
				.getColumnIndex(YiConversationColumns.SUB_MSG)));

		if (StringUtils.isRoomJid(user)) {
			if (YiXmppConstant.CONVERSATION_TYPE_REQUEST == type) {
				final String jid = StringUtils
						.escapeUserResource(cursor.getString(cursor
								.getColumnIndex(YiConversationColumns.SUB_MSG)));
				String tag = (String) holder.mIconView.getTag();
				if (!jid.equals(tag)) {
					holder.mIconView
							.setImageResource(R.drawable.mini_avatar_shadow);
					holder.mIconView.setTag(jid);
					YiAsyncImageLoader.loadBitmapFromStore(jid,
							new YiImageLoaderListener() {
								@Override
								public void onImageLoaded(String url,
										Bitmap bitmap) {
									// TODO Auto-generated method stub
									imageView.setImageBitmap(bitmap);
								}
							});
				}

				final YiXmppVCard vcard = new YiXmppVCard();
				vcard.load(mContext, jid, false, true,
						new YiXmppVCardListener() {
							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								// 加载显示名
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										holder.mMsgView.setText(vcard
												.displayName());
									}
								});
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										holder.mMsgView.setText(user
												.replaceAll("@.+$", ""));
									}
								});
							}
						});

				if (reg) {
					holder.mSubMsgView.setText(mContext.getString(
							R.string.str_room_register_request,
							StringUtils.escapeUserHost(user)));
				} else {
					holder.mSubMsgView.setText(mContext.getString(
							R.string.str_room_add_request,
							StringUtils.escapeUserHost(user)));
				}
				final YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
				roomInfo.load(mContext, user, false, true,
						new YiXmppRoomInfoListener() {
							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								if (!YiUtils.isStringInvalid(roomInfo.getName())) {
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											if (reg) {
												holder.mSubMsgView.setText(mContext
														.getString(
																R.string.str_room_register_request,
																StringUtils
																		.escapeUserHost(user)));
											} else {
												holder.mSubMsgView.setText(mContext
														.getString(
																R.string.str_room_add_request,
																StringUtils
																		.escapeUserHost(user)));
											}
										}
									});
								}
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub
							}
						});
			} else {
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
										holder.mMsgView.setText(roomInfo
												.getName());

										if (YiUtils.isStringInvalid(roomInfo
												.getName())) {
											holder.mMsgView.setText(roomInfo
													.getJid().replaceAll(
															"@.+$", ""));
										} else {
											holder.mMsgView.setText(roomInfo
													.getName());
										}

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
										holder.mMsgView.setText(user
												.replaceAll("@.+$", ""));
									}
								});
							}
						});
			}
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
						}
					});
				}

				@Override
				public void onFailed() {
					// TODO Auto-generated method stub
					holder.mMsgView.setText(user.replaceAll("@.+$", ""));
				}
			});

			if (YiXmppConstant.CONVERSATION_TYPE_REQUEST == type) {
				holder.mSubMsgView.setText(mContext
						.getString(R.string.str_entry_add_request));
			}
		}

		Date msg_date = new Date(cursor.getLong(cursor
				.getColumnIndex(YiConversationColumns.MODIFIED_DATE)));
		holder.mDateView.setText(DateUtils.format(mContext, msg_date));
		int dealt = cursor.getInt(cursor
				.getColumnIndex(YiConversationColumns.DEALT));

		if (dealt == 1 && YiXmppConstant.CONVERSATION_TYPE_REQUEST == type) {
			holder.mActiveView
					.setBackgroundResource(R.drawable.friendactivity_newnotice);
			holder.mActiveView.setText("");
		} else {
			if (YiXmppConstant.CONVERSATION_TYPE_RECORD == type && dealt > 0) {
				holder.mActiveView
						.setBackgroundResource(R.drawable.drawable_unread);
				holder.mActiveView.setText(String.valueOf(dealt));
			} else {
				holder.mActiveView.setBackgroundDrawable(null);
				holder.mActiveView.setText("");
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View convertView = LayoutInflater.from(mContext).inflate(
				R.layout.contact_item, null);
		ViewHolder holder = new ViewHolder();
		holder.mRootView = (RelativeLayout) convertView
				.findViewById(R.id.tab_chats_rootview);
		holder.mIconView = (ImageView) convertView
				.findViewById(R.id.tab_chats_head);
		holder.mMsgView = (TextView) convertView
				.findViewById(R.id.tab_chats_msg);
		holder.mSubMsgView = (TextView) convertView
				.findViewById(R.id.tab_chats_sub_msg);
		holder.mDateView = (TextView) convertView
				.findViewById(R.id.tab_chats_date);
		holder.mActiveView = (TextView) convertView
				.findViewById(R.id.tab_chats_active);
		holder.mDeleteBtn = (Button) convertView.findViewById(R.id.delete);
		convertView.setTag(holder);

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

		Button mDeleteBtn;
	}

	@Override
	public OnItemClickListener getItemClickListener() {
		// TODO Auto-generated method stub
		return mItemClickListener;
	}

	@Override
	public void setItemClickListener(OnItemClickListener listener) {
		// TODO Auto-generated method stub
		mItemClickListener = listener;
	}

	@Override
	public void setVisible(boolean v) {
		// TODO Auto-generated method stub
		mVisible = v;
	}
}
