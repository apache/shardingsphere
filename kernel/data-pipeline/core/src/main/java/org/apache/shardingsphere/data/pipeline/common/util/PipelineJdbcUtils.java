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

package org.apache.shardingsphere.data.pipeline.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Pipeline JDBC utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJdbcUtils {
    
    /**
     * Whether column is integer type.
     *
     * @param columnType column type, value of java.sql.Types
     * @return true or false
     */
    public static boolean isIntegerColumn(final int columnType) {
        return Types.INTEGER == columnType || Types.BIGINT == columnType || Types.SMALLINT == columnType || Types.TINYINT == columnType;
    }
    
    /**
     * Whether column is string column.
     *
     * @param columnType column type, value of java.sql.Types
     * @return true or false
     */
    public static boolean isStringColumn(final int columnType) {
        switch (columnType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Whether column is binary column.
     * <p>it doesn't include BLOB etc.</p>
     *
     * @param columnType column type, value of java.sql.Types
     * @return true or false
     */
    public static boolean isBinaryColumn(final int columnType) {
        switch (columnType) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Cancel statement.
     *
     * @param statement statement
     * @throws SQLWrapperException if cancelling statement failed
     */
    public static void cancelStatement(final Statement statement) throws SQLWrapperException {
        try {
            if (null == statement || statement.isClosed()) {
                return;
            }
            statement.cancel();
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
}
