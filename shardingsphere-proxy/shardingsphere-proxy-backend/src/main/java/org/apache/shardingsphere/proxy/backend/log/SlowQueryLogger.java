package org.apache.shardingsphere.proxy.backend.log;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j(topic = "slow-query-log")
public class SlowQueryLogger {
    private static final String Pattern = "# Time: {}\n" +
            "# User@Host: {}[{}] @  [{}]  Id:    {}\n" +
            "# Query_time: {}  Lock_time: {} Rows_sent: {}  Rows_examined: {}\n" +
            "SET timestamp={};\n" +
            "{}";

    @SneakyThrows
    public static void slowQueryLog(long startTime, String sql, SlowQueryInfo slowQueryInfo) {
        long executeTime = System.currentTimeMillis() - startTime;
        ConfigurationProperties props = ProxyContext.getInstance().getSchemaContexts().getProps();
        if (props.<Boolean>getValue(ConfigurationPropertyKey.SLOW_QUERY_SQL_SHOW) &&
                executeTime > Double.parseDouble(ProxyContext.getInstance().getSchemaContexts().getProps().getValue(ConfigurationPropertyKey.LONG_QUERY_TIME)) * 1000) {
            LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
            log.info(Pattern, start, slowQueryInfo.getUser(), InetAddress.getLocalHost().getHostName(), slowQueryInfo.getIpAddress(), slowQueryInfo.getId(), (double) executeTime / 1000, 0, 0, 0, System.currentTimeMillis(), sql);
        }
    }
}
