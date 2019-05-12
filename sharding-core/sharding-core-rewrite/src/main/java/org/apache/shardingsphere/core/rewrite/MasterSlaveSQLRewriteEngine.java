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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SQLToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.SchemaToken;
import org.apache.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;

import java.util.Collections;
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
public final class MasterSlaveSQLRewriteEngine {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final String originalSQL;
    
    private final SQLStatement sqlStatement;
    
    private final ShardingDataSourceMetaData dataSourceMetaData;
    
    /**
     * Constructs master slave SQL rewrite engine.
     * 
     * @param masterSlaveRule master slave rule
     * @param originalSQL original SQL
     * @param sqlStatement SQL statement
     * @param dataSourceMetaData datasource meta data
     */
    public MasterSlaveSQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final String originalSQL, final SQLStatement sqlStatement, final ShardingDataSourceMetaData dataSourceMetaData) {
        this.masterSlaveRule = masterSlaveRule;
        this.originalSQL = originalSQL;
        this.sqlStatement = sqlStatement;
        this.dataSourceMetaData = dataSourceMetaData;
    }
    
    /**
     * Rewrite SQL.
     * 
     * @return SQL
     */
    public String rewrite() {
        if (sqlStatement.getSQLTokens().isEmpty()) {
            return originalSQL;
        }
        SQLBuilder result = new SQLBuilder(Collections.emptyList());
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
        return result.toSQL(getTableTokens());
    }
    
    private Map<String, String> getTableTokens() {
        Map<String, String> result = new HashMap<>();
        for (String each : sqlStatement.getTables().getTableNames()) {
            result.put(each.toLowerCase(), each.toLowerCase());
        }
        return result;
    }
    
    private void appendSchemaPlaceholder(final SQLBuilder sqlBuilder, final SchemaToken schemaToken, final int count) {
        String schemaName = originalSQL.substring(schemaToken.getStartIndex(), schemaToken.getStopIndex() + 1);
        sqlBuilder.appendPlaceholder(new SchemaPlaceholder(schemaName.toLowerCase(), schemaToken.getTableName().toLowerCase(), masterSlaveRule, dataSourceMetaData));
        appendRest(sqlBuilder, count, schemaToken.getStopIndex() + 1);
    }
    
    private void appendRest(final SQLBuilder sqlBuilder, final int count, final int startIndex) {
        int stopPosition = sqlStatement.getSQLTokens().size() - 1 == count ? originalSQL.length() : sqlStatement.getSQLTokens().get(count + 1).getStartIndex();
        sqlBuilder.appendLiterals(originalSQL.substring(startIndex > originalSQL.length() ? originalSQL.length() : startIndex, stopPosition));
    }
}
