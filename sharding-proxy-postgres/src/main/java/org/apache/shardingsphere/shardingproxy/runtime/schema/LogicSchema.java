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

package org.apache.shardingsphere.shardingproxy.runtime.schema;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaDataFactory;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Logic schema.
 *
 * @author panjuan
 */
@Getter
public abstract class LogicSchema {
    
    private final String name;
    
    private final Map<String, YamlDataSourceParameter> dataSources;
    
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private JDBCBackendDataSource backendDataSource;
    
    public LogicSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources) {
        this.name = name;
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        this.dataSources = dataSources;
        backendDataSource = new JDBCBackendDataSource(dataSources);
        eventBus.register(this);
    }
    
    protected final Map<String, String> getDataSourceURLs(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, YamlDataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUrl());
        }
        return result;
    }
    
    /**
     * Get sharding meta data.
     * 
     * @return sharding meta data.
     */
    public abstract ShardingMetaData getMetaData();
    
    /**
     * Renew data source configuration.
     *
     * @param dataSourceChangedEvent data source changed event.
     * @throws Exception exception
     */
    @Subscribe
    public final synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) throws Exception {
        if (!name.equals(dataSourceChangedEvent.getShardingSchemaName())) {
            return;
        }
        backendDataSource.close();
        dataSources.clear();
        dataSources.putAll(DataSourceConverter.getDataSourceParameterMap(dataSourceChangedEvent.getDataSourceConfigurations()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
    }
    
    /**
     * Refresh table meta data.
     * 
     * @param sqlStatement SQL statement
     */
    public final void refreshTableMetaData(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            refreshTableMetaData((CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            refreshTableMetaData((AlterTableStatement) sqlStatement);
        } else if (sqlStatement instanceof DropTableStatement) {
            refreshTableMetaData((DropTableStatement) sqlStatement);
        }
    }
    
    private void refreshTableMetaData(final CreateTableStatement createTableStatement) {
        getMetaData().getTable().put(createTableStatement.getTables().getSingleTableName(), TableMetaDataFactory.newInstance(createTableStatement));
    }
    
    private void refreshTableMetaData(final AlterTableStatement alterTableStatement) {
        String logicTableName = alterTableStatement.getTables().getSingleTableName();
        TableMetaData newTableMetaData = TableMetaDataFactory.newInstance(alterTableStatement, getMetaData().getTable().get(logicTableName));
        Optional<String> newTableName = alterTableStatement.getNewTableName();
        if (newTableName.isPresent()) {
            getMetaData().getTable().put(newTableName.get(), newTableMetaData);
            getMetaData().getTable().remove(logicTableName);
        } else {
            getMetaData().getTable().put(logicTableName, newTableMetaData);
        }
    }
    
    private void refreshTableMetaData(final DropTableStatement dropTableStatement) {
        for (String each : dropTableStatement.getTables().getTableNames()) {
            getMetaData().getTable().remove(each);
        }
    }
}
