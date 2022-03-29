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

package org.apache.shardingsphere.proxy.backend.response.header.query.impl;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;

import java.sql.SQLException;

/**
 * QueryHeaderBuilder for openGauss.
 */
public final class OpenGaussQueryHeaderBuilder extends QueryHeaderBuilder {
    
    private final PostgreSQLQueryHeaderBuilder delegated = new PostgreSQLQueryHeaderBuilder();
    
    @Override
    public String getDatabaseType() {
        return new OpenGaussDatabaseType().getName();
    }
    
    @Override
    protected QueryHeader doBuild(final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData, final String columnName, final String columnLabel, 
                                  final int columnIndex, final LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException {
        return delegated.doBuild(queryResultMetaData, metaData, columnName, columnLabel, columnIndex, dataNodeContainedRule);
    }
}
