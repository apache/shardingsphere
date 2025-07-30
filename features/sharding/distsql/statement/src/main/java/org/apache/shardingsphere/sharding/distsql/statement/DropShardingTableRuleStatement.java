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

import lombok.Getter;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.type.DropRuleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;

import java.util.Collection;

/**
 * Drop sharding table rule statement.
 */
@Getter
public final class DropShardingTableRuleStatement extends DropRuleStatement {
    
    private final Collection<TableNameSegment> tableNames;
    
    public DropShardingTableRuleStatement(final boolean ifExists, final Collection<TableNameSegment> tableNames) {
        super(ifExists);
        this.tableNames = tableNames;
    }
}
