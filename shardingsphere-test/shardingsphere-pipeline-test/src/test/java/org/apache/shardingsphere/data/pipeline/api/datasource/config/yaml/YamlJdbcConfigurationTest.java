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

package org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlJdbcConfigurationTest {
    
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    @Test
    public void assertConstructionWithJdbcUrl() {
        Map<String, String> dataSourceProps = ImmutableMap.of("jdbcUrl", JDBC_URL, "username", USERNAME, "password", PASSWORD);
        verifyFields(YamlEngine.unmarshal(YamlEngine.marshal(dataSourceProps), YamlJdbcConfiguration.class));
    }
    
    @Test
    public void assertConstructionWithUrl() {
        Map<String, String> dataSourceProps = ImmutableMap.of("url", JDBC_URL, "username", USERNAME, "password", PASSWORD);
        verifyFields(YamlEngine.unmarshal(YamlEngine.marshal(dataSourceProps), YamlJdbcConfiguration.class));
    }
    
    private void verifyFields(final YamlJdbcConfiguration actual) {
        assertThat(actual.getJdbcUrl(), is(JDBC_URL));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
    }
}
