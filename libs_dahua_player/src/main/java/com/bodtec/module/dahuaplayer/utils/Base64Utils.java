package com.bodtec.module.dahuaplayer.utils;

import android.text.TextUtils;
import android.util.Base64;

public class Base64Utils {
	public static String encode(byte[] bytes){
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	public static String encodeNoWrap(byte[] bytes){
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	public static byte[] decode(String str) {
		return Base64.decode(str, Base64.DEFAULT);
	}

	public static byte[] decodeToBytesByBase64(String str) {
		byte[] result = null;
		try {
			if (!TextUtils.isEmpty(str)) {
				byte[] codes = Base64.decode(str.replaceAll(" ", "+"), Base64.DEFAULT);
				result = codes;
			}
		} catch (Exception e) {
			//logger.error(str, e);
		}
		return result;
	}
}
