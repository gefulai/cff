package com.cff.common.constant;

import java.time.format.DateTimeFormatter;

public final class DateFormat {

    private DateFormat() {
    }

    // 默认日期格式常量
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    // 短日期格式常量
    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";

    // 默认日期时间格式常量
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 默认日期时间分钟格式常量
    public static final String DEFAULT_DATE_TIME_MIN_FORMAT = "yyyy-MM-dd HH:mm";
    // SQL日期时间格式常量（包含毫秒）
    public static final String SQL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    // 默认日期格式化器
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    public static final DateTimeFormatter DEFAULT_DATE_TIME_MIN_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_MIN_FORMAT);
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);

}
