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

package org.apache.shardingsphere.sqlfederation.spi;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;

import java.util.Optional;

/**
 * SQL federation column type convert.
 */
public interface SQLFederationColumnTypeConverter extends DatabaseTypedSPI {
    
    /**
     * Transforming the column results of a federated query.
     * 
     * @param columnValue column value
     * @return convert column value result
     */
    Optional<Object> convertColumnValue(Object columnValue);
    
    /**
     * Converting the column types of a federated query.
     * 
     * @param columnType column type
     * @return convert column type result
     */
    Optional<Integer> convertColumnType(Integer columnType);
}
