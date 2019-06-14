package com.qiniu.util;

import java.util.Map;

public class UrlUtils {
	public static String composeUrlWithQueries(String url, StringMap queries) {
		StringBuilder queryStr = new StringBuilder();
		if (queries.size() != 0) {
			queryStr.append("?");
			for (String key : queries.keySet()) {
				queryStr.append(key).append("=").append(queries.get(key)).append("&");
			}
			queryStr.deleteCharAt(queryStr.length() - 1);
		}
		return url + queryStr.toString();
	}
}