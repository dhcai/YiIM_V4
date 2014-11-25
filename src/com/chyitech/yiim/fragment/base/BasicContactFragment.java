package com.chyitech.yiim.fragment.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.GroupListAdapter;
import com.chyitech.yiim.common.SwipeExpandableAdapter;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.entity.RosterGroup;
import com.ikantech.support.common.YiPinnedHeaderExListViewMng;
import com.ikantech.support.common.YiPinnedHeaderExListViewMng.OnPinnedHeaderChangeListener;
import com.ikantech.support.widget.YiFragment;

/**
 * 群组及好友列表的基础Fragment
 * 
 * @author saint
 * 
 */
public abstract class BasicContactFragment extends YiFragment implements
		OnChildClickListener {
	public static final int MSG_UPDATE_LIST = 0x01;
	public static final int MSG_UPDATE = 0x02;

	private View mRootView;

	private NativeReceiver mNativeReceiver;

	protected GroupListAdapter mAdapter;
	private ExpandableListView mExpandableListView;
	private YiPinnedHeaderExListViewMng mExListViewMng;

	private View mPinnedHeaderView;
	private ImageView mPinnedImageView;
	private TextView mPinnedTextView;
	private TextView mPinnedRightTextView;

	private View mLoadingRootView;
	private View mLoadingView;
	private View mNodataView;

	private List<RosterGroup> mGroups = new ArrayList<RosterGroup>();
	private Map<String, List<ContactsModel>> mEntries = new HashMap<String, List<ContactsModel>>();

	protected List<RosterGroup> mGroups2 = new ArrayList<RosterGroup>();
	protected Map<String, List<ContactsModel>> mEntries2 = new HashMap<String, List<ContactsModel>>();

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_UPDATE_LIST:
			mGroups.clear();
			mGroups.addAll(mGroups2);

			mEntries.clear();
			mEntries.putAll(mEntries2);

			mAdapter.notifyDataSetChanged();

			if (mGroups.size() > 0) {
				mLoadingRootView.setVisibility(View.GONE);
			} else {
				mLoadingRootView.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
				mNodataView.setVisibility(View.VISIBLE);
			}
			break;
		case MSG_UPDATE:
			updateList();
		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = inflater.inflate(R.layout.frag_tab_contacts, null);

		mExpandableListView = (ExpandableListView) mRootView
				.findViewById(R.id.contacts_list);

		mPinnedHeaderView = mRootView.findViewById(R.id.list_group_header);

		mPinnedImageView = (ImageView) mRootView
				.findViewById(R.id.list_group_img);
		mPinnedTextView = (TextView) mRootView
				.findViewById(R.id.list_group_text);
		mPinnedRightTextView = (TextView) mRootView
				.findViewById(R.id.list_group_right_text);

		mPinnedImageView.setBackgroundResource(R.drawable.group_unfold_arrow);

		mLoadingRootView = mRootView.findViewById(R.id.common_loading);
		mLoadingView = mRootView.findViewById(R.id.common_waiting);
		mNodataView = mRootView.findViewById(R.id.common_nodata);

		mExpandableListView.setOnChildClickListener(this);

		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mAdapter = new GroupListAdapter(getActivity(), mGroups, mEntries);
		mAdapter.setOnDeleteClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				delete((ContactsModel) v.getTag());
			}
		});

		mExListViewMng = new YiPinnedHeaderExListViewMng(mExpandableListView,
				mAdapter, mPinnedHeaderView);
		mExListViewMng
				.setOnPinnedHeaderChangeListener(new OnPinnedHeaderChangeListener() {

					@Override
					public void onPinnedHeaderChanged(int groupPosition) {
						// TODO Auto-generated method stub
						final RosterGroup rosterGroup = (RosterGroup) mAdapter
								.getGroup(groupPosition);
						if (rosterGroup != null
								&& !rosterGroup.equals(mPinnedHeaderView
										.getTag())) {
							mPinnedHeaderView.setTag(rosterGroup);
							mPinnedTextView.setText(rosterGroup.getName());
							mPinnedRightTextView.setText(String.format("%d/%d",
									rosterGroup.getOnlineCount(),
									rosterGroup.getEntryCount()));
						}
					}
				});
		mExpandableListView.setAdapter((SwipeExpandableAdapter) mAdapter);

		mNativeReceiver = new NativeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(broadcastAction());
		getActivity().registerReceiver(mNativeReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(mNativeReceiver);
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (!hidden) {
			getHandler().removeMessages(MSG_UPDATE);
			getHandler().sendEmptyMessageDelayed(MSG_UPDATE, 200);
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getHandler().removeMessages(MSG_UPDATE);
		getHandler().sendEmptyMessageDelayed(MSG_UPDATE, 200);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (mAdapter != null) {
			mAdapter.setVisible(false);
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null) {
			mAdapter.setVisible(true);
		}
	}

	protected abstract void updateList();

	protected abstract String broadcastAction();

	protected abstract void delete(ContactsModel model);

	private class NativeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(broadcastAction())) {
				getHandler().removeMessages(MSG_UPDATE);
				getHandler().sendEmptyMessageDelayed(MSG_UPDATE, 200);
			}
		}
	}

}
