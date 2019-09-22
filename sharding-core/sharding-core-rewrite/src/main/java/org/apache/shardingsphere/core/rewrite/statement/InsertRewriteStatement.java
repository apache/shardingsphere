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
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.encrypt.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.optimize.sharding.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
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
    
    public InsertRewriteStatement(final InsertOptimizedStatement optimizedStatement, 
                                  final ShardingConditions shardingConditions, final EncryptConditions encryptConditions, final EncryptRule encryptRule) {
        super(optimizedStatement, shardingConditions, encryptConditions);
        if (optimizedStatement instanceof ShardingInsertOptimizedStatement) {
            processGeneratedKey((ShardingInsertOptimizedStatement) optimizedStatement);
        }
        processEncrypt(optimizedStatement, encryptRule);
    }
    
    private void processGeneratedKey(final ShardingInsertOptimizedStatement optimizedStatement) {
        Optional<GeneratedKey> generatedKey = optimizedStatement.getGeneratedKey();
        if (generatedKey.isPresent() && generatedKey.get().isGenerated()) {
            Iterator<Comparable<?>> generatedValues = generatedKey.get().getGeneratedValues().descendingIterator();
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
}
