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
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Drop sharding table reference rules statement.
 */
@Getter
public final class DropShardingTableReferenceRuleStatement extends DropRuleStatement {
    
    private final Collection<TableReferenceRuleSegment> rules;
    
    public DropShardingTableReferenceRuleStatement(final boolean ifExists, final Collection<TableReferenceRuleSegment> rules) {
        super(ifExists);
        this.rules = rules;
    }
    
    /**
     * Get table references.
     *
     * @return table references
     */
    public Collection<String> getTableReferences() {
        Collection<String> result = new LinkedList<>();
        for (TableReferenceRuleSegment each : rules) {
            result.add(each.getTableGroup());
        }
        return result;
    }
}
