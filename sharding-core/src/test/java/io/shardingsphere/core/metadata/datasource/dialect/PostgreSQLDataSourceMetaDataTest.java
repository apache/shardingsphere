package io.shardingsphere.core.metadata.datasource.dialect;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PostgreSQLDataSourceMetaDataTest {
    @Test
    public void assertGetALLProperties() {
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1:3306/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test
    public void assertGetALLPropertiesWithDefaultPort() {
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(5432));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetALLPropertiesFailure() {
        new PostgreSQLDataSourceMetaData("jdbc:postgresql:xxxxxxxx");
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        PostgreSQLDataSourceMetaData target = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1:5432/ds_0");
        assertThat(actual.isInSameDatabaseInstance(target), is(true));
    }
}