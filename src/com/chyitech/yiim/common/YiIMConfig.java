package com.chyitech.yiim.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ikantech.support.util.YiPrefsKeeper;
import com.ikantech.support.util.YiPrefsKeeper.YiPrefsKeepable;

public class YiIMConfig implements YiPrefsKeepable {
	private static YiIMConfig mConfig = null;

	private boolean mIsExited;

	public static YiIMConfig getInstance(Context context) {
		if (mConfig == null) {
			mConfig = new YiIMConfig(context);
		}
		return mConfig;
	}

	private YiIMConfig(Context context) {
		YiPrefsKeeper.read(context, this);
	}

	@Override
	public void save(Editor editor) {
		// TODO Auto-generated method stub
		editor.putBoolean("is_exited", mIsExited);
	}

	@Override
	public void restore(SharedPreferences preferences) {
		// TODO Auto-generated method stub
		mIsExited = preferences.getBoolean("is_exited", true);
	}

	@Override
	public String getPrefsName() {
		// TODO Auto-generated method stub
		return "yiim_config";
	}

	public boolean isExited() {
		return mIsExited;
	}

	public void setExited(boolean isExited) {
		this.mIsExited = isExited;
	}
}
