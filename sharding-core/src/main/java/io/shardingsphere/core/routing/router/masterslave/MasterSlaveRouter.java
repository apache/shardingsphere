/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.router.masterslave;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.util.SQLLogger;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Master slave router interface.
 * 
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class MasterSlaveRouter {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final boolean showSQL;
    
    /**
     * Route Master slave.
     *
     * @param sql SQL
     * @return data source names
     */
    // TODO for multiple masters may return more than one data source
    public Collection<String> route(final String sql) {
        Collection<String> result = route(new SQLJudgeEngine(sql).judge().getType());
        if (showSQL) {
            SQLLogger.logSQL(sql, result);
        }
        return result;
    }
    
    private Collection<String> route(final SQLType sqlType) {
        if (isMasterRoute(sqlType)) {
            MasterVisitedManager.setMasterVisited();
            return Collections.singletonList(masterSlaveRule.getMasterDataSourceName());
        }
        return Collections.singletonList(masterSlaveRule.getLoadBalanceAlgorithm().getDataSource(
                masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), new ArrayList<>(masterSlaveRule.getSlaveDataSourceNames())));
    }
    
    private boolean isMasterRoute(final SQLType sqlType) {
        return SQLType.DQL != sqlType || MasterVisitedManager.isMasterVisited() || HintManagerHolder.isMasterRouteOnly();
    }
}
