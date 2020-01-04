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

package org.apache.shardingsphere.shardingproxy.backend.schema.impl;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.log.ConfigurationLogger;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializerEntry;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.metadata.decorator.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import org.apache.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import org.apache.shardingsphere.sharding.execute.metadata.loader.ShardingTableMetaDataLoader;
import org.apache.shardingsphere.shardingproxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.backend.schema.ProxyConnectionManager;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.TableMetaDataInitializer;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding schema.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 * @author sunbufu
 */
@Getter
public final class ShardingSchema extends LogicSchema {
    
    private ShardingRule shardingRule;
    
    private final ShardingSphereMetaData metaData;
    
    public ShardingSchema(
            final String name, final Map<String, YamlDataSourceParameter> dataSources, final ShardingRuleConfiguration shardingRuleConfig, final boolean isUsingRegistry) throws SQLException {
        super(name, dataSources);
        shardingRule = createShardingRule(shardingRuleConfig, dataSources.keySet(), isUsingRegistry);
        metaData = createMetaData();
    }
    
    private ShardingRule createShardingRule(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> dataSourceNames, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationShardingRule(shardingRuleConfig, dataSourceNames) : new ShardingRule(shardingRuleConfig, dataSourceNames);
    }
    
    private ShardingSphereMetaData createMetaData() throws SQLException {
        DataSourceMetas dataSourceMetas = new DataSourceMetas(LogicSchemas.getInstance().getDatabaseType(), getDatabaseAccessConfigurationMap());
        TableMetas tableMetas = createTableMetaDataInitializerEntry(dataSourceMetas).initAll();
        return new ShardingSphereMetaData(dataSourceMetas, tableMetas);
    }
    
    /**
     * Renew sharding rule.
     *
     * @param shardingRuleChangedEvent sharding rule changed event.
     */
    @Subscribe
    public synchronized void renew(final ShardingRuleChangedEvent shardingRuleChangedEvent) {
        if (getName().equals(shardingRuleChangedEvent.getShardingSchemaName())) {
            ConfigurationLogger.log(shardingRuleChangedEvent.getShardingRuleConfiguration());
            shardingRule = new OrchestrationShardingRule(shardingRuleChangedEvent.getShardingRuleConfiguration(), getDataSources().keySet());
        }
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationShardingSchema shardingSchema = disabledStateChangedEvent.getShardingSchema();
        if (getName().equals(shardingSchema.getSchemaName())) {
            for (MasterSlaveRule each : shardingRule.getMasterSlaveRules()) {
                ((OrchestrationMasterSlaveRule) each).updateDisabledDataSourceNames(shardingSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled());
            }
        }
    }
    
    @Override
    public void refreshTableMetaData(final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        if (sqlStatementContext.getSqlStatement() instanceof CreateTableStatement) {
            refreshTableMetaDataForCreateTable(sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof AlterTableStatement) {
            refreshTableMetaDataForAlterTable(sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof DropTableStatement) {
            refreshTableMetaDataForDropTable(sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof CreateIndexStatement) {
            refreshTableMetaDataForCreateIndex(sqlStatementContext);
        } else if (sqlStatementContext.getSqlStatement() instanceof DropIndexStatement) {
            refreshTableMetaDataForDropIndex(sqlStatementContext);
        }
    }
    
    private void refreshTableMetaDataForCreateTable(final SQLStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        getMetaData().getTables().put(tableName, createTableMetaDataInitializerEntry(metaData.getDataSources()).init(tableName));
    }
    
    private void refreshTableMetaDataForAlterTable(final SQLStatementContext sqlStatementContext) throws SQLException {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        getMetaData().getTables().put(tableName, createTableMetaDataInitializerEntry(metaData.getDataSources()).init(tableName));
    }
    
    private void refreshTableMetaDataForDropTable(final SQLStatementContext sqlStatementContext) {
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            getMetaData().getTables().remove(each);
        }
    }
    
    private void refreshTableMetaDataForCreateIndex(final SQLStatementContext sqlStatementContext) {
        CreateIndexStatement createIndexStatement = (CreateIndexStatement) sqlStatementContext.getSqlStatement();
        if (null != createIndexStatement.getIndex()) {
            getMetaData().getTables().get(sqlStatementContext.getTablesContext().getSingleTableName()).getIndexes().add(createIndexStatement.getIndex().getName());
        }
    }
    
    private void refreshTableMetaDataForDropIndex(final SQLStatementContext sqlStatementContext) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) sqlStatementContext.getSqlStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        if (!sqlStatementContext.getTablesContext().isEmpty()) {
            getMetaData().getTables().get(sqlStatementContext.getTablesContext().getSingleTableName()).getIndexes().removeAll(indexNames);
        }
        for (String each : indexNames) {
            Optional<String> logicTableName = findLogicTableName(getMetaData().getTables(), each);
            if (logicTableName.isPresent()) {
                getMetaData().getTables().get(sqlStatementContext.getTablesContext().getSingleTableName()).getIndexes().remove(each);
            }
        }
    }
    
    private TableMetaDataInitializerEntry createTableMetaDataInitializerEntry(final DataSourceMetas dataSourceMetas) {
        ShardingSphereProperties properties = ShardingProxyContext.getInstance().getProperties();
        Map<BaseRule, TableMetaDataInitializer> tableMetaDataInitializes = new HashMap<>(2, 1);
        tableMetaDataInitializes.put(shardingRule, 
                new ShardingTableMetaDataLoader(dataSourceMetas, BackendExecutorContext.getInstance().getExecutorEngine(), new ProxyConnectionManager(getBackendDataSource()), 
                properties.<Integer>getValue(PropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY), properties.<Boolean>getValue(PropertiesConstant.CHECK_TABLE_METADATA_ENABLED)));
        if (!shardingRule.getEncryptRule().getEncryptTableNames().isEmpty()) {
            tableMetaDataInitializes.put(shardingRule.getEncryptRule(), new EncryptTableMetaDataDecorator());
        }
        return new TableMetaDataInitializerEntry(tableMetaDataInitializes);
    }
    
    private Collection<String> getIndexNames(final DropIndexStatement dropIndexStatement) {
        Collection<String> result = new LinkedList<>();
        for (IndexSegment each : dropIndexStatement.getIndexes()) {
            result.add(each.getName());
        }
        return result;
    }
    
    private Optional<String> findLogicTableName(final TableMetas tableMetas, final String logicIndexName) {
        for (String each : tableMetas.getAllTableNames()) {
            if (tableMetas.get(each).containsIndex(logicIndexName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
