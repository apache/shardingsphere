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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.Getter;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;

/**
 * Exception for rejected model-generated EXPLAIN SQL syntax.
 */
@Getter
public final class ExplainSQLSyntaxException extends MCPInvalidRequestException {
    
    private static final long serialVersionUID = 2267318188070094144L;
    
    private final String database;
    
    private final String schema;
    
    private final String sql;
    
    private final String explainSql;
    
    public ExplainSQLSyntaxException(final String database, final String schema, final String sql, final String explainSql, final RuntimeException cause) {
        super("Generated explain_sql is not valid for the target database.", cause);
        this.database = database;
        this.schema = schema;
        this.sql = sql;
        this.explainSql = explainSql;
    }
}
