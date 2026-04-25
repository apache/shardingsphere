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

package org.apache.shardingsphere.mcp.jdbc;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeDatabaseConfigurationTest {
    
    @TempDir
    private Path tempDir;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDatabaseTypeArguments")
    void assertConstructWithInvalidDatabaseType(final String name, final String databaseType, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new RuntimeDatabaseConfiguration(databaseType, "jdbc:h2:mem:logic", "", "", ""));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @Test
    void assertOpenConnectionWithoutDriverClassName() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "connection-factory");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        try (Connection actual = new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "").openConnection("logic_db")) {
            assertFalse(actual.isClosed());
        }
    }
    
    @Test
    void assertOpenConnectionWithDriverClassNameAndCredentials() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "credentialed-connection");
        try (Connection actual = new RuntimeDatabaseConfiguration("H2", jdbcUrl, "sa", "pwd", "org.h2.Driver").openConnection("logic_db")) {
            assertFalse(actual.isClosed());
        }
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertOpenConnectionWithUnavailableDriverClassName() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:missing-driver", "", "", "org.example.MissingDriver").openConnection("logic_db"));
        assertThat(actual.getMessage(), is("JDBC driver `org.example.MissingDriver` is not available for database `logic_db`."));
    }
    
    private static Stream<Arguments> invalidDatabaseTypeArguments() {
        return Stream.of(
                Arguments.of("null database type", null, "databaseType cannot be null."),
                Arguments.of("empty database type", "", "databaseType cannot be empty."),
                Arguments.of("blank database type", "   ", "databaseType cannot be empty."));
    }
}
