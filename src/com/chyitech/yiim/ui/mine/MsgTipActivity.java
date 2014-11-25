package com.chyitech.yiim.ui.mine;

import java.util.HashMap;
import java.util.Map;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.chyitech.yiim.R;
import com.chyitech.yiim.common.CheckSwitchButton;
import com.chyitech.yiim.common.YiUserInfo;
import com.chyitech.yiim.sdk.api.YiXmppResult;
import com.chyitech.yiim.ui.base.CustomTitleActivity;

public class MsgTipActivity extends CustomTitleActivity implements
		OnCheckedChangeListener {
	public static final String MSG_SOUND_OFFICE = "msg_office";
	public static final String MSG_SOUND_CHORD = "msg_chord";
	public static final String MSG_SOUND_TRITONE = "msg_tritone";

	private CheckSwitchButton mMsgTipSwitchAll;
	private CheckSwitchButton mMsgTipRosterAudioSwitch;
	private CheckSwitchButton mMsgTipRosterVibratorSwitch;
	private CheckSwitchButton mMsgTipRoomAudioSwitch;
	private CheckSwitchButton mMsgTipRoomVibratorSwitch;
	private CheckSwitchButton mMsgTipKeyguardSwitch;

	private ImageView mMsgSoundOffice;
	private ImageView mMsgSoundChord;
	private ImageView mMsgSoundTritone;

	private SoundPool mSoundPool;
	private Map<String, Integer> mSoundIds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_msg_tip);
		super.onCreate(savedInstanceState);
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
		mMsgTipSwitchAll = (CheckSwitchButton) findViewById(R.id.msg_tip_switch_all);
		mMsgTipRosterAudioSwitch = (CheckSwitchButton) findViewById(R.id.msg_audio_tip_switch_roaster);
		mMsgTipRosterVibratorSwitch = (CheckSwitchButton) findViewById(R.id.msg_vibrator_tip_switch_roster);
		mMsgTipRoomAudioSwitch = (CheckSwitchButton) findViewById(R.id.msg_audio_tip_switch_room);
		mMsgTipRoomVibratorSwitch = (CheckSwitchButton) findViewById(R.id.msg_vibrator_tip_switch_room);
		mMsgTipKeyguardSwitch = (CheckSwitchButton) findViewById(R.id.msg_tip_switch_keyguard);

		mMsgSoundOffice = (ImageView) findViewById(R.id.msg_sound_office);
		mMsgSoundChord = (ImageView) findViewById(R.id.msg_sound_chord);
		mMsgSoundTritone = (ImageView) findViewById(R.id.msg_sound_tritone);
	}

	@Override
	protected void initDatas() {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo != null) {
			mMsgTipSwitchAll.setChecked(userInfo.msgTipEnabled(), false);
			mMsgTipKeyguardSwitch.setChecked(userInfo.keyguardMsgTipEnabled(), false);
			mMsgTipRosterAudioSwitch.setChecked(
					userInfo.msgTipRosterAudioEnabled(), false);
			mMsgTipRosterVibratorSwitch.setChecked(
					userInfo.msgTipRosterVibratorEnabled(), false);
			mMsgTipRoomAudioSwitch.setChecked(userInfo.msgTipRoomAudioEnable(),
					false);
			mMsgTipRoomVibratorSwitch.setChecked(
					userInfo.msgTipRoomVibratorEnable(), false);

			if (userInfo.getMsgTipSound().equals(MSG_SOUND_OFFICE)) {
				mMsgSoundOffice.setVisibility(View.VISIBLE);
			} else if (userInfo.getMsgTipSound().equals(MSG_SOUND_CHORD)) {
				mMsgSoundChord.setVisibility(View.VISIBLE);
			} else if (userInfo.getMsgTipSound().equals(MSG_SOUND_TRITONE)) {
				mMsgSoundTritone.setVisibility(View.VISIBLE);
			}
		}

		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		mSoundIds = new HashMap<String, Integer>();
		mSoundIds.put(MSG_SOUND_OFFICE, mSoundPool.load(this, R.raw.office, 1));
		mSoundIds.put(MSG_SOUND_CHORD, mSoundPool.load(this, R.raw.chrod, 2));
		mSoundIds.put(MSG_SOUND_TRITONE,
				mSoundPool.load(this, R.raw.tritone, 3));
	}

	@Override
	protected void installListeners() {
		// TODO Auto-generated method stub
		mMsgTipSwitchAll.setOnCheckedChangeListener(this);
		mMsgTipKeyguardSwitch.setOnCheckedChangeListener(this);
		mMsgTipRosterAudioSwitch.setOnCheckedChangeListener(this);
		mMsgTipRosterVibratorSwitch.setOnCheckedChangeListener(this);
		mMsgTipRoomAudioSwitch.setOnCheckedChangeListener(this);
		mMsgTipRoomVibratorSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	protected void uninstallListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;

		if (buttonView == mMsgTipSwitchAll) {
			userInfo.enableMsgTip(mMsgTipSwitchAll.isChecked());
		} else if (buttonView == mMsgTipRosterAudioSwitch) {
			userInfo.enableMsgTipRosterAudio(mMsgTipRosterAudioSwitch
					.isChecked());
		} else if (buttonView == mMsgTipRosterVibratorSwitch) {
			userInfo.enableMsgTipRosterVibrator(mMsgTipRosterVibratorSwitch
					.isChecked());
		} else if (buttonView == mMsgTipRoomAudioSwitch) {
			userInfo.enableMsgRoomAudio(mMsgTipRoomAudioSwitch.isChecked());
		} else if (buttonView == mMsgTipRoomVibratorSwitch) {
			userInfo.enableMsgRoomVibratorEnable(mMsgTipRoomVibratorSwitch
					.isChecked());
		}else if(buttonView == mMsgTipKeyguardSwitch) {
			userInfo.enableKeyguardMsgTip(mMsgTipKeyguardSwitch.isChecked());
		}

		userInfo.persist(this);
	}

	private void hideMsgSoundImage() {
		mMsgSoundOffice.setVisibility(View.GONE);
		mMsgSoundChord.setVisibility(View.GONE);
		mMsgSoundTritone.setVisibility(View.GONE);
	}

	public void onSoundOfficeClick(View v) {
		hideMsgSoundImage();
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;

		userInfo.setMsgTipSound(MSG_SOUND_OFFICE);
		userInfo.persist(this);

		mMsgSoundOffice.setVisibility(View.VISIBLE);
		mSoundPool.play(mSoundIds.get(MSG_SOUND_OFFICE), 1, 1, 0, 0, 1);
	}

	public void onSoundChordClick(View v) {
		hideMsgSoundImage();
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;

		userInfo.setMsgTipSound(MSG_SOUND_CHORD);
		userInfo.persist(this);

		mMsgSoundChord.setVisibility(View.VISIBLE);
		mSoundPool.play(mSoundIds.get(MSG_SOUND_CHORD), 1, 1, 0, 0, 1);
	}

	public void onSoundTritoneClick(View v) {
		hideMsgSoundImage();
		YiUserInfo userInfo = YiUserInfo.getUserInfo(this);
		if (userInfo == null)
			return;

		userInfo.setMsgTipSound(MSG_SOUND_TRITONE);
		userInfo.persist(this);

		mMsgSoundTritone.setVisibility(View.VISIBLE);
		mSoundPool.play(mSoundIds.get(MSG_SOUND_TRITONE), 1, 1, 0, 0, 1);
	}
}
