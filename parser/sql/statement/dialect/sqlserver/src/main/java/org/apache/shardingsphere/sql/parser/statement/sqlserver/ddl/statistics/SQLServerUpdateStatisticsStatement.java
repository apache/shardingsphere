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

package org.apache.shardingsphere.sql.parser.statement.sqlserver.ddl.statistics;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.statistics.StatisticsStrategySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.List;

/**
 * Update statistics statement for SQLServer.
 */
@Getter
public final class SQLServerUpdateStatisticsStatement extends DDLStatement {
    
    private final SimpleTableSegment table;
    
    private final List<IndexSegment> indexes;
    
    private final StatisticsStrategySegment strategy;
    
    public SQLServerUpdateStatisticsStatement(final DatabaseType databaseType, final SimpleTableSegment table, final List<IndexSegment> indexes, final StatisticsStrategySegment strategy) {
        super(databaseType);
        this.table = table;
        this.indexes = indexes;
        this.strategy = strategy;
    }
}
