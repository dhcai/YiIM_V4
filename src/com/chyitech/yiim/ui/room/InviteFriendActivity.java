package com.chyitech.yiim.ui.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.GroupListAdapter;
import com.chyitech.yiim.adapter.GroupListAdapter.FriendItem;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.entity.RosterGroup;
import com.chyitech.yiim.fragment.ContactFragment;
import com.chyitech.yiim.runnable.LocalGetRosterRunnable;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.util.YiParamsExt;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.adapter.PinnedHeaderExpandableListViewAdapter;
import com.ikantech.support.common.YiPinnedHeaderExListViewMng;
import com.ikantech.support.common.YiPinnedHeaderExListViewMng.OnPinnedHeaderChangeListener;
import com.ikantech.support.util.YiUtils;

public class InviteFriendActivity extends CustomTitleActivity {
	private View mPinnedHeaderView;
	private ImageView mPinnedImageView;
	private TextView mPinnedTextView;
	private TextView mPinnedRightTextView;

	private String mWhichActivity;
	private ExpandableListView mListView;

	private List<RosterGroup> mGroups = new ArrayList<RosterGroup>();

	private Map<String, List<ContactsModel>> mEntries = new HashMap<String, List<ContactsModel>>();

	private List<RosterGroup> mGroups2 = new ArrayList<RosterGroup>();
	private Map<String, List<ContactsModel>> mEntries2 = new HashMap<String, List<ContactsModel>>();

	private List<FriendItem> mSelectedFriendItems = new ArrayList<GroupListAdapter.FriendItem>();

	private PinnedHeaderExpandableListViewAdapter mAdapter = null;
	private YiPinnedHeaderExListViewMng mExListViewMng;

	private String mRoomJid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_invite_friend);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case ContactFragment.MSG_UPDATE_LIST:
			updateFriendList();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mListView = (ExpandableListView) findViewById(R.id.invite_friend_list);

		mPinnedHeaderView = findViewById(R.id.invite_friend_list_group_header);

		mPinnedImageView = (ImageView) findViewById(R.id.list_group_img);
		mPinnedTextView = (TextView) findViewById(R.id.list_group_text);
		mPinnedRightTextView = (TextView) findViewById(R.id.list_group_right_text);

		mPinnedImageView.setBackgroundResource(R.drawable.group_unfold_arrow);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mRoomJid = getIntent().getStringExtra("jid");
		mWhichActivity = getIntent().getStringExtra("which");

		mAdapter = new GroupListAdapter(this, mGroups, mEntries,
				mSelectedFriendItems, true);

		setTitleBarRightBtnText(getString(R.string.str_finish));

		mExListViewMng = new YiPinnedHeaderExListViewMng(mListView, mAdapter,
				mPinnedHeaderView);
		mExListViewMng
				.setOnPinnedHeaderChangeListener(new OnPinnedHeaderChangeListener() {

					@Override
					public void onPinnedHeaderChanged(int groupPosition) {
						// TODO Auto-generated method stub
						RosterGroup rosterGroup = (RosterGroup) mAdapter
								.getGroup(groupPosition);
						if (rosterGroup != null) {
							mPinnedTextView.setText(rosterGroup.getName());
							// mPinnedRightTextView.setText(String.format("%d/%d",
							// rosterGroup.getOnlineCount(),
							// rosterGroup.getEntryCount()));
						}
					}
				});

		mListView.setAdapter(mAdapter);

		loadFriendList();
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				FriendItem item = new FriendItem(groupPosition, childPosition);
				if (mSelectedFriendItems.contains(item)) {
					mSelectedFriendItems.remove(item);
				} else {
					mSelectedFriendItems.add(item);
				}
				mAdapter.notifyDataSetChanged();
				return false;
			}
		});
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		if (mSelectedFriendItems.size() > 0) {
			String[] rets = new String[mSelectedFriendItems.size()];
			for (int i = 0; i < mSelectedFriendItems.size(); i++) {
				FriendItem item = mSelectedFriendItems.get(i);
				ContactsModel model = (ContactsModel) mAdapter.getChild(
						item.groupPosition, item.childPosition);
				if (model != null) {
					rets[i] = model.getUser();
					if (RoomInfoActivity.class.getSimpleName().equals(
							mWhichActivity)) {
						YiIMSDK.getInstance().inviteRoomMember(mRoomJid,
								rets[i]);
					}
				} else {
					rets[i] = "";
				}
			}
			if (!RoomInfoActivity.class.getSimpleName().equals(mWhichActivity)) {
				Intent intent = getIntent();
				intent.putExtra("friends", rets);
				setResult(RESULT_OK, intent);
			}
			finish();
		}
	}

	public void updateFriendList() {
		if (!YiUtils.isStringInvalid(mRoomJid)) {
			Iterator<YiParamsExt> nicks = YiIMSDK.getInstance()
					.getRoomMembers(mRoomJid).iterator();
			if (nicks != null) {
				while (nicks.hasNext()) {
					YiParamsExt exit = nicks.next();
					Iterator<List<ContactsModel>> iterator = mEntries2.values()
							.iterator();
					while (iterator.hasNext()) {
						List<ContactsModel> models = iterator.next();
						Iterator<ContactsModel> iterator2 = models.iterator();
						while (iterator2.hasNext()) {
							ContactsModel model = iterator2.next();
							if (model.getUser().startsWith(
									StringUtils.getJidResouce(exit
											.getParam("jid")))) {
								iterator2.remove();
							}
						}

					}
				}
			}
		}

		if (mGroups2 != null) {
			mGroups.clear();
			mGroups.addAll(mGroups2);

			mGroups2.clear();
			mGroups2 = null;
		}

		if (mEntries2 != null) {
			mEntries.clear();
			mEntries.putAll(mEntries2);

			mEntries2.clear();
			mEntries2 = null;
		}

		mAdapter.notifyDataSetChanged();
	}

	public void loadFriendList() {
		YiIMSDK.getInstance()
				.getBackgroundService()
				.execute(
						new LocalGetRosterRunnable(this, getHandler(),
								mGroups2, mEntries2));
	}
}
