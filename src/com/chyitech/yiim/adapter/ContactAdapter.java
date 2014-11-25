package com.chyitech.yiim.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ListItemDeleteView;
import com.chyitech.yiim.common.SwipeListViewAdapter;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiUtils;

public class ContactAdapter extends BaseAdapter implements SwipeListViewAdapter {
	private Context mContext;
	private List<ContactsModel> mDatas = null;
	private boolean mEdit;
	private int mDeleteRes;

	private OnItemClickListener mItemClickListener;
	private boolean mVisible;

	private View.OnClickListener mOnDeleteClickListener = null;

	public ContactAdapter(Context context, List<ContactsModel> datas,
			boolean edit) {
		super();
		mContext = context;
		mDatas = datas;
		mEdit = edit;
		mDeleteRes = R.string.str_delete;
	}

	public void setDeleteRes(int deleteRes) {
		this.mDeleteRes = deleteRes;
	}

	public View.OnClickListener getOnDeleteClickListener() {
		return mOnDeleteClickListener;
	}

	public void setOnDeleteClickListener(
			View.OnClickListener onDeleteClickListener) {
		this.mOnDeleteClickListener = onDeleteClickListener;
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
		ContactsModel item = (ContactsModel) getItem(position);
		if (item == null) {
			return null;
		}

		ViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.contact_item, null);
			holder = new ViewHolder();
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
			holder.mTimeView = (TextView) convertView
					.findViewById(R.id.tab_chats_active);
			holder.mRatioImageView = (ImageView) convertView
					.findViewById(R.id.ratio);
			holder.mDelete = (Button) convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (mVisible) {
//			ListItemDeleteView dv = (ListItemDeleteView) convertView;
//			dv.beginScroll(300);
			convertView.scrollTo(0, 0);
		}

		convertView.setTag(R.id.key_id1, Integer.valueOf(position));
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (getItemClickListener() != null) {
					int p = (Integer) v.getTag(R.id.key_id1);
					getItemClickListener().onItemClick(null, null, p, 0);
				}
			}
		});

		if (mEdit) {
			holder.mDelete.setVisibility(View.VISIBLE);
		} else {
			holder.mDelete.setVisibility(View.GONE);
		}

		holder.mDelete.setText(mDeleteRes);

		final ImageView avatarImageView = holder.mIconView;
		if (StringUtils.isRoomJid(item.getUser())) {
			RoomIcon icon = RoomIcon.eval(item.getIcon());
			avatarImageView.setImageResource(icon.getResId());
			avatarImageView.setTag(null);
		} else {
			String tag = (String) avatarImageView.getTag();
			if (!item.getUser().equals(tag)) {
				avatarImageView.setImageResource(R.drawable.mini_avatar_shadow);
				avatarImageView.setTag(item.getUser());
				YiAsyncImageLoader.loadBitmapFromStore(item.getUser(),
						new YiImageLoaderListener() {
							@Override
							public void onImageLoaded(String url, Bitmap bitmap) {
								// TODO Auto-generated method stub
								avatarImageView.setImageBitmap(bitmap);
							}
						});
			}
		}

		if (YiUtils.isStringInvalid(item.getMsg())) {
			holder.mMsgView.setText(item.getUser().replaceAll("@.+$", ""));
		} else {
			holder.mMsgView.setText(item.getMsg().replaceAll("@.+$", ""));
		}

		if ("owner".equals(item.getAffiliation())) {
			holder.mSubMsgView.setText(String.format("[%s]%s",
					mContext.getString(R.string.str_affiliation_owner),
					item.getSubMsg()));
		} else if ("admin".equals(item.getAffiliation())) {
			holder.mSubMsgView.setText(String.format("[%s]%s",
					mContext.getString(R.string.str_affiliation_admin),
					item.getSubMsg()));
		} else {
			holder.mSubMsgView.setText(item.getSubMsg());
		}

		holder.mDelete.setTag(item.getUser());
		holder.mDelete.setOnClickListener(mOnDeleteClickListener);

		return convertView;
	}

	private class ViewHolder {
		RelativeLayout mRootView;

		ImageView mIconView;
		TextView mMsgView;
		TextView mSubMsgView;
		TextView mDateView;
		TextView mTimeView;

		Button mDelete;

		ImageView mRatioImageView;
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
