package com.chyitech.yiim.ui.mine;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ContactAdapter;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.ui.base.CustomTitleActivity;
import com.chyitech.yiim.ui.contact.UserInfoActivity;

public class PrivacyListActivity extends CustomTitleActivity {
	private static final int MSG_COMPLETED = 0x01;

	private ListView mListView;
	private ContactAdapter mAdapter;
	private List<ContactsModel> mDatas;

	private View mLoadingRootView;
	private View mLoadingView;
	private View mNodataView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_contact_base);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_COMPLETED:
			List<ContactsModel> datas = (List<ContactsModel>) msg.obj;
			mDatas.clear();
			if (datas.size() > 0) {
				mDatas.addAll((List<ContactsModel>) msg.obj);
				mLoadingRootView.setVisibility(View.GONE);
			} else {
				mLoadingRootView.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
				mNodataView.setVisibility(View.VISIBLE);
			}
			mAdapter.notifyDataSetChanged();
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
		mListView = (ListView) findViewById(R.id.contact_base_list);

		mLoadingRootView = findViewById(R.id.common_loading);
		mLoadingView = findViewById(R.id.common_waiting);
		mNodataView = findViewById(R.id.common_nodata);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mDatas = new ArrayList<ContactsModel>();
		mAdapter = new ContactAdapter(this, mDatas, true);
		mAdapter.setDeleteRes(R.string.str_cancel_privacy);
		mAdapter.setOnDeleteClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				YiIMSDK.getInstance().cancelBlockMessage((String) v.getTag());
				updateList();
			}
		});

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ContactsModel model = (ContactsModel) mAdapter.getItem(arg2);
				if (model != null) {
					Intent intent = new Intent(PrivacyListActivity.this,
							UserInfoActivity.class);
					intent.putExtra("jid", model.getUser());
					startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		updateList();
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

	protected void updateList() {
		mLoadingRootView.setVisibility(View.VISIBLE);
		mLoadingView.setVisibility(View.VISIBLE);
		mNodataView.setVisibility(View.GONE);

		YiIMSDK.getInstance().getBackgroundService()
				.execute(new LoadRunnable());
	}

	private class LoadRunnable implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<String> datas = YiIMSDK.getInstance().blockedList();
			ArrayList<ContactsModel> models = new ArrayList<ContactsModel>();
			for (String jid : datas) {
				ContactsModel model = new ContactsModel();
				YiXmppVCard vcard = new YiXmppVCard();
				vcard.load(PrivacyListActivity.this, jid, false, false, null);

				model.setUser(jid);
				model.setMsg(vcard.displayName());
				model.setSubMsg(vcard.getSign());

				models.add(model);
			}

			Message msg = getHandler().obtainMessage(MSG_COMPLETED, models);
			getHandler().sendMessage(msg);
		}
	}
}
