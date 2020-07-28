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

package org.apache.shardingsphere.rdl.parser.binder.context;

import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Create sharding rule statement context.
 */
public final class CreateShardingRuleStatementContext extends CommonSQLStatementContext<CreateShardingRuleStatement> {
    
    public CreateShardingRuleStatementContext(final CreateShardingRuleStatement sqlStatement) {
        super(sqlStatement);
    }
    
    /**
     * Get logic table.
     *
     * @return logic table
     */
    public String getLogicTable() {
        return "";
    }
    
    /**
     * Get data sources.
     *
     * @return data sources
     */
    public Collection<String> getDataSources() {
        return new LinkedList<>();
    }
    
    /**
     * Get sharding column.
     *
     * @return sharding column
     */
    public String getShardingColumn() {
        return "";
    }
    
    /**
     * Get sharding algorithm type.
     *
     * @return sharding algorithm type
     */
    public String getAlgorithmType() {
        return "";
    }
    
    /**
     * Get algorithm properties.
     *
     * @return algorithm properties
     */
    public Properties getAlgorithmProperties() {
        return new Properties();
    }
}
