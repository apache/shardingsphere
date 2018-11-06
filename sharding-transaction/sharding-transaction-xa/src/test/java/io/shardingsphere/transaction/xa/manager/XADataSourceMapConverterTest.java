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

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XADataSourceMapConverterTest {
    
    private XADataSourceMapConverter xaDataSourceMapConverter = new XADataSourceMapConverter();
    
    @Test
    public void getMysqlXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(createDBCPDataSourceMap(), DatabaseType.MySQL);
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void getH2XATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(createDBCPDataSourceMap(), DatabaseType.H2);
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void getPGXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(createDBCPDataSourceMap(), DatabaseType.PostgreSQL);
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test
    public void getMSXATransactionalDataSourceSuccess() {
        Map<String, DataSource> xaDataSourceMap = xaDataSourceMapConverter.convert(createDBCPDataSourceMap(), DatabaseType.SQLServer);
        assertThat(xaDataSourceMap.size(), is(2));
        assertThat(xaDataSourceMap.get("ds1"), instanceOf(AtomikosDataSourceBean.class));
        assertThat(xaDataSourceMap.get("ds2"), instanceOf(AtomikosDataSourceBean.class));
    }
    
    private Map<String, DataSource> createDBCPDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", newBasicDataSource());
        result.put("ds2", newBasicDataSource());
        return result;
    }
    
    private BasicDataSource newBasicDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setUrl("jdbc:mysql://localhost:3306");
        result.setMaxTotal(10);
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
