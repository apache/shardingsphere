package io.shardingsphere.core.metadata.datasource.dialect;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OracleDataSourceMetaDataTest {
    
    @Test
    public void assertGetALLProperties() {
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:3306/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test
    public void assertGetALLPropertiesWithDefaultPort() {
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(1521));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetALLPropertiesFailure() {
        new OracleDataSourceMetaData("jdbc:oracle:xxxxxxxx");
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        OracleDataSourceMetaData target = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1/ds_0");
        OracleDataSourceMetaData actual = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:1521/ds_0");
        assertThat(actual.isInSameDatabaseInstance(target), is(true));
    }
}