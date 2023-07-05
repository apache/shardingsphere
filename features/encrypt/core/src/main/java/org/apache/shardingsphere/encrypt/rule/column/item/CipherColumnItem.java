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
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * Cipher column item.
 */
@RequiredArgsConstructor
@Getter
public final class CipherColumnItem {
    
    private final String name;
    
    @SuppressWarnings("rawtypes")
    private final StandardEncryptAlgorithm encryptor;
    
    /**
     * Encrypt.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param logicColumnName logic column name
     * @param originalValue original value
     * @return encrypted value
     */
    @SuppressWarnings("unchecked")
    public Object encrypt(final String databaseName, final String schemaName, final String tableName, final String logicColumnName, final Object originalValue) {
        if (null == originalValue) {
            return null;
        }
        EncryptContext context = EncryptContextBuilder.build(databaseName, schemaName, tableName, logicColumnName);
        return encryptor.encrypt(originalValue, context);
    }
    
    /**
     * Encrypt.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param logicColumnName logic column name
     * @param originalValues original values
     * @return encrypted values
     */
    @SuppressWarnings("unchecked")
    public List<Object> encrypt(final String databaseName, final String schemaName, final String tableName, final String logicColumnName, final List<Object> originalValues) {
        EncryptContext context = EncryptContextBuilder.build(databaseName, schemaName, tableName, logicColumnName);
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : encryptor.encrypt(each, context));
        }
        return result;
    }
    
    /**
     * Decrypt.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param logicColumnName logic column name
     * @param cipherValue cipher value
     * @return decrypted value
     */
    @SuppressWarnings("unchecked")
    public Object decrypt(final String databaseName, final String schemaName, final String tableName, final String logicColumnName, final Object cipherValue) {
        if (null == cipherValue) {
            return null;
        }
        EncryptContext context = EncryptContextBuilder.build(databaseName, schemaName, tableName, logicColumnName);
        return encryptor.decrypt(cipherValue, context);
    }
}
