package com.chyitech.yiim.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.NearByAdapter;
import com.chyitech.yiim.app.YiIMApplication;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.util.YiParamsExt;
import com.chyitech.yiim.ui.contact.UserInfoActivity;
import com.chyitech.yiim.ui.room.RoomInfoActivity;
import com.chyitech.yiim.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ikantech.support.widget.YiFragment;

/**
 * 附近TAB页
 * @author saint
 *
 */
public class NearByFragment extends YiFragment {
	private static final int MSG_CANCEL_LBS = 0x01;

	//每页的条目大小
	private static final int PAGE_SIZE = 10;

	private View mRootView;
	private PullToRefreshListView mListView;

	private View mLoadingRootView;
	private View mLoadingView;
	private View mNodataView;

	private NativeReceiver mNativeReceiver;
	private List<YiParamsExt> mDatas;
	private NearByAdapter mAdapter;

	//当前经度
	private String mLng;
	//当前纬度
	private String mLat;
	//PageSize
	private int mLimit;
	//PageOffset
	private int mOffset;

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_CANCEL_LBS:
			updateLoading();
			mListView.onRefreshComplete();
			break;
		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = inflater.inflate(R.layout.frag_tab_nearby, null);

		mListView = (PullToRefreshListView) mRootView
				.findViewById(R.id.tab_nearby_list);

		mLoadingRootView = mRootView.findViewById(R.id.common_loading);
		mLoadingView = mRootView.findViewById(R.id.common_waiting);
		mNodataView = mRootView.findViewById(R.id.common_nodata);

		mDatas = new ArrayList<YiParamsExt>();

		mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			//下拉时，先获取LBS信息，再刷新
			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				requestLBS(true);
			}

			//上拉加载更多
			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				getHandler().post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mOffset += PAGE_SIZE;
						List<YiParamsExt> items = YiIMSDK.getInstance()
								.requestNearBy(mLng, mLat, "mix",
										String.valueOf(mLimit),
										String.valueOf(mOffset));
						mDatas.addAll(items);

						updateLoading();
						mListView.onRefreshComplete();

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				});

			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				YiParamsExt ext = (YiParamsExt) arg0.getAdapter().getItem(arg2);
				if (ext != null) {
					String jid = ext.getParam("jid");
					//如果是群组，则跳转至群组信息页
					if (StringUtils.isRoomJid(jid)) {
						Intent intent = new Intent(getActivity(),
								RoomInfoActivity.class);
						intent.putExtra("jid", jid);
						startActivity(intent);
					} else {//如果是用户
						Intent intent = new Intent(getActivity(),
								UserInfoActivity.class);
						intent.putExtra("jid", jid);
						startActivity(intent);
					}
				}
			}
		});

		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mNativeReceiver = new NativeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.chyitech.yiim.LBS_UPDATE");
		getActivity().registerReceiver(mNativeReceiver, intentFilter);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (!hidden) {
			requestLBS(false);
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		requestLBS(false);
	}

	protected void requestLBS(boolean force) {
		if (force || mDatas.size() < 1) {
			((YiIMApplication) getActivity().getApplication()).mLocationClient
					.stop();
			((YiIMApplication) getActivity().getApplication()).mLocationClient
					.start();
			getHandler().removeMessages(MSG_CANCEL_LBS);
			getHandler().sendEmptyMessageDelayed(MSG_CANCEL_LBS, 5000);
		}
	}

	private void updateLoading() {
		if (mDatas.size() > 0) {
			mLoadingRootView.setVisibility(View.GONE);
		} else {
			mLoadingRootView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		}
	}

	protected void updateList() {
		if (mDatas == null || mDatas.size() < 1) {
			List<YiParamsExt> items = YiIMSDK.getInstance().requestNearBy(mLng,
					mLat, "mix", String.valueOf(mLimit),
					String.valueOf(mOffset));
			mDatas.addAll(items);

			mAdapter = new NearByAdapter(getActivity(), mDatas, getHandler());

			mAdapter.setLng(Double.parseDouble(mLng));
			mAdapter.setLat(Double.parseDouble(mLat));

			mListView.setAdapter(mAdapter);
		} else {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		updateLoading();
		mListView.onRefreshComplete();
	}

	private class NativeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, final Intent intent) {
			// TODO Auto-generated method stub
			//当获取LBS成功后
			if (intent.getAction().equals("com.chyitech.yiim.LBS_UPDATE")) {
				getHandler().post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						getHandler().removeMessages(MSG_CANCEL_LBS);
						mDatas.clear();
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
							updateLoading();
						}

						mLng = intent.getStringExtra("longitude");
						mLat = intent.getStringExtra("latitude");
						mLimit = PAGE_SIZE;
						mOffset = 0;

						if (mAdapter != null) {
							mAdapter.setLng(Double.parseDouble(mLng));
							mAdapter.setLat(Double.parseDouble(mLat));
						}

						getHandler().post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								updateList();
							}
						});
					}
				});
			}
		}
	}
}
