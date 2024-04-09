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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * JDBC repository SQL Loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRepositorySQLLoader {
    
    private static final String ROOT_DIRECTORY = "sql";
    
    private static final String FILE_EXTENSION = ".xml";
    
    private static final ObjectMapper XML_MAPPER = XmlMapper.builder().build();
    
    /**
     * Load JDBC repository SQL.
     *
     * @param type type of JDBC repository SQL
     * @return loaded JDBC repository SQL
     */
    @SneakyThrows(IOException.class)
    public static JDBCRepositorySQL load(final String type) {
        JDBCRepositorySQL result = null;
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read(JDBCRepositorySQLLoader.class.getClassLoader(), ROOT_DIRECTORY)) {
            Iterable<String> resourceNameIterable = resourceNameStream::iterator;
            for (String each : resourceNameIterable) {
                if (!each.endsWith(FILE_EXTENSION)) {
                    continue;
                }
                JDBCRepositorySQL provider = XML_MAPPER.readValue(JDBCRepositorySQLLoader.class.getClassLoader().getResourceAsStream(each), JDBCRepositorySQL.class);
                if (provider.isDefault()) {
                    result = provider;
                }
                if (Objects.equals(provider.getType(), type)) {
                    result = provider;
                    break;
                }
            }
        }
        return result;
    }
}
