package com.example.chartdemo.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuihaiyang1 on 2019/3/22.
 */
public class DateTimeUtil {
    public static final String PATTERN_YYYYMM = "yyyyMM";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_HH_MM = "HH:mm";
    // 用于存放不同模板的日期
    private static final ThreadLocal<Map<String, SimpleDateFormat>> LOCAL = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        @Override
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }

    };

    /**
     * 返回一个SimpleDateFormat,每个线程只会new一次pattern对应的sdf
     *
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdf(String pattern) {
        Map<String, SimpleDateFormat> map = LOCAL.get();
        SimpleDateFormat sdf = map.get(pattern);
        if (sdf == null) {
            sdf = new SimpleDateFormat(pattern);
            map.put(pattern, sdf);
        }
        return sdf;
    }

    /**
     * 获取当前日期
     *
     * @param pattern
     * @return
     */
    public static String getNow(String pattern) {
        SimpleDateFormat sdf = getSdf(pattern);
        return sdf.format(new Date());
    }
    public static String getDateString(String pattern,long time) {
        SimpleDateFormat sdf = getSdf(pattern);
        return sdf.format(new Date(time));
    }

    public static boolean isToday(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(new Date());
        calendar2.setTime(date);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }
}
