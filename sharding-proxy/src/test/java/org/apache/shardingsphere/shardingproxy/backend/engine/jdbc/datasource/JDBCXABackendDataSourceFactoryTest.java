/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.backend.engine.jdbc.datasource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JDBCXABackendDataSourceFactoryTest {
    
    @Test
    public void assertBuild() throws Exception {
        YamlDataSourceParameter parameter = createDataSourceParameter();
        AtomikosDataSourceBean actual = (AtomikosDataSourceBean) JDBCXABackendDataSourceFactory.getInstance().build("ds1", parameter);
        assertThat(actual, Matchers.instanceOf(AtomikosDataSourceBean.class));
        assertThat(actual.getXaDataSource(), instanceOf(MysqlXADataSource.class));
        assertThat(actual.getUniqueResourceName(), is("ds1"));
        assertThat(actual.getMaxPoolSize(), is(parameter.getMaxPoolSize()));
        assertThat(actual.getXaProperties().get("user"), Is.<Object>is(parameter.getUsername()));
        assertThat(actual.getXaProperties().get("password"), Is.<Object>is(parameter.getPassword()));
        assertThat(actual.getXaProperties().get("URL"), Is.<Object>is(parameter.getUrl()));
    }
    
    private YamlDataSourceParameter createDataSourceParameter() {
        YamlDataSourceParameter result = new YamlDataSourceParameter();
        result.setUsername("root");
        result.setPassword("root");
        result.setUrl("jdbc:mysql://db.mysql:3306/test");
        result.setMaxPoolSize(10);
        return result;
    }
}
