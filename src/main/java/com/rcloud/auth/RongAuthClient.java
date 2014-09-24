package com.rcloud.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;

public class RongAuthClient {

	private static URL AUTH_URL;
	
	private static final SecureRandom random = new SecureRandom();

	static {
		try {
			AUTH_URL = new URL("https://api.cn.rong.io/user/getToken.json");
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
			
			String nonce = new DecimalFormat("000000").format(random.nextInt(100000));
			String timestamp = String.valueOf(System.currentTimeMillis());
			
			StringBuilder toSign = new StringBuilder(appSecret).append(nonce).append(timestamp);
			
			conn.setRequestProperty("App-Key", appKey);
			conn.setRequestProperty("Timestamp", timestamp);
			conn.setRequestProperty("Nonce", nonce);
			conn.setRequestProperty("Signature", hexSHA1(toSign.toString()));
			
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
	
	public static String hexSHA1(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(value.getBytes("utf-8"));
			byte[] digest = md.digest();
			return byteToHexString(digest);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String byteToHexString(byte[] bytes) {
		String stmp = "";  
        StringBuilder sb = new StringBuilder("");  
        for (int n = 0; n < bytes.length; n++) {  
            stmp = Integer.toHexString(bytes[n] & 0xFF);  
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);  
        }  
        return sb.toString().toUpperCase().trim();  
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(auth("inputYourAppKey","inputYourAppSecret","inputYourUserId","inputYourUserName","inputYourUserPortraitUri"));
		} catch (RongAuthException e) {
			System.err.println("Error getting token from api server, errorCode="+e.getErrorCode()+", errorMessage="+e.getMessage());
		}
	}
	
}