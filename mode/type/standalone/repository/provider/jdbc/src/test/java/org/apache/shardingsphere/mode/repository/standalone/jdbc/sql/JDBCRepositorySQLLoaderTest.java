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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.sql;

import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

class JDBCRepositorySQLLoaderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSpecifiedTypes")
    void assertLoad(final String name, final String type) {
        JDBCRepositorySQL actual = JDBCRepositorySQLLoader.load(type);
        assertThat(actual.getType(), is(type));
    }
    
    @Test
    void assertLoadByDefault() {
        assertThat(JDBCRepositorySQLLoader.load("nonexistent").getType(), is("H2"));
    }
    
    @Test
    void assertLoadSQL() {
        JDBCRepositorySQL actual = JDBCRepositorySQLLoader.load("MySQL");
        assertThat(actual.getDriverClassName(), is("com.mysql.jdbc.Driver"));
        assertFalse(actual.isDefault());
        assertThat(actual.getCreateTableSQL(), is("CREATE TABLE IF NOT EXISTS `repository`(id varchar(36) PRIMARY KEY, `key` TEXT, `value` TEXT, parent TEXT)"));
        assertThat(actual.getSelectByKeySQL(), is("SELECT `value` FROM `repository` WHERE `key` = ?"));
        assertThat(actual.getSelectByParentKeySQL(), is("SELECT DISTINCT(`key`) FROM `repository` WHERE parent = ? ORDER BY `key` ASC"));
        assertThat(actual.getInsertSQL(), is("INSERT INTO `repository` VALUES(?, ?, ?, ?)"));
        assertThat(actual.getUpdateSQL(), is("UPDATE `repository` SET `value` = ? WHERE `key` = ?"));
        assertThat(actual.getDeleteSQL(), is("DELETE FROM `repository` WHERE `key` LIKE ?"));
    }
    
    @Test
    void assertLoadEscapedSQLWithMissingValues() {
        try (MockedStatic<ClasspathResourceDirectoryReader> mockedReader = mockStatic(ClasspathResourceDirectoryReader.class)) {
            mockedReader.when(() -> ClasspathResourceDirectoryReader.read(JDBCRepositorySQLLoader.class.getClassLoader(), "sql"))
                    .thenReturn(Stream.of("sql-test/jdbc-repository-sql.xml"));
            JDBCRepositorySQL actual = JDBCRepositorySQLLoader.load("foo_type");
            assertFalse(actual.isDefault());
            assertThat(actual.getSelectByKeySQL(), is("SELECT 中文 & value"));
            assertNull(actual.getDeleteSQL());
        }
    }
    
    @Test
    void assertRejectDTD() {
        assertLoadInvalidXML("sql-test/dtd.xml");
    }
    
    @Test
    void assertRejectUnexpectedRoot() {
        assertLoadInvalidXML("sql-test/unexpected-root.xml");
    }
    
    private void assertLoadInvalidXML(final String resourceName) {
        try (MockedStatic<ClasspathResourceDirectoryReader> mockedReader = mockStatic(ClasspathResourceDirectoryReader.class)) {
            mockedReader.when(() -> ClasspathResourceDirectoryReader.read(JDBCRepositorySQLLoader.class.getClassLoader(), "sql"))
                    .thenReturn(Stream.of(resourceName));
            assertThrows(IOException.class, () -> JDBCRepositorySQLLoader.load("foo_type"));
        }
    }
    
    private static Stream<Arguments> getSpecifiedTypes() {
        return Stream.of(Arguments.of("MySQL", "MySQL"), Arguments.of("HSQLDB", "HSQLDB"), Arguments.of("H2", "H2"));
    }
    
}
