package com.chyitech.yiim.ui.room;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ContactAdapter;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.ikantech.support.util.YiUtils;

public class FindRoomActivity extends CustomTitleActivity {
	private EditText mEditText;
	private ListView mListView;

	private View mLoadingRootView;
	private View mLoadingView;
	private View mNodataView;

	private ContactAdapter mAdapter;
	private List<ContactsModel> mDatas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_find_room);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mEditText = (EditText) findViewById(R.id.find_room_edittext);
		mListView = (ListView) findViewById(R.id.find_room_list);

		mLoadingRootView = findViewById(R.id.common_loading);
		mLoadingView = findViewById(R.id.common_waiting);
		mNodataView = findViewById(R.id.common_nodata);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mDatas = new ArrayList<ContactsModel>();
		mAdapter = new ContactAdapter(this, mDatas, false);
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ContactsModel model = (ContactsModel) mAdapter.getItem(arg2);
				if (model != null) {
					Intent intent = new Intent(FindRoomActivity.this,
							RoomInfoActivity.class);
					intent.putExtra("jid", model.getUser());
					startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	public void onSearchClick(View v) {
		if (isStringInvalid(mEditText.getText())) {
			showMsgDialog(R.string.err_search_empty);
			return;
		}

		mDatas.clear();
		mAdapter.notifyDataSetChanged();

		mLoadingRootView.setVisibility(View.VISIBLE);
		mLoadingView.setVisibility(View.VISIBLE);
		mNodataView.setVisibility(View.GONE);

		final YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
		roomInfo.loadByName(this, mEditText.getText().toString().trim(), false,
				true, new YiXmppRoomInfo.YiXmppRoomInfoListener() {
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						if (roomInfo.isExist()) {
							getHandler().post(new UpdateRunnable(roomInfo));
						} else {
							getHandler().post(new NoDataRunnable());
						}
					}

					@Override
					public void onFailed() {
						// TODO Auto-generated method stub
						getHandler().post(new NoDataRunnable());
					}
				});
	}

	private class NoDataRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mDatas.clear();
			mAdapter.notifyDataSetChanged();

			mLoadingRootView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		}

	}

	private class UpdateRunnable implements Runnable {
		private YiXmppRoomInfo mRoomInfo;

		public UpdateRunnable(YiXmppRoomInfo roomInfo) {
			mRoomInfo = roomInfo;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ContactsModel model = new ContactsModel();

			model.setUser(mRoomInfo.getJid());

			model.setMsg(mRoomInfo.getName());

			model.setIcon(mRoomInfo.getIcon());

			// 加载用户的个性签名
			String sign = mRoomInfo.getSign();
			if (!YiUtils.isStringInvalid(sign)) {
				model.setSubMsg(sign);
			}

			mDatas.clear();
			mDatas.add(model);
			mAdapter.notifyDataSetChanged();

			mLoadingRootView.setVisibility(View.GONE);
		}

	}
}
