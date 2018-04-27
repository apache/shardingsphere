/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.routing.router;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Master slave router interface.
 * 
 * @author zhangiang
 */
@RequiredArgsConstructor
public final class MasterSlaveRouter {
    
    private static final ThreadLocal<Boolean> DML_FLAG = new ThreadLocal<Boolean>() {
        
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    private final Collection<MasterSlaveRule> masterSlaveRules;
    
    /**
     * Route Master slave after sharding.
     * 
     * @param sqlRouteResult SQL route result
     * @return parse result
     */
    public SQLRouteResult route(final SQLRouteResult sqlRouteResult) {
        for (MasterSlaveRule each : masterSlaveRules) {
            route(each, sqlRouteResult);
        }
        return sqlRouteResult;
    }
    
    private void route(final MasterSlaveRule masterSlaveRule, final SQLRouteResult sqlRouteResult) {
        Collection<SQLExecutionUnit> toBeRemoved = new LinkedList<>();
        Collection<SQLExecutionUnit> toBeAdded = new LinkedList<>();
        for (SQLExecutionUnit each : sqlRouteResult.getExecutionUnits()) {
            if (!masterSlaveRule.getName().equalsIgnoreCase(each.getDataSource())) {
                continue;
            }
            toBeRemoved.add(each);
            if (isMasterRoute(sqlRouteResult.getSqlStatement().getType())) {
                DML_FLAG.set(true);
                toBeAdded.add(new SQLExecutionUnit(masterSlaveRule.getMasterDataSourceName(), each.getSqlUnit()));
            } else {
                toBeAdded.add(new SQLExecutionUnit(masterSlaveRule.getLoadBalanceAlgorithm().getDataSource(
                        masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), new ArrayList<>(masterSlaveRule.getSlaveDataSourceNames())), each.getSqlUnit()));
            }
        }
        sqlRouteResult.getExecutionUnits().removeAll(toBeRemoved);
        sqlRouteResult.getExecutionUnits().addAll(toBeAdded);
    }
    
    private boolean isMasterRoute(final SQLType sqlType) {
        return SQLType.DQL != sqlType || DML_FLAG.get() || HintManagerHolder.isMasterRouteOnly();
    }
    
    /**
     * reset DML flag.
     */
    public static void resetDMLFlag() {
        DML_FLAG.remove();
    }
}
