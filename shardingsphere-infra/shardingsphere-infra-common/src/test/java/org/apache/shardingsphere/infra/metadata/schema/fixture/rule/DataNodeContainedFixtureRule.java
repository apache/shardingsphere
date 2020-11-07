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

package org.apache.shardingsphere.infra.metadata.schema.fixture.rule;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public final class DataNodeContainedFixtureRule implements DataNodeContainedRule, TableContainedRule {
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return null;
    }
    
    @Override
    public Collection<String> getAllActualTables() {
        return Arrays.asList("data_node_routed_table_0", "data_node_routed_table_2");
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return Optional.empty();
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return false;
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return Optional.empty();
    }
    
    @Override
    public Collection<String> getTables() {
        return Collections.singleton("data_node_routed_table");
    }
}
