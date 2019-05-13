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

package org.apache.shardingsphere.core.rewrite.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SchemaToken;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL rewrite engine for master slave rule.
 * 
 * <p>should rewrite schema name.</p>
 * 
 * @author chenqingyang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class MasterSlaveSQLRewriteEngine implements SQLRewriteEngine {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final String originalSQL;
    
    private final SQLStatement sqlStatement;
    
    private final ShardingDataSourceMetaData dataSourceMetaData;
    
    @Override
    public SQLBuilder rewrite() {
        SQLBuilder result = new SQLBuilder();
        if (sqlStatement.getSQLTokens().isEmpty()) {
            return appendOriginalLiterals(result);
        }
        int count = 0;
        for (SQLToken each : sqlStatement.getSQLTokens()) {
            if (0 == count) {
                result.appendLiterals(originalSQL.substring(0, each.getStartIndex()));
            }
            if (each instanceof SchemaToken) {
                appendSchemaPlaceholder(result, (SchemaToken) each, count);
            }
            count++;
        }
        return result;
    }
    
    private SQLBuilder appendOriginalLiterals(final SQLBuilder sqlBuilder) {
        sqlBuilder.appendLiterals(originalSQL);
        return sqlBuilder;
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaToken.getSchemaName().toLowerCase(), schemaToken.getTableName().toLowerCase(), schemaToken.getQuoteCharacter(), masterSlaveRule, dataSourceMetaData));
        appendRest(sqlBuilder, count, schemaToken.getStopIndex() + 1);
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int startIndex) {
        int stopPosition = sqlStatement.getSQLTokens().size() - 1 == count ? originalSQL.length() : sqlStatement.getSQLTokens().get(count + 1).getStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(startIndex > originalSQL.length() ? originalSQL.length() : startIndex, stopPosition));
    }
    
    @Override
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final SQLBuilder sqlBuilder) {
        return sqlBuilder.toSQL(getTableTokens());
    }
    
    private Map<String, String> getTableTokens() {
        Map<String, String> result = new HashMap<>();
        for (String each : sqlStatement.getTables().getTableNames()) {
            result.put(each.toLowerCase(), each.toLowerCase());
        }
        return result;
    }
}
