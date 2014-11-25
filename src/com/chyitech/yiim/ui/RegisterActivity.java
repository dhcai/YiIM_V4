package com.chyitech.yiim.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.ActionSheet;
import com.chyitech.yiim.common.ActionSheet.ActionSheetListener;
import com.chyitech.yiim.common.CheckSwitchButton;
import com.chyitech.yiim.sdk.api.YiIMSDK;
import com.chyitech.yiim.sdk.api.YiXmppListener;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppError;
import com.chyitech.yiim.sdk.api.YiXmppVCard;
import com.chyitech.yiim.ui.base.CustomTitleFragmentActivity;
import com.chyitech.yiim.util.FileUtils;

/**
 * 注册界面
 * @author saint
 *
 */
public class RegisterActivity extends CustomTitleFragmentActivity implements
		OnCheckedChangeListener, ActionSheetListener, YiXmppListener {
	private static final int MSG_REGISTER_SUCCESS = 0x01;

	private static final int REQ_CHOISE_PHOTO = 0x01;
	private static final int REQ_TAKE_PHOTO = 0x02;
	private static final int REQ_CROP_PHOTO = 0x03;

	private EditText mUsername;
	private EditText mNickname;
	private EditText mPassword;
	private EditText mConfirm;
	private CheckSwitchButton mGender;
	private ImageView mAvatar;
	private EditText mBirthday;
	private EditText mEmail;
	private EditText mCountry;
	private EditText mProvinc;
	private TextView mGenderTip;

	private byte[] mAvatarData = null;

	private Uri mImageUri;

	private DatePickerDialog mDatePickerDialog = null;
	private Calendar mCalendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_register);
		super.onCreate(savedInstanceState);
		initViews();
		initDatas();
		installListeners();
	}

	@Override
	public void processHandlerMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case MSG_REGISTER_SUCCESS:
			cancelProgressDialog();
			showMsgDialog(getString(R.string.err_register_success),
					getString(R.string.str_ok), new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							RegisterActivity.this.finish();
						}
					});
			break;

		default:
			break;
		}
	}

	@Override
	public void onTitleBarRightBtnClick(View view) {
		// TODO Auto-generated method stub
		if (mUsername.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.err_empty_user_name));
			return;
		}

		if (mNickname.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.err_empty_nick_name));
			return;
		}

		if (mPassword.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.err_empty_passwd));
			return;
		}

		if (mConfirm.getText().toString().length() < 1) {
			showMsgDialog(this.getString(R.string.err_empty_confirm_passwd));
			return;
		}

		if (!mPassword.getText().toString()
				.equals(mConfirm.getText().toString())) {
			showMsgDialog(this.getString(R.string.err_noteq_passwd));
			return;
		}

		if (!mUsername.getText().toString().matches("^[a-z0-9]{5,}$")) {
			showMsgDialog(this.getString(R.string.err_illegal_username));
			return;
		}

		if (!mPassword.getText().toString().matches("^[a-z0-9_\\.]{5,}$")) {
			showMsgDialog(this.getString(R.string.err_illegal_passwd));
			return;
		}

		if (mEmail.getText().toString().length() > 0) {
			if (!mEmail
					.getText()
					.toString()
					.matches(
							"^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")) {
				showMsgDialog(this.getString(R.string.err_illegal_email));
				return;
			}
		}

		showProgressDialog(R.string.str_registering);
		//连接服务器，连接成功才能发起注册
		YiIMSDK.getInstance().connect(this);
	}

	protected void onUIXmppResponse(YiXmppResult result) {
		// TODO Auto-generated method stub
		switch (result.what) {
		case XMPP_CONNECT:
			if (!result.success()) {
				cancelProgressDialog();
				showMsgDialog(this
						.getString(R.string.err_connecting_server_failed));
			} else {
				YiXmppVCard vcard = new YiXmppVCard();

				vcard.setNickname(mNickname.getText().toString());

				vcard.setAvatar(mAvatarData);

				if (mGender.isChecked()) {
					vcard.setGender("F");
				} else {
					vcard.setGender("M");
				}

				if (mBirthday.getText().toString().length() > 0) {
					vcard.setBirthday(mBirthday.getText().toString());
				}
				if (mEmail.getText().toString().length() > 0) {
					vcard.setEmail(mEmail.getText().toString());
				}
				if (mCountry.getText().toString().length() > 0) {
					vcard.setCountry(mCountry.getText().toString());
				}
				if (mProvinc.getText().toString().length() > 0) {
					vcard.setProvince(mProvinc.getText().toString());
				}

				//注册用户
				YiIMSDK.getInstance().registerUser(
						mUsername.getText().toString(),
						mPassword.getText().toString(), vcard, this);
			}
			break;
		case XMPP_REGISTER:
			if (!result.success()) {
				cancelProgressDialog();
				//注册太频繁
				if (result.error == YiXmppError.XMPP_ERR_REGISTER_TOO_QUIKLY) {
					showMsgDialog(this
							.getString(R.string.err_register_too_quikly));
				} else {
					showMsgDialog(this.getString(R.string.err_register_failed));
				}
			} else {
				getHandler().sendEmptyMessage(MSG_REGISTER_SUCCESS);
			}
			break;
		default:
			break;
		}
	}

	protected void initViews() {
		// TODO Auto-generated method stub
		mUsername = (EditText) findViewById(R.id.register_username);
		mNickname = (EditText) findViewById(R.id.register_nickname);
		mPassword = (EditText) findViewById(R.id.register_password);
		mConfirm = (EditText) findViewById(R.id.register_confirm);
		mGender = (CheckSwitchButton) findViewById(R.id.regiser_gender_switch);
		mAvatar = (ImageView) findViewById(R.id.register_avatar);
		mBirthday = (EditText) findViewById(R.id.register_birthday);
		mEmail = (EditText) findViewById(R.id.register_email);
		mCountry = (EditText) findViewById(R.id.register_country);
		mProvinc = (EditText) findViewById(R.id.register_province);
		mGenderTip = (TextView) findViewById(R.id.register_gender_tip);
	}

	protected void initDatas() {
		// TODO Auto-generated method stub
		setTheme(R.style.ActionSheetStyleIOS7);

		setTitleBarRightBtnText(this.getString(R.string.str_tijiao));
		mGender.setChecked(true);
		mCalendar = Calendar.getInstance();
	}

	protected void installListeners() {
		// TODO Auto-generated method stub
		mGender.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView == mGender) {
			if (mGender.isChecked()) {
				mGenderTip.setText(R.string.str_female);
			} else {
				mGenderTip.setText(R.string.str_male);
			}
		}
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
			mAvatar.setImageBitmap(bitmap);

			YiIMSDK.getInstance().getBackgroundService()
					.execute(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							try {
								bitmap.compress(Bitmap.CompressFormat.JPEG, 60,
										baos);
								mAvatarData = baos.toByteArray();
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

	public void onAvatarClick(View v) {
		showActionSheet();
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

	@Override
	public void onXmppResonpse(final YiXmppResult result) {
		// TODO Auto-generated method stub
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				onUIXmppResponse(result);
			}
		});
	}

	//选择出生日期
	public void onBirthdayClick(View view) {
		if (mDatePickerDialog == null) {
			mDatePickerDialog = new DatePickerDialog(RegisterActivity.this,
					new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							// TODO Auto-generated method stub
							mBirthday.setText(String.format("%04d-%02d-%02d",
									year, monthOfYear + 1, dayOfMonth));
							mCalendar.set(Calendar.YEAR, year);
							mCalendar.set(Calendar.MONTH, monthOfYear);
							mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						}
					}, 1980, 0, 1);
			mCalendar.set(Calendar.YEAR, 1980);
			mCalendar.set(Calendar.MONTH, 0);
			mCalendar.set(Calendar.DAY_OF_MONTH, 1);
			mDatePickerDialog.setCanceledOnTouchOutside(true);
			mDatePickerDialog
					.setTitle(getString(R.string.str_register_birthday_hint));
		}
		mDatePickerDialog.show();
	}
}
