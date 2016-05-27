/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.RoundRobinSlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.slave.SlaveLoadBalanceStrategy;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 读写分离配置规则.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class MasterSlaveRule {
    
    private static final ThreadLocal<Boolean> WAS_UPDATED = new ThreadLocal<Boolean>() {
        
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    private final String logicDataSource;
    
    private final String masterDataSource;
    
    private final List<String> slaveDataSources;
    
    private final SlaveLoadBalanceStrategy slaveLoadBalanceStrategy = new RoundRobinSlaveLoadBalanceStrategy();
    
    /**
     * 获取主或从节点的数据源名称.
     * 
     * @param sqlStatementType SQL类型
     * @return 主或从节点的数据源名称
     */
    public String getMasterOrSlaveDataSource(final SQLStatementType sqlStatementType) {
        if (SQLStatementType.SELECT != sqlStatementType || WAS_UPDATED.get()) {
            WAS_UPDATED.set(true);
            return masterDataSource;
        }
        return slaveLoadBalanceStrategy.getDataSource(logicDataSource, slaveDataSources);
    }
    
    /**
     * 判断该真实数据源是否属于本读写分离规则配置.
     * 
     * @param actualDataSource 真实数据源名称
     * @return 是否属于本读写分离规则配置
     */
    public boolean within(final String actualDataSource) {
        return actualDataSource.equals(masterDataSource) || slaveDataSources.contains(actualDataSource);
    }
}
