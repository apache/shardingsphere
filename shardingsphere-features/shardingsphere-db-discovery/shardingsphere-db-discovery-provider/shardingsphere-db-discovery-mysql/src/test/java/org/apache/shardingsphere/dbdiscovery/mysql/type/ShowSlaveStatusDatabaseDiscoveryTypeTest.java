package org.apache.shardingsphere.dbdiscovery.mysql.type;

import com.google.common.eventbus.EventBus;
import org.apache.shardingsphere.dbdiscovery.mysql.AbstractDatabaseDiscoveryType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShowSlaveStatusDatabaseDiscoveryTypeTest {
    
    private final ShowSlaveStatusDatabaseDiscoveryType showSlaveStatusDatabaseDiscoveryType = new ShowSlaveStatusDatabaseDiscoveryType();
    
    @Test
    public void assertUpdatePrimaryDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", getDataSource(false, 3306, 0));
        dataSourceMap.put("ds_1", getDataSource(true, 3307, 0));
        showSlaveStatusDatabaseDiscoveryType.updatePrimaryDataSource("discovery_db", dataSourceMap, Collections.emptySet(), "group_name");
        assertThat(showSlaveStatusDatabaseDiscoveryType.getPrimaryDataSource(), is("ds_0"));
    }
    
    @Test
    public void assertUpdateMemberState() throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field declaredField = AbstractDatabaseDiscoveryType.class.getDeclaredField("oldPrimaryDataSource");
        declaredField.setAccessible(true);
        declaredField.set(showSlaveStatusDatabaseDiscoveryType, "ds_0");
        EventBus eventBus = mock(EventBus.class);
        mockStatic(ShardingSphereEventBus.class);
        when(ShardingSphereEventBus.getInstance()).thenReturn(eventBus);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", getDataSource(false, 3306, 0));
        dataSourceMap.put("ds_1", getDataSource(true, 3307, 1));
        dataSourceMap.put("ds_2", getDataSource(true, 3308, 2));
        showSlaveStatusDatabaseDiscoveryType.getProps().setProperty("delay-milliseconds-threshold", "2000");
        showSlaveStatusDatabaseDiscoveryType.updateMemberState("discovery_db", dataSourceMap, Collections.emptySet());
        verify(eventBus).post(Mockito.refEq(new DataSourceDisabledEvent("discovery_db", "ds_2", true)));
    }
    
    private DataSource getDataSource(final boolean slave, final int port, final long secondsBehindMaster) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(result.getConnection()).thenReturn(connection);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery("SHOW SLAVE STATUS")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        if (slave) {
            when(resultSet.getString("Master_Host")).thenReturn("127.0.0.1");
            when(resultSet.getString("Master_Port")).thenReturn(Integer.toString(3306));
            when(resultSet.getLong("Seconds_Behind_Master")).thenReturn(secondsBehindMaster);
        }
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:mysql://127.0.0.1:" + port + "/test?serverTimezone=UTC&useSSL=false");
        return result;
    }
}
