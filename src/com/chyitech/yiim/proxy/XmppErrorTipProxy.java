package com.chyitech.yiim.proxy;

import android.content.Context;

import com.chyitech.yiim.R;
import com.chyitech.yiim.sdk.api.YiXmppResult.YiXmppError;
import com.ikantech.support.proxy.YiDialogProxy.YiDialogExtProxiable;

/**
 * 错误代码转换为本地字符串，并用Dialog提示给用户
 * @author saint
 *
 */
public class XmppErrorTipProxy {
	public static boolean handle(Context context,
			YiDialogExtProxiable proxiable, YiXmppError error) {
		switch (error) {
		case XMPP_ERR_ILLEGAL_PASSWORD:
		case XMPP_ERR_ILLEGAL_USERNAME:
		case XMPP_ERR_SERVER_NOT_CONNECTED:
			proxiable.showMsgDialog(errorToString(context, error));
			return true;
		default:
			return false;
		}
	}

	public static boolean handle(Context context,
			YiDialogExtProxiable proxiable, YiXmppError error,
			boolean handleUnknownError) {
		if (handleUnknownError && error.equals(YiXmppError.XMPP_ERR_UNKNOWN)) {
			proxiable.showMsgDialog(errorToString(context, error));
			return true;
		} else {
			return handle(context, proxiable, error);
		}
	}
	
	public static String errorToString(Context context, YiXmppError error) {
		switch (error) {
		case XMPP_ERR_SERVER_NOT_CONNECTED:
			return context.getString(R.string.err_server_not_connected);
		case XMPP_ERR_NOT_AUTHED:
			return context.getString(R.string.err_server_not_authed);
		case XMPP_ERR_ILLEGAL_USERNAME:
			return context.getString(R.string.err_illegal_username);
		case XMPP_ERR_ILLEGAL_PASSWORD:
			return context.getString(R.string.err_illegal_passwd);
		default:
			return context.getString(R.string.err_unknown);
		}
	}
}
