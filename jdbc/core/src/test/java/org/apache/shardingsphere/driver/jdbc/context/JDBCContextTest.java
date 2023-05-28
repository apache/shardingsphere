package org.apache.shardingsphere.driver.jdbc.context;

import org.apache.shardingsphere.driver.state.circuit.datasource.CircuitBreakerDataSource;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(MockitoExtension.class)
class JDBCContextTest {

    public static final String TEST_DB = "test_db";

    @Mock
    private DataSourceChangedEvent event;

    @Test
    void assertNullCachedDbMetadataWithEmptyDatasource() throws Exception {
        JDBCContext jdbcContext = new JDBCContext(new HashMap<>());
        assertThat(jdbcContext.getCachedDatabaseMetaData(), nullValue());
    }

    @Test
    void assertNotNullCashedDbMetadataWith() throws SQLException {
        Map<String, DataSource> dataSourceMap = getStringDataSourceMap();
        JDBCContext jdbcContext = new JDBCContext(dataSourceMap);
        assertThat(jdbcContext.getCachedDatabaseMetaData(), notNullValue());
    }
    @Test
    void assetNullMetadataAfterRefreshingExisting() throws SQLException {
        Map<String, DataSource> stringDataSourceMap = getStringDataSourceMap();
        JDBCContext jdbcContext = new JDBCContext(stringDataSourceMap);
        jdbcContext.refreshCachedDatabaseMetaData(event);
        assertThat(jdbcContext.getCachedDatabaseMetaData(), nullValue());
    }

    private static Map<String, DataSource> getStringDataSourceMap() {
        CircuitBreakerDataSource dataSource = new CircuitBreakerDataSource();
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put(TEST_DB, dataSource);
        return dataSourceMap;
    }
}