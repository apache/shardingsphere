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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.rewrite.placeholder.Alterable;
import org.apache.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private final List<Object> parameters;
    
    private StringBuilder currentSegment;
    
    public SQLBuilder() {
        this(Collections.emptyList());
    }
    
    public SQLBuilder(final List<Object> parameters) {
        segments = new LinkedList<>();
        this.parameters = parameters;
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append literals.
     *
     * @param literals literals for SQL
     */
    public void appendLiterals(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * Append sharding placeholder.
     *
     * @param shardingPlaceholder sharding placeholder
     */
    public void appendPlaceholder(final ShardingPlaceholder shardingPlaceholder) {
        segments.add(shardingPlaceholder);
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Convert to SQL unit.
     *
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit toSQL(final Map<String, String> logicAndActualTables) {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof Alterable) {
                result.append(((Alterable) each).toString(null, logicAndActualTables));
            } else {
                result.append(each);
            }
        }
        return new SQLUnit(result.toString(), Collections.emptyList());
    }
    
    /**
     * Convert to SQL unit.
     *
     * @param insertOptimizeResult insert optimize result
     * @return SQL unit
     */
    public SQLUnit toSQL(final InsertOptimizeResult insertOptimizeResult) {
        StringBuilder result = new StringBuilder();
        List<Object> insertParameters = getInsertParameters(insertOptimizeResult);
        for (Object each : segments) {
            if (each instanceof Alterable) {
                result.append(((Alterable) each).toString(null, Collections.<String, String>emptyMap()));
            } else {
                result.append(each);
            }
        }
        return insertParameters.isEmpty() ? new SQLUnit(result.toString(), new ArrayList<>(parameters)) : new SQLUnit(result.toString(), insertParameters);
    }
    
    /**
     * Convert to SQL unit.
     *
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual map
     * @param insertOptimizeResult insert optimize result
     * @return SQL unit
     */
    public SQLUnit toSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables, final InsertOptimizeResult insertOptimizeResult) {
        StringBuilder result = new StringBuilder();
        List<Object> insertParameters = getInsertParameters(insertOptimizeResult, routingUnit);
        for (Object each : segments) {
            if (each instanceof Alterable) {
                result.append(((Alterable) each).toString(routingUnit, logicAndActualTables));
            } else {
                result.append(each);
            }
        }
        return insertParameters.isEmpty() ? new SQLUnit(result.toString(), new ArrayList<>(parameters)) : new SQLUnit(result.toString(), insertParameters);
    }
    
    private List<Object> getInsertParameters(final InsertOptimizeResult insertOptimizeResult) {
        List<Object> result = new LinkedList<>();
        if (null == insertOptimizeResult) {
            return result;
        }
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            result.addAll(Arrays.asList(each.getParameters()));
        }
        return result;
    }
    
    private List<Object> getInsertParameters(final InsertOptimizeResult insertOptimizeResult, final RoutingUnit routingUnit) {
        List<Object> result = new LinkedList<>();
        if (null == insertOptimizeResult) {
            return result;
        }
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            if (isAppendInsertParameter(each, routingUnit)) {
                result.addAll(Arrays.asList(each.getParameters()));
            }
        }
        return result;
    }
    
    private boolean isAppendInsertParameter(final InsertOptimizeResultUnit unit, final RoutingUnit routingUnit) {
        for (DataNode each : unit.getDataNodes()) {
            if (routingUnit.getTableUnit(each.getDataSourceName(), each.getTableName()).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
