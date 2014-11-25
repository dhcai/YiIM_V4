package com.chyitech.yiim.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.ikantech.support.util.YiBase64;
import com.ikantech.support.util.YiFileUtils;

import eu.janmuller.android.simplecropimage.CropImage;

public class FileUtils {
	private String mRootDir = null;

	private static FileUtils mFileUtils = null;

	public static FileUtils getInstance() {
		if (mFileUtils == null) {
			mFileUtils = new FileUtils();
		}
		return mFileUtils;
	}

	private FileUtils() {
		mRootDir = YiFileUtils.getStorePath() + "yiim/";
	}

	public String getStoreRootPath() {
		return mRootDir;
	}

	/**
	 * 将语音短消息缓存至文件
	 * 
	 * @param user
	 *            用户名
	 * @param source
	 *            Base64编码语音短消息
	 * @return 返回缓存的文件路径
	 */
	@SuppressLint("SimpleDateFormat")
	public synchronized String storeAudioFile(String user, String source)
			throws Exception {
		BufferedOutputStream outputStream = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy_MM_dd_HH_mm_ss_SSS");
			String filename = mRootDir + StringUtils.escapeUserHost(user)
					+ File.separatorChar + "audio/";

			File file = new File(filename);
			if (!file.exists()) {
				file.mkdirs();
			}

			filename += simpleDateFormat.format(Calendar.getInstance()
					.getTime());
			filename += ".ik";

			byte[] buffer = YiBase64.decode(source.getBytes("ASCII"));

			file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}
			outputStream = new BufferedOutputStream(new FileOutputStream(
					filename));
			outputStream.write(buffer);
			return filename;
		} catch (Exception e) {
			throw e;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					outputStream = null;
				} catch (Exception e2) {
				}
			}
		}
	}

	/**
	 * 从用户相册中选择照片
	 */
	public static void doChoicePhoto(Activity activty, int outputSize,
			int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		intent.putExtra("return-data", true);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputSize);
		intent.putExtra("outputY", outputSize);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		activty.startActivityForResult(intent, requestCode);
	}

	/**
	 * 从用户相册中选择照片
	 */
	public static void doChoicePhoto(Activity activty, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		intent.putExtra("return-data", true);
		activty.startActivityForResult(intent, requestCode);
	}

	/**
	 * 拍照
	 */
	public static void doTakePhoto(Activity activity, int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// action is
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 拍照
	 */
	public static void doTakePhoto(Activity activity, Uri imageUri,
			int requestCode) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// action is
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 剪裁照片
	 */
	public static void cropImageUri(Activity activity, Bitmap data,
			int outputSize, int requestCode) {
		Intent intent = new Intent(activity, CropImage.class);
		intent.setType("image/*");
		intent.putExtra("data", data);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputSize);
		intent.putExtra("outputY", outputSize);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 剪裁照片
	 */
	public static void cropImageUri(Activity activity, String path,
			int outputSize, int requestCode) {
		Intent intent = new Intent(activity, CropImage.class);
		intent.setType("image/*");
		intent.putExtra(CropImage.IMAGE_PATH, path);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputSize);
		intent.putExtra("outputY", outputSize);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 生成拍照缓存文件
	 */
	public static Uri generateImageUri() {
		File dir = new File(YiFileUtils.getStorePath() + "yiim/dcim");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss-SSS");

		File file = new File(dir.getAbsolutePath() + "/"
				+ format.format(Calendar.getInstance().getTime()) + ".jpg");
		return Uri.fromFile(file);
	}

	/**
	 * 从本地选择视频
	 */
	public static void doChoiceVideo(Activity activty, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("video/*");
		intent.putExtra("return-data", true);
		activty.startActivityForResult(intent, requestCode);
	}

	/**
	 * 获取照片路径
	 */
	public static String getPath(Context context, Uri uri) {
		ContentResolver cr = context.getContentResolver();
		String strPath = "";
		Cursor c = null;
		try {
			if ("content".equalsIgnoreCase(uri.getScheme())) {
				c = cr.query(uri,
						new String[] { MediaStore.Images.Media.DATA }, null,
						null, null);
				if (c != null && c.getCount() > 0) {
					if (c.moveToFirst()) {
						strPath = c.getString((c
								.getColumnIndex(MediaStore.Images.Media.DATA)));
					}
				}
			} else {
				strPath = uri.toString();
			}

			if (strPath.length() != 0) {
				if (strPath.startsWith("file:")) {
					strPath = strPath.replaceFirst("file:", "");
				}
			}
		} catch (Exception ex) {

		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return strPath;
	}
}
