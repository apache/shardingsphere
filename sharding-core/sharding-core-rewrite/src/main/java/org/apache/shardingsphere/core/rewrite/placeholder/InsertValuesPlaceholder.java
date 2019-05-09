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

package org.apache.shardingsphere.core.rewrite.placeholder;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Insert values placeholder for rewrite.
 *
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class InsertValuesPlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final Collection<String> columnNames;
    
    private final List<InsertOptimizeResultUnit> units;
    
    public String toString(final TableUnit tableUnit, final Map<String, String> logicAndActualTableMap) {
        StringBuilder result = new StringBuilder();
        result.append(" (").append(Joiner.on(", ").join(columnNames)).append(") VALUES ");
        appendUnits(tableUnit, result);
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }
    
    private void appendUnits(final TableUnit tableUnit, final StringBuilder result) {
        for (InsertOptimizeResultUnit each : units) {
            if (isToAppendInsertOptimizeResult(tableUnit, each)) {
                result.append(each).append(", ");
            }
        }
    }
    
    private boolean isToAppendInsertOptimizeResult(final TableUnit tableUnit, final InsertOptimizeResultUnit unit) {
        if (unit.getDataNodes().isEmpty() || null == tableUnit) {
            return true;
        }
        for (DataNode each : unit.getDataNodes()) {
            if (tableUnit.getRoutingTable(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
