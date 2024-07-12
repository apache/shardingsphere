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

package org.apache.shardingsphere.sql.parser.statement.sqlserver.dml;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.SQLServerStatement;

import java.util.Optional;

/**
 * SQLServer select statement.
 */
@Setter
public final class SQLServerSelectStatement extends SelectStatement implements SQLServerStatement {
    
    private LimitSegment limit;
    
    private TableSegment intoSegment;
    
    private WithTableHintSegment withTableHintSegment;
    
    @Override
    public Optional<LimitSegment> getLimit() {
        return Optional.ofNullable(limit);
    }
    
    @Override
    public Optional<TableSegment> getIntoSegment() {
        return Optional.ofNullable(intoSegment);
    }
    
    /**
     * Get with table hint segment.
     *
     * @return with table hint segment.
     */
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.ofNullable(withTableHintSegment);
    }
}
