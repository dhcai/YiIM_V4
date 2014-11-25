package com.chyitech.yiim.entity;

import java.util.Date;

/**
 * 联系人数据模型
 * 
 * @author saint
 * 
 */
public class ContactsModel {
	/**
	 * MSG
	 */
	private String mMsg;
	/**
	 * 当前用户JID
	 */
	private String mUser;
	/**
	 * SubMsg
	 */
	private String mSubMsg;
	/**
	 * 更新时间
	 */
	private Date mDateTime;
	/**
	 * 好友关系
	 */
	private int mRosterType;
	/**
	 * 好友关系数据库ID，即联系人在本地数据库中的ID
	 */
	private long mRosterId;
	/**
	 * 是否在线，online为在线，unonlie为离线
	 */
	private String mPresence;
	/**
	 * 头像Icon，主要用于群组
	 */
	private String mIcon;
	/**
	 * 是否为群主
	 */
	private boolean mIsRoomOwner;
	/**
	 * 群组角色
	 */
	private String mAffiliation;

	public ContactsModel() {
		mMsg = "";
		mSubMsg = "";
		mRosterType = 0;
		mDateTime = new Date();
		mRosterId = -1;
		mIsRoomOwner = false;
	}

	public String getIcon() {
		return mIcon;
	}

	public void setIcon(String icon) {
		this.mIcon = icon;
	}

	public boolean isRoomOwner() {
		return mIsRoomOwner;
	}

	public void setRoomOwner(boolean isRoomOwner) {
		this.mIsRoomOwner = isRoomOwner;
	}

	public long getRosterId() {
		return mRosterId;
	}

	public void setRosterId(long rosterId) {
		this.mRosterId = rosterId;
	}

	public String getMsg() {
		return mMsg;
	}

	public void setMsg(String mMsg) {
		this.mMsg = mMsg;
	}

	public String getSubMsg() {
		return mSubMsg;
	}

	public void setSubMsg(String mSubMsg) {
		this.mSubMsg = mSubMsg;
	}

	public Date getDateTime() {
		return mDateTime;
	}

	public void setDateTime(Date mDateTime) {
		this.mDateTime = mDateTime;
	}

	public String getUser() {
		return mUser;
	}

	public void setUser(String mUser) {
		this.mUser = mUser;
	}

	public int getRosterType() {
		return mRosterType;
	}

	public void setRosterType(int mRosterType) {
		this.mRosterType = mRosterType;
	}

	public String getPresence() {
		return mPresence;
	}

	public void setPresence(String presence) {
		this.mPresence = presence;
	}

	public String getAffiliation() {
		return mAffiliation;
	}

	public void setAffiliation(String mAffiliation) {
		this.mAffiliation = mAffiliation;
	}
}
