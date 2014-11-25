package com.chyitech.yiim.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ExpandableListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.fragment.base.BasicContactFragment;
import com.chyitech.yiim.runnable.LocalGetRosterRunnable;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.ui.ChatActivity;

/**
 * 好友列表
 * @author saint
 *
 */
public class ContactFragment extends BasicContactFragment {
	@Override
	protected void updateList() {
		// TODO Auto-generated method stub
		getHandler().post(
						new LocalGetRosterRunnable(getActivity(), getHandler(),
								mGroups2, mEntries2));
	}

	@Override
	protected String broadcastAction() {
		// TODO Auto-generated method stub
		return YiXmppConstant.NOTIFICATION_ON_ROSTER_UPDATED;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		final ContactsModel model = (ContactsModel) mAdapter.getChild(
				groupPosition, childPosition);
		if (model == null) {
			return false;
		}

		Intent intent = new Intent(getActivity(), ChatActivity.class);
		intent.putExtra("to", model.getUser());
		getActivity().startActivity(intent);
		return true;
	}

	@Override
	protected void delete(final ContactsModel model) {
		// TODO Auto-generated method stub
		showMsgDialog(null,
				getString(R.string.str_delete_friend_tip, model.getMsg()),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//删除好友
						YiIMSDK.getInstance().removeFriend(model.getUser());
					}
				}, null);
	}
}
