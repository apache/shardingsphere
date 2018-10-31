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

package io.shardingsphere.transaction.manager.xa.convert;

import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.manager.xa.fixture.DataSourceUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DataSourceParameterFactoryTest {
    
    @Test
    public void assertBuildParameterFromHikari() {
    }
    
    @Test
    public void assertBuildParameterFromDruid() {
    }
    
    @Test
    public void assertBuildParameterFromDBCPTomcat() {
        DataSourceParameter parameter = DataSourceParameterFactory.build(DataSourceUtils.build(PoolType.DBCP_TOMCAT));
        assertThat(parameter.getOriginPoolType(), is(PoolType.DBCP_TOMCAT));
        assertThat(parameter.getUrl(), is("jdbc:mysql://localhost:3306"));
        assertThat(parameter.getUsername(), is("root"));
        assertThat(parameter.getPassword(), is("root"));
        assertThat(parameter.getMaximumPoolSize(), is(10));
    }
    
    @Test
    public void assertBuildParameterFromDBCP2() {
    }
    
}
