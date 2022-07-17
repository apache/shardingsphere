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

package org.apache.shardingsphere.infra.federation.optimizer.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.util.Collections;
import java.util.Map;

/**
 * Optimizer context.
 */
@RequiredArgsConstructor
@Getter
public final class OptimizerContext {
    
    private final SQLParserRule sqlParserRule;
    
    private final FederationMetaData federationMetaData;
    
    private final Map<String, OptimizerParserContext> parserContexts;
    
    private final Map<String, OptimizerPlannerContext> plannerContexts;
    
    /**
     * Add database.
     *
     * @param databaseName database name
     * @param protocolType protocol database type
     */
    public void addDatabase(final String databaseName, final DatabaseType protocolType) {
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData(databaseName.toLowerCase(), Collections.emptyMap());
        federationMetaData.getDatabases().put(databaseName.toLowerCase(), federationDatabaseMetaData);
        parserContexts.put(databaseName.toLowerCase(), OptimizerParserContextFactory.create(protocolType));
        plannerContexts.put(databaseName.toLowerCase(), OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
    
    /**
     * Alter database.
     *
     * @param database to be altered database
     * @param globalRuleMetaData global rule meta data
     */
    public void alterDatabase(final ShardingSphereDatabase database, final ShardingSphereRuleMetaData globalRuleMetaData) {
        String databaseName = database.getName().toLowerCase();
        OptimizerContext toBeAlteredOptimizerContext = OptimizerContextFactory.create(Collections.singletonMap(databaseName, database), globalRuleMetaData);
        federationMetaData.getDatabases().put(databaseName, toBeAlteredOptimizerContext.getFederationMetaData().getDatabases().get(databaseName));
        plannerContexts.put(databaseName, toBeAlteredOptimizerContext.getPlannerContexts().get(databaseName));
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        federationMetaData.getDatabases().remove(databaseName.toLowerCase());
        parserContexts.remove(databaseName.toLowerCase());
        plannerContexts.remove(databaseName.toLowerCase());
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        federationMetaData.getDatabases().get(databaseName.toLowerCase()).putTable(schemaName, new ShardingSphereTable());
        // TODO add schema only
        plannerContexts.put(databaseName.toLowerCase(), OptimizerPlannerContextFactory.create(federationMetaData.getDatabases().get(databaseName.toLowerCase())));
    }
    
    /**
     * Alter table.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     */
    public void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable) {
        FederationDatabaseMetaData federationDatabaseMetaData = federationMetaData.getDatabases().get(databaseName.toLowerCase());
        federationDatabaseMetaData.putTable(schemaName.toLowerCase(), toBeChangedTable);
        plannerContexts.put(databaseName.toLowerCase(), OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
    
    /**
     * Drop table.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     */
    public void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        FederationDatabaseMetaData federationDatabaseMetaData = federationMetaData.getDatabases().get(databaseName.toLowerCase());
        federationDatabaseMetaData.removeTableMetadata(schemaName, toBeDeletedTableName);
        plannerContexts.put(databaseName.toLowerCase(), OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
}
