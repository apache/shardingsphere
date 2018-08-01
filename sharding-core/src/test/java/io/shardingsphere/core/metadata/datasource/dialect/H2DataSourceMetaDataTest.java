package io.shardingsphere.core.metadata.datasource.dialect;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class H2DataSourceMetaDataTest {
    
    @Test
    public void assertGetALLProperties() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(actual.getHostName(), is("mem"));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetALLPropertiesFailure() {
        new H2DataSourceMetaData("jdbc:h2:file:/data/sample");
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        H2DataSourceMetaData target = new H2DataSourceMetaData("jdbc:h2:~/ds_0;MODE=MYSQL");
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(actual.isInSameDatabaseInstance(target), is(false));
    }
}