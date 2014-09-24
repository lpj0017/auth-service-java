package com.rcloud.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RongAuthClient {

	private static URL AUTH_URL;

	static {
		try {
			AUTH_URL = new URL("http://api.cn.rong.io/user/getToken.json");
		} catch (Exception ignore) {
		}
	}

	/**
	 * @param appKey appKey
     * @param appSecret your app's secret
     * @param userId user's id
     * @param name user's name
     * @param portraitUri user's portrait uri.
     * @param deviceId user's device id.
     * @return response in json format or null if error occurred(you should use some http debug tools to get the detailed error infomations).
	 */
	
	public static String auth(String appKey, String appSecret, String userId,
			String name, String portraitUri) throws RongAuthException {
		StringBuilder retSb = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) AUTH_URL.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			HttpURLConnection.setFollowRedirects(true);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("appKey", appKey);
			conn.setRequestProperty("appSecret", appSecret);
			conn.setRequestProperty("Content-Type",
					"Application/x-www-form-urlencoded");
			StringBuilder sb = new StringBuilder("userId=");
			sb.append(URLEncoder.encode(userId, "UTF-8"));
			sb.append("&name=").append(URLEncoder.encode(name, "UTF-8"));
			sb.append("&portraitUri=").append(
					URLEncoder.encode(portraitUri, "UTF-8"));

			OutputStream out = conn.getOutputStream();
			out.write(sb.toString().getBytes("UTF-8"));
			out.flush();
			out.close();
			
			if(conn.getResponseCode() == 200){
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
	
				retSb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					retSb.append(line);
				}
				reader.close();
			}else{
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						conn.getErrorStream()));
				
	
				retSb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					retSb.append(line);
				}
				reader.close();
				throw new RongAuthException(conn.getResponseCode(), retSb == null ? null : retSb.toString(), null); 
			}
		} catch (Exception ex) {
			if(ex instanceof RongAuthException){
				throw (RongAuthException)ex;
			}else{
				throw new RongAuthException(502, ex.getMessage(), ex);
			}
		} finally {
			try{
			conn.disconnect();
			}catch(Exception ignore){}
		}
		return retSb == null ? null : retSb.toString();
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(auth("inputYourAppKey","inputYourAppSecret","inputYourUserId","inputYourUserName","inputYourUserPortraitUri"));
		} catch (RongAuthException e) {
			System.err.println("Error getting token from api server, errorCode="+e.getErrorCode()+", errorMessage="+e.getMessage());
		}
	}
	
}