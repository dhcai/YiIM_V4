package com.chyitech.yiim.ui.base;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ChatMsgViewAdapter;
import com.chyitech.yiim.common.EmotionManager;
import com.chyitech.yiim.common.ViewImageDialog;
import com.chyitech.yiim.common.VoiceRecordDialog;
import com.chyitech.yiim.common.VoiceRecordDialog.OnErrorListener;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.entity.YiMessage;
import com.chyitech.yiim.entity.YiMessage.MsgType;
import com.chyitech.yiim.gif.GifEmotionUtils;
import com.chyitech.yiim.media.AudioPlayer;
import com.chyitech.yiim.media.AudioRecorder;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.ui.ChatActivity;
import com.chyitech.yiim.ui.contact.UserInfoActivity;
import com.chyitech.yiim.util.FileUtils;
import com.chyitech.yiim.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ikantech.support.util.YiImageUtil;
import com.ikantech.support.util.YiUtils;

/**
 * 聊天界面基础类
 * 
 * @author saint
 * 
 */
public abstract class BaseChatActivity extends CustomTitleActivity implements
		OnErrorListener {
	protected static final int MSG_INIT = 0x01;
	protected static final int MSG_RECORD_READED = 0x03;
	public static final int MSG_SEND_EMOTION = 0x04;
	public static final int MSG_SEND_CLASSICAL_EMOTION = 0x05;
	public static final int MSG_SEND_AUDIO = 0x06;

	// 显示最近20条记录
	private int mLimit = 20;

	private EditText mEditTextContent;
	private PullToRefreshListView mListView;
	// 底部工具栏和表情
	private View mFooterView;

	// 工具栏
	private View mToolsView;

	// 底部pager
	private ViewPager mFooterPager;
	private View mPagerIndexPanel;
	// 表情管理
	protected EmotionManager mEmotionManager;

	protected ChatMsgViewAdapter mAdapter;

	private Button mVoiceButton;
	private ImageButton mSendBtn;
	private InputMethodManager mInputMethodManager;
	// 录音提示框
	private VoiceRecordDialog mVoiceRecordDialog;

	// 查看大图对话框
	private ViewImageDialog mImageDialog;
	// 录音类
	private AudioRecorder mAudioRecorder;

	protected static final int CHOICE_PHOTO = 0; // 用于标识从用户相册选择照片
	protected static final int TAKE_PHOTO = 1; // 用于标识拍照
	protected static final int CHOICE_VIDEO = 2;// 选择视频文件
	protected static final int USER_INFO = 3;

	// 用于播放语音短消息
	private AudioPlayer mAudioPlayer;

	// 接收者
	protected String mUserTo;

	// 当前账号类
	protected YiUserInfo mUser;

	private boolean mTypeShow = false;

	private Uri mImageUri;

	// Gif表情辅助工具
	protected GifEmotionUtils mGifEmotionUtils;
	// 消息广播接收器
	private MsgReceivedBroadcast mMsgReceivedBroadcast;

	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activty_chat);
		super.onCreate(savedInstanceState);
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		getHandler().sendEmptyMessage(MSG_INIT);
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_INIT:
			init();
			break;
		case MSG_RECORD_READED:// 当聊天记录读取完成
			if (mAdapter != null && mAdapter.getCursor() != null) {
				mListView.setAdapter((ListAdapter) null);
				mAdapter.getCursor().close();
				mAdapter = null;
			}
			initAdapter(msg.obj);
			mAdapter.setOnAudioClickListener(new NativeAudioClickListener());
			mAdapter.setOnImageClickListener(new ImageClickListener());
			mAdapter.setResendClickListener(new ResendClickListener());
			mAdapter.setIsMultiChat(StringUtils.isRoomJid(mUserTo));
			mListView.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();
			break;
		case MSG_SEND_EMOTION:// 发送大表情
			try {
				YiMessage message = new YiMessage();
				message.setType(MsgType.BIG_EMOTION);
				message.setBody((String) msg.obj);
				sendYiIMMessage(message.toString());
			} catch (Exception e) {
				// TODO: handle exception
			}
			_onTypeSelectBtnClick(false);
			if (mEmotionManager.isShow()) {
				mEmotionManager.destory();
				mFooterView.setVisibility(View.GONE);
			}
			break;
		case MSG_SEND_AUDIO:// 发送语音短消息
			sendAudioMsg();
			break;
		case MSG_SEND_CLASSICAL_EMOTION:// 点击了经典表情
			if ("[DEL]".equals((String) msg.obj)) {// 如果点击的是删除键
				int keyCode = KeyEvent.KEYCODE_DEL;
				KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN,
						keyCode);
				KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
				mEditTextContent.onKeyDown(keyCode, keyEventDown);
				mEditTextContent.onKeyUp(keyCode, keyEventUp);
			} else {// 否则将表情插入当前光标所有位置
				int index = mEditTextContent.getSelectionStart();
				Editable editable = mEditTextContent.getEditableText();
				if (index < 0 || index >= editable.length()) {
					editable.append((String) msg.obj);
				} else {
					editable.insert(index, (String) msg.obj);
				}
				mGifEmotionUtils.setSpannableText(mEditTextContent,
						mEditTextContent.getText().toString(), getHandler());
			}
			break;
		default:
			break;
		}
	}

	protected abstract void initAdapter(Object obj);

	protected void clearConversationDealFlag() {
		YiIMSDK.getInstance().clearUnReadMsgFor(mUserTo);
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		mListView = (PullToRefreshListView) findViewById(R.id.chat_listview);
		mEditTextContent = (EditText) findViewById(R.id.chat_msg_edit);
		mFooterView = findViewById(R.id.chat_footer);
		mFooterPager = (ViewPager) findViewById(R.id.chat_viewpager);
		mToolsView = findViewById(R.id.chat_tools);
		mPagerIndexPanel = findViewById(R.id.chat_pager_index);
		mVoiceButton = (Button) findViewById(R.id.chat_btn_voice);
		mSendBtn = (ImageButton) findViewById(R.id.chat_btn_send);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mAudioRecorder = new AudioRecorder();

		mUserTo = getIntent().getStringExtra("to");
		// 设置标题
		setTitle(mUserTo.replaceAll("@.+$", ""));
		setTitleBarRightImageBtnSrc(R.drawable.btn_title_info_selector);

		mEmotionManager = new EmotionManager(this, mFooterPager,
				mPagerIndexPanel, findViewById(R.id.chat_pager_emoji_toolbar),
				getHandler());

		mGifEmotionUtils = new GifEmotionUtils(this,
				EmotionManager.getClassicalEmotions(),
				EmotionManager.getClassicalEmotionDescs(), R.drawable.face);

		mUser = YiUserInfo.getUserInfo(this);
		mListView.getRefreshableView().setStackFromBottom(true);
		mListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				mLimit += 10;
				loadRecrod();
			}
		});

		mVoiceButton.setOnTouchListener(new OnTouchListener() {
			private float lastX;
			private float lastY;
			private Rect rect;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!checkRoomJoined()) {
						showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
						return false;
					}

					rect = new Rect();
					mVoiceButton.getLocalVisibleRect(rect);
					lastX = event.getX();
					lastY = event.getY();
					mVoiceButton.setText(getString(R.string.str_voice_up));
					mVoiceButton.setBackgroundDrawable(getResources()
							.getDrawable(
									R.drawable.chatting_send_btn_bg_pressed));
					// 弹出voice dialog
					if (mVoiceRecordDialog == null) {
						mVoiceRecordDialog = new VoiceRecordDialog(
								BaseChatActivity.this,
								R.style.custom_dialog_transparent,
								mAudioRecorder);
						mVoiceRecordDialog
								.setOnErrorListener(BaseChatActivity.this);
					}
					mVoiceRecordDialog.show();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mVoiceButton.setText(getString(R.string.str_voice_press));
					mVoiceButton.setBackgroundDrawable(getResources()
							.getDrawable(R.drawable.chatting_send_btn_bg));

					if (mVoiceRecordDialog != null
							&& mVoiceRecordDialog.isShowing()) {
						mVoiceRecordDialog.requestDismiss();
					}
					// 如果松开后，仍在按钮区域
					if (rect != null && rect.contains((int) lastX, (int) lastY)
							&& !mVoiceRecordDialog.isTooShort()) {
						getHandler().sendEmptyMessageDelayed(MSG_SEND_AUDIO,
								300);
					}

					rect = null;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					lastX = event.getX();
					lastY = event.getY();

					if (rect != null) {
						if (rect.contains((int) lastX, (int) lastY)) {
							if (mVoiceRecordDialog != null) {
								mVoiceRecordDialog.showRecordingView();
							}
						} else {
							if (mVoiceRecordDialog != null) {
								mVoiceRecordDialog.showCancelRecordView();
							}
						}
					}
				}
				return false;
			}
		});

		mEditTextContent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				_onTypeSelectBtnClick(false);
				if (mEmotionManager.isShow()) {
					mEmotionManager.destory();
					mFooterView.setVisibility(View.GONE);
				}
				return false;
			}
		});

		mEditTextContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				// 当输入的文字大于1时，隐藏录音按钮
				if (mEditTextContent.getText().length() >= 1) {
					mSendBtn.setImageDrawable(getResources().getDrawable(
							R.drawable.chatting_setmode_send_btn_normal));
				} else {
					mSendBtn.setImageDrawable(getResources().getDrawable(
							R.drawable.chatting_setmode_voice_btn_normal));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		mMsgReceivedBroadcast = new MsgReceivedBroadcast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(YiXmppConstant.NOTIFICATION_ON_MESSAGE_RECEIVED);
		registerReceiver(mMsgReceivedBroadcast, intentFilter);
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub
		unregisterReceiver(mMsgReceivedBroadcast);
	}

	// 按下+号
	public void onTypeSelectBtnClick(View view) {
		_onTypeSelectBtnClick(true);
	}

	private void _onTypeSelectBtnClick(boolean force) {
		mFooterPager.setVisibility(View.GONE);
		mToolsView.setVisibility(View.VISIBLE);
		if (!mTypeShow && force) {
			if (mEmotionManager.isShow()) {
				mEmotionManager.destory();
			}

			mInputMethodManager.hideSoftInputFromWindow(
					mFooterView.getWindowToken(), 0);
			mFooterView.setVisibility(View.VISIBLE);
			mTypeShow = true;

			if (mVoiceButton.getVisibility() == View.VISIBLE) {
				onVoiceChooseBtnClick(mSendBtn);
			}
		} else {
			mFooterView.setVisibility(View.GONE);
			mTypeShow = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		// 按下返回键
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mFooterView.getVisibility() == View.VISIBLE) {
				_onTypeSelectBtnClick(false);
				if (mEmotionManager.isShow()) {
					mEmotionManager.destory();
					mFooterView.setVisibility(View.GONE);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// 发送语音短消息
	private void sendAudioMsg() {
		if (!checkRoomJoined()) {
			showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
			return;
		}
		YiIMSDK.getInstance().getBackgroundService().execute(new Runnable() {
			@Override
			public void run() {
				try {
					String msg = mAudioRecorder.getRecordedResource();
					MediaPlayer player = new MediaPlayer();
					player.setDataSource(mAudioRecorder.getAudioFilePath());
					player.prepare();

					YiMessage message = new YiMessage();
					message.setType(MsgType.AUDIO);
					message.setBody(msg);
					// 获取语音短消息的时长
					message.addParam("audio_duration",
							String.valueOf(player.getDuration()));

					sendYiIMMessage(message.toString(),
							player.getDuration() + 5000);

					player.release();
					player = null;
				} catch (Exception e) {
				}
			}
		});
	}

	// 默认超时5秒发送消息，5秒后没发到服务器，则提示发送失败
	protected void sendYiIMMessage(String msg) {
		sendYiIMMessage(msg, 5000);
	}

	protected abstract void sendYiIMMessage(String msg, long delay);

	// 表情按钮按下时
	public void onEmoBtnClick(View view) {
		if (mEmotionManager.isShow()) {
			mEmotionManager.destory();
			mFooterView.setVisibility(View.GONE);
		} else {
			mToolsView.setVisibility(View.GONE);
			mFooterPager.setVisibility(View.VISIBLE);
			mFooterView.setVisibility(View.VISIBLE);
			mEmotionManager.initialize();
			mTypeShow = false;

			mInputMethodManager.hideSoftInputFromWindow(
					mFooterView.getWindowToken(), 0);

			if (mVoiceButton.getVisibility() == View.VISIBLE) {
				onVoiceChooseBtnClick(mSendBtn);
			}
		}
	}

	// 语音按钮按下时
	public void onVoiceChooseBtnClick(View view) {
		ImageButton imageView = (ImageButton) view;
		if (mVoiceButton.getVisibility() == View.GONE) {
			mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
					0);
			mEditTextContent.setVisibility(View.GONE);
			mVoiceButton.setVisibility(View.VISIBLE);
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.chatting_setmode_keyboard_btn_normal));
		} else {
			mVoiceButton.setVisibility(View.GONE);
			mEditTextContent.setVisibility(View.VISIBLE);
			imageView.setImageDrawable(getResources().getDrawable(
					R.drawable.chatting_setmode_voice_btn_normal));
		}
	}

	protected abstract void initChat();

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initChat();
	}

	private void init() {
		loadRecrod();
		initChat();
	}

	// 加载聊天记录
	protected void loadRecrod() {
		Cursor cursor = YiIMSDK.getInstance().msgRecordWithLimit(mLimit,
				mUserTo);
		if (cursor != null) {
			Message message = getHandler().obtainMessage(MSG_RECORD_READED,
					cursor);
			message.sendToTarget();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 将当前JID设置为活动JID，这样在当前界面下，如果对方发来消息，就不会有提示音了。
		YiIMSDK.getInstance().setActiveJid(mUserTo);
		clearConversationDealFlag();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		// 将活动JID设置为空
		YiIMSDK.getInstance().setActiveJid(null);
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CHOICE_PHOTO:
			if (data != null) {
				// 获取路径
				try {
					Uri originalUri = data.getData();
					String path = FileUtils.getPath(this, originalUri);
					// 获取图片的宽高
					int[] bounds = YiImageUtil
							.getImageWidhtHeightFromFilePath(path);
					YiIMSDK.getInstance().sendFile(mUserTo, originalUri,
							bounds[0], bounds[1]);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			break;
		case TAKE_PHOTO:
			String path = FileUtils.getPath(this, mImageUri);
			if (resultCode == Activity.RESULT_OK) {
				try {
					// 获取图片的宽高
					int[] bounds = YiImageUtil
							.getImageWidhtHeightFromFilePath(path);
					YiIMSDK.getInstance().sendFile(mUserTo, mImageUri,
							bounds[0], bounds[1]);
				} catch (Exception e) {
				}
			} else {// 删除可能拍好的照片
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
			break;
		case USER_INFO:
			if (resultCode == Activity.RESULT_OK) {
				finish();
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mImageDialog != null && mImageDialog.isShowing()) {
			mImageDialog.dismiss();
		}

		try {

			if (mAdapter != null && mAdapter.getCursor() != null) {
				mListView.setAdapter((ListAdapter) null);
				mAdapter.getCursor().close();
				mAdapter = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		mGifEmotionUtils.destory();
		super.onDestroy();
	}

	// 检查是否已经出席了群组
	protected boolean checkRoomJoined() {
		if (StringUtils.isRoomJid(mUserTo)) {
			return YiIMSDK.getInstance().roomJoinedByJid(mUserTo);
		}
		return true;
	}

	// 当发送按钮按下时
	public void onSendBtnClick(View view) {
		String contString = mEditTextContent.getText().toString();
		if (contString.length() > 0) {
			if (!checkRoomJoined()) {
				showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
				return;
			}
			sendMessage(contString);
			mEditTextContent.setText("");
			mListView.getRefreshableView()
					.setSelection(mAdapter.getCount() - 1);
		} else {
			onVoiceChooseBtnClick(view);
			if (mFooterView.getVisibility() == View.VISIBLE) {
				_onTypeSelectBtnClick(false);
				if (mEmotionManager.isShow()) {
					mEmotionManager.destory();
					mFooterView.setVisibility(View.GONE);
				}
			}
		}
	}

	public void sendMessage(String msg) {
		try {
			YiMessage message = new YiMessage();
			message.setBody(msg);
			sendYiIMMessage(message.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 当重新发送按钮按下时
	private class ResendClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final String msgId = (String) v.getTag();
			if (!checkRoomJoined()) {
				showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
				return;
			}
			showMsgDialog(null, getString(R.string.str_re_send),
					getString(R.string.str_ok), getString(R.string.str_cancel),
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							YiIMSDK.getInstance().resend(msgId, mUserTo);
						}
					}, null);
		}
	}

	// 当点击了某个图片时
	private class ImageClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			YiMessage message = (YiMessage) v.getTag();

			if (message != null && message.getBody() != null
					&& message.getType().equals(MsgType.IMAGE)) {

				if (mImageDialog == null) {
					mImageDialog = new ViewImageDialog(BaseChatActivity.this,
							R.style.ImageViewDialog);
				}
				mImageDialog.setBitmapPath(message.getBody(), message
						.getParams().get("small_url"));
				mImageDialog.show();
			}
		}
	}

	public void onChoicePhotoBtnClick(View view) {
		if (!checkRoomJoined()) {
			showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
			return;
		}
		FileUtils.doChoicePhoto(this, CHOICE_PHOTO);
	}

	public void onTakePhotoBtnClick(View view) {
		if (!checkRoomJoined()) {
			showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
			return;
		}
		mImageUri = FileUtils.generateImageUri();
		FileUtils.doTakePhoto(this, mImageUri, TAKE_PHOTO);
	}

	// 某个用户的头像被点击时
	public void onAvatarClick(View v) {
		String jid = (String) v.getTag();
		if (!YiUtils.isStringInvalid(jid)) {
			Intent intent = new Intent(BaseChatActivity.this,
					UserInfoActivity.class);
			intent.putExtra("jid", jid);
			intent.putExtra("which", ChatActivity.class.getSimpleName());
			startActivityForResult(intent, USER_INFO);
		}
	}

	private class NativeAudioClickListener implements View.OnClickListener {
		@Override
		public void onClick(final View v) {
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
	}

	@Override
	public void onError(String msg) {
		// TODO Auto-generated method stub
		mVoiceRecordDialog.requestDismiss();
		showMsgDialog(msg);
	}

	private class MsgReceivedBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					YiXmppConstant.NOTIFICATION_ON_MESSAGE_RECEIVED)) {
				getHandler().post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mAdapter == null || mAdapter.getCursor() == null) {
							loadRecrod();
						}
					}
				});
			}
		}
	}
}
