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

package org.apache.shardingsphere.sqlfederation.engine.processor;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.optimizer.context.OptimizerContext;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * SQL federation processor.
 */
public interface SQLFederationProcessor {
    
    /**
     * Register executor.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param databaseName database name
     * @param schemaName schema name
     * @param federationContext federation context
     * @param optimizerContext optimizer context
     * @param schemaPlus sql federation schema
     */
    default void registerExecutor(DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, JDBCExecutorCallback<? extends ExecuteResult> callback,
                                  String databaseName, String schemaName, SQLFederationContext federationContext, OptimizerContext optimizerContext, SchemaPlus schemaPlus) {
    }
    
    /**
     * Execute plan.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param executionPlan execution plan
     * @param converter converter
     * @param federationContext federation context
     * @param schemaPlus sql federation schema
     * @return resultset
     */
    ResultSet executePlan(DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, JDBCExecutorCallback<? extends ExecuteResult> callback,
                          SQLFederationExecutionPlan executionPlan, SqlToRelConverter converter, SQLFederationContext federationContext, SchemaPlus schemaPlus);
    
    /**
     * Get conversion.
     *
     * @return conversion
     */
    Convention getConvention();
}
