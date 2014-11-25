package com.chyitech.yiim.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.ikantech.support.util.YiBase64;
import com.ikantech.support.util.YiFileUtils;
import com.ikantech.support.util.YiLog;

/**
 * 用于录制语音短消息
 * @author saint
 *
 */
public class AudioRecorder {
	public static String mStorePath = "yiim/";

	//录制后的文件名
	private static final String RECORDER_FILE = "record.ik";
	private MediaRecorder mMediaRecorder;

	public synchronized void startRecorder() throws Exception {
		if (mMediaRecorder == null) {
			try {
				mMediaRecorder = new MediaRecorder();
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				
				//AMR格式
				if(Build.VERSION.SDK_INT > 16) {
					mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
				}else {
					mMediaRecorder
					.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
				}
				
				File file = Environment.getExternalStorageDirectory();
				if (!file.exists()) {
					throw new Exception("err_no_store_device");
				}
				file = new File(YiFileUtils.getStorePath() + mStorePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				file = new File(YiFileUtils.getStorePath() + mStorePath
						+ RECORDER_FILE);

				if (!file.exists()) {
					file.createNewFile();
				}
				YiLog.getInstance()
						.i("record file: %s", file.getAbsoluteFile());
				mMediaRecorder.setOutputFile(YiFileUtils.getStorePath()
						+ mStorePath + RECORDER_FILE);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			} catch (Exception e) {
				throw new Exception("err_unknown");
			}
		}
		try {
			mMediaRecorder.prepare();
		} catch (IOException e) {
			throw new Exception("err_unknown");
		}
		mMediaRecorder.start();
	}

	//将语音短消息编码为Base64字符串
	public synchronized String getRecordedResource() throws Exception {
		File file = new File(YiFileUtils.getStorePath() + mStorePath
				+ RECORDER_FILE);
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				int bytes = (int) file.length();
				byte[] buffer = new byte[bytes];
				int readBytes = bis.read(buffer);
				if (readBytes != buffer.length) {
					throw new IOException("Entire file not read");
				}
				return new String(YiBase64.encode(buffer), "ASCII");
			} catch (Exception ex) {
				throw new Exception("err_unknown");
			} finally {
				if (bis != null) {
					try {
						bis.close();
						bis = null;
					} catch (Exception e) {
					}
				}
			}
		}
		throw new Exception("err_unknown");
	}

	public synchronized void release() throws Exception {
		stopRecording();
	}

	public synchronized MediaRecorder getMediaRecorder() {
		return mMediaRecorder;
	}

	public String getAudioFilePath() {
		return YiFileUtils.getStorePath() + mStorePath + RECORDER_FILE;
	}

	public synchronized void stopRecording() throws Exception {
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			} catch (Exception e) {
				throw new Exception("err_unknown");
			}
		}
	}
}
