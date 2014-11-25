package com.chyitech.yiim.entity;

/**
 * 分组数据模型
 * @author saint
 *
 */
public class RosterGroup {
	/**
	 * 分组名称
	 */
	private String mName;
	/**
	 * 分组成员总数
	 */
	private int mEntryCount;
	/**
	 * 在线或已出席的成员数
	 */
	private int mOnlineCount;

	public RosterGroup() {
		mName = "";
		mEntryCount = 0;
		mOnlineCount = 0;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getEntryCount() {
		return mEntryCount;
	}

	public void setEntryCount(int entryCount) {
		this.mEntryCount = entryCount;
	}

	public int getOnlineCount() {
		return mOnlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.mOnlineCount = onlineCount;
	}

	public void addOnlineCount() {
		mOnlineCount++;
	}
	
	public void addEntryCount() {
		mEntryCount++;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mName;
	}

}
