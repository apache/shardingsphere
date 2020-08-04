package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MySQLServerInfoTest {

    private final String SPECIFIC_VERSION = "5.1.47";

    private final String EXPECTED_VERSION = "5.1.47-ShardingSphere-Proxy 5.0.0-RC1";

    @Test
    public void assertSetServerVersion() {
        MySQLServerInfo.setServerVersion(SPECIFIC_VERSION);
        assertThat(MySQLServerInfo.getServerVersion(), is(EXPECTED_VERSION));
    }
}
