package com.chyitech.yiim.runnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.chyitech.yiim.R;
import com.chyitech.yiim.entity.ContactsModel;
import com.chyitech.yiim.entity.RosterGroup;
import com.chyitech.yiim.fragment.ContactFragment;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.provider.YiGroupColumns;
import com.chyitech.yiim.sdk.provider.YiRoomColumns;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.util.YiUtils;

/**
 * 获取群组列表
 * @author saint
 *
 */
public class LocalGetRoomRunnable implements Runnable {
	private List<RosterGroup> mGroups;
	private Map<String, List<ContactsModel>> mEntries;
	private Context mContext;
	private Handler mHandler;

	public LocalGetRoomRunnable(Context context, Handler handler,
			List<RosterGroup> groups, Map<String, List<ContactsModel>> entries) {
		this.mGroups = groups;
		this.mEntries = entries;
		mContext = context;
		mHandler = handler;

		this.mGroups.clear();
		this.mEntries.clear();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Cursor groupCursor = null;
		Cursor rosterCursor = null;

		//未分组的群组
		String unfiledGroup = mContext.getString(R.string.str_my_room);
		RosterGroup unfiledRosterGroup = new RosterGroup();
		unfiledRosterGroup.setName(unfiledGroup);
		mGroups.add(unfiledRosterGroup);

		List<ContactsModel> e = new ArrayList<ContactsModel>();
		mEntries.put(unfiledGroup, e);

		FriendListSort sort = new FriendListSort();
		try {
			//获取群组分组
			groupCursor = YiIMSDK.getInstance().getRoomGroups();
			if (groupCursor != null && groupCursor.getCount() > 0) {
				groupCursor.moveToFirst();

				do {
					RosterGroup rosterGroup1 = new RosterGroup();
					rosterGroup1.setName(groupCursor.getString(groupCursor
							.getColumnIndex(YiGroupColumns.NAME)));
					mGroups.add(rosterGroup1);

					List<ContactsModel> e1 = new ArrayList<ContactsModel>();
					mEntries.put(rosterGroup1.getName(), e1);
				} while (groupCursor.moveToNext());
			}

			//获取群组列表
			rosterCursor = YiIMSDK.getInstance().getRooms();
			if (rosterCursor != null && rosterCursor.getCount() > 0) {
				rosterCursor.moveToFirst();
				do {
					ContactsModel model = new ContactsModel();

					String group = rosterCursor.getString(rosterCursor
							.getColumnIndex(YiRoomColumns.GROUP_NAME));
					if (YiUtils.isStringInvalid(group)) {
						group = unfiledGroup;
					}
					RosterGroup rosterGroup = null;
					for (RosterGroup rosterGroup2 : mGroups) {
						if (rosterGroup2.getName().equals(group)) {
							rosterGroup = rosterGroup2;
						}
					}

					List<ContactsModel> list = mEntries.get(group);

					model.setUser(rosterCursor.getString(rosterCursor
							.getColumnIndex(YiRoomColumns.JID)));
					//加载群组详细资料
					YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
					//以block模式加载
					roomInfo.load(mContext, model.getUser(), false, false, null);

					model.setMsg(roomInfo.getName());

					//判断群组是否已出席
					if (YiIMSDK.getInstance().roomJoinedByJid(model.getUser())) {
						model.setPresence("online");
					} else {
						model.setPresence("unonlie");
					}

					model.setRosterId(rosterCursor.getLong(0));

					if ("online".equals(model.getPresence())) {
						model.setSubMsg("["
								+ mContext.getString(R.string.str_joined) + "]");
						rosterGroup.addOnlineCount();
					} else {
						model.setSubMsg("["
								+ mContext.getString(R.string.str_unjoined)
								+ "]");
					}

					model.setIcon(roomInfo.getIcon());
					model.setRoomOwner(YiIMSDK.getInstance().isRoomOwner(
							roomInfo.getJid()));

					// 加载用户的个性签名
					String sign = roomInfo.getSign();
					if (!YiUtils.isStringInvalid(sign)) {
						model.setSubMsg(model.getSubMsg() + sign);
					}

					rosterGroup.addEntryCount();
					list.add(model);
				} while (rosterCursor.moveToNext());
			}

			Iterator<List<ContactsModel>> it = mEntries.values().iterator();
			while (it.hasNext()) {
				List<ContactsModel> cmList = it.next();
				Collections.sort(cmList, sort);
			}

			mHandler.sendEmptyMessage(ContactFragment.MSG_UPDATE_LIST);
		} catch (Exception ex) {
			// TODO: handle exception
		} finally {
			if (groupCursor != null) {
				groupCursor.close();
				groupCursor = null;
			}

			if (rosterCursor != null) {
				rosterCursor.close();
				rosterCursor = null;
			}
		}
	}

	//按是否已出席排序
	private class FriendListSort implements Comparator<ContactsModel> {
		@Override
		public int compare(ContactsModel lhs, ContactsModel rhs) {
			// TODO Auto-generated method
			// stub
			int l = 0;
			int r = 1;
			try {
				l = Integer.valueOf(StringUtils.escapeUserHost(lhs.getUser()));
				r = Integer.valueOf(StringUtils.escapeUserHost(rhs.getUser()));
			} catch (Exception e) {
				// TODO: handle exception
			}
			int f = r - l;
			if ("online".equals(lhs.getPresence())
					&& !lhs.getPresence().equals(rhs)) {
				return -1;
			} else if ("online".equals(rhs.getPresence())
					&& !rhs.getPresence().equals(lhs)) {
				return 1;
			} else if (lhs.getPresence().equals(rhs)) {
				return (f > 0 ? -1 : 1);
			}
			return 0;
		}
	}
}
