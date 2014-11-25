package com.chyitech.yiim.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.net.Uri;

import com.ikantech.support.util.YiLog;
import com.qiniu.auth.Authorizer;
import com.qiniu.io.IO;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.PutExtra;
import com.qiniu.utils.Util;

public class YiQiqiuUtils {
	// 请将下面的KEY修改成自己申请的七牛相关信息
	public static final String ACCESSKEY = "iPbM0LbSWcdj9p1PgXk0CEAQIKup1LNiDTyGJMF7";
	public static final String BUBBULE = "ikantech";
	public static final String BUBBLUE_URL = "http://ikantech.qiniudn.com/";
	public static final String SECRETKEY = "TpsPj9dMHqVj6V9wAOukhJ-eeKXJlFKJPVW0L89O";

	public static final String generateUploadToken() throws Exception {
		String putPolicy = String.format("{\"scope\":\"%s\",\"deadline\":%d}",
				BUBBULE, System.currentTimeMillis() / 1000 + 600);
		String putPolicyBase64 = Util.urlsafeBase64(putPolicy);
		byte[] hmacSha1 = HmacSHA1Encrypt(putPolicyBase64, SECRETKEY);
		String encodedSign = Util.urlsafeBase64(hmacSha1);
		return String.format("%s:%s:%s", ACCESSKEY, encodedSign,
				putPolicyBase64);
	}

	protected static byte[] HmacSHA1Encrypt(String encryptText,
			String encryptKey) throws Exception {
		byte[] data = encryptKey.getBytes("UTF-8");
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance("HmacSHA1");
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);

		byte[] text = encryptText.getBytes("UTF-8");
		// 完成 Mac 操作
		return mac.doFinal(text);
	}

	public static final String generateKey() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss-SSS");
		return simpleDateFormat.format(Calendar.getInstance().getTime());
	}

	public static final String generateDownloadUrl(String key,
			boolean isThumbnail) throws Exception {
		String url = String.format("%s%s", BUBBLUE_URL, key);
		String e = null;
		if (isThumbnail) {
			e = String.format("?imageView2/2/h/200/q/60/format/JPG&e=%d",
					System.currentTimeMillis() / 1000 + 3600 * 24 * 7);
		} else {
			e = String.format("?e=%d", System.currentTimeMillis() / 1000 + 3600
					* 24 * 7);
		}
		byte[] surl_base64 = HmacSHA1Encrypt(
				String.format("%s%s", url, e), SECRETKEY);
		String hash = Util.urlsafeBase64(surl_base64);
		String token = String.format("&token=%s:%s", ACCESSKEY, hash);
		return String.format("%s%s%s", url, e, token);
	}

	public static final void upload(Context context, String token, String key,
			Uri uri, PutExtra extra, CallBack callback) {
		YiLog.getInstance().i("upload token %s", token);
		Authorizer authorizer = new Authorizer();
		authorizer.setUploadToken(token);
		IO.putFile(context, authorizer, key, uri, extra, callback);
	}
}
