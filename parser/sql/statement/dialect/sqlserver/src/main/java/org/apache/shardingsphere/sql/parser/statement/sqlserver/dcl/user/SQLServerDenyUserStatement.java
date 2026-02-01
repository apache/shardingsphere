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

package org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Deny user statement for SQLServer.
 */
@Getter
@Setter
public final class SQLServerDenyUserStatement extends DCLStatement {
    
    private SQLStatementAttributes attributes;
    
    private SimpleTableSegment table;
    
    private final Collection<ColumnSegment> columns = new LinkedList<>();
    
    public SQLServerDenyUserStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(table));
    }
}
