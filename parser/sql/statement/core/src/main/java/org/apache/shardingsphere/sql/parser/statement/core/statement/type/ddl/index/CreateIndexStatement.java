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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create index statement.
 */
@Getter
@Setter
public final class CreateIndexStatement extends DDLStatement {
    
    private IndexSegment index;
    
    private SimpleTableSegment table;
    
    private boolean ifNotExists;
    
    private Integer anonymousIndexStartIndex;
    
    private AlgorithmTypeSegment algorithmType;
    
    private LockTableSegment lockTable;
    
    private final Collection<ColumnSegment> columns = new LinkedList<>();
    
    public CreateIndexStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get anonymous index start index.
     *
     * @return anonymous index start index
     */
    public Optional<Integer> getAnonymousIndexStartIndex() {
        return Optional.ofNullable(anonymousIndexStartIndex);
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
     * Get lock table.
     *
     * @return lock table
     */
    public Optional<LockTableSegment> getLockTable() {
        return Optional.ofNullable(lockTable);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new TableSQLStatementAttribute(table), new CreateIndexIndexSQLStatementAttribute());
    }
    
    private class CreateIndexIndexSQLStatementAttribute implements IndexSQLStatementAttribute {
        
        private static final String UNDERLINE = "_";
        
        private static final String GENERATED_LOGIC_INDEX_NAME_SUFFIX = "idx";
        
        @Override
        public Collection<IndexSegment> getIndexes() {
            if (null != index) {
                return Collections.singleton(index);
            }
            if (!getAnonymousIndexStartIndex().isPresent()) {
                return Collections.emptyList();
            }
            int anonymousIndexStartIndex = getAnonymousIndexStartIndex().get();
            IndexNameSegment anonymousIndexNameSegment = new IndexNameSegment(anonymousIndexStartIndex, anonymousIndexStartIndex, new IdentifierValue(getGeneratedLogicIndexName(columns)));
            return Collections.singleton(new IndexSegment(anonymousIndexStartIndex, anonymousIndexStartIndex, anonymousIndexNameSegment));
        }
        
        private String getGeneratedLogicIndexName(final Collection<ColumnSegment> columns) {
            return columns.stream().map(each -> each.getIdentifier().getValue() + UNDERLINE).collect(Collectors.joining("", "", GENERATED_LOGIC_INDEX_NAME_SUFFIX));
        }
        
        @Override
        public Collection<ColumnSegment> getIndexColumns() {
            return columns;
        }
    }
}
