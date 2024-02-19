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

package org.apache.shardingsphere.encrypt.rule.column.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Like query column item.
 */
@RequiredArgsConstructor
public final class LikeQueryColumnItem {
    
    @Getter
    private final String name;
    
    private final EncryptAlgorithm encryptor;
    
    /**
     * Get encrypt like query value.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param logicColumnName logic column name
     * @param originalValue original value
     * @return like query values
     */
    public Object encrypt(final String databaseName, final String schemaName, final String tableName, final String logicColumnName, final Object originalValue) {
        if (null == originalValue) {
            return null;
        }
        return encryptor.encrypt(originalValue, new AlgorithmSQLContext(databaseName, schemaName, tableName, logicColumnName));
    }
    
    /**
     * Get encrypt like query values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param logicColumnName logic column name
     * @param originalValues original values
     * @return like query values
     */
    public List<Object> encrypt(final String databaseName, final String schemaName, final String tableName, final String logicColumnName, final List<Object> originalValues) {
        AlgorithmSQLContext algorithmSQLContext = new AlgorithmSQLContext(databaseName, schemaName, tableName, logicColumnName);
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : encryptor.encrypt(each, algorithmSQLContext));
        }
        return result;
    }
}
