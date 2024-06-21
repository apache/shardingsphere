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

package org.apache.shardingsphere.sql.parser.statement.core.statement.dal;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Optional;

/**
 * Show index statement.
 */
@Setter
public abstract class ShowIndexStatement extends AbstractSQLStatement implements DALStatement {
    
    @Getter
    private SimpleTableSegment table;
    
    private FromSchemaSegment fromSchema;
    
    /**
     * Get from schema.
     * 
     * @return from schema
     */
    public Optional<FromSchemaSegment> getFromSchema() {
        return Optional.ofNullable(fromSchema);
    }
}
