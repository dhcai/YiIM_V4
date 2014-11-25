package com.chyitech.yiim.ui.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.chyitech.yiim.common.ActionSheet;
import com.chyitech.yiim.common.ActionSheet.ActionSheetListener;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.util.YiParamsExt;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.ui.contact.UserInfoActivity;

public class RoomMemberActivity extends CustomTitleFragmentActivity implements
		ActionSheetListener {
	private static final int MSG_ONLINE_MEMBER = 0x01;

	private String mJid;
	private ListView mListView;
	private ContactAdapter mAdapter;
	private List<ContactsModel> mDatas;

	private ContactsModel mLastModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_contact_base);
		super.onCreate(savedInstanceState);

		initViews();
		initDatas();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_ONLINE_MEMBER:
			mDatas.clear();
			mDatas.addAll((List<ContactsModel>) msg.obj);
			
			Collections.sort(mDatas, new MemberListSort());
			mAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mListView = (ListView) findViewById(R.id.contact_base_list);
	}

	protected void initDatas() {
		// TODO Auto-generated method stub
		setTheme(R.style.ActionSheetStyleIOS7);

		mJid = getIntent().getStringExtra("jid");
		mDatas = new ArrayList<ContactsModel>();
		mAdapter = new ContactAdapter(this, mDatas, false);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ContactsModel model = (ContactsModel) mAdapter.getItem(arg2);
				if (model != null) {
					mLastModel = model;
					boolean isOwner = YiIMSDK.getInstance().isRoomOwner(mJid);
					boolean isAdmin = YiIMSDK.getInstance().isRoomAdmin(mJid);
					if ((isOwner || isAdmin)
							&& !"owner".equals(model.getAffiliation())) {
						if ("admin".equals(model.getAffiliation())) {
							if (isOwner) {
								ActionSheet
										.createBuilder(RoomMemberActivity.this,
												getSupportFragmentManager())
										.setCancelButtonTitle(
												getString(R.string.str_cancel))
										.setOtherButtonTitles(
												getString(R.string.str_cancel_admin),
												getString(R.string.str_view_userinfo),
												getString(R.string.str_delete))
										.setCancelableOnTouchOutside(true)
										.setListener(RoomMemberActivity.this)
										.show();
							} else {
								viewUserInfo(model.getUser());
							}
						} else {
							if (isOwner) {
								ActionSheet
										.createBuilder(RoomMemberActivity.this,
												getSupportFragmentManager())
										.setCancelButtonTitle(
												getString(R.string.str_cancel))
										.setOtherButtonTitles(
												getString(R.string.str_set_admin),
												getString(R.string.str_view_userinfo),
												getString(R.string.str_delete))
										.setCancelableOnTouchOutside(true)
										.setListener(RoomMemberActivity.this)
										.show();
							} else {
								ActionSheet
										.createBuilder(RoomMemberActivity.this,
												getSupportFragmentManager())
										.setCancelButtonTitle(
												getString(R.string.str_cancel))
										.setOtherButtonTitles(
												getString(R.string.str_view_userinfo),
												getString(R.string.str_delete))
										.setCancelableOnTouchOutside(true)
										.setListener(RoomMemberActivity.this)
										.show();
							}
						}
					} else {
						viewUserInfo(model.getUser());
					}
				}
			}
		});

		updateList();
	}

	private void viewUserInfo(String jid) {
		Intent intent = new Intent(RoomMemberActivity.this,
				UserInfoActivity.class);
		intent.putExtra("jid", jid);
		startActivity(intent);
	}

	protected void updateList() {
		YiIMSDK.getInstance().getBackgroundService()
				.execute(new LoadOnlineMember());
	}

	private class LoadOnlineMember implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			List<YiParamsExt> datas = YiIMSDK.getInstance()
					.getRoomMembers(mJid);


			ArrayList<ContactsModel> models = new ArrayList<ContactsModel>();
			for (YiParamsExt ext : datas) {
				ContactsModel model = new ContactsModel();
				YiXmppVCard vcard = new YiXmppVCard();
				vcard.load(RoomMemberActivity.this, ext.getParam("jid"), false,
						false, null);

				model.setUser(ext.getParam("jid"));
				model.setMsg(vcard.displayName());
				model.setSubMsg(vcard.getSign());
				model.setAffiliation(ext.getParam("affiliation"));

				models.add(model);
			}

			Message msg = getHandler().obtainMessage(MSG_ONLINE_MEMBER, models);
			getHandler().sendMessage(msg);
		}
	}

	private class MemberListSort implements Comparator<ContactsModel> {
		@Override
		public int compare(ContactsModel lhs, ContactsModel rhs) {
			// TODO Auto-generated method stub
			String la = lhs.getAffiliation();
			String ra = rhs.getAffiliation();
			if (la.equals(ra)) {
				return 0;
			}

			if (la.equals("owner")) {
				return -1;
			}

			if (ra.equals("owner")) {
				return 1;
			}

			if (la.equals("admin")) {
				return -1;
			}

			return 1;
		}
	}

	@Override
	public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOtherButtonClick(ActionSheet actionSheet, int index) {
		// TODO Auto-generated method stub
		boolean isOwner = YiIMSDK.getInstance().isRoomOwner(mJid);
		boolean isAdmin = YiIMSDK.getInstance().isRoomAdmin(mJid);
		if ((isOwner || isAdmin)
				&& !"owner".equals(mLastModel.getAffiliation())) {
			if ("admin".equals(mLastModel.getAffiliation())) {
				if (isOwner) {
					if (index == 0) { // cancel admin
						YiIMSDK.getInstance().setAffiliation(mJid, mLastModel.getUser(), "member");
						mLastModel.setAffiliation("member");
						Collections.sort(mDatas, new MemberListSort());
						mAdapter.notifyDataSetChanged();
					} else if (index == 1) { // view userinfo
						viewUserInfo(mLastModel.getUser());
					} else if (index == 2) { // del user
						YiIMSDK.getInstance().setAffiliation(mJid, mLastModel.getUser(), "none");
						mDatas.remove(mLastModel);
						mAdapter.notifyDataSetChanged();
					}
				}
			} else {
				if (isOwner) {
					if (index == 0) { // set admin
						YiIMSDK.getInstance().setAffiliation(mJid, mLastModel.getUser(), "admin");
						mLastModel.setAffiliation("admin");
						Collections.sort(mDatas, new MemberListSort());
						mAdapter.notifyDataSetChanged();
					} else if (index == 1) { // view userinfo
						viewUserInfo(mLastModel.getUser());
					} else if (index == 2) { // del user
						YiIMSDK.getInstance().setAffiliation(mJid, mLastModel.getUser(), "none");
						mDatas.remove(mLastModel);
						mAdapter.notifyDataSetChanged();
					}
				} else {
					if (index == 0) { // view userinfo
						viewUserInfo(mLastModel.getUser());
					} else if (index == 1) { // del user
						YiIMSDK.getInstance().setAffiliation(mJid, mLastModel.getUser(), "none");
						mDatas.remove(mLastModel);
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
}
