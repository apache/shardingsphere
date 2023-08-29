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

package org.apache.shardingsphere.encrypt.rule;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLogicColumnNotFoundException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Encrypt table.
 */
public final class EncryptTable {
    
    @Getter
    private final String table;
    
    private final Map<String, EncryptColumn> columns;
    
    @SuppressWarnings("rawtypes")
    public EncryptTable(final EncryptTableRuleConfiguration config, final Map<String, StandardEncryptAlgorithm> standardEncryptors,
                        final Map<String, AssistedEncryptAlgorithm> assistedEncryptors, final Map<String, LikeEncryptAlgorithm> likeEncryptors) {
        table = config.getName();
        columns = createEncryptColumns(config, standardEncryptors, assistedEncryptors, likeEncryptors);
    }
    
    @SuppressWarnings("rawtypes")
    private Map<String, EncryptColumn> createEncryptColumns(final EncryptTableRuleConfiguration config, final Map<String, StandardEncryptAlgorithm> standardEncryptors,
                                                            final Map<String, AssistedEncryptAlgorithm> assistedEncryptors, final Map<String, LikeEncryptAlgorithm> likeEncryptors) {
        Map<String, EncryptColumn> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (EncryptColumnRuleConfiguration each : config.getColumns()) {
            result.put(each.getName(), createEncryptColumn(each, standardEncryptors, assistedEncryptors, likeEncryptors));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private EncryptColumn createEncryptColumn(final EncryptColumnRuleConfiguration config, final Map<String, StandardEncryptAlgorithm> standardEncryptors,
                                              final Map<String, AssistedEncryptAlgorithm> assistedEncryptors, final Map<String, LikeEncryptAlgorithm> likeEncryptors) {
        EncryptColumn result = new EncryptColumn(config.getName(), new CipherColumnItem(config.getCipher().getName(), standardEncryptors.get(config.getCipher().getEncryptorName())));
        if (config.getAssistedQuery().isPresent()) {
            result.setAssistedQuery(new AssistedQueryColumnItem(config.getAssistedQuery().get().getName(), assistedEncryptors.get(config.getAssistedQuery().get().getEncryptorName())));
        }
        if (config.getLikeQuery().isPresent()) {
            result.setLikeQuery(new LikeQueryColumnItem(config.getLikeQuery().get().getName(), likeEncryptors.get(config.getLikeQuery().get().getEncryptorName())));
        }
        return result;
    }
    
    /**
     * Find encryptor.
     *
     * @param logicColumnName logic column name
     * @return found encryptor
     */
    public Optional<StandardEncryptAlgorithm<?, ?>> findEncryptor(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? Optional.of((StandardEncryptAlgorithm<?, ?>) columns.get(logicColumnName).getCipher().getEncryptor()) : Optional.empty();
    }
    
    /**
     * Get logic columns.
     *
     * @return logic column names
     */
    public Collection<String> getLogicColumns() {
        return columns.keySet();
    }
    
    /**
     * Is encrypt column or not.
     *
     * @param logicColumnName logic column name
     * @return encrypt column or not
     */
    public boolean isEncryptColumn(final String logicColumnName) {
        return columns.containsKey(logicColumnName);
    }
    
    /**
     * Get encrypt column.
     *
     * @param logicColumnName logic column name
     * @return encrypt column
     */
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
                return entry.getKey();
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
                return entry.getKey();
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
}
