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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;

/**
 * Explain statement.
 */
@Getter
public final class ExplainStatement extends DALStatement {
    
    private final SQLStatement explainableSQLStatement;
    
    private SQLStatementAttributes attributes;
    
    public ExplainStatement(final DatabaseType databaseType, final SQLStatement explainableSQLStatement) {
        super(databaseType);
        this.explainableSQLStatement = explainableSQLStatement;
    }
    
    @Override
    public void buildAttributes() {
        TableExtractor extractor = new TableExtractor();
        // TODO extract table from declare, execute, createMaterializedView, refreshMaterializedView
        extractor.extractTablesFromSQLStatement(explainableSQLStatement);
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(extractor.getRewriteTables()));
    }
}
