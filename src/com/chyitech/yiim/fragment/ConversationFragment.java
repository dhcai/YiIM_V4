package com.chyitech.yiim.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.adapter.ConversationAdapter;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo;
import com.chyitech.yiim.sdk.api.YiXmppRoomInfo.YiXmppRoomInfoListener;
import com.chyitech.yiim.sdk.provider.YiConversationColumns;
import com.chyitech.yiim.ui.ChatActivity;
import com.chyitech.yiim.util.StringUtils;
import com.ikantech.support.widget.YiFragment;

/**
 * 会话列表
 * 
 * @author saint
 * 
 */
public class ConversationFragment extends YiFragment {
	private View mRootView;
	private ListView mListView;
	private ConversationAdapter mAdapter;
	private Cursor mCursor;

	private View mLoadingRootView;
	private View mLoadingView;
	private View mNodataView;

	private NativeReceiver mNativeReceiver;

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mRootView = inflater.inflate(R.layout.frag_tab_conversation, null);

		mListView = (ListView) mRootView
				.findViewById(R.id.tab_conversation_list);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Cursor cursor = (Cursor) mAdapter.getItem(arg2);
				if (cursor != null) {
					// 如果是消息记录类，则跳转至聊天界面
					if (cursor.getInt(cursor
							.getColumnIndex(YiConversationColumns.MSG_TYPE)) == YiXmppConstant.CONVERSATION_TYPE_RECORD) {
						Intent intent = new Intent(getActivity(),
								ChatActivity.class);
						intent.putExtra("to", cursor.getString(cursor
								.getColumnIndex(YiConversationColumns.JID)));
						getActivity().startActivity(intent);
					} else {
						// 如果是邀请类并且已处理，则直接返回
						if (cursor.getInt(cursor
								.getColumnIndex(YiConversationColumns.DEALT)) < 1) {
							return;
						}

						final String uu = cursor.getString(cursor
								.getColumnIndex(YiConversationColumns.JID));
						final String user = StringUtils.escapeUserResource(uu)
								.split(":")[0];
						// 如果是群组申请
						final boolean reg = uu.contains(":register:");
						final String jid = StringUtils.escapeUserResource(cursor.getString(cursor
								.getColumnIndex(YiConversationColumns.SUB_MSG)));

						String msg = null;
						// 如果是群组
						if (StringUtils.isRoomJid(user)) {
							// 如果是群组申请，即有人申请加入某个群组
							if (reg) {
								msg = getString(R.string.str_agree_register_room);
							} else {// 如果是群组邀请，即别人邀请自己加入某个群组
								msg = getString(R.string.str_agree_add_room);
							}
						} else {
							// 如果是好友申请类
							msg = getString(R.string.str_agree_add_friend);
						}

						showMsgDialog(null, msg, getString(R.string.str_agree),
								getString(R.string.str_deagree),
								new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										if (StringUtils.isRoomJid(user)) {
											final YiXmppRoomInfo roomInfo = new YiXmppRoomInfo();
											roomInfo.load(
													getActivity(),
													user,
													false,
													true,
													new YiXmppRoomInfoListener() {
														@Override
														public void onSuccess() {
															// TODO
															// Auto-generated
															// method stub
															if (reg) {// 同意群组申请
																YiIMSDK.getInstance()
																		.agreeRegisterInRoom(
																				user,
																				jid);
															} else {// 加入群组
																roomInfo.setAffiliation("member");
																YiIMSDK.getInstance()
																		.joinRoom(
																				roomInfo,
																				null);
															}
														}

														@Override
														public void onFailed() {
															// TODO
															// Auto-generated
															// method stub
															showMsgDialog(R.string.err_join_room_room_not_exist);
														}
													});
										} else {
											// 同意好友申请
											YiIMSDK.getInstance()
													.agreeAddFriend(user, null,
															null);
										}
										YiIMSDK.getInstance()
												.setConversationDealt(
														0,
														YiXmppConstant.CONVERSATION_TYPE_REQUEST,
														uu);
									}
								}, new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										if (StringUtils.isRoomJid(user)) {
											if (reg) {
												// 拒绝群组申请
												YiIMSDK.getInstance()
														.disAgreeRegisterInRoom(
																user, jid);
											} else {
												// 拒绝群组邀请
												YiIMSDK.getInstance().deInvite(
														user, jid);
											}
										} else {
											// 拒绝好友申请
											YiIMSDK.getInstance()
													.disAgreeAddFriend(user);
										}
										YiIMSDK.getInstance()
												.setConversationDealt(
														0,
														YiXmppConstant.CONVERSATION_TYPE_REQUEST,
														uu);
									}
								});
					}
				}
			}
		});

		mLoadingRootView = mRootView.findViewById(R.id.common_loading);
		mLoadingView = mRootView.findViewById(R.id.common_waiting);
		mNodataView = mRootView.findViewById(R.id.common_nodata);

		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mNativeReceiver = new NativeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(YiXmppConstant.NOTIFICATION_ON_CONVERSATION_UPDATED);
		getActivity().registerReceiver(mNativeReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		getActivity().unregisterReceiver(mNativeReceiver);
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (!hidden) {
			updateList();
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		updateList();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (mAdapter != null) {
			mAdapter.setVisible(false);
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null) {
			mAdapter.setVisible(true);
		}
	}

	protected void updateList() {
		if (mCursor == null || mCursor.getCount() < 1) {
			mCursor = YiIMSDK.getInstance().getConversations();

			mAdapter = new ConversationAdapter(getActivity(), mCursor,
					getHandler());

			mAdapter.setOnDeleteClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String jid = (String) v.getTag(R.id.key_id1);
					int type = (Integer) v.getTag(R.id.key_id2);
					YiIMSDK.getInstance().removeConversation(jid, type);
				}
			});
			mListView.setAdapter(mAdapter);
		} else {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}

		updateLoading();
	}

	private void updateLoading() {
		if (mCursor.getCount() > 0) {
			mLoadingRootView.setVisibility(View.GONE);
		} else {
			mLoadingRootView.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		}
	}

	private class NativeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					YiXmppConstant.NOTIFICATION_ON_CONVERSATION_UPDATED)
					&& (mAdapter == null || mAdapter.getCursor() == null || mAdapter
							.getCursor().getCount() < 1)) {
				getHandler().post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateList();
					}
				});
			}
		}
	}
}
