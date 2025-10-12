package com.cff.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.cff.common.constant.DateFormat.DEFAULT_DATE_FORMATTER;

public final class DateTimeUtils {

    // 私有构造方法，防止实例化
    private DateTimeUtils() {
    }

    /**
     * 获取当前时间的Date对象
     * @return 当前时间的Date对象
     */
    public static Date now() {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取今天日期的Date对象，时间部分为00:00:00
     * @return 今天日期的Date对象
     */
    public static Date today() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将LocalDateTime类型转换为默认格式(yyyy-MM-dd)的字符串
     * @param localDateTime LocalDateTime对象
     * @return 格式化后的日期字符串
     */
    public static String format(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return DEFAULT_DATE_FORMATTER.format(localDateTime);
    }

    /**
     * 将LocalDateTime类型转换为指定格式的字符串
     * @param localDateTime LocalDateTime对象
     * @param format 日期格式
     * @return 格式化后的日期字符串
     */
    public static String format(LocalDateTime localDateTime, String format) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    /**
     * 将Date类型转换为默认格式(yyyy-MM-dd)的字符串
     *
     * @param date Date对象
     * @return 格式化后的日期字符串
     */
    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toDateTime(date);
        return format(localDateTime);
    }

    /**
     * 将Date类型转换为指定格式的字符串
     *
     * @param date   Date对象
     * @param format 日期格式
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toDateTime(date);
        return format(localDateTime, format);
    }

    /**
     * 将Date类型转换为LocalDateTime类型
     *
     * @param date Date对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime toDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将LocalDateTime类型转换为Date类型
     *
     * @param localDateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 计算Date类型加指定天数后的日期
     *
     * @param date 原始日期
     * @param days 要增加的天数
     * @return 增加天数后的日期
     */
    public static Date plusDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toDateTime(date);
        LocalDateTime newLocalDateTime = localDateTime.plusDays(days);
        return toDate(newLocalDateTime);
    }

    /**
     * 计算Date类型减指定天数后的日期
     *
     * @param date 原始日期
     * @param days 要减少的天数
     * @return 减少天数后的日期
     */
    public static Date minusDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toDateTime(date);
        LocalDateTime newLocalDateTime = localDateTime.minusDays(days);
        return toDate(newLocalDateTime);
    }
}
