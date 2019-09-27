package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;



public class LogicSchemaTest {

    @Test
    public void checkDataSourcesReturned() {
        //Given
        LogicSchema absSchema = Mockito.mock(
                LogicSchema.class,
                Mockito.CALLS_REAL_METHODS);

        JDBCBackendDataSource jdbcBackendDataSource = Mockito.mock(JDBCBackendDataSource.class);
        Whitebox.setInternalState(absSchema, "backendDataSource", jdbcBackendDataSource);
        Map<String, YamlDataSourceParameter> expectedMap = Maps.newHashMap();
        Mockito.when(jdbcBackendDataSource.getDataSourceParameters())
                .thenReturn(expectedMap);

        // when
        Map<String, YamlDataSourceParameter> dataSourceParameterMap = absSchema.getDataSources();


        // then
        assertNotNull(dataSourceParameterMap);
        assertEquals(expectedMap, dataSourceParameterMap);
    }
}

