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
import org.apache.shardingsphere.core.log.ConfigurationLogger;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.engine.ExecutorEngine;

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
    
    private final ShardingSphereProperties properties;
    
    private final DatabaseType databaseType;
    
    private final ExecutorEngine executorEngine;
    
    private final SQLParseEngine parseEngine;
    
    protected AbstractRuntimeContext(final T rule, final Properties props, final DatabaseType databaseType) {
        this.rule = rule;
        this.properties = new ShardingSphereProperties(null == props ? new Properties() : props);
        this.databaseType = databaseType;
        executorEngine = new ExecutorEngine(properties.<Integer>getValue(PropertiesConstant.EXECUTOR_SIZE));
        parseEngine = SQLParseEngineFactory.getSQLParseEngine(DatabaseTypes.getTrunkDatabaseTypeName(databaseType));
        ConfigurationLogger.log(rule.getRuleConfiguration());
        ConfigurationLogger.log(props);
    }
    
    protected abstract ShardingSphereMetaData getMetaData();
    
    @Override
    public void close() throws Exception {
        executorEngine.close();
    }
}
