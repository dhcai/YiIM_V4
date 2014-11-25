package com.chyitech.yiim.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.provider.YiGroupColumns;
import com.ikantech.support.util.YiUtils;

public abstract class BaseGroupManagerActivity extends CustomTitleActivity {
	private static final int DIALOG_MODE_ADD_GROUP = 0x01;
	private static final int DIALOG_MODE_RENAME_GROUP = 0x02;

	private ListView mListView;
	private NativeAdapter mAdapter;

	private EditText mGroupEditText;
	private Dialog mGroupDialog;

	// 用于区分是分组管理，还是变更分组
	private String mMode = null;
	private String mJid = null;
	private String mGroupName = null;

	private String mCurrentSelectGroupName = null;

	private int mDialogMode = -1;

	private ArrayList<String> mDatas;
	private int mSelectedIndex = -1;

	private DeleteGroupListener mDeleteGroupListener;
	private RenameGroupListener mRenameGroupListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_group_manager);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		if (mGroupDialog != null && mGroupDialog.isShowing()) {
			mGroupDialog.dismiss();
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
		mListView = (ListView) findViewById(R.id.group_manager_list);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		mMode = getIntent().getStringExtra("mode");
		mGroupName = getIntent().getStringExtra("groupName");
		if (isStringInvalid(mGroupName)) {
			mGroupName = getUnfiledGroupName();
		}
		mJid = getIntent().getStringExtra("jid");
		mCurrentSelectGroupName = mGroupName;

		mDeleteGroupListener = new DeleteGroupListener();
		mRenameGroupListener = new RenameGroupListener();

		if (mMode != null && "modify".equals(mMode)) {
			setTitle(getString(R.string.str_modify_group_title));
			setTitleBarRightBtnText(getString(R.string.str_finish));
		} else {
			setTitleBarRightBtnText(getString(R.string.str_add));
		}

		mDatas = getGroup();
		mAdapter = new NativeAdapter(this, R.layout.group_manager_item,
				R.id.text, mDatas);

		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// 变更分组
				if (mMode != null && "modify".equals(mMode)) {
					String item = ((String) mAdapter.getItem(arg2));
					mCurrentSelectGroupName = item;
					mSelectedIndex = arg2;
					mAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	protected ArrayList<String> getGroup() {
		ArrayList<String> ret = new ArrayList<String>();
		Cursor cursor = null;
		try {
			ret.add(getUnfiledGroupName());

			cursor = getGroupCursor();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					ret.add(cursor.getString(cursor
							.getColumnIndex(YiGroupColumns.NAME)));
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return ret;
	}

	protected abstract Cursor getGroupCursor();

	protected abstract void removeGroup(String group);

	protected abstract void addGroup(String group);

	protected abstract void renameGroup(String oldName, String newName);

	protected abstract void moveToGroup(String jid, String group);

	protected abstract String getUnfiledGroupName();

	protected abstract String getDeleteGroupString();

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	private void showGroupDialog() {
		if (mGroupEditText == null) {
			mGroupEditText = new EditText(this);
			mGroupEditText.setHint(R.string.str_group_name_hint);
		}

		if (mGroupDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(mGroupEditText);
			builder.setPositiveButton(getString(R.string.str_ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if (YiUtils.isStringInvalid(mGroupEditText
									.getText().toString())) {
								showMsgDialog(R.string.err_empty_group_name);
								return;
							}

							if (mDialogMode == DIALOG_MODE_ADD_GROUP) {
								addGroup(mGroupEditText.getText().toString()
										.trim());
								mDatas.add(mGroupEditText.getText().toString()
										.trim());
								mAdapter.notifyDataSetChanged();
							} else if (mDialogMode == DIALOG_MODE_RENAME_GROUP
									&& !YiUtils
											.isStringInvalid(mCurrentSelectGroupName)) {
								if (mCurrentSelectGroupName
										.equals(getUnfiledGroupName())) {
									showMsgDialog(R.string.err_rename_group_default);
								} else if (mCurrentSelectGroupName
										.equals(mGroupEditText.getText()
												.toString())) {
									showMsgDialog(R.string.err_multi_group_name);
								} else {
									renameGroup(mCurrentSelectGroupName,
											mGroupEditText.getText().toString());
									mDatas.set(mSelectedIndex, mGroupEditText
											.getText().toString().trim());
									mAdapter.notifyDataSetChanged();
								}
							}
							mDialogMode = -1;
							if (mCurrentSelectGroupName != null) {
								mGroupEditText.setText("");
							}
							mSelectedIndex = -1;
						}
					});
			builder.setNegativeButton(getString(R.string.str_cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mDialogMode = -1;
							if (mCurrentSelectGroupName != null) {
								mGroupEditText.setText("");
							}
							mSelectedIndex = -1;
						}
					});
			mGroupDialog = builder.create();
		}

		if (mDialogMode == DIALOG_MODE_ADD_GROUP) {
			mGroupDialog.setTitle(R.string.str_add_group);
		} else if (mDialogMode == DIALOG_MODE_RENAME_GROUP) {
			mGroupDialog.setTitle(R.string.str_rename_group);
			mGroupEditText.setText(mCurrentSelectGroupName);
		}

		mGroupDialog.show();
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// 变更分组
		if (mMode != null && "modify".equals(mMode)) {
			if (!mCurrentSelectGroupName.equals(mGroupName) && mJid != null) {
				if (mCurrentSelectGroupName.equals(getUnfiledGroupName())) {
					moveToGroup(mJid, "");
				} else {
					moveToGroup(mJid, mCurrentSelectGroupName);
				}
			}
			finish();
		} else {
			mDialogMode = DIALOG_MODE_ADD_GROUP;
			showGroupDialog();
		}
	}

	private class DeleteGroupListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				final String name = (String) v.getTag(R.id.group_name);
				if (getUnfiledGroupName().equals(name)) {
					showMsgDialog(R.string.err_del_group_default);
					return;
				}
				showMsgDialog(null, getDeleteGroupString(),
						getString(R.string.str_ok),
						getString(R.string.str_cancel),
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								removeGroup(name);
								mDatas.remove(name);
								mAdapter.notifyDataSetChanged();
							}
						}, null);

			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private class RenameGroupListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				mCurrentSelectGroupName = (String) v.getTag(R.id.group_name);
				mSelectedIndex = (Integer) v.getTag(R.id.group_id);
				if (getUnfiledGroupName().equals(mCurrentSelectGroupName)) {
					showMsgDialog(R.string.err_rename_group_default);
					return;
				}

				mDialogMode = DIALOG_MODE_RENAME_GROUP;
				showGroupDialog();
			} catch (Exception e) {
			}
		}
	}

	private class NativeAdapter extends ArrayAdapter<String> {
		public NativeAdapter(Context context, int resource,
				int textViewResourceId, List<String> objects) {
			super(context, resource, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View retView = super.getView(position, convertView, parent);

			ViewHolder holder = null;
			if (retView.getTag() != null) {
				holder = (ViewHolder) retView.getTag();
			} else {
				holder = new ViewHolder();
				holder.mainTextView = (TextView) retView
						.findViewById(R.id.text);
				holder.delBtn = (ImageButton) retView
						.findViewById(R.id.group_manager_del);
				holder.moreBtn = (ImageButton) retView
						.findViewById(R.id.group_manager_more);
				holder.ratio = retView.findViewById(R.id.ratio);

				retView.setTag(holder);
			}

			// 变更分组
			if (mMode != null && "modify".equals(mMode)) {
				holder.delBtn.setVisibility(View.GONE);
				holder.moreBtn.setVisibility(View.GONE);

				if (holder.mainTextView.getText().equals(
						mCurrentSelectGroupName)) {
					holder.ratio.setVisibility(View.VISIBLE);
				} else {
					holder.ratio.setVisibility(View.GONE);
				}
			} else {
				holder.delBtn.setTag(R.id.group_name,
						holder.mainTextView.getText());
				holder.delBtn.setTag(R.id.group_id, position);
				holder.moreBtn.setTag(R.id.group_name,
						holder.mainTextView.getText());
				holder.moreBtn.setTag(R.id.group_id, position);

				holder.delBtn.setOnClickListener(mDeleteGroupListener);
				holder.moreBtn.setOnClickListener(mRenameGroupListener);
			}

			return retView;
		}

		private class ViewHolder {
			TextView mainTextView;
			ImageButton delBtn;
			ImageButton moreBtn;

			View ratio;
		}
	}
}
