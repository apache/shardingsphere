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

package org.apache.shardingsphere.infra.datasource.props;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DataSourcePropertiesValidatorTest {
    
    @Test
    public void assertValidateSuccess() throws InvalidResourcesException {
        new DataSourcePropertiesValidator().validate(Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), createValidProperties())), new H2DatabaseType());
    }
    
    @Test(expected = InvalidResourcesException.class)
    public void assertDatabaseTypeInValidateFail() throws InvalidResourcesException {
        new DataSourcePropertiesValidator().validate(Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), createValidProperties())), new MySQLDatabaseType());
    }
    
    private Map<String, Object> createValidProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("driverClassName", "org.h2.Driver");
        result.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    @Test(expected = InvalidResourcesException.class)
    public void assertValidateFailed() throws InvalidResourcesException {
        new DataSourcePropertiesValidator().validate(Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), createInvalidProperties())), new H2DatabaseType());
    }
    
    private Map<String, Object> createInvalidProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("jdbcUrl", "InvalidJdbcUrl");
        return result;
    }
}
