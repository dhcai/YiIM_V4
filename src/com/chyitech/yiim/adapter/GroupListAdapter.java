package com.chyitech.yiim.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ListItemDeleteView;
import com.chyitech.yiim.common.SwipeExpandableAdapter;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.entity.RoomIcon;
import com.chyitech.yiim.entity.RosterGroup;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiUtils;

public class GroupListAdapter extends SwipeExpandableAdapter {
	private static final int INT_KEY_GROUP = R.id.key_group;
	private static final int INT_KEY_CHILD = R.id.key_child;

	private Context mContext;

	private List<FriendItem> mSelectedFriendItems;

	private List<RosterGroup> mGroups = new ArrayList<RosterGroup>();

	private Map<String, List<ContactsModel>> mEntries = new HashMap<String, List<ContactsModel>>();

	private boolean mSelectMode = false;
	private boolean mEdit = true;
	private View.OnClickListener mOnDeleteClickListener = null;

	public GroupListAdapter(Context context, List<RosterGroup> rosterGroups,
			Map<String, List<ContactsModel>> etries) {
		this(context, rosterGroups, etries, null, false);
		mEdit = true;
	}

	public GroupListAdapter(Context context, List<RosterGroup> rosterGroups,
			Map<String, List<ContactsModel>> etries, List<FriendItem> items,
			boolean selectMode) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mGroups = rosterGroups;
		mEntries = etries;
		mSelectedFriendItems = items;
		mSelectMode = selectMode;
		mEdit = false;
	}

	@Override
	public View getGroupHeaderView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupViewHolder viewHolder = null;

		if (convertView != null) {
			viewHolder = (GroupViewHolder) convertView.getTag();
		} else {
			viewHolder = new GroupViewHolder();

			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.list_group_item, null);
			viewHolder.mArrowImageView = (ImageView) convertView
					.findViewById(R.id.list_group_img);
			viewHolder.mGroupTextView = (TextView) convertView
					.findViewById(R.id.list_group_text);
			viewHolder.mRightTextView = (TextView) convertView
					.findViewById(R.id.list_group_right_text);

			convertView.setTag(viewHolder);
		}

		convertView.setTag(INT_KEY_GROUP, Integer.valueOf(groupPosition));
		convertView.setTag(INT_KEY_CHILD, Integer.valueOf(-1));

		if (isExpanded) {
			viewHolder.mArrowImageView
					.setBackgroundResource(R.drawable.group_unfold_arrow);
		} else {
			viewHolder.mArrowImageView
					.setBackgroundResource(R.drawable.group_fold_arrow);
		}

		RosterGroup group = (RosterGroup) getGroup(groupPosition);

		if (!YiUtils.isStringInvalid(group.getName())) {
			viewHolder.mGroupTextView.setText(group.getName());
		}

		if (!mSelectMode) {
			viewHolder.mRightTextView.setText(String.format("%d/%d",
					group.getOnlineCount(), group.getEntryCount()));
		}

		return convertView;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mGroups.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		if (groupPosition < 0 || groupPosition >= mGroups.size()) {
			return null;
		}
		return mGroups.get(groupPosition);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		RosterGroup groupObject = (RosterGroup) getGroup(groupPosition);
		if (groupObject != null) {
			List<ContactsModel> entries = mEntries.get(groupObject.getName());
			if (entries != null) {
				return entries.size();
			}
		}
		return 0;
	}

	public View.OnClickListener getOnDeleteClickListener() {
		return mOnDeleteClickListener;
	}

	public void setOnDeleteClickListener(
			View.OnClickListener onDeleteClickListener) {
		this.mOnDeleteClickListener = onDeleteClickListener;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ContactsModel item = (ContactsModel) getChild(groupPosition,
				childPosition);
		if (item == null) {
			return null;
		}

		ChildViewHolder holder = null;
		if (null == convertView) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.contact_item, null);
			holder = new ChildViewHolder();
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
			holder = (ChildViewHolder) convertView.getTag();
		}

		if (isVisible()) {
//			Integer gp = (Integer) convertView.getTag(INT_KEY_GROUP);
//			Integer cp = (Integer) convertView.getTag(INT_KEY_CHILD);
//			boolean sm = false;
//			if (gp != null && cp != null) {
//				if (gp == groupPosition && cp == childPosition) {
//					sm = true;
//				}
//			}
//			if (sm) {
//				ListItemDeleteView dv = (ListItemDeleteView) convertView;
//				dv.beginScroll(300);
//			} else {
				convertView.scrollTo(0, 0);
//			}
		}

		convertView.setTag(INT_KEY_GROUP, Integer.valueOf(groupPosition));
		convertView.setTag(INT_KEY_CHILD, Integer.valueOf(childPosition));
		if (mEdit) {
			holder.mDelete.setVisibility(View.VISIBLE);
		} else {
			holder.mDelete.setVisibility(View.GONE);
		}

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int gp = (Integer) v.getTag(INT_KEY_GROUP);
				int cp = (Integer) v.getTag(INT_KEY_CHILD);
				if (getChildClickListener() != null) {
					getChildClickListener().onChildClick(null, null, gp, cp, 0);
				}
			}
		});

		holder.mDelete.setTag(item);
		holder.mDelete.setOnClickListener(mOnDeleteClickListener);
		if (item.isRoomOwner()) {
			holder.mDelete.setText(mContext.getString(R.string.str_destroy));
		} else {
			holder.mDelete.setText(mContext.getString(R.string.str_delete));
		}

		final ImageView avatarImageView = holder.mIconView;
		if (StringUtils.isRoomJid(item.getUser())) {
			RoomIcon icon = RoomIcon.eval(item.getIcon());
			avatarImageView.setImageResource(icon.getResId());
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

		holder.mSubMsgView.setText(item.getSubMsg());

		if (mSelectMode) {
			FriendItem item2 = new FriendItem(groupPosition, childPosition);
			if (mSelectedFriendItems.contains(item2)) {
				holder.mRatioImageView.setVisibility(View.VISIBLE);
			} else {
				holder.mRatioImageView.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		RosterGroup groupObject = (RosterGroup) getGroup(groupPosition);
		if (groupObject != null) {
			List<ContactsModel> entries = mEntries.get(groupObject.getName());
			if (entries != null) {
				if (childPosition >= 0 && childPosition < entries.size()) {
					return entries.get(childPosition);
				}
			}
		}
		return null;
	}

	private class GroupViewHolder {
		ImageView mArrowImageView;
		TextView mGroupTextView;
		TextView mRightTextView;
	}

	private class ChildViewHolder {
		RelativeLayout mRootView;

		ImageView mIconView;
		TextView mMsgView;
		TextView mSubMsgView;
		TextView mDateView;
		TextView mTimeView;

		Button mDelete;

		ImageView mRatioImageView;
	}

	public static class FriendItem {
		public int groupPosition;
		public int childPosition;

		public FriendItem(int group, int child) {
			groupPosition = group;
			childPosition = child;
		}

		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			if (o instanceof FriendItem) {
				FriendItem item = (FriendItem) o;
				return item.groupPosition == groupPosition
						&& item.childPosition == childPosition;
			}
			return super.equals(o);
		}
	}
}
