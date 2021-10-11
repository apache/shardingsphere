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

package org.apache.shardingsphere.scaling.distsql.handler;

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Check scaling query result set.
 */
public final class CheckScalingQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        CheckScalingStatement checkScalingStatement = (CheckScalingStatement) sqlStatement;
        Map<String, DataConsistencyCheckResult> checkResultMap;
        if (null == checkScalingStatement.getTypeStrategy()) {
            checkResultMap = ScalingAPIFactory.getScalingAPI().dataConsistencyCheck(checkScalingStatement.getJobId());
        } else {
            checkResultMap = ScalingAPIFactory.getScalingAPI().dataConsistencyCheck(checkScalingStatement.getJobId(), checkScalingStatement.getTypeStrategy().getName());
        }
        data = checkResultMap.entrySet().stream()
                .map(each -> {
                    Collection<Object> list = new LinkedList<>();
                    list.add(each.getKey());
                    list.add(each.getValue().getSourceCount());
                    list.add(each.getValue().getTargetCount());
                    list.add(each.getValue().isCountValid() + "");
                    list.add(each.getValue().isDataValid() + "");
                    return list;
                }).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "source_count", "target_count", "count_valid", "data_valid");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return CheckScalingStatement.class.getCanonicalName();
    }
}
