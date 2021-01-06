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

package org.apache.shardingsphere.ha.mgr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.ha.spi.HAType;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public final class MGRPeriodicalJob implements SimpleJob {
    
    private final HAType haType;
    
    private final Map<String, DataSource> originalDataSourceMap;
    
    private final String schemaName;
    
    private final Collection<String> disabledDataSourceNames;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(originalDataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            activeDataSourceMap.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        log.info(" +++ " + activeDataSourceMap.toString());
        haType.updatePrimaryDataSource(originalDataSourceMap, schemaName, disabledDataSourceNames);
        haType.updateMemberState(originalDataSourceMap, schemaName, disabledDataSourceNames);
    }
}
