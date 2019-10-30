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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.SQLParseEngineFactory;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.util.ConfigurationLogger;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.Properties;

/**
 * Abstract runtime context.
 *
 * @author zhangliang
 * 
 * @param <T> type of rule
 */
@Getter
public abstract class AbstractRuntimeContext<T extends BaseRule> implements RuntimeContext<T> {
    
    private final T rule;
    
    private final ShardingProperties props;
    
    private final DatabaseType databaseType;
    
    private final ShardingExecuteEngine executeEngine;
    
    private final SQLParseEngine parseEngine;
    
    protected AbstractRuntimeContext(final T rule, final Properties props, final DatabaseType databaseType) {
        this.rule = rule;
        this.props = new ShardingProperties(null == props ? new Properties() : props);
        this.databaseType = databaseType;
        executeEngine = new ShardingExecuteEngine(this.props.<Integer>getValue(ShardingPropertiesConstant.EXECUTOR_SIZE));
        parseEngine = SQLParseEngineFactory.getSQLParseEngine(databaseType);
        ConfigurationLogger.log(rule.getRuleConfiguration());
        ConfigurationLogger.log(props);
    }
    
    @Override
    public void close() throws Exception {
        executeEngine.close();
    }
}
