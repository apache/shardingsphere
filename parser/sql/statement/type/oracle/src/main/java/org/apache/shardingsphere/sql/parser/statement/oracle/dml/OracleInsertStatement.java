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

package org.apache.shardingsphere.sql.parser.statement.oracle.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.OracleStatement;

import java.util.Optional;

/**
 * Oracle insert statement.
 */
@Getter
@Setter
public final class OracleInsertStatement extends InsertStatement implements OracleStatement {
    
    private MultiTableInsertType multiTableInsertType;
    
    private MultiTableInsertIntoSegment multiTableInsertIntoSegment;
    
    private MultiTableConditionalIntoSegment multiTableConditionalIntoSegment;
    
    private WhereSegment where;
    
    @Override
    public Optional<MultiTableInsertType> getMultiTableInsertType() {
        return Optional.ofNullable(multiTableInsertType);
    }
    
    @Override
    public Optional<MultiTableInsertIntoSegment> getMultiTableInsertIntoSegment() {
        return Optional.ofNullable(multiTableInsertIntoSegment);
    }
    
    @Override
    public Optional<MultiTableConditionalIntoSegment> getMultiTableConditionalIntoSegment() {
        return Optional.ofNullable(multiTableConditionalIntoSegment);
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
}
