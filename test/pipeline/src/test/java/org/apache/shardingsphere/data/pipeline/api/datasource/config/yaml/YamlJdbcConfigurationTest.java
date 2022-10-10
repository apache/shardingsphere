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

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlJdbcConfigurationTest {
    
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    @Test
    public void assertConstructionWithUrl() {
        assertYamlJdbcConfiguration(YamlEngine.unmarshal(YamlEngine.marshal(getDataSourcePropsWithUrl()), YamlJdbcConfiguration.class));
    }
    
    private Map<String, String> getDataSourcePropsWithUrl() {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put("url", JDBC_URL);
        result.put("username", USERNAME);
        result.put("password", PASSWORD);
        return result;
    }
    
    private void assertYamlJdbcConfiguration(final YamlJdbcConfiguration actual) {
        assertThat(actual.getUrl(), is(JDBC_URL));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
    }
}
