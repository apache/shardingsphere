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

package org.apache.shardingsphere.infra.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfigurationValidator;
import org.apache.shardingsphere.infra.config.datasource.InvalidDataSourceConfigurationException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public final class DataSourceConfigurationValidatorTest {
    
    @Test
    public void assertValidate() throws InvalidDataSourceConfigurationException {
        DataSourceConfigurationValidator dataSourceConfigurationValidator = new DataSourceConfigurationValidator();
        dataSourceConfigurationValidator.validate("name", createDataSourceConfiguration());
    }
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", "org.h2.Driver");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", "root");
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
}
