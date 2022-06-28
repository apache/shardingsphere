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
        FederationDatabaseMetaData federationDatabaseMetaData = new FederationDatabaseMetaData(databaseName, Collections.emptyMap());
        federationMetaData.getDatabases().put(databaseName, federationDatabaseMetaData);
        parserContexts.put(databaseName, OptimizerParserContextFactory.create(protocolType));
        plannerContexts.put(databaseName, OptimizerPlannerContextFactory.create(federationDatabaseMetaData));
    }
}
