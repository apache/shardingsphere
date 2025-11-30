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

package org.apache.shardingsphere.infra.binder.context.statement.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;

import java.util.Collections;

/**
 * Common SQL statement context.
 */
@RequiredArgsConstructor
@Getter
public final class CommonSQLStatementContext implements SQLStatementContext {
    
    private final SQLStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    public CommonSQLStatementContext(final SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        tablesContext = new TablesContext(sqlStatement.getAttributes().findAttribute(TableSQLStatementAttribute.class).map(TableSQLStatementAttribute::getTables).orElse(Collections.emptyList()));
    }
}
