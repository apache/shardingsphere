package io.shardingsphere.core.metadata.datasource.dialect;

import io.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SQLServerDataSourceMetaDataTest {
    @Test
    public void assertGetALLProperties() {
        SQLServerDataSourceMetaData actual = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:3306;DatabaseName=ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test
    public void assertGetALLPropertiesWithDefaultPort() {
        SQLServerDataSourceMetaData actual = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(1433));
        assertThat(actual.getSchemeName(), is("ds_0"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetALLPropertiesFailure() {
        new SQLServerDataSourceMetaData("jdbc:postgresql:xxxxxxxx");
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        SQLServerDataSourceMetaData target = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=ds_0");
        SQLServerDataSourceMetaData actual = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:1433;DatabaseName=ds_0");
        assertThat(actual.isInSameDatabaseInstance(target), is(true));
    }
}