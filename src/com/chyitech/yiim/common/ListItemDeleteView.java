package com.chyitech.yiim.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.chyitech.yiim.R;

public class ListItemDeleteView extends LinearLayout {

	private Scroller mScroller;// 滑动控制
	private float mLastMotionX;// 记住上次触摸屏的位置
	private int deltaX;
	private int back_width;
	private float downX;
	private int scrollX;
	private View firstView;

	public ListItemDeleteView(Context context) {
		this(context, null);
	}

	public ListItemDeleteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mScroller = new Scroller(context);
		setClickable(true);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {// 会更新Scroller中的当前x,y位置
			if (getScrollX() < 0) {
				return;
			}
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			scrollX = mScroller.getCurrX();
			postInvalidate();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
			if (i == 1 && getChildAt(i).getVisibility() != View.GONE) {
				back_width = getChildAt(i).getMeasuredWidth();
			}

			if (i == 0) {
				firstView = getChildAt(i);
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			downX = x;
			setBackgroundResource(R.drawable.title_bg_pressed);
			break;
		case MotionEvent.ACTION_MOVE:
			deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			int scrollx = getScrollX() + deltaX;
			if (scrollx > 0 && scrollx < back_width) {
				scrollBy(deltaX, 0);
			} else if (scrollx > back_width) {
				scrollTo(back_width, 0);
			} else if (scrollx < 0) {
				scrollTo(0, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			setBackgroundResource(R.drawable.title_white_bg);
			int scroll = getScrollX();
			if (deltaX > 0) {
				if (scroll > back_width / 4) {
					scrollTo(back_width, 0);
				} else {
					scrollTo(0, 0);
				}
			} else {
				if (scroll > back_width * 3 / 4) {
					scrollTo(back_width, 0);
				} else {
					scrollTo(0, 0);
				}
			}

			if (Math.abs(x - downX) < 5) {// 这里根据点击距离来判断是否是itemClick
				performClick();
				return false;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			scrollTo(0, 0);
			setBackgroundResource(R.drawable.title_white_bg);
			break;
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int margeLeft = 0;
		int size = getChildCount();
		for (int i = 0; i < size; i++) {
			View view = getChildAt(i);
			if (view.getVisibility() != View.GONE) {
				int childWidth = view.getMeasuredWidth();
				// 将内部子孩子横排排列
				view.layout(margeLeft, 0, margeLeft + childWidth,
						view.getMeasuredHeight());
				margeLeft += childWidth;
			}
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		// TODO Auto-generated method stub
		if (x < 0) {
			return;
		}
		scrollX = x;
		super.scrollTo(x, y);
	}

	public void beginScroll(int dur) {
		if (getScrollX() > 0) {
			if (getScrollX() < back_width / 2) {
				dur = 0;
			}
			mScroller.startScroll(getScrollX(), 0, getScrollX() - back_width
					* 2, 0, dur);
			invalidate();
		}
	}
}
