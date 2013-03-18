package com.qiniu.qbox;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {

	public static String ACCESS_KEY	= "<Please apply your access key>";
	public static String SECRET_KEY	= "<Dont change here>";

	public static String REDIRECT_URI  = "<RedirectURL>";
	public static String AUTHORIZATION_ENDPOINT = "<AuthURL>";
	public static String TOKEN_ENDPOINT = "https://acc.qbox.me/oauth2/token";

	public static String IO_HOST = "http://iovip.qbox.me";
	public static String FS_HOST = "https://fs.qbox.me";
	public static String RS_HOST = "http://rs.qbox.me:10100";
	public static String UP_HOST = "http://up.qbox.me";

	public static int BLOCK_SIZE = 1024 * 1024 * 4;
	public static int PUT_CHUNK_SIZE = 1024 * 256;
	public static int PUT_RETRY_TIMES = 3;
	public static int PUT_TIMEOUT = 300000; // 300s = 5m

    private static final String KEY_PREFIX = "qiniu.";


    private static void setConfigs(Map<String, String> kvs) {
        if (kvs == null)
            return;
        Field[] fields = Config.class.getDeclaredFields();
        for (Field field : fields) {
            final int modifiers = field.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isPublic(modifiers)) {
                String name = field.getName();
                String key = KEY_PREFIX + name.toLowerCase().replaceAll("_", ".");
                String value = kvs.get(key);
                if (value != null && value.trim().length() > 0) {
                    setFieldValue(field, value);
                }
            }
        }
    }

    public static void main(String[] args) {
        //just for test.
        HashMap<String, String> kvs = new HashMap<String, String>();
        kvs.put("qiniu.access.key", "test_key");
        kvs.put("qiniu.secret.key", "test_secret");
        kvs.put("qiniu.redirect.uri", "http://www.google.com");
        kvs.put("qiniu.block.size", "1024");
        kvs.put("qiniu.put.retry.times", "10");
        Config.setConfigs(kvs);
        System.out.println(Config.ACCESS_KEY);
        System.out.println(Config.SECRET_KEY);
        System.out.println(Config.REDIRECT_URI);
        System.out.println(Config.BLOCK_SIZE);
        System.out.println(Config.PUT_RETRY_TIMES);
    }

    private static void setFieldValue(Field field, String value) {
        try {
            if (field.getType() == int.class) {
                field.setInt(Config.class, Integer.parseInt(value));
            } else {
                field.set(Config.class, value);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void info(Object msg) {
        System.out.println("[INFO]" + msg);
    }

    static {
        //Prefer qiniu.properties
        Properties props = new Properties();
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qiniu.properties"));
            info("Found qiniu.properties in classpath,prefer to use it,the config is :" + props);
            setConfigs((Map) props);
        } catch (Exception e) {
            //ignore
            //Prefer environment variables
            setConfigs(System.getenv());
        }

    }
}