package com.tang.draw.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/********
 *文件名: LogUtils
 *创建者: 醉意丶千层梦
 *创建时间:2022/2/8 16:27
 *描述: LogUtils
 ********/
public class LogUtils {
    private static final String TAG_LOG = "MyLogs: ";

    private static final int DOMAIN_ID = 0xD002B00;

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, DOMAIN_ID, LogUtils.TAG_LOG);

    private static final String LOG_FORMAT = "%{public}s: %{public}s";

    private LogUtils() {
        /* Do nothing */
    }

    /**
     * Print debug log
     *
     *
     * @param msg log message
     */
    public static void debug(String msg) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, TAG_LOG, msg);
    }

    /**
     * Print info log
     *
     * @param msg log message
     */
    public static void info(String msg) {
        HiLog.info(LABEL_LOG, msg);
    }

    /**
     * Print warn log
     *
     *
     * @param msg log message
     */
    public static void warn(String msg) {
        HiLog.warn(LABEL_LOG, LOG_FORMAT, TAG_LOG, msg);
    }

    /**
     * Print error log
     *
     *
     * @param msg log message
     */
    public static void error(String msg) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, TAG_LOG, msg);
    }
}
