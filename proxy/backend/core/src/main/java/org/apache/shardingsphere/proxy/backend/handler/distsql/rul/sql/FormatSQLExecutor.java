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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql;

import org.apache.shardingsphere.distsql.parser.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.executor.ConnectionSessionRequiredRULExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLFormatEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Format SQL executor.
 */
public final class FormatSQLExecutor implements ConnectionSessionRequiredRULExecutor<FormatStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singleton("formatted_result");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final FormatStatement sqlStatement) {
        return Collections.singleton(new LocalDataQueryResultRow(formatSQL(sqlStatement.getSql(), connectionSession.getProtocolType().getType())));
    }
    
    private Object formatSQL(final String sql, final String databaseType) {
        Properties props = new Properties();
        props.setProperty("parameterized", Boolean.FALSE.toString());
        return new SQLFormatEngine(databaseType, new CacheOption(1, 1L)).format(sql, false, props);
    }
    
    @Override
    public String getType() {
        return FormatStatement.class.getName();
    }
}
