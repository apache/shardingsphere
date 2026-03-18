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

package org.apache.shardingsphere.sharding.distsql.statement;

import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowDatabaseRulesStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;

import java.util.Optional;

/**
 * Show sharding table rules used auditor statement.
 */
public final class ShowShardingTableRulesUsedAuditorStatement extends ShowDatabaseRulesStatement {
    
    private final String auditorName;
    
    public ShowShardingTableRulesUsedAuditorStatement(final String auditorName, final FromDatabaseSegment fromDatabase) {
        super(fromDatabase);
        this.auditorName = auditorName;
    }
    
    /**
     * Get auditor name.
     *
     * @return auditor name.
     */
    public Optional<String> getAuditorName() {
        return Optional.ofNullable(auditorName);
    }
}
