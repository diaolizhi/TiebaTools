package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @program: tiebatool
 * @description: 工具类
 * @author:
 * @create: 2018-11-12 15:24
 **/
public class OtherUtils {

    /**
    * @Description: 以下三个方法用于 MD5 加密
    * @Param: [txt, hashType]
    * @return: java.lang.String
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //error action
        }
        return null;
    }

    private static String md5(String txt) {
        return OtherUtils.getHash(txt, "MD5").toUpperCase();
    }

    /**
    * @Description: 通过 TreeMap 计算出 SIGN 的值
    * @Param: [body]
    * @return: java.lang.String
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    public static String countSign(TreeMap<String, String> body) {
        // 临时字符串，用于计算 sign
        StringBuilder tString = new StringBuilder();
        // 真正的请求主体
        StringBuilder bodyString = new StringBuilder();

        for (Map.Entry<String, String> entry : body.entrySet()) {
            tString.append(entry.getKey()).append("=")
                    .append(entry.getValue());

            bodyString.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        tString.append("tiebaclient!!!");
        String sign = OtherUtils.md5(tString.toString());
        bodyString.append("sign")
                .append("=")
                .append(sign);
        return bodyString.toString();
    }

    /** 
    * @Description: 将时间戳格式化
    * @Param: [time] 
    * @return: java.lang.String 
    * @Author: diaolizhi
    * @Date: 2018/12/22 
    */ 
    public static String formatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        Date date = new Date(Long.parseLong(time + "000"));
        return format.format(date);
    }

}
