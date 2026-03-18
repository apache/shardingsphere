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

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collections;
import java.util.Optional;

/**
 * Comment statement.
 */
@Getter
public final class CommentStatement extends DDLStatement {
    
    private final SimpleTableSegment table;
    
    private final ColumnSegment column;
    
    private final IdentifierValue comment;
    
    private final IndexTypeSegment indexType;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private CommentStatement(final DatabaseType databaseType, final SimpleTableSegment table, final ColumnSegment column, final IdentifierValue comment, final IndexTypeSegment indexType) {
        super(databaseType);
        this.table = table;
        this.column = column;
        this.comment = comment;
        this.indexType = indexType;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(null == table ? Collections.emptyList() : Collections.singletonList(table)));
    }
    
    /**
     * Get index type.
     *
     * @return index type
     */
    public Optional<IndexTypeSegment> getIndexType() {
        return Optional.ofNullable(indexType);
    }
}
