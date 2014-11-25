package com.chyitech.yiim.ui.base;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ChatRecordAdapter;
import com.chyitech.yiim.common.ViewImageDialog;
import com.chyitech.yiim.entity.YiMessage;
import com.chyitech.yiim.entity.YiMessage.MsgType;
import com.chyitech.yiim.media.AudioPlayer;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppResult;

/**
 * 聊天记录界面
 * @author saint
 *
 */
public class MsgRecordActivity extends CustomTitleActivity {
	private static final int PAGE_SIZE = 15;

	private ListView mListView;
	private EditText mEditText;
	private TextView mTextView;

	private String mJid;

	private int mMaxPages = 0;
	private int mCurrentPage = 0;

	private Cursor mCursor;
	private ChatRecordAdapter mCursorAdapter;

	private ViewImageDialog mImageDialog;

	private AudioPlayer mAudioPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_view_chat_record);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		if (mImageDialog != null && mImageDialog.isShowing()) {
			mImageDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mListView = (ListView) findViewById(R.id.view_chat_record_list);
		mEditText = (EditText) findViewById(R.id.view_chat_record_edit);
		mTextView = (TextView) findViewById(R.id.view_chat_record_text);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mJid = getIntent().getStringExtra("jid");

		mMaxPages = (int) Math.ceil(YiIMSDK.getInstance().totalMessageOfJid(
				mJid)
				* 1.0F / PAGE_SIZE);
		mCurrentPage = mMaxPages;

		mTextView.setText("/" + mMaxPages);
		mEditText.setText("" + mCurrentPage);
		loadPage();
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO) {
					try {
						Integer page = Integer.valueOf(mEditText.getText()
								.toString());
						if (page > 0 && page <= mMaxPages) {
							mCurrentPage = page;
							loadPage();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				return false;
			}
		});
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	public void onPrevClick(View v) {
		int old = mCurrentPage;
		mCurrentPage--;
		if (mCurrentPage < 1) {
			mCurrentPage = 1;
		}
		if (old != mCurrentPage) {
			loadPage();
		}
	}

	public void onNextClick(View v) {
		int old = mCurrentPage;
		mCurrentPage++;
		if (mCurrentPage > mMaxPages) {
			mCurrentPage = mMaxPages;
		}
		if (old != mCurrentPage) {
			loadPage();
		}
	}

	public void loadPage() {
		if (mMaxPages < 1)
			return;
		if (mCurrentPage < 1 || mCurrentPage > mMaxPages)
			return;

		mCursor = YiIMSDK.getInstance().msgRecordWithLimit(PAGE_SIZE,
				mCurrentPage - 1, mJid);

		if (mCursorAdapter == null) {
			mCursorAdapter = new ChatRecordAdapter(this, mCursor);
			mCursorAdapter
					.setOnAudioClickListener(new NativeAudioClickListener());
			mCursorAdapter.setOnImageClickListener(new ImageClickListener());
			mListView.setAdapter(mCursorAdapter);
		} else {
			mCursorAdapter.changeCursor(mCursor);
		}
		mCursorAdapter.notifyDataSetChanged();

		mEditText.setText("" + mCurrentPage);
	}

	public void onClearChatRecordClick(View v) {
		showMsgDialog(null, getString(R.string.str_clear_chat_record_confirm),
				getString(R.string.str_ok), getString(R.string.str_cancel),
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							YiIMSDK.getInstance().removeMessage(mJid);
							YiIMSDK.getInstance().removeConversation(mJid,
									YiXmppConstant.CONVERSATION_TYPE_RECORD);
							mCurrentPage = 0;
							mMaxPages = 0;
							mEditText.setText("0");
							mTextView.setText("/0");
							mCursorAdapter.changeCursor(null);
							mCursorAdapter.notifyDataSetChanged();
						} catch (Exception e) {
						}
					}
				}, null);
	}

	private class ImageClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			YiMessage message = (YiMessage) v.getTag();
			if (message != null && message.getBody() != null
					&& message.getType().equals(MsgType.IMAGE)) {

				if (mImageDialog == null) {
					mImageDialog = new ViewImageDialog(MsgRecordActivity.this,
							R.style.ImageViewDialog);
				}
				mImageDialog.setBitmapPath(message.getBody(), message
						.getParams().get("small_url"));
				mImageDialog.show();
			}
		}
	}

	private class NativeAudioClickListener implements View.OnClickListener {
		@Override
		public void onClick(final View v) {
			// TODO Auto-generated method stub
			YiIMSDK.getInstance().getBackgroundService()
					.execute(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (mAudioPlayer == null) {
								mAudioPlayer = new AudioPlayer();
							}
							if (mAudioPlayer.getMediaPlayer() != null) {
								mAudioPlayer.stopPlaying();
							}
							try {
								mAudioPlayer.startPlaying((String) v.getTag());
							} catch (Exception e) {
							}
						}
					});
		}
	}
}
