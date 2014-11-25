package com.chyitech.yiim.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.ikantech.support.util.YiUtils;

public class StringUtils {
	private StringUtils() {

	}

	/**
	 * 判断JID是否为群组
	 * 
	 * @param jid
	 *            JID
	 * @return true，如果是群组JID
	 */
	public static boolean isRoomJid(String jid) {
		if (jid != null && jid.contains("conference")) {
			return true;
		}
		return false;
	}

	/**
	 * 输入10000@192.168.1.104/Smack
	 * 
	 * @param user
	 * @return 10000@192.168.1.104
	 */
	public static String escapeUserResource(String user) {
		return user.replaceAll("/.+$", "");
	}

	/**
	 * 输入10000@192.168.1.104/Smack
	 * 
	 * @param user
	 * @return 10000
	 */
	public static String escapeUserHost(String user) {
		return user.replaceAll("@.+$", "");
	}

	/**
	 * 获取JID的资源
	 * 
	 * @param user
	 *            JID
	 * @return 返回资源
	 */
	public static String getJidResouce(String user) {
		if (YiUtils.isStringInvalid(user)) {
			return "";
		}
		return user.substring(user.indexOf('/') + 1, user.length());
	}

	/**
	 * 将XMPP时间解析为Java的Data
	 * 
	 * @param when
	 *            XMPP时间字符串
	 * @return 解析好的Date
	 */
	public static Date parseXmppTime(String when) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return simpleDateFormat.parse(when);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * 将Java的date格式化为XMPP字符串
	 * 
	 * @param date
	 *            要格式化的Date
	 * @return 格式化好的字符串
	 */
	public static String formatXmppTime(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return simpleDateFormat.format(date);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
