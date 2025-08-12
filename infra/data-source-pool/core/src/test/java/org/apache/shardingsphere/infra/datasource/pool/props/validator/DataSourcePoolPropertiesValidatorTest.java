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

package org.apache.shardingsphere.infra.datasource.pool.props.validator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourcePoolPropertiesValidatorTest {
    
    @BeforeAll
    static void setUp() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @Test
    void assertValidate() {
        assertTrue(DataSourcePoolPropertiesValidator.validate(
                Collections.singletonMap("name", new DataSourcePoolProperties(HikariDataSource.class.getName(), Collections.singletonMap("jdbcUrl", "jdbc:mock"))), Collections.emptySet()).isEmpty());
    }
}
