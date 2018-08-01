package com.zhweizhu.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnalyseUtil {
	
	public static void print(String str) {
		System.out.println(str);
	}
	
	//To get the HTML content from an URL
	public static String getURLContent(String urlStr) {
		String charset = "UTF-8";// charset is default as "utf-8"
		String urlContent = null;
		AnalyseUtil.print("Getting content from: " + urlStr);
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			br.close();
			urlContent = sb.toString();

		} catch (Exception e) {
			AnalyseUtil.print("Exception happens in getURLContent: \n" + e);
		} finally {
			AnalyseUtil.print("\n The page content is: \n" + urlContent);
		}

		return urlContent;
	}

}
