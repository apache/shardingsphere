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

package org.apache.shardingsphere.proxy.config;

import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameterMerger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlDataSourceParameterMergerTest {
    
    @Test
    public void assertMergedParameterAll() {
        YamlDataSourceParameter parameterAll = new YamlDataSourceParameter();
        Map<String, Object> commonProps = generateCommonProps();
        parameterAll.setUrl("jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false");
        YamlDataSourceParameterMerger.merged(parameterAll, commonProps);
        
        assertThat(parameterAll.getUsername(), is("root"));
        assertThat(parameterAll.getPassword(), is("123456"));
        assertThat(parameterAll.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(parameterAll.getMaxLifetimeMilliseconds(), is(30000L));
        assertThat(parameterAll.getMaxPoolSize(), is(30));
        assertThat(parameterAll.getMinPoolSize(), is(1));
        assertThat(parameterAll.isReadOnly(), is(false));
        assertThat(parameterAll.getMaintenanceIntervalMilliseconds(), is(0L));
    }
    
    @Test
    public void assertParameterPartMerged() {
        Map<String, Object> commonProps =  generateCommonProps();
        YamlDataSourceParameter parameterPartMerged = generateYamlDataSourceParameter();
        YamlDataSourceParameterMerger.merged(parameterPartMerged, commonProps);

        assertThat(parameterPartMerged.getUsername(), is("sharding"));
        assertThat(parameterPartMerged.getPassword(), is("admin"));
        assertThat(parameterPartMerged.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(parameterPartMerged.getMaxLifetimeMilliseconds(), is(30000L));
        assertThat(parameterPartMerged.getMaxPoolSize(), is(100));
        assertThat(parameterPartMerged.getMinPoolSize(), is(10));
        assertThat(parameterPartMerged.isReadOnly(), is(true));
        assertThat(parameterPartMerged.getMaintenanceIntervalMilliseconds(), is(0L));
    }
    
    private Map<String, Object> generateCommonProps() {
        Map<String, Object> commonProps = new HashMap<>();
        commonProps.put("username", "root");
        commonProps.put("password", "123456");
        commonProps.put("connectionTimeoutMilliseconds", 30000L);
        commonProps.put("maxLifetimeMilliseconds", 30000L);
        commonProps.put("maxPoolSize", 30);
        commonProps.put("minPoolSize", 1);
        commonProps.put("readOnly", false);
        return generateCommonProps();
    }
    
    private YamlDataSourceParameter generateYamlDataSourceParameter() {
        YamlDataSourceParameter parameterPartMerged = new YamlDataSourceParameter();
        parameterPartMerged.setUrl("jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false");
        parameterPartMerged.setUsername("sharding");
        parameterPartMerged.setPassword("admin");
        parameterPartMerged.setMaxPoolSize(100);
        parameterPartMerged.setMinPoolSize(10);
        parameterPartMerged.setReadOnly(true);
        return parameterPartMerged;
    }
}
