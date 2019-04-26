package com.cvd.chevdian.common.util;


import com.cvd.chevdian.common.constant.GlobalConstant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

    // 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
    public static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    // 标准日期格式：yyyyMMddHHmmss
    public static final String PURE_DATETIME_FORMAT = "yyyyMMddHHmmss";

    public static String curTimeToFormatString(String format, Date date) {
        SimpleDateFormat simpleDateFormat = getFormat(format);
        return simpleDateFormat.format(date);
    }

    public static String curTime(Date date) {
        return curTimeToFormatString(PURE_DATETIME_FORMAT, date);
    }

    private static SimpleDateFormat getFormat(String format) {
        return new SimpleDateFormat(format);
    }

    public static Map<String, String> getDateMap(long ab) {
        long curren = System.currentTimeMillis();
        curren += ab;
        Date date = new Date(curren);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
        Integer minute = calendar.get(Calendar.MINUTE);
        Integer second = calendar.get(Calendar.SECOND);
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month = calendar.get(Calendar.MONTH) + 1;
        Integer year = calendar.get(Calendar.YEAR);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("year", year.toString());
        resultMap.put("month", month.toString());
        resultMap.put("day", day.toString());
        resultMap.put("hour", hour.toString());
        resultMap.put("minute", minute.toString());
        resultMap.put("second", second.toString());
        return resultMap;
    }

    public static Date getYearDate(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.YEAR, +1);
            date = calendar.getTime();
            DateFormat dateFormat = new SimpleDateFormat(GlobalConstant.DEFAULT_FORMAT);
            return dateFormat.parse(dateFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
