package com.qiniu.qbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class Config {
	private static String accessKey;
	private static String secretKey;
	
	private static String rediretUri;
	private static String authorizationEndPoint;
	private static String tokenEndPoint;
	
	private static String ioHost;
	private static String fsHost;
	private static String rsHost;
	private static String upHost;
	
	/** 单位为字节(例如：4M = 4 * 1024 * 1024 = 4194304字节) */
	private static int blockSize;	
	/**	单位为字节(例如：256K = 256 * 1024 = 262144字节) */
	private static int putChuncksize;	 
	/**	上传失败后重试的次数 (例如：3次)*/
	private static int putRetryTimes;	
	/**	 单位为秒 */
	private static int putTimeOut;		

	public static String getAccessKey() {
		return accessKey;
	}

	public static String getSecretKey() {
		return secretKey;
	}

	public static String getRediretUri() {
		return rediretUri;
	}

	public static String getAuthorizationEndPoint() {
		return authorizationEndPoint;
	}

	public static String getTokenEndPoint() {
		return tokenEndPoint;
	}

	public static String getIoHost() {
		return ioHost;
	}

	public static String getFsHost() {
		return fsHost;
	}

	public static String getRsHost() {
		return rsHost;
	}

	public static String getUpHost() {
		return upHost;
	}

	public static int getBlockSize() {
		return blockSize;
	}

	public static int getPutChuncksize() {
		return putChuncksize;
	}

	public static int getPutRetryTimes() {
		return putRetryTimes;
	}

	public static int getPutTimeOut() {
		return putTimeOut;
	}

	private Config() {}
	
	/**
	 * Reads the file line by line, and concatenate the each line as a big String.
	 * @param 		file the config file passed by the caller
	 * @return 		A String containing all the content of the file
	 */
	private static String loadConfig(String file) {
		BufferedReader reader = null;
		StringBuffer jsonBuf = new StringBuffer();
		try {
			reader = new BufferedReader(new FileReader(new File(file)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				jsonBuf.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonBuf.toString();
	}

	/**
	 * Initializes each field with the content according to the json object.
	 * @param 	json		An json object
	 * @throws 	Exception	If any key not presents in the config file
	 */
	private static void unmarshal(JSONObject json) throws Exception {
		try {
			if (json.has("ACCESS_KEY")) {
				accessKey = json.getString("ACCESS_KEY");
			} else {
				throw new Exception("No ACCESS_KEY specified in the config file, please check it!");
			}
			if (json.has("SECRET_KEY")) {
				secretKey = json.getString("SECRET_KEY");
			} else {
				throw new Exception("No SECRET_KEY specified in the config file, please check it!");
			}
			if (json.has("REDIRECT_URI")) {
				rediretUri = json.getString("REDIRECT_URI");
			} else {
				throw new Exception("No REDIRECT_URI specified in the config file, please check it!");
			}
			if (json.has("AUTHORIZATION_ENDPOINT")) {
				authorizationEndPoint = json.getString("AUTHORIZATION_ENDPOINT");
			} else {
				throw new Exception("No AUTHORIZATION_ENDPOINT specified in the config file, please check it!");
			}
			if (json.has("TOKEN_ENDPOINT")) {
				tokenEndPoint = json.getString("TOKEN_ENDPOINT");
			} else {
				throw new Exception("No TOKEN_ENDPOINT specified in the config file, please check it!");
			}
			if (json.has("IO_HOST")) {
				ioHost = json.getString("IO_HOST");
			} else {
				throw new Exception("No IO_HOST specified in the config file, please check it!");
			}
			if (json.has("FS_HOST")) {
				fsHost = json.getString("FS_HOST");
			} else {
				throw new Exception("No FS_HOST specified in the config file, please check it!");
			}
			if (json.has("RS_HOST")) {
				rsHost = json.getString("RS_HOST");
			} else {
				throw new Exception("No RS_HOST specified in the config file, please check it!");
			}
			if (json.has("UP_HOST")) {
				upHost = json.getString("UP_HOST");
			} else {
				throw new Exception("No UP_HOST specified in the config file, please check it!");
			}
			if (json.has("BLOCK_SIZE")) {
				blockSize = json.getInt("BLOCK_SIZE");
			} else {
				throw new Exception("No BLOCK_SIZE specified in the config file, please check it!");
			}
			if (json.has("PUT_CHUNK_SIZE")) {
				putChuncksize = json.getInt("PUT_CHUNK_SIZE");
			} else {
				throw new Exception("No PUT_CHUNK_SIZE specified in the config file, please check it!");
			}
			if (json.has("PUT_RETRY_TIMES")) {
				putRetryTimes = json.getInt("PUT_RETRY_TIMES");
			} else {
				throw new Exception("No PUT_RETRY_TIMES specified in the config file, please check it!");
			}
			if (json.has("PUT_TIMEOUT")) {
				putTimeOut = json.getInt("PUT_TIMEOUT");
			} else {
				throw new Exception("No PUT_TIMEOUT specified in the config file, please check it!");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the global config information using the specified config file.
	 * @param 	configFile	The global config file passed by the user.
	 */
	public static void init(String configFile) {
		String jsonString = loadConfig(configFile);
		JSONObject json;
		try {
			json = new JSONObject(jsonString);
			unmarshal(json);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}