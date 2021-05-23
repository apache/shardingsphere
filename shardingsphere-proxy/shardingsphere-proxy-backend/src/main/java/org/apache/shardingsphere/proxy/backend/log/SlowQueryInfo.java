package org.apache.shardingsphere.proxy.backend.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlowQueryInfo {
    private String user = "unknown";
    private String ipAddress = "0.0.0.0";
    private int id = 0;
    static private ThreadLocal<SlowQueryInfo> slowQueryLogInfoThreadLocal = ThreadLocal.withInitial(SlowQueryInfo::new);

    static public ThreadLocal<SlowQueryInfo> getThreadLocal() {
        return slowQueryLogInfoThreadLocal;
    }

    static public void setThreadLocal(SlowQueryInfo slowQueryInfo) {
        slowQueryLogInfoThreadLocal.set(slowQueryInfo);
    }
}
