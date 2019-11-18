package org.apache.shardingsphere.spi.database;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaData;
import org.junit.Before;
import org.junit.Test;

public class H2DatabaseTypeTest {
    
    private DataSourceInfo dataSourceInfo;
    
    @Before
    public void setUp() {
        dataSourceInfo = new DataSourceInfo();
        dataSourceInfo.setUrl("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSourceInfo.setUsername("test");
    }
    
    @Test
    public void assertDataSourceInfoParam() {
        H2DatabaseType databaseType = new H2DatabaseType();
        H2DataSourceMetaData actual = (H2DataSourceMetaData) databaseType.getDataSourceMetaData(dataSourceInfo);
        assertThat(actual.getHostName(), is(""));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertEquals(actual.getSchemaName(), null);
    }
}
