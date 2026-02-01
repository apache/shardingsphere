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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Cursor statement.
 */
@Getter
public final class CursorStatement extends DDLStatement {
    
    private final CursorNameSegment cursorName;
    
    private final SelectStatement select;
    
    private SQLStatementAttributes attributes;
    
    public CursorStatement(final DatabaseType databaseType, final CursorNameSegment cursorName, final SelectStatement select) {
        super(databaseType);
        this.cursorName = cursorName;
        this.select = select;
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new CursorSQLStatementAttribute(cursorName));
    }
}
