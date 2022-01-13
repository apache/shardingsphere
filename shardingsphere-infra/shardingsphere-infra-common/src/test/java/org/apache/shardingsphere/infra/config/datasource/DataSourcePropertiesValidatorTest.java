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

package org.apache.shardingsphere.infra.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DataSourcePropertiesValidatorTest {
    
    @Test
    public void assertValidateSuccess() throws InvalidResourcesException {
        DataSourcePropertiesValidator validator = new DataSourcePropertiesValidator();
        validator.validate(Collections.singletonMap("name", createValidDataSourceProperties()));
    }
    
    private DataSourceProperties createValidDataSourceProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("driverClassName", MockedDataSource.class.getCanonicalName());
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", "root");
        DataSourceProperties result = new DataSourceProperties(HikariDataSource.class.getName());
        result.getProperties().putAll(props);
        return result;
    }
    
    @Test(expected = InvalidResourcesException.class)
    public void assertValidateFailed() throws InvalidResourcesException {
        DataSourcePropertiesValidator validator = new DataSourcePropertiesValidator();
        validator.validate(Collections.singletonMap("name", createInvalidDataSourceProperties()));
    }
    
    private DataSourceProperties createInvalidDataSourceProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("driverClassName", "InvalidDriver");
        DataSourceProperties result = new DataSourceProperties(HikariDataSource.class.getName());
        result.getProperties().putAll(props);
        return result;
    }
}
