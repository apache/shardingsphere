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

package org.apache.shardingsphere.driver.jdbc.core.driver.reader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ConfigurationContentReaderTest {
    
    private static final String FIXTURE_JDBC_URL_KEY = "fixture.config.driver.jdbc-url";
    
    private static final String FIXTURE_USERNAME_KEY = "fixture.config.driver.username";
    
    @BeforeAll
    static void beforeAll() {
        System.setProperty(FIXTURE_JDBC_URL_KEY, "jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        System.setProperty(FIXTURE_USERNAME_KEY, "sa");
    }
    
    @AfterAll
    static void afterAll() {
        System.clearProperty(FIXTURE_JDBC_URL_KEY);
        System.clearProperty(FIXTURE_USERNAME_KEY);
    }
    
    @Test
    void assertReadWithSystemProperties() throws IOException {
        byte[] actual = readContent("config/driver/foo-driver-to-be-replaced-fixture.yaml");
        byte[] expected = readContent("config/driver/foo-driver-fixture.yaml");
        assertThat(actual, is(expected));
    }
    
    private byte[] readContent(final String name) throws IOException {
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(name)).getPath();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return ConfigurationContentReader.read(reader, ConfigurationContentReaderType.SYSTEM_PROPS);
        }
    }
}
