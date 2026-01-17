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

package org.apache.shardingsphere.sql.parser.statement.doris.dal.show;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.DatabaseSelectRequiredSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TablelessDataSourceBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * Show data skew statement for Doris.
 */
@Getter
public final class DorisShowDataSkewStatement extends DALStatement {
    
    private final SimpleTableSegment table;
    
    private final Collection<PartitionSegment> partitions;
    
    public DorisShowDataSkewStatement(final DatabaseType databaseType, final SimpleTableSegment table, final Collection<PartitionSegment> partitions) {
        super(databaseType);
        this.table = table;
        this.partitions = partitions;
    }
    
    /**
     * Get table.
     *
     * @return table
     */
    public Optional<SimpleTableSegment> getTable() {
        return Optional.ofNullable(table);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        String databaseName = null == table || !table.getOwner().isPresent() ? null : table.getOwner().get().getIdentifier().getValue();
        return new SQLStatementAttributes(new DatabaseSelectRequiredSQLStatementAttribute(), new TableSQLStatementAttribute(table), new TablelessDataSourceBroadcastRouteSQLStatementAttribute(),
                new AllowNotUseDatabaseSQLStatementAttribute(true, databaseName));
    }
}
