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
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.provider.YiGroupColumns;
import com.chyitech.yiim.sdk.provider.YiRosterColumns;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.util.YiUtils;

/**
 * 获取好友列表
 * @author saint
 *
 */
public class LocalGetRosterRunnable implements Runnable {
	private List<RosterGroup> mGroups;
	private Map<String, List<ContactsModel>> mEntries;
	private Context mContext;
	private Handler mHandler;

	public LocalGetRosterRunnable(Context context,Handler handler, List<RosterGroup> groups,
			Map<String, List<ContactsModel>> entries) {
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
		
		//未分组好友
		String unfiledGroup = mContext.getString(R.string.str_my_friend);
		RosterGroup unfiledRosterGroup = new RosterGroup();
		unfiledRosterGroup.setName(unfiledGroup);
		mGroups.add(unfiledRosterGroup);
		
		List<ContactsModel> e = new ArrayList<ContactsModel>();
		mEntries.put(unfiledGroup, e);

		FriendListSort sort = new FriendListSort();
		try {
			//获取好友分组列表
			groupCursor = YiIMSDK.getInstance().getRosterGroups();
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

			//获取好友列表
			rosterCursor = YiIMSDK.getInstance().getRoster();
			if(rosterCursor != null && rosterCursor.getCount() > 0) {
				rosterCursor.moveToFirst();
				do {
					ContactsModel model = new ContactsModel();
					
					String group = rosterCursor.getString(rosterCursor.getColumnIndex(YiRosterColumns.GROUP_NAME));
					if (YiUtils.isStringInvalid(group)) {
						group = unfiledGroup;
					}
					RosterGroup rosterGroup = null;
					for (RosterGroup rosterGroup2 : mGroups) {
						if(rosterGroup2.getName().equals(group)) {
							rosterGroup = rosterGroup2;
						}
					}
					
					List<ContactsModel> list = mEntries.get(group);
					
					model.setUser(rosterCursor.getString(rosterCursor.getColumnIndex(YiRosterColumns.JID)));
					YiXmppVCard vcard = new YiXmppVCard();
					//以block方式加载VCard
					vcard.load(mContext, model.getUser(), false, false, null);
					
					//设置显示名称
					model.setMsg(vcard.displayName());
					
					//判断用户是否在线
					if(YiIMSDK.getInstance().isOnlineByJid(model.getUser())) {
						model.setPresence("online");
					}else {
						model.setPresence("unonlie");
					}
					//设置好友类型
					model.setRosterType(rosterCursor.getInt(rosterCursor.getColumnIndex(YiRosterColumns.TYPE)));
					
					model.setRosterId(rosterCursor.getLong(0));
					
					if ("online".equals(model.getPresence())) {
						model.setSubMsg("["
								+ mContext
										.getString(R.string.str_online)
								+ "]");
						rosterGroup.addOnlineCount();
					} else {
						model.setSubMsg("["
								+ mContext
										.getString(R.string.str_unavailable)
								+ "]");
					}

					if (!vcard.verified()) {
						model.setSubMsg(model.getSubMsg()
								+ "["
								+ mContext
										.getString(R.string.str_not_certified)
								+ "]");
					}

					// 加载用户的个性签名
					String sign = vcard.getSign();
					if (!YiUtils.isStringInvalid(sign)) {
						model.setSubMsg(model.getSubMsg() + sign);
					}
					
					rosterGroup.addEntryCount();
					list.add(model);
				} while (rosterCursor.moveToNext());
			}
			
			Iterator<List<ContactsModel>> it = mEntries.values().iterator();
			while(it.hasNext()) {
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

	//按用户是否在线排序
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
