package com.qiniu.util;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.BitSet;

public class UrlUtils {
    public static final String ALWAYS_NON_ENCODING = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "-_.";
    // EagleId: 767ba4a715644841959032513e
    // Location: /s%E5%85%B1df/%2A/~/@/:/%21/$/&/&amp;/%27/%28/%29/%2A/+/-/_/,/;%22/=/%20/sdf/%2A/~/@/:/%21/$/&/&amp;/%27/%28/%29/%2A/+/-/_/,/;%22/=/%20/?sdfr=34sdf/*/~/@/:/!/$/&/&amp;/%27/(/)/*/+/-/_/,/;%22/=/%20/
    // Via: cache20.l2cn1824[75,301-0,M], cache39.l2cn1824[76,0], vcache2.cn822[145,301-0,M], vcache19.cn822[147,0]
    // X-Cache: MISS TCP_MISS dirn:-2:-2
    // X-Log: X-Log
    // X-M-Log: QNM:xs1166;SRCPROXY:xs488;SRC:2/301;SRCPROXY:2/301;QNM3:50/301
    // X-M-Reqid: kwYAAE1_CVUWKrYV
    // X-Qiniu-Zone: 0
    // X-Qnm-Cache: Validate,MissValidate
    // X-Reqid: kowAAABb8VcWKrYV
    // CHECKSTYLE:ON
    public static final String PLUS_NON_ENCODING = "~@:$&+,;=/?";
    /**
     * https://github.com/google/guava/blob/v28.0/guava/src/com/google/common/net/UrlEscapers.java
     */
    public static final String PLUS_NON_ENCODING2 = "~@:!$&'()*+,;=/?";
    static final int caseDiff = ('a' - 'A');

    // CHECKSTYLE:OFF
    // http://asfd.clouddn.com//s共df/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/-/_/,/;"/=/ /sdf/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/-/_/,/;"/=/ /?sdfr=34sdf/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/-/_/,/;"/=/ /

    // Request URL: http://asfd.clouddn.com//s%E5%85%B1df/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/-/_/,/;%22/=/%20/sdf/*/~/@/:/!/$/&/&amp;/'/(/)/*/+/-/_/,/;%22/=/%20/?sdfr=34sdf/*/~/@/:/!/$/&/&amp;/%27/(/)/*/+/-/_/,/;%22/=/%20/
    // Request Method: GET

    private UrlUtils() {
    }

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

    public static String urlEncode(String s) {
        return urlEncode(s, Charset.forName("UTF-8"));
    }

    public static String urlEncode(String s, String nonEncoding) {
        return urlEncode(s, Charset.forName("UTF-8"), nonEncoding);
    }

    public static String urlEncode(String s, Charset enc) {
        return urlEncode(s, enc, PLUS_NON_ENCODING);
    }

    public static String urlEncode(String s, Charset enc, String nonEncoding) {
        BitSet dontNeedEncoding = buildDontNeedEncoding(nonEncoding);
        return urlEncode(s, enc, dontNeedEncoding);
    }

    private static BitSet buildDontNeedEncoding(String nonEncoding) {
        BitSet dontNeedEncoding = new BitSet();
        nonEncoding = ALWAYS_NON_ENCODING + nonEncoding;
        for (int i = 0; i < nonEncoding.length(); i++) {
            char c = nonEncoding.charAt(i);
            dontNeedEncoding.set(c);
        }
        return dontNeedEncoding;
    }

    // base on java.net.URLEncoder
    /* java.net.URLEncoder
    public static String encode(String s, String enc)
            throws UnsupportedEncodingException {
    */
    private static String urlEncode(String s, Charset charset, BitSet dontNeedEncoding) { // this

        boolean needToChange = false;
        StringBuilder out = new StringBuilder(s.length());
        /* java.net.URLEncoder
         * Charset charset;
         */
        CharArrayWriter charArrayWriter = new CharArrayWriter();

        /* java.net.URLEncoder
        if (enc == null)
            throw new NullPointerException("charsetName");

        try {
            charset = Charset.forName(enc);
        } catch (IllegalCharsetNameException e) {
            throw new UnsupportedEncodingException(enc);
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(enc);
        }
        */
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }

        for (int i = 0; i < s.length(); ) {
            int c = (int) s.charAt(i);
            //System.out.println("Examining character: " + c);
            if (dontNeedEncoding.get(c)) {
                /* java.net.URLEncoder
                if (c == ' ') {
                    c = '+';
                    needToChange = true;
                }
                */
                //System.out.println("Storing: " + c);
                out.append((char) c);
                i++;
            } else {
                // convert to external encoding before hex conversion
                do {
                    charArrayWriter.write(c);
                    /*
                     * If this character represents the start of a Unicode
                     * surrogate pair, then pass in two characters. It's not
                     * clear what should be done if a byte reserved in the
                     * surrogate pairs range occurs outside of a legal
                     * surrogate pair. For now, just treat it as if it were
                     * any other character.
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                          System.out.println(Integer.toHexString(c)
                          + " is high surrogate");
                        */
                        if ((i + 1) < s.length()) {
                            int d = (int) s.charAt(i + 1);
                            /*
                              System.out.println("\tExamining "
                              + Integer.toHexString(d));
                            */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                  System.out.println("\t"
                                  + Integer.toHexString(d)
                                  + " is low surrogate");
                                */
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));

                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] ba = str.getBytes(charset);
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }

        return (needToChange ? out.toString() : s);
    }
}
