package com.chyitech.yiim.ui;

import android.content.Intent;
import android.database.Cursor;
import android.view.View;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ChatMsgViewAdapter;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.ui.base.BaseChatActivity;
import com.chyitech.yiim.ui.contact.UserInfoActivity;
import com.chyitech.yiim.ui.room.RoomInfoActivity;
import com.chyitech.yiim.util.StringUtils;

/**
 * 聊天界面
 * @author saint
 *
 */
public class ChatActivity extends BaseChatActivity {

	@Override
	protected void sendYiIMMessage(String msg, long delay) {
		// TODO Auto-generated method stub
		if (!checkRoomJoined()) {
			showMsgDialog(R.string.str_room_not_joined_sendmsg_tip);
			return;
		}
		YiIMSDK.getInstance().sendMessage(msg, mUserTo, delay);
	}

	@Override
	protected void initChat() {
		if (StringUtils.isRoomJid(mUserTo)) {
			// 设置聊天窗口标题
			try {
				final YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
				roomInfo.load(this, mUserTo, false, true,
						new YiXmppRoomInfoListener() {

							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								getHandler().post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										setTitle(roomInfo.getName());
									}
								});
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub

							}
						});
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else {
			// 设置聊天窗口标题
			try {
				final YiXmppVCard vcard = new YiXmppVCard();
				vcard.load(this, mUserTo, false, true,
						new YiXmppVCardListener() {

							@Override
							public void onSuccess() {
								// TODO Auto-generated method stub
								getHandler().post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										setTitle(vcard.displayName());
									}
								});
							}

							@Override
							public void onFailed() {
								// TODO Auto-generated method stub

							}
						});
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@Override
	protected void initAdapter(Object obj) {
		// TODO Auto-generated method stub
		mAdapter = new ChatMsgViewAdapter(ChatActivity.this, mGifEmotionUtils,
				(Cursor) obj, mUser.getJid(), mEmotionManager);
	}

	@Override
	public void onTitleBarRightImgBtnClick(View view) {
		//如果是群组，则跳转至群组资料页面
		if (StringUtils.isRoomJid(mUserTo)) {
			Intent intent = new Intent(ChatActivity.this,
					RoomInfoActivity.class);
			intent.putExtra("jid", mUserTo);
			intent.putExtra("which", ChatActivity.class.getSimpleName());
			startActivityForResult(intent, USER_INFO);
		} else {
			Intent intent = new Intent(ChatActivity.this,
					UserInfoActivity.class);
			intent.putExtra("jid", mUserTo);
			intent.putExtra("which", ChatActivity.class.getSimpleName());
			startActivityForResult(intent, USER_INFO);
		}
	}

	@Override
	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub

	}
}