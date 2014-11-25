package com.chyitech.yiim.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ExpandableListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.fragment.base.BasicContactFragment;
import com.chyitech.yiim.runnable.LocalGetRoomRunnable;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.ui.ChatActivity;

/**
 * 群组列表
 * @author saint
 *
 */
public class RoomFragment extends BasicContactFragment {

	@Override
	protected void updateList() {
		// TODO Auto-generated method stub
		getHandler().post(
				new LocalGetRoomRunnable(getActivity(), getHandler(), mGroups2,
						mEntries2));
	}

	@Override
	protected String broadcastAction() {
		// TODO Auto-generated method stub
		return YiXmppConstant.NOTIFICATION_ON_ROOM_UPDATED;
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
		String msg = null;
		//如果是群主
		if (model.isRoomOwner()) {
			msg = getString(R.string.str_destroy_room_tip, model.getMsg());
		} else {
			msg = getString(R.string.str_delete_room_tip, model.getMsg());
		}
		showMsgDialog(null, msg, getString(R.string.str_ok),
				getString(R.string.str_cancel), new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (model.isRoomOwner()) {
							//销毁群组
							YiIMSDK.getInstance().destroyRoom(model.getUser(),
									null);
						} else {
							//退出群组
							YiIMSDK.getInstance().removeRoom(model.getUser());
						}
					}
				}, null);
	}
}
