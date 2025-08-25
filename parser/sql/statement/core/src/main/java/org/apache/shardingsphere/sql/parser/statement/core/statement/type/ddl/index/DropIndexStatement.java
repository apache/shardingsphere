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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Drop index statement.
 */
@Getter
@Setter
public final class DropIndexStatement extends DDLStatement {
    
    private final Collection<IndexSegment> indexes = new LinkedList<>();
    
    private boolean ifExists;
    
    private SimpleTableSegment simpleTable;
    
    private AlgorithmTypeSegment algorithmType;
    
    private LockTableSegment lockTable;
    
    public DropIndexStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get simple table.
     *
     * @return simple table
     */
    public Optional<SimpleTableSegment> getSimpleTable() {
        return Optional.ofNullable(simpleTable);
    }
    
    /**
     * Get algorithm type.
     *
     * @return algorithm type
     */
    public Optional<AlgorithmTypeSegment> getAlgorithmType() {
        return Optional.ofNullable(algorithmType);
    }
    
    /**
     * Get lock table segment.
     *
     * @return lock table segment
     */
    public Optional<LockTableSegment> getLockTable() {
        return Optional.ofNullable(lockTable);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new TableSQLStatementAttribute(simpleTable), new DropIndexIndexSQLStatementAttribute());
    }
    
    private class DropIndexIndexSQLStatementAttribute implements IndexSQLStatementAttribute {
        
        @Override
        public Collection<IndexSegment> getIndexes() {
            return indexes;
        }
        
        @Override
        public Collection<ColumnSegment> getIndexColumns() {
            return Collections.emptyList();
        }
    }
}
