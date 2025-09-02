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

package org.apache.shardingsphere.sqlfederation.resultset.converter;

import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

/**
 * SQL federation column type converter.
 */
@SingletonSPI
public interface SQLFederationColumnTypeConverter extends DatabaseTypedSPI {
    
    /**
     * Convert column value.
     *
     * @param columnValue column value
     * @return converted column value
     */
    default Object convertColumnValue(Object columnValue) {
        return columnValue;
    }
    
    /**
     * Convert column type.
     *
     * @param sqlTypeName column type
     * @return converted column type
     */
    default int convertColumnType(SqlTypeName sqlTypeName) {
        return sqlTypeName.getJdbcOrdinal();
    }
}
