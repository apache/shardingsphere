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
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.ConfigurationLogger;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import org.apache.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;

import java.sql.SQLException;
import java.util.Collection;
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
        DataSourceMetas dataSourceMetas = new DataSourceMetas(getDataSourceURLs(getDataSources()), LogicSchemas.getInstance().getDatabaseType());
        TableMetas tableMetas = new TableMetas(getTableMetaDataInitializer(dataSourceMetas).load(shardingRule));
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
    public void refreshTableMetaData(final OptimizedStatement optimizedStatement) throws SQLException {
        if (null == optimizedStatement) {
            return;
        }
        if (optimizedStatement.getSQLStatement() instanceof CreateTableStatement) {
            refreshTableMetaDataForCreateTable(optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof AlterTableStatement) {
            refreshTableMetaDataForAlterTable(optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropTableStatement) {
            refreshTableMetaDataForDropTable(optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof CreateIndexStatement) {
            refreshTableMetaDataForCreateIndex(optimizedStatement);
        } else if (optimizedStatement.getSQLStatement() instanceof DropIndexStatement) {
            refreshTableMetaDataForDropIndex(optimizedStatement);
        }
    }
    
    private void refreshTableMetaDataForCreateTable(final OptimizedStatement optimizedStatement) throws SQLException {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        getMetaData().getTables().put(tableName, getTableMetaDataInitializer(metaData.getDataSources()).load(tableName, shardingRule));
    }
    
    private void refreshTableMetaDataForAlterTable(final OptimizedStatement optimizedStatement) throws SQLException {
        String tableName = optimizedStatement.getTables().getSingleTableName();
        getMetaData().getTables().put(tableName, getTableMetaDataInitializer(metaData.getDataSources()).load(tableName, shardingRule));
    }
    
    private void refreshTableMetaDataForDropTable(final OptimizedStatement optimizedStatement) {
        for (String each : optimizedStatement.getTables().getTableNames()) {
            getMetaData().getTables().remove(each);
        }
    }
    
    private void refreshTableMetaDataForCreateIndex(final OptimizedStatement optimizedStatement) {
        CreateIndexStatement createIndexStatement = (CreateIndexStatement) optimizedStatement.getSQLStatement();
        if (null != createIndexStatement.getIndex()) {
            getMetaData().getTables().get(optimizedStatement.getTables().getSingleTableName()).getIndexes().add(createIndexStatement.getIndex().getName());
        }
    }
    
    private void refreshTableMetaDataForDropIndex(final OptimizedStatement optimizedStatement) {
        DropIndexStatement dropIndexStatement = (DropIndexStatement) optimizedStatement.getSQLStatement();
        Collection<String> indexNames = getIndexNames(dropIndexStatement);
        if (!optimizedStatement.getTables().isEmpty()) {
            getMetaData().getTables().get(optimizedStatement.getTables().getSingleTableName()).getIndexes().removeAll(indexNames);
        }
        for (String each : indexNames) {
            Optional<String> logicTableName = findLogicTableName(getMetaData().getTables(), each);
            if (logicTableName.isPresent()) {
                getMetaData().getTables().get(optimizedStatement.getTables().getSingleTableName()).getIndexes().remove(each);
            }
        }
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
