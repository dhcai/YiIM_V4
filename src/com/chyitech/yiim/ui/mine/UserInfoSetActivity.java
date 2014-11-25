package com.chyitech.yiim.ui.mine;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ActionSheet;
import com.chyitech.yiim.common.ActionSheet.ActionSheetListener;
import com.chyitech.yiim.common.ViewImageDialog;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppConstant;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.sdk.api.YiXmppVCard.YiXmppVCardListener;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.util.FileUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.ikantech.support.listener.YiImageLoaderListener;
import com.ikantech.support.util.YiAsyncImageLoader;
import com.ikantech.support.util.YiUtils;

public class UserInfoSetActivity extends CustomTitleFragmentActivity implements
		YiXmppVCardListener, ActionSheetListener {
	private static final int MSG_LOAD_VCARD_COMPELETE = 0x01;

	private static final int REQ_CHOISE_PHOTO = 0x01;
	private static final int REQ_TAKE_PHOTO = 0x02;
	private static final int REQ_CROP_PHOTO = 0x03;

	private PullToRefreshScrollView mPullToRefreshScrollView;
	private ImageView mAvatarImageView;
	private TextView mNicknameTextView;
	private TextView mAccountTextView;
	private TextView mGenderTextView;
	private TextView mDistrictTextView;
	private TextView mSignTextView;

	private YiXmppVCard mVCard;
	private Uri mImageUri;

	private ViewImageDialog mImageDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_userinfo_set);
		super.onCreate(savedInstanceState);

		initViews();
		initDatas();
		installListeners();
	}

	protected void onStart() {
		super.onStart();
		loadVCard(false);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		uninstallListeners();
		super.onDestroy();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_LOAD_VCARD_COMPELETE:
			mPullToRefreshScrollView.onRefreshComplete();
			updateVCard();
			break;

		default:
			break;
		}
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.userinfo_set_scrollview);
		mAvatarImageView = (ImageView) findViewById(R.id.userinfo_set_avatar);
		mNicknameTextView = (TextView) findViewById(R.id.userinfo_set_nick);
		mAccountTextView = (TextView) findViewById(R.id.userinfo_set_account);
		mGenderTextView = (TextView) findViewById(R.id.userinfo_set_gender);
		mDistrictTextView = (TextView) findViewById(R.id.userinfo_set_district);
		mSignTextView = (TextView) findViewById(R.id.userinfo_set_sign);
	}

	protected void initDatas() {
		// TODO Auto-generated method stub
		setTheme(R.style.ActionSheetStyleIOS7);

		mVCard = new YiXmppVCard();
		loadVCard(false);
	}

	protected void installListeners() {
		// TODO Auto-generated method stub
		mPullToRefreshScrollView
				.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ScrollView> refreshView) {
						// TODO Auto-generated method stub
						// 清除图片缓存
						YiAsyncImageLoader.removeMemoryCache(YiIMSDK
								.getInstance().getCurrentUserJid());
						if (mImageDialog != null) {
							mImageDialog.clearCache();
						}

						loadVCard(true);
					}
				});
	}

	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	protected void loadVCard(boolean forceNetwork) {
		mVCard.load(this, YiIMSDK.getInstance().getCurrentUserJid(),
				forceNetwork, true, this);
	}

	protected void updateVCard() {
		if (!mVCard.isExist()) {
			return;
		}

		YiAsyncImageLoader.loadBitmapFromStore(YiIMSDK.getInstance()
				.getCurrentUserJid(), new YiImageLoaderListener() {
			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				// TODO Auto-generated method stub
				mAvatarImageView.setImageBitmap(bitmap);
			}
		});

		if (!YiUtils.isStringInvalid(mVCard.getNickname())) {
			mNicknameTextView.setText(mVCard.getNickname());
		}

		mAccountTextView.setText(YiIMSDK.getInstance().getCurrentUserName());

		if (!YiUtils.isStringInvalid(mVCard.getGender())) {
			if (YiXmppConstant.MALE.equals(mVCard.getGender())
					|| "m".equals(mVCard.getGender())) {
				mGenderTextView.setText(getString(R.string.str_male));
			} else {
				mGenderTextView.setText(getString(R.string.str_female));
			}
		}

		StringBuilder builder = new StringBuilder();
		if (!YiUtils.isStringInvalid(mVCard.getCountry())) {
			builder.append(mVCard.getCountry());
		}

		if (!YiUtils.isStringInvalid(mVCard.getProvince())) {
			builder.append(" ");
			builder.append(mVCard.getProvince());
		}

		if (!YiUtils.isStringInvalid(mVCard.getAddress())) {
			builder.append(" ");
			builder.append(mVCard.getAddress());
		}
		mDistrictTextView.setText(builder.toString());

		if (!YiUtils.isStringInvalid(mVCard.getSign())) {
			mSignTextView.setText(mVCard.getSign());
		}
	}

	@Override
	public void onSuccess() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_VCARD_COMPELETE);
	}

	@Override
	public void onFailed() {
		// TODO Auto-generated method stub
		getHandler().sendEmptyMessage(MSG_LOAD_VCARD_COMPELETE);
	}

	public void onNicknameClick(View v) {
		Intent intent = new Intent(this, ModifyNicknameActivity.class);
		startActivity(intent);
	}

	public void onRegionClick(View v) {
		Intent intent = new Intent(this, ModifyRegionActivity.class);
		startActivity(intent);
	}

	public void onSignClick(View v) {
		Intent intent = new Intent(this, ModifySignActivity.class);
		startActivity(intent);
	}

	public void onAvatarClick(View v) {
		File file = new File(YiXmppVCard.getAvatarPathByJid(YiIMSDK
				.getInstance().getCurrentUserJid()));
		if (file.exists()) {
			if (mImageDialog == null) {
				mImageDialog = new ViewImageDialog(this,
						R.style.ImageViewDialog);
			}
			mImageDialog.setBitmapPath(Uri.fromFile(file).toString());
			mImageDialog.show();
		}
	}

	public void onAvatarRootViewClick(View v) {
		showActionSheet();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		switch (arg0) {
		case REQ_CHOISE_PHOTO:
			if (arg1 == RESULT_OK) {
				storeAvatar(arg2);
			}
			break;
		case REQ_TAKE_PHOTO: {
			if (arg1 == RESULT_OK) {
				String path = FileUtils.getPath(this, mImageUri);
				FileUtils.cropImageUri(this, path, 64, REQ_CROP_PHOTO);
			} else {
				if (mImageUri != null) {
					String path = FileUtils.getPath(this, mImageUri);
					File file = new File(path);
					if (file.exists()) {
						file.delete();
					}
					mImageUri = null;
				}
			}
			break;
		}
		case REQ_CROP_PHOTO: {
			if (arg1 == RESULT_OK) {
				storeAvatar(arg2);
			}
			if (mImageUri != null) {
				String path = FileUtils.getPath(this, mImageUri);
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				mImageUri = null;
			}
			break;
		}
		default:
			break;
		}

	}

	protected void storeAvatar(final Intent arg2) {
		if (arg2 != null) {
			final Bitmap bitmap = arg2.getParcelableExtra("data");
			mAvatarImageView.setImageBitmap(bitmap);

			// 清除图片缓存
			YiAsyncImageLoader.removeMemoryCache(YiIMSDK.getInstance()
					.getCurrentUserJid());
			if (mImageDialog != null) {
				mImageDialog.clearCache();
			}

			YiIMSDK.getInstance().getBackgroundService()
					.execute(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							try {
								bitmap.compress(Bitmap.CompressFormat.JPEG, 60,
										baos);
								mVCard.setAvatar(baos.toByteArray());
								mVCard.store(null);
							} catch (Exception e) {
								// TODO: handle exception
							} finally {
								try {
									if (baos != null) {
										baos.close();
										baos = null;
									}
								} catch (Exception e2) {
									// TODO: handle exception
								}
							}
						}
					});
		}
	}

	protected void showActionSheet() {
		ActionSheet
				.createBuilder(this, getSupportFragmentManager())
				.setCancelButtonTitle(getString(R.string.str_cancel))
				.setOtherButtonTitles(getString(R.string.str_take_photo),
						getString(R.string.str_choise_photo))
				.setCancelableOnTouchOutside(true).setListener(this).show();
	}

	@Override
	public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOtherButtonClick(ActionSheet actionSheet, int index) {
		// TODO Auto-generated method stub
		switch (index) {
		case 0:
			mImageUri = FileUtils.generateImageUri();
			FileUtils.doTakePhoto(this, mImageUri, REQ_TAKE_PHOTO);
			break;
		case 1:
			FileUtils.doChoicePhoto(this, 64, REQ_CHOISE_PHOTO);
			// FileUtils.cropImageUri(this, mTakePhotoUri, 64, REQ_CROP_PHOTO);
			break;
		default:
			break;
		}
	}
}
