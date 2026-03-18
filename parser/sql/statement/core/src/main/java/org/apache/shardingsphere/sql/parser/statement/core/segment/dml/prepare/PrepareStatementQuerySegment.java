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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Optional;

/**
 * Prepare statement query segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class PrepareStatementQuerySegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private SelectStatement select;
    
    private InsertStatement insert;
    
    private UpdateStatement update;
    
    private DeleteStatement delete;
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return Optional.ofNullable(select);
    }
    
    /**
     * Get insert statement.
     *
     * @return insert statement
     */
    public Optional<InsertStatement> getInsert() {
        return Optional.ofNullable(insert);
    }
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public Optional<UpdateStatement> getUpdate() {
        return Optional.ofNullable(update);
    }
    
    /**
     * Get delete statement.
     *
     * @return delete statement
     */
    public Optional<DeleteStatement> getDelete() {
        return Optional.ofNullable(delete);
    }
}
