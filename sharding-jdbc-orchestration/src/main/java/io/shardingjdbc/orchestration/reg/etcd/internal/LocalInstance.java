package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.internal.util.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * @author junxiong
 */
public class LocalInstance {
    private static final String DELIMITER = "@-@";
    private static final String PID_FLAG = "@";

    public static String getID() {
        return IpUtils.getIp() + DELIMITER + ManagementFactory.getRuntimeMXBean().getName().split(PID_FLAG)[0];
    }
}
