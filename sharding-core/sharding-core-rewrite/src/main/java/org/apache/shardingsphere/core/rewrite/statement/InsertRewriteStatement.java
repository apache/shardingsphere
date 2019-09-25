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

package org.apache.shardingsphere.core.rewrite.statement;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValue;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertOptimizedStatement;
import org.apache.shardingsphere.core.rewrite.encrypt.EncryptConditions;
import org.apache.shardingsphere.core.rewrite.statement.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.statement.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Iterator;

/**
 * Insert Statement for rewrite.
 *
 * @author zhangliang
 */
public final class InsertRewriteStatement extends RewriteStatement {
    
    private final GeneratedKey generatedKey;
    
    public InsertRewriteStatement(final InsertOptimizedStatement optimizedStatement, 
                                  final ShardingConditions shardingConditions, final EncryptConditions encryptConditions, final GeneratedKey generatedKey, final EncryptRule encryptRule) {
        super(optimizedStatement, shardingConditions, encryptConditions);
        this.generatedKey = generatedKey;
        processGeneratedKey(optimizedStatement);
        processEncrypt(optimizedStatement, encryptRule);
    }
    
    private void processGeneratedKey(final InsertOptimizedStatement optimizedStatement) {
        if (null != generatedKey && generatedKey.isGenerated()) {
            Iterator<Comparable<?>> generatedValues = generatedKey.getGeneratedValues().descendingIterator();
            for (InsertValue each : optimizedStatement.getInsertValues()) {
                each.appendValue(generatedValues.next(), ShardingDerivedColumnType.KEY_GEN);
            }
        }
    }
    
    private void processEncrypt(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule) {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return;
        }
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.findShardingEncryptor(tableName, each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertValues(optimizedStatement, encryptRule, shardingEncryptor.get(), tableName, each);
            }
        }
    }
    
    private void encryptInsertValues(final InsertOptimizedStatement optimizedStatement, final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                     final String tableName, final String encryptLogicColumnName) {
        int columnIndex = optimizedStatement.getColumnNames().indexOf(encryptLogicColumnName);
        for (InsertValue each : optimizedStatement.getInsertValues()) {
            encryptInsertValue(encryptRule, shardingEncryptor, tableName, columnIndex, each, encryptLogicColumnName);
        }
    }
    
    private void encryptInsertValue(final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                    final String tableName, final int columnIndex, final InsertValue insertValue, final String encryptLogicColumnName) {
        Object originalValue = insertValue.getValue(columnIndex);
        insertValue.setValue(columnIndex, shardingEncryptor.encrypt(originalValue));
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            insertValue.appendValue(((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(originalValue.toString()), EncryptDerivedColumnType.ENCRYPT);
        }
        if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            insertValue.appendValue(originalValue, EncryptDerivedColumnType.ENCRYPT);
        }
    }
    
    /**
     * Get generated key.
     *
     * @return generated key
     */
    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.fromNullable(generatedKey);
    }
}
