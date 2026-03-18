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

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create index statement.
 */
@Getter
public final class CreateIndexStatement extends DDLStatement {
    
    private final IndexSegment index;
    
    private final SimpleTableSegment table;
    
    private final boolean ifNotExists;
    
    private final Integer anonymousIndexStartIndex;
    
    private final AlgorithmTypeSegment algorithmType;
    
    private final LockTableSegment lockTable;
    
    private final Collection<ColumnSegment> columns;
    
    private final String indexType;
    
    private final PropertiesSegment properties;
    
    private final String comment;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private CreateIndexStatement(final DatabaseType databaseType, final IndexSegment index, final SimpleTableSegment table, final boolean ifNotExists,
                                 final Integer anonymousIndexStartIndex, final AlgorithmTypeSegment algorithmType, final LockTableSegment lockTable,
                                 final Collection<ColumnSegment> columns, final String indexType, final PropertiesSegment properties, final String comment) {
        super(databaseType);
        this.index = index;
        this.table = table;
        this.ifNotExists = ifNotExists;
        this.anonymousIndexStartIndex = anonymousIndexStartIndex;
        this.algorithmType = algorithmType;
        this.lockTable = lockTable;
        this.columns = null == columns ? Collections.emptyList() : columns;
        this.indexType = indexType;
        this.properties = properties;
        this.comment = comment;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(table), new CreateIndexIndexSQLStatementAttribute());
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
