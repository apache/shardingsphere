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

package org.apache.shardingsphere.single.distsql.statement.rql;

import org.apache.shardingsphere.distsql.statement.rql.resource.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;

import java.util.Optional;

/**
 * Show single table statement.
 */
public final class ShowSingleTableStatement extends ShowTablesStatement {
    
    private final String tableName;
    
    public ShowSingleTableStatement(final String tableName, final String likePattern, final DatabaseSegment database) {
        super(likePattern, database);
        this.tableName = tableName;
    }
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public Optional<String> getTableName() {
        return Optional.ofNullable(tableName);
    }
}
