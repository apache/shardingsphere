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

package org.apache.shardingsphere.encrypt.rule.table;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLogicColumnNotFoundException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Encrypt table.
 */
public final class EncryptTable {
    
    @Getter
    private final String table;
    
    private final Map<String, EncryptColumn> columns;
    
    public EncryptTable(final EncryptTableRuleConfiguration config, final Map<String, EncryptAlgorithm> encryptors) {
        table = config.getName();
        columns = createEncryptColumns(config, encryptors);
    }
    
    private Map<String, EncryptColumn> createEncryptColumns(final EncryptTableRuleConfiguration config, final Map<String, EncryptAlgorithm> encryptors) {
        Map<String, EncryptColumn> result = new CaseInsensitiveMap<>();
        for (EncryptColumnRuleConfiguration each : config.getColumns()) {
            result.put(each.getName(), createEncryptColumn(each, encryptors));
        }
        return result;
    }
    
    private EncryptColumn createEncryptColumn(final EncryptColumnRuleConfiguration config, final Map<String, EncryptAlgorithm> encryptors) {
        CipherColumnItem cipherColumnItem = new CipherColumnItem(config.getCipher().getName(), encryptors.get(config.getCipher().getEncryptorName()));
        EncryptColumn result = new EncryptColumn(config.getName(), cipherColumnItem);
        if (config.getAssistedQuery().isPresent()) {
            AssistedQueryColumnItem assistedQueryColumn = new AssistedQueryColumnItem(config.getAssistedQuery().get().getName(), encryptors.get(config.getAssistedQuery().get().getEncryptorName()));
            result.setAssistedQuery(assistedQueryColumn);
        }
        if (config.getLikeQuery().isPresent()) {
            LikeQueryColumnItem likeQueryColumn = new LikeQueryColumnItem(config.getLikeQuery().get().getName(), encryptors.get(config.getLikeQuery().get().getEncryptorName()));
            result.setLikeQuery(likeQueryColumn);
        }
        return result;
    }
    
    /**
     * Find encryptor.
     *
     * @param logicColumnName logic column name
     * @return found encryptor
     */
    @HighFrequencyInvocation
    public Optional<EncryptAlgorithm> findEncryptor(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? Optional.of(columns.get(logicColumnName).getCipher().getEncryptor()) : Optional.empty();
    }
    
    /**
     * Is encrypt column or not.
     *
     * @param logicColumnName logic column name
     * @return encrypt column or not
     */
    @HighFrequencyInvocation
    public boolean isEncryptColumn(final String logicColumnName) {
        return columns.containsKey(logicColumnName);
    }
    
    /**
     * Get encrypt column.
     *
     * @param logicColumnName logic column name
     * @return encrypt column
     */
    @HighFrequencyInvocation
    public EncryptColumn getEncryptColumn(final String logicColumnName) {
        ShardingSpherePreconditions.checkState(isEncryptColumn(logicColumnName), () -> new EncryptColumnNotFoundException(table, logicColumnName));
        return columns.get(logicColumnName);
    }
    
    /**
     * Is cipher column or not.
     *
     * @param columnName column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String columnName) {
        return columns.values().stream().anyMatch(each -> each.getCipher().getName().equalsIgnoreCase(columnName));
    }
    
    /**
     * Get logic column by cipher column.
     *
     * @param cipherColumnName cipher column name
     * @return logic column name
     * @throws EncryptLogicColumnNotFoundException encrypt logic column not found exception
     */
    public String getLogicColumnByCipherColumn(final String cipherColumnName) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getCipher().getName().equalsIgnoreCase(cipherColumnName)) {
                return entry.getValue().getName();
            }
        }
        throw new EncryptLogicColumnNotFoundException(cipherColumnName);
    }
    
    /**
     * Get logic column by assisted query column.
     *
     * @param assistQueryColumnName assisted query column name
     * @return logic column name
     * @throws EncryptLogicColumnNotFoundException encrypt logic column not found exception
     */
    public String getLogicColumnByAssistedQueryColumn(final String assistQueryColumnName) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getAssistedQuery().isPresent() && entry.getValue().getAssistedQuery().get().getName().equalsIgnoreCase(assistQueryColumnName)) {
                return entry.getValue().getName();
            }
        }
        throw new EncryptLogicColumnNotFoundException(assistQueryColumnName);
    }
    
    /**
     * Is assisted query column or not.
     *
     * @param columnName column name
     * @return assisted query column or not
     */
    public boolean isAssistedQueryColumn(final String columnName) {
        return columns.values().stream().anyMatch(each -> columnName.equalsIgnoreCase(each.getAssistedQuery().map(AssistedQueryColumnItem::getName).orElse(null)));
    }
    
    /**
     * Is like query column or not.
     *
     * @param columnName column name
     * @return like query column or not
     */
    public boolean isLikeQueryColumn(final String columnName) {
        return columns.values().stream().anyMatch(each -> columnName.equalsIgnoreCase(each.getLikeQuery().map(LikeQueryColumnItem::getName).orElse(null)));
    }
    
    /**
     * Find query encryptor.
     *
     * @param columnName column name
     * @return query encryptor
     */
    @HighFrequencyInvocation
    public Optional<EncryptAlgorithm> findQueryEncryptor(final String columnName) {
        return isEncryptColumn(columnName) ? Optional.of(getEncryptColumn(columnName).getQueryEncryptor()) : Optional.empty();
    }
    
    /**
     * Whether derived column.
     *
     * @param columnName column name
     * @return is derived column or not
     */
    public boolean isDerivedColumn(final String columnName) {
        return isAssistedQueryColumn(columnName) || isLikeQueryColumn(columnName);
    }
}
