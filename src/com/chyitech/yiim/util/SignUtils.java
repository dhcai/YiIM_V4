package com.chyitech.yiim.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

import com.ikantech.support.net.YiHttpRequest;

/**
 * 暂未使用
 * @author saint
 *
 */
public class SignUtils {
	public static final String UTF8 = "UTF-8";

	public static void sign(YiHttpRequest request, String secret)
			throws IOException {
		sign(request, null, secret);
	}

	public static void sign(YiHttpRequest request,
			List<String> ignoreParamNames, String secret) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		List<NameValuePair> pairs = request.getParams();
		for (NameValuePair nameValuePair : pairs) {
			params.put(nameValuePair.getName(), nameValuePair.getValue());
		}
		String sig = sign(params, ignoreParamNames, secret);
		request.addParam("sign", sig);
	}

	/**
	 * 使用<code>secret</code>对paramValues按以下算法进行签名： <br/>
	 * uppercase(hex(sha1(secretkey1value1key2value2...secret))
	 * 
	 * @param paramValues
	 *            参数列表
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues, String secret)
			throws IOException {
		return sign(paramValues, null, secret);
	}

	/**
	 * 对paramValues进行签名，其中ignoreParamNames这些参数不参与签名
	 * 
	 * @param paramValues
	 * @param ignoreParamNames
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues,
			List<String> ignoreParamNames, String secret) throws IOException {
		try {
			StringBuilder sb = new StringBuilder();
			List<String> paramNames = new ArrayList<String>(paramValues.size());
			paramNames.addAll(paramValues.keySet());
			if (ignoreParamNames != null && ignoreParamNames.size() > 0) {
				for (String ignoreParamName : ignoreParamNames) {
					paramNames.remove(ignoreParamName);
				}
			}
			Collections.sort(paramNames);

			sb.append(secret);
			for (String paramName : paramNames) {
				sb.append(paramName).append(paramValues.get(paramName));
			}
			sb.append(secret);
			byte[] sha1Digest = getSHA1Digest(sb.toString());
			return byte2hex(sha1Digest);
		} catch (IOException e) {
			throw e;
		}
	}

	public static String utf8Encoding(String value, String sourceCharsetName) {
		try {
			return new String(value.getBytes(sourceCharsetName), UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] getSHA1Digest(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			bytes = md.digest(data.getBytes(UTF8));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse.getMessage());
		}
		return bytes;
	}

	/**
	 * 二进制转十六进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}
}
