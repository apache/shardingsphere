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

package org.apache.shardingsphere.transaction.rule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.constant.TransactionOrder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transaction rule.
 */
@Getter
@Slf4j
public final class TransactionRule implements GlobalRule, AutoCloseable {
    
    private final TransactionRuleConfiguration configuration;
    
    private final TransactionType defaultType;
    
    private final String providerType;
    
    private final Properties props;
    
    @Getter(AccessLevel.NONE)
    private final AtomicReference<ShardingSphereTransactionManagerEngine> resource;
    
    private final RuleAttributes attributes;
    
    public TransactionRule(final TransactionRuleConfiguration ruleConfig, final Collection<ShardingSphereDatabase> databases) {
        configuration = ruleConfig;
        defaultType = TransactionType.valueOf(ruleConfig.getDefaultType().toUpperCase());
        providerType = ruleConfig.getProviderType();
        props = ruleConfig.getProps();
        resource = new AtomicReference<>(createTransactionManagerEngine(databases));
        attributes = new RuleAttributes();
    }
    
    private synchronized ShardingSphereTransactionManagerEngine createTransactionManagerEngine(final Collection<ShardingSphereDatabase> databases) {
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine(defaultType);
        if (databases.isEmpty()) {
            return result;
        }
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(databases.size(), 1F);
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(databases.size(), 1F);
        for (ShardingSphereDatabase each : databases) {
            each.getResourceMetaData().getStorageUnits().forEach((key, value) -> {
                databaseTypes.put(each.getName() + "." + key, value.getStorageType());
                dataSourceMap.put(each.getName() + "." + key, value.getDataSource());
            });
        }
        result.init(databaseTypes, dataSourceMap, providerType);
        return result;
    }
    
    /**
     * Get resource.
     *
     * @return got resource
     */
    public ShardingSphereTransactionManagerEngine getResource() {
        return resource.get();
    }
    
    /**
     * Judge whether to implicit commit transaction.
     *
     * @param sqlStatement sql statement
     * @param multiExecutionUnits is multiple execution units
     * @param connectionTransaction connection transaction
     * @param isAutoCommit is auto commit
     * @return is implicit commit transaction or not
     */
    public boolean isImplicitCommitTransaction(final SQLStatement sqlStatement, final boolean multiExecutionUnits, final ConnectionTransaction connectionTransaction, final boolean isAutoCommit) {
        if (!isAutoCommit) {
            return false;
        }
        if (!TransactionType.isDistributedTransaction(defaultType) || connectionTransaction.isInDistributedTransaction()) {
            return false;
        }
        return isWriteDMLStatement(sqlStatement) && multiExecutionUnits;
    }
    
    private boolean isWriteDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement);
    }
    
    @Override
    public void refresh(final Collection<ShardingSphereDatabase> databases, final GlobalRuleChangedType changedType) {
        if (GlobalRuleChangedType.DATABASE_CHANGED != changedType) {
            return;
        }
        ShardingSphereTransactionManagerEngine previousEngine = resource.get();
        close(previousEngine);
        resource.set(createTransactionManagerEngine(databases));
    }
    
    @Override
    public void close() {
        // TODO Consider shutting down the transaction manager gracefully
        ShardingSphereTransactionManagerEngine engine = resource.get();
        if (null != engine) {
            resource.set(null);
            close(engine);
        }
    }
    
    private void close(final ShardingSphereTransactionManagerEngine engine) {
        try {
            engine.close();
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Close transaction engine failed.", ex);
        }
    }
    
    @Override
    public int getOrder() {
        return TransactionOrder.ORDER;
    }
}
