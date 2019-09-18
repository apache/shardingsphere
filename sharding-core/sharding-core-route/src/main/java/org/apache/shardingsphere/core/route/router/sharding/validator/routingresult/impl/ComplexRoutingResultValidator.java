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

package org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;

/**
 * Complex routing result validator.
 *
 * @author sunbufu
 * @author zhangliang
 */
public final class ComplexRoutingResultValidator extends AbstractRoutingResultValidator {
    
    public ComplexRoutingResultValidator(final ShardingRule shardingRule, final ShardingSphereMetaData metaData) {
        super(shardingRule, metaData);
    }
    
    @Override
    protected void throwException(final ShardingOptimizedStatement shardingStatement, final ShardingConditions shardingConditions, final Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap) {
        RoutingUnit routingUnit = absentRoutingUnitMap.keySet().iterator().next();
        Collection<String> absentDataNodes = Lists.newArrayListWithExpectedSize(absentRoutingUnitMap.get(routingUnit).size());
        for (TableUnit each : absentRoutingUnitMap.get(routingUnit)) {
            absentDataNodes.add(routingUnit.getDataSourceName() + "." + each.getActualTableName());
        }
        if (!absentDataNodes.isEmpty()) {
            throwExceptionForAbsentDataNode(absentDataNodes, "");
        }
    }
    
    private void throwExceptionForAbsentDataNode(final Collection<String> absentDataNodes, final CharSequence detail) {
        String msg = "We get some absent DataNodes=" + absentDataNodes + " in routing result, " + (Strings.isNullOrEmpty(detail.toString()) ? "" : detail)
            + "please check the configuration of rule and data node.";
        throw new ShardingException(msg.replace("%", "%%"));
    }
}
