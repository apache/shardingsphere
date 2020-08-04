package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MySQLServerInfoTest {

    private String SPECIFIC_VERSION = "5.1.47";

    private String EXPECTED_VERSION = "5.1.47-ShardingSphere-Proxy 5.0.0-RC1";

    private String EXPECTED_NULL_VERSION = "null-ShardingSphere-Proxy 5.0.0-RC1";

    @Test
    public void assertSetServerVersion() {
        MySQLServerInfo.setServerVersion(SPECIFIC_VERSION);
        assertThat(MySQLServerInfo.getServerVersion(), is(EXPECTED_VERSION));
    }

    @Test
    public void assertSetServerVersionForNull() {
        MySQLServerInfo.setServerVersion(null);
        assertThat(MySQLServerInfo.getServerVersion(), is(EXPECTED_NULL_VERSION));
    }
}
