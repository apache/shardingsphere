package org.apache.shardingsphere.db.protocol.mysql.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MySQLServerInfoTest {

    @Test
    public void assertSetServerVersion() {
        MySQLServerInfo.setServerVersion("5.1.47");
        assertThat(MySQLServerInfo.getServerVersion(), is("5.1.47-ShardingSphere-Proxy 5.0.0-RC1"));
    }

    @Test
    public void assertSetServerVersionForNull() {
        MySQLServerInfo.setServerVersion(null);
        assertThat(MySQLServerInfo.getServerVersion(), is("null-ShardingSphere-Proxy 5.0.0-RC1"));
    }
}
