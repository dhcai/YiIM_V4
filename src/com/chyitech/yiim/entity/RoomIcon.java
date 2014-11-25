package com.chyitech.yiim.entity;

import com.chyitech.yiim.R;

public enum RoomIcon {
	// 群组Icon，如果需要可扩展此头像
	DEFAULIT_1("default1", R.drawable.troop_default_head_1), DEFAULIT_2(
			"default2", R.drawable.troop_default_head_2), DEFAULIT_3(
			"default3", R.drawable.troop_default_head_3), DEFAULIT_4(
			"default4", R.drawable.troop_default_head_4);
	// Icon在本地的资源ID
	int mId;
	// Icon字符串描述
	String mDesc;

	private RoomIcon(String desc, int id) {
		// TODO Auto-generated constructor stub
		mId = id;
		mDesc = desc;
	}

	public int getResId() {
		return mId;
	}

	public String toString() {
		return mDesc;
	}

	//将Icon字符串转换成RoomIcon
	public static RoomIcon eval(String str) {
		RoomIcon[] types = RoomIcon.values();
		for (RoomIcon msgType : types) {
			if (msgType.toString().equals(str)) {
				return msgType;
			}
		}
		return RoomIcon.DEFAULIT_4;
	}
}
