package com.chyitech.yiim.ui.room;

import android.database.Cursor;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.ui.base.BaseGroupManagerActivity;

public class RoomGroupActivity extends BaseGroupManagerActivity {

	@Override
	protected Cursor getGroupCursor() {
		// TODO Auto-generated method stub
		return YiIMSDK.getInstance().getRoomGroups();
	}

	@Override
	protected void removeGroup(String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().removeRoomGroup(group);
	}

	@Override
	protected void addGroup(String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().addRoomGroup(group);
	}

	@Override
	protected void renameGroup(String oldName, String newName) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().renameRoomGroup(oldName, newName);
	}

	@Override
	protected void moveToGroup(String jid, String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().moveRoom(jid, group);
	}

	@Override
	protected String getUnfiledGroupName() {
		// TODO Auto-generated method stub
		return getString(R.string.str_my_room);
	}

	@Override
	protected String getDeleteGroupString() {
		// TODO Auto-generated method stub
		return getString(R.string.str_del_room_group_tip);
	}

}
