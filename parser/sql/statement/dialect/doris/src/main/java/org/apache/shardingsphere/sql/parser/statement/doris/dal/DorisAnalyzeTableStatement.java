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

package org.apache.shardingsphere.sql.parser.statement.doris.dal;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Analyze table statement for Doris.
 */
@Getter
public final class DorisAnalyzeTableStatement extends DALStatement {
    
    private final SimpleTableSegment table;
    
    private final DatabaseSegment database;
    
    private final Collection<ColumnSegment> columns;
    
    private final boolean sync;
    
    private final String sampleType;
    
    private final Number sampleValue;
    
    public DorisAnalyzeTableStatement(final DatabaseType databaseType, final SimpleTableSegment table, final DatabaseSegment database, final Collection<ColumnSegment> columns,
                                      final boolean sync, final String sampleType, final Number sampleValue) {
        super(databaseType);
        this.table = table;
        this.database = database;
        this.columns = columns;
        this.sync = sync;
        this.sampleType = sampleType;
        this.sampleValue = sampleValue;
    }
    
    /**
     * Get columns.
     *
     * @return columns
     */
    public Collection<ColumnSegment> getColumns() {
        return null == columns ? Collections.emptyList() : columns;
    }
    
    /**
     * Get sample type.
     *
     * @return sample type
     */
    public Optional<String> getSampleType() {
        return Optional.ofNullable(sampleType);
    }
    
    /**
     * Get sample value.
     *
     * @return sample value
     */
    public Optional<Number> getSampleValue() {
        return Optional.ofNullable(sampleValue);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return null != table ? new SQLStatementAttributes(new TableSQLStatementAttribute(Collections.singleton(table)), new TableBroadcastRouteSQLStatementAttribute()) : new SQLStatementAttributes();
    }
}
