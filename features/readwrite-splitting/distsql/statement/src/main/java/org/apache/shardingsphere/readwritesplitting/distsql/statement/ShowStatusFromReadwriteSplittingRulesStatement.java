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

package org.apache.shardingsphere.readwritesplitting.distsql.statement;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ResourceQueryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;

import java.util.Optional;

/**
 * Show status from readwrite-splitting rules statement.
 */
@RequiredArgsConstructor
public final class ShowStatusFromReadwriteSplittingRulesStatement extends ResourceQueryStatement {
    
    private final FromDatabaseSegment fromDatabase;
    
    private final String ruleName;
    
    /**
     * Get rule name.
     *
     * @return rule name.
     */
    public Optional<String> getRuleName() {
        return Optional.ofNullable(ruleName);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new FromDatabaseSQLStatementAttribute(fromDatabase));
    }
}
