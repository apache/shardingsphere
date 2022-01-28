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

package org.apache.shardingsphere.encrypt.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;

/**
 * Encrypt context builder.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class EncryptContextBuilder {
    
    /**
     * Build encrypt context.
     * 
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @param encryptRule encrypt rule
     * @return encrypt context
     */
    public static EncryptContext build(final String schemaName, final String tableName, final String columnName, final EncryptRule encryptRule) {
        EncryptContext result = new EncryptContext(schemaName, tableName, columnName);
        encryptRule.findEncryptTable(tableName).flatMap(optional -> optional.findEncryptColumn(columnName)).ifPresent(optional -> setEncryptDataType(result, optional));
        return result;
    }
    
    private static void setEncryptDataType(final EncryptContext result, final EncryptColumn encryptColumn) {
        result.setLogicDataType(encryptColumn.getLogicDataType());
        result.setPlainDataType(encryptColumn.getPlainDataType());
        result.setCipherDataType(encryptColumn.getCipherDataType());
        result.setAssistedQueryDataType(encryptColumn.getAssistedQueryDataType());
    }
}
