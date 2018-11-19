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

import com.atomikos.beans.PropertyException;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.transaction.ProxyPoolType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.convert.dialect.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.dialect.XADatabaseType;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import org.apache.tomcat.dbcp.dbcp2.managed.BasicManagedDataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XATransactionDataSourceWrapperTest {
    
    private XATransactionManager xaTransactionManager = new AtomikosTransactionManager();
    
    private final XADataSource xaDataSource = XADataSourceFactory.build(DatabaseType.MySQL);
    
    private final DataSourceParameter parameter = new DataSourceParameter();
    
    private final XATransactionDataSourceWrapper xaDataSourceWrapper = new XATransactionDataSourceWrapper(xaTransactionManager.getUnderlyingTransactionManager());
    
    @Before
    public void setup() {
        parameter.setUsername("root");
        parameter.setPassword("root");
        parameter.setUrl("db:url");
        parameter.setMaximumPoolSize(10);
    }
    
    @After
    public void teardown() {
        xaTransactionManager.destroy();
    }
    
    @Test
    public void assertWrapToAtomikosDataSourceBean() throws PropertyException {
        parameter.setProxyDatasourceType(ProxyPoolType.VENDOR);
        AtomikosDataSourceBean targetDataSource = (AtomikosDataSourceBean) xaDataSourceWrapper.wrap(xaDataSource, "ds1", parameter);
        assertThat(targetDataSource, Matchers.instanceOf(AtomikosDataSourceBean.class));
        assertThat(targetDataSource.getXaDataSource(), is(xaDataSource));
        assertThat(targetDataSource.getXaDataSourceClassName(), is(XADatabaseType.MySQL.getClassName()));
        assertThat(targetDataSource.getUniqueResourceName(), is("ds1"));
        assertThat(targetDataSource.getMaxPoolSize(), is(parameter.getMaximumPoolSize()));
        assertThat(targetDataSource.getXaProperties().get("user"), Is.<Object>is(parameter.getUsername()));
        assertThat(targetDataSource.getXaProperties().get("password"), Is.<Object>is(parameter.getPassword()));
        assertThat(targetDataSource.getXaProperties().get("URL"), Is.<Object>is(parameter.getUrl()));
    }
    
    @Test
    public void assertWrapToTomcatDBCP() throws PropertyException, IllegalAccessException {
        parameter.setProxyDatasourceType(ProxyPoolType.TOMCAT_DBCP2);
        BasicManagedDataSource targetDataSource = (BasicManagedDataSource) xaDataSourceWrapper.wrap(xaDataSource, "ds1", parameter);
        assertThat(targetDataSource, Matchers.instanceOf(BasicManagedDataSource.class));
        assertThat(targetDataSource.getXaDataSourceInstance(), is(xaDataSource));
        assertThat(targetDataSource.getXADataSource(), is(XADatabaseType.MySQL.getClassName()));
        assertThat(targetDataSource.getMaxTotal(), is(parameter.getMaximumPoolSize()));
        MatcherAssert.assertThat(ReflectiveUtil.getProperty(targetDataSource.getXaDataSourceInstance(), "user"), Is.<Object>is(parameter.getUsername()));
        MatcherAssert.assertThat(ReflectiveUtil.getProperty(targetDataSource.getXaDataSourceInstance(), "password"), Is.<Object>is(parameter.getPassword()));
        MatcherAssert.assertThat(ReflectiveUtil.getProperty(targetDataSource.getXaDataSourceInstance(), "url"), Is.<Object>is(parameter.getUrl()));
    }
}
