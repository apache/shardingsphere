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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

/**
 * Database meta data dialect handler.
 */
public interface DatabaseMetaDataDialectHandler extends TypedSPI {
    
    /**
     * Get schema.
     *
     * @param connection connection
     * @return schema schema
     */
    default String getSchema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (SQLException e) {
            return null;
        }
    }
    
    /**
     * Decorate table name pattern.
     *
     * @param tableNamePattern table name pattern
     * @return decorated table name pattern
     */
    default String decorate(String tableNamePattern) {
        return tableNamePattern;
    }
    
    /**
     * Get delimiter.
     *
     * @return delimiter
     */
    default Pair<String, String> getDelimiter() {
        return Pair.of("", "");
    }
}
