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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Format SQL handler.
 */
public final class FormatSQLHandler extends SQLRULBackendHandler<FormatStatement> {
    
    private static final String FORMATTED_RESULT = "formatted_result";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singleton(FORMATTED_RESULT);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        String sql = getSqlStatement().getSql();
        String databaseType = getConnectionSession().getProtocolType().getType();
        return Collections.singleton(new LocalDataQueryResultRow(formatSQL(sql, databaseType)));
    }
    
    private Object formatSQL(final String sql, final String databaseType) {
        Properties props = new Properties();
        props.setProperty("parameterized", Boolean.FALSE.toString());
        return new SQLVisitorEngine(databaseType, "FORMAT", false, props).visit(parseSQL(sql, databaseType));
    }
    
    private ParseASTNode parseSQL(final String sql, final String databaseType) {
        return new SQLParserEngine(databaseType, new CacheOption(1, 1L)).parse(sql, false);
    }
}
