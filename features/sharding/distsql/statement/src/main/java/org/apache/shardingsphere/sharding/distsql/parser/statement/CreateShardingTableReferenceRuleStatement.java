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

package org.apache.shardingsphere.sharding.distsql.parser.statement;

import lombok.Getter;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Create sharding table reference rule statement.
 */
@Getter
public final class CreateShardingTableReferenceRuleStatement extends CreateRuleStatement {
    
    private final Collection<TableReferenceRuleSegment> rules;
    
    public CreateShardingTableReferenceRuleStatement(final boolean ifNotExists, final Collection<TableReferenceRuleSegment> rules) {
        super(ifNotExists);
        this.rules = rules;
    }
    
    /**
     * Get table names.
     *
     * @return table names
     */
    public Collection<String> getTableNames() {
        return rules.stream().flatMap(each -> each.getTableNames().stream()).collect(Collectors.toList());
    }
}
