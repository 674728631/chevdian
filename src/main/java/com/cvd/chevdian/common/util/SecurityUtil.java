package com.cvd.chevdian.common.util;

import java.security.MessageDigest;

public class SecurityUtil {
    private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    public static String sha1(String str){
        if(null==str||"".equals(str))
            return null;
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] md = messageDigest.digest();
            int len = md.length;
            StringBuilder sb = new StringBuilder(len*2);
            for (int i=0;i<len;i++){
                sb.append(HEX_DIGITS[(md[i]>>4)&0xf]);
                sb.append(HEX_DIGITS[md[i]&0xf]);
            }
            return sb.toString();
        }catch (Exception e){
            return null;
        }
    }
}
