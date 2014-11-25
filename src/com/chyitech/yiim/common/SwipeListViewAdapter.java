package com.chyitech.yiim.common;

import android.widget.AdapterView.OnItemClickListener;

public interface SwipeListViewAdapter {
	OnItemClickListener getItemClickListener();

	void setItemClickListener(OnItemClickListener listener);
	
	void setVisible(boolean v);
}
