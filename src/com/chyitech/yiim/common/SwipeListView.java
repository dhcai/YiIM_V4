package com.chyitech.yiim.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ikantech.support.util.YiLog;

public class SwipeListView extends ListView {

	private float minDis = 10;
	private float mLastMotionX;// 记住上次X触摸屏的位置
	private float mLastMotionY;// 记住上次Y触摸屏的位置
	private boolean isLock = false;

	private OnItemClickListener mItemClickListener;
	private ListAdapter mAdapter;

	private float mLastTouchY;
	private boolean isMoving = false;

	public SwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 如果一个ViewGroup的onInterceptTouchEvent()方法返回true，说明Touch事件被截获，
	 * 子View不再接收到Touch事件，而是转向本ViewGroup的
	 * onTouchEvent()方法处理。从Down开始，之后的Move，Up都会直接在onTouchEvent()方法中处理。
	 * 先前还在处理touch event的child view将会接收到一个 ACTION_CANCEL。
	 * 如果onInterceptTouchEvent()返回false，则事件会交给child view处理。
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!isIntercept(ev)) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		float y = ev.getY();
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE) {
			float deltay = Math.abs(mLastTouchY - y);
			if (deltay > minDis && !isMoving) {
				YiLog.getInstance().i("deltay2 %f", deltay);
				isMoving = true;
				int start = getFirstVisiblePosition();
				int end = getLastVisiblePosition();
				for (int i = start; i <= end; i++) {
					View child = getChildAt(i);
					if (child != null && child instanceof ListItemDeleteView) {
						ListItemDeleteView vv = (ListItemDeleteView) child;
						vv.beginScroll(300);
					}
				}
			}
		} else if (action == MotionEvent.ACTION_DOWN) {
			mLastTouchY = y;
		} else if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			isMoving = false;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 检测是ListView滑动还是item滑动 isLock 一旦判读是item滑动，则在up之前都是返回false
	 */
	private boolean isIntercept(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.e("test", "isIntercept  ACTION_DOWN  " + isLock);
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.e("test", "isIntercept  ACTION_MOVE  " + isLock);
			if (!isLock) {
				float deltaX = Math.abs(mLastMotionX - x);
				float deltay = Math.abs(mLastMotionY - y);
				mLastMotionX = x;
				mLastMotionY = y;
				if (deltaX > deltay && deltaX > minDis) {
					isLock = true;
					return false;
				}
			} else {
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.e("test", "isIntercept  ACTION_UP  " + isLock);
			isLock = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.e("test", "isIntercept  ACTION_CANCEL  " + isLock);
			isLock = false;
			break;
		}
		return true;
	}

	@Override
	public void setOnItemClickListener(
			android.widget.AdapterView.OnItemClickListener listener) {
		// TODO Auto-generated method stub
		mItemClickListener = listener;
		if (mAdapter != null && mAdapter instanceof SwipeListViewAdapter) {
			SwipeListViewAdapter aa = (SwipeListViewAdapter) mAdapter;
			aa.setItemClickListener(mItemClickListener);
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		super.setAdapter(adapter);
		mAdapter = adapter;
		if (mAdapter instanceof SwipeListViewAdapter
				&& mItemClickListener != null) {
			SwipeListViewAdapter aa = (SwipeListViewAdapter) mAdapter;
			aa.setItemClickListener(mItemClickListener);
		}
	}
}
