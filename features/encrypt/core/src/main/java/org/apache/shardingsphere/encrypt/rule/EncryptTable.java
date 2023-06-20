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
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.LinkedList;
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
        EncryptColumn result = new EncryptColumn(config.getName(),
                new EncryptColumnItem<StandardEncryptAlgorithm<?, ?>>(config.getCipher().getName(), standardEncryptors.get(config.getCipher().getEncryptorName())));
        if (config.getAssistedQuery().isPresent()) {
            result.setAssistedQuery(
                    new EncryptColumnItem<AssistedEncryptAlgorithm<?, ?>>(config.getAssistedQuery().get().getName(), assistedEncryptors.get(config.getAssistedQuery().get().getEncryptorName())));
        }
        if (config.getLikeQuery().isPresent()) {
            result.setLikeQuery(new EncryptColumnItem<LikeEncryptAlgorithm<?, ?>>(config.getLikeQuery().get().getName(), likeEncryptors.get(config.getLikeQuery().get().getEncryptorName())));
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
     * Find assisted query encryptor.
     *
     * @param logicColumnName logic column name
     * @return found assisted query encryptor
     */
    public Optional<AssistedEncryptAlgorithm<?, ?>> findAssistedQueryEncryptor(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? columns.get(logicColumnName).getAssistedQuery().map(optional -> (AssistedEncryptAlgorithm<?, ?>) optional.getEncryptor()) : Optional.empty();
    }
    
    /**
     * Find like query encryptor.
     *
     * @param logicColumnName logic column name
     * @return found like query encryptor
     */
    public Optional<LikeEncryptAlgorithm<?, ?>> findLikeQueryEncryptor(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? columns.get(logicColumnName).getLikeQuery().map(optional -> (LikeEncryptAlgorithm<?, ?>) optional.getEncryptor()) : Optional.empty();
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
     * Get logic column by cipher column.
     * 
     * @param cipherColumnName cipher column name
     * @return logic column
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
     * Is cipher column or not.
     *
     * @param logicColumnName logic column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String logicColumnName) {
        return columns.values().stream().anyMatch(each -> each.getCipher().getName().equalsIgnoreCase(logicColumnName));
    }
    
    /**
     * Get cipher column.
     *
     * @param logicColumnName logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicColumnName) {
        return columns.get(logicColumnName).getCipher().getName();
    }
    
    /**
     * Get assisted query columns.
     *
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getAssistedQuery().isPresent()) {
                result.add(each.getAssistedQuery().get().getName());
            }
        }
        return result;
    }
    
    /**
     * Get like query columns.
     *
     * @return like query columns
     */
    public Collection<String> getLikeQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getLikeQuery().isPresent()) {
                result.add(each.getLikeQuery().get().getName());
            }
        }
        return result;
    }
    
    /**
     * Find assisted query column.
     *
     * @param logicColumnName logic column name
     * @return assisted query column
     */
    public Optional<String> findAssistedQueryColumn(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? columns.get(logicColumnName).getAssistedQuery().map(EncryptColumnItem::getName) : Optional.empty();
    }
    
    /**
     * Find like query column.
     *
     * @param logicColumnName logic column name
     * @return like query column
     */
    public Optional<String> findLikeQueryColumn(final String logicColumnName) {
        return columns.containsKey(logicColumnName) ? columns.get(logicColumnName).getLikeQuery().map(EncryptColumnItem::getName) : Optional.empty();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns() {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCipher().getName());
        }
        return result;
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
}
