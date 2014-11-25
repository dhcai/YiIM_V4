package com.chyitech.yiim.ui.contact;

import android.database.Cursor;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.ui.base.BaseGroupManagerActivity;

public class ContactGroupActivity extends BaseGroupManagerActivity {

	@Override
	protected void removeGroup(String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().removeGroup(group);
	}

	@Override
	protected void addGroup(String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().addGroup(group);
	}

	@Override
	protected void renameGroup(String oldName, String newName) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().renameGroup(oldName, newName);
	}

	@Override
	protected void moveToGroup(String jid, String group) {
		// TODO Auto-generated method stub
		YiIMSDK.getInstance().moveFriend(jid, group);
	}

	@Override
	protected String getUnfiledGroupName() {
		// TODO Auto-generated method stub
		return getString(R.string.str_my_friend);
	}

	@Override
	protected Cursor getGroupCursor() {
		// TODO Auto-generated method stub
		return YiIMSDK.getInstance().getRosterGroups();
	}

	@Override
	protected String getDeleteGroupString() {
		// TODO Auto-generated method stub
		return getString(R.string.str_del_group_tip);
	}

}
