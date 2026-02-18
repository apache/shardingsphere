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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generated value utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneratedValueUtils {
    
    /**
     * Get generated value.
     *
     * @param resultSet result set
     * @param generatedKeysColumnName generated keys column name
     * @param columnName column name
     * @return generated value
     * @throws SQLException SQL exception
     */
    public static Comparable<?> getGeneratedValue(final ResultSet resultSet, final String generatedKeysColumnName, final String columnName) throws SQLException {
        if (null != generatedKeysColumnName) {
            try {
                return (Comparable<?>) resultSet.getObject(generatedKeysColumnName);
            } catch (final SQLException ignored) {
            }
        }
        if (null != columnName && !columnName.equals(generatedKeysColumnName)) {
            try {
                return (Comparable<?>) resultSet.getObject(columnName);
            } catch (final SQLException ignored) {
            }
        }
        return (Comparable<?>) resultSet.getObject(1);
    }
}
