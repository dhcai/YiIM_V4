package com.chyitech.yiim.common;

import android.widget.ExpandableListView.OnChildClickListener;

import com.ikantech.support.adapter.PinnedHeaderExpandableListViewAdapter;

public abstract class SwipeExpandableAdapter extends
		PinnedHeaderExpandableListViewAdapter {
	private OnChildClickListener childClickListener;
	private boolean mVisible;

	public OnChildClickListener getChildClickListener() {
		return childClickListener;
	}

	public void setChildClickListener(OnChildClickListener childClickListener) {
		this.childClickListener = childClickListener;
	}

	public boolean isVisible() {
		return mVisible;
	}

	public void setVisible(boolean visible) {
		this.mVisible = visible;
	}
}
