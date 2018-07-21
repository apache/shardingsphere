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

package io.shardingsphere.proxy.metadata;

import io.shardingsphere.core.metadata.AbstractRefreshHandler;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Refresh table metadata of proxy sharding.
 *
 * @author zhaojun
 */
@Slf4j
public final class ProxyShardingRefreshHandler extends AbstractRefreshHandler {
    
    private ProxyShardingRefreshHandler(final SQLStatement sqlStatement, final ShardingMetaData shardingMetaData, final ShardingRule shardingRule) {
        super(sqlStatement, shardingMetaData, shardingRule);
    }
    
    @Override
    public void execute() {
        if (isNeedRefresh()) {
            String logicTable = getSqlStatement().getTables().getSingleTableName();
            TableRule tableRule = getShardingRule().getTableRule(logicTable);
            getShardingMetaData().refresh(tableRule, getShardingRule());
        }
    }
    
    /**
     * create new instance of {@code ProxyShardingRefreshHandler}.
     *
     * @param sqlStatement SQL statement
     * @return {@code ProxyShardingRefreshHandler}
     */
    public static ProxyShardingRefreshHandler build(final SQLStatement sqlStatement) {
        return new ProxyShardingRefreshHandler(sqlStatement, RuleRegistry.getInstance().getShardingMetaData(), RuleRegistry.getInstance().getShardingRule());
    }
}
