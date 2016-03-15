package zzhao.network.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
*
 * @author zzhao
 * @version 2016年1月14日
*/
public class DateTimeUtils {

    public final static long MS_PER_DAY = 86400000L;
    public final static long MS_PER_HOUR = 3600000L;
    public final static long MS_PER_SECOND = 1000L;
    public final static long MS_PER_MINUTE = 60000L;

    public final static long SECOND_PER_DAY = 86400L;
    public final static long SECOND_PER_HOUR = 3600L;
    public final static long SECOND_PER_MINUTE = 60L;

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long elapse(long start) {
        return DateTimeUtils.now() - start;
    }

    public static long getTimeByDateString(String dateString, String format) throws ParseException {
        if (StringUtils.isNotBlank(dateString)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format , Locale.ENGLISH);
            Date date = sdf.parse(dateString);
            return date.getTime();
        }
        return -1L;
    }

    /**
     * 向后推迟i天
     * 
     * @param time
     * @param i
     * @return
     */
    public static long addOrSubtractDay(long time, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.add(Calendar.DATE, i);
        return calendar.getTimeInMillis();
    }

    /**
     * 月份的加减
     * 
     * @param curTime
     *            为负可实现减的效果
     * @param m
     * @return
     */
    public static long addOrSubtractMonth(long curTime, int m) {
        Calendar calendar = Calendar.getInstance();
        // calendar.setTimeInMillis(now());
        calendar.setTime(new Date(curTime));
        calendar.add(Calendar.MONTH, m);
        return calendar.getTimeInMillis();
    }

    public static int getCurDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now());
        return calendar.get(Calendar.DATE);
    }

    public static int getCurHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now());
        return calendar.get(Calendar.HOUR_OF_DAY);// 24小时制
    }

    public static long setHour24(long curTime, int h) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(curTime);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        return calendar.getTimeInMillis();
    }

    public static int getCurrDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getCurMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now());
        return calendar.get(Calendar.MINUTE);
    }

    public static String formatDate(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String dateStr = sdf.format(new Date(time));
        return dateStr;
    }

    public static String format(long time) {
        return formatDate(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String currentTimeToString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return format.format(new Date());
    }
    
    public static String currentDateToSimpleString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date());
    }
    
    public static String currentTimeToSimpleString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
    
    public static String timeToString(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return format.format(new Date(time));
    }

    /**
     * 指定时间格式的时间字符串解析
     * 
     * @param timeStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date parse(String timeStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 常用的 yyyy-MM-dd HH:mm:ss 格式解析
     * 
     * @param timeStr
     * @return
     * @throws ParseException
     */
    public static Date parse(String timeStr) throws ParseException {
        return parse(timeStr, "yyyy-MM-dd HH:mm:ss");
    }
}
