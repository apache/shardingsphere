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
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.convert.datasource.XADataSourceFactory;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomikosDataSourceBeanWrapperTest {
    
    private final XADataSource xaDataSource = XADataSourceFactory.build(DatabaseType.MySQL);
    
    private final DataSourceParameter parameter = new DataSourceParameter();
    
    @Before
    public void setUp() {
        parameter.setUsername("root");
        parameter.setPassword("root");
        parameter.setUrl("db:url");
        parameter.setMaxPoolSize(10);
    }
    
    @Test
    public void assertWrapToAtomikosDataSourceBean() throws PropertyException {
        AtomikosDataSourceBeanWrapper atomikosDataSourceBeanWrapper = new AtomikosDataSourceBeanWrapper();
        AtomikosDataSourceBean targetDataSource = (AtomikosDataSourceBean) atomikosDataSourceBeanWrapper.wrap(DatabaseType.MySQL, xaDataSource, "ds1", parameter);
        assertThat(targetDataSource, Matchers.instanceOf(AtomikosDataSourceBean.class));
        assertThat(targetDataSource.getXaDataSource(), is(xaDataSource));
        assertThat(targetDataSource.getUniqueResourceName(), is("ds1"));
        assertThat(targetDataSource.getMaxPoolSize(), is(parameter.getMaxPoolSize()));
        assertThat(targetDataSource.getXaProperties().get("user"), Is.<Object>is(parameter.getUsername()));
        assertThat(targetDataSource.getXaProperties().get("password"), Is.<Object>is(parameter.getPassword()));
        assertThat(targetDataSource.getXaProperties().get("URL"), Is.<Object>is(parameter.getUrl()));
    }
}
