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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.large.sqlbuilder;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;

import java.util.List;

public interface IntPkLargeOrderSQLBuilder extends DatabaseTypedSPI {
    
    /**
     * Build create table SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return create table SQL
     */
    String buildCreateTableSQL(String qualifiedTableName);
    
    /**
     * Build prepared insert SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return prepared insert SQL
     */
    String buildPreparedInsertSQL(String qualifiedTableName);
    
    /**
     * Generate insert data.
     *
     * @param keyGenerateAlgorithm key generate algorithm
     * @param recordCount record count
     * @return insert data
     */
    List<Object[]> generateInsertData(KeyGenerateAlgorithm keyGenerateAlgorithm, int recordCount);
    
    /**
     * Build prepared simple insert SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return prepared simple insert SQL
     */
    default String buildPreparedSimpleInsertSQL(final String qualifiedTableName) {
        return "INSERT INTO " + qualifiedTableName + " (order_id, user_id, status) VALUES (?, ?, ?)";
    }
}
