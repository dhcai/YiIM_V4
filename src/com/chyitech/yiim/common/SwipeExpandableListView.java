package com.chyitech.yiim.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.ikantech.support.util.YiLog;

public class SwipeExpandableListView extends ExpandableListView {
	private float minDis = 10;
	private float mLastMotionX;// 记住上次X触摸屏的位置
	private float mLastMotionY;// 记住上次Y触摸屏的位置
	private boolean isLock = false;
	private OnChildClickListener mChildClickListener;
	private SwipeExpandableAdapter mAdapter;
	private float mLastTouchY;
	private boolean isMoving = false;

	public SwipeExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

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
						// ViewPropertyAnimator.animate(child).translationX(0)
						// .setDuration(100);
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
			float deltaX = Math.abs(mLastMotionX - x);
			float deltay = Math.abs(mLastMotionY - y);
			if (!isLock) {
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
	public void setOnChildClickListener(
			OnChildClickListener onChildClickListener) {
		// TODO Auto-generated method stub
		mChildClickListener = onChildClickListener;
		if (mAdapter != null) {
			mAdapter.setChildClickListener(onChildClickListener);
		}
	}

	public void setAdapter(ExpandableListAdapter adapter) {
		// TODO Auto-generated method stub
		super.setAdapter(adapter);
		if (adapter instanceof SwipeExpandableAdapter) {
			mAdapter = (SwipeExpandableAdapter) adapter;
			mAdapter.setChildClickListener(mChildClickListener);
		}
	}
}
