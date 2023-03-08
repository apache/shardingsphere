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
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DataSourcePropertiesValidatorTest {
    
    @Test
    public void assertValidateSuccess() {
        assertTrue(new DataSourcePropertiesValidator().validate(Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), createValidProperties()))).isEmpty());
    }
    
    private Map<String, Object> createValidProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("driverClassName", "org.h2.Driver");
        result.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    @Test
    public void assertValidateFailed() {
        Collection<String> actual = new DataSourcePropertiesValidator().validate(
                Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), createInvalidProperties())));
        assertThat(actual, is(Collections.singletonList("Invalid data source `name`, error message is: The URL `InvalidJdbcUrl` is not recognized, please refer to the pattern `jdbc:.*`.")));
    }
    
    private Map<String, Object> createInvalidProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("jdbcUrl", "InvalidJdbcUrl");
        return result;
    }
}
