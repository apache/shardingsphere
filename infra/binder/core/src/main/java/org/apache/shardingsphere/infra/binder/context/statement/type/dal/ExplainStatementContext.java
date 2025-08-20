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

package org.apache.shardingsphere.infra.binder.context.statement.type.dal;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;

import java.util.Collections;

/**
 * Explain statement context.
 */
@Getter
public final class ExplainStatementContext implements SQLStatementContext {
    
    private final ExplainStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final SQLStatementContext explainableSQLStatementContext;
    
    public ExplainStatementContext(final ShardingSphereMetaData metaData, final ExplainStatement sqlStatement, final String currentDatabaseName) {
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).map(TableSQLStatementAttribute::getTables).orElse(Collections.emptyList()));
        explainableSQLStatementContext = SQLStatementContextFactory.newInstance(metaData, sqlStatement.getExplainableSQLStatement(), currentDatabaseName);
    }
}
