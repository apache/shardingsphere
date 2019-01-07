/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.convert;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import org.junit.Test;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public final class XADataSourceMapConverterTest {
    
    private XADataSourceConverter xaDataSourceMapConverter = new XADataSourceConverter();
    
    @Test
    public void assertGetH2XATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(DatabaseType.H2, createDataSourceMap(PoolType.DRUID, DatabaseType.H2));
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void assertGetMySQLXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(DatabaseType.MySQL, createDataSourceMap(PoolType.DBCP2, DatabaseType.MySQL));
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void assertGetPostgreSQLXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(DatabaseType.PostgreSQL, createDataSourceMap(PoolType.DRUID, DatabaseType.PostgreSQL));
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void assertGetSQLServerXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(DatabaseType.SQLServer, createDataSourceMap(PoolType.HIKARI, DatabaseType.SQLServer));
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void assertGetTransactionDataSourceFailed() {
        XATransactionManager xaTransactionManager = mock(XATransactionManager.class);
        doThrow(ShardingException.class).when(xaTransactionManager).wrapDataSource((DatabaseType) any(), (XADataSource) any(), anyString(), (DataSourceParameter) any());
        ReflectiveUtil.setProperty(xaDataSourceMapConverter, "xaTransactionManager", xaTransactionManager);
        Map<String, DataSource> actualDataSourceMap = xaDataSourceMapConverter.convert(DatabaseType.SQLServer, createDataSourceMap(PoolType.HIKARI, DatabaseType.SQLServer));
        assertThat(actualDataSourceMap.size(), is(0));
    }
    
    private Map<String, DataSource> createDataSourceMap(final PoolType poolType, final DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", DataSourceUtils.build(poolType, databaseType, "demo_ds_1"));
        result.put("ds2", DataSourceUtils.build(poolType, databaseType, "demo_ds_2"));
        return result;
    }
}
