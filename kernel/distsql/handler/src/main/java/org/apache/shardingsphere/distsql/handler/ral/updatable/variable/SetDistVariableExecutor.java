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

package org.apache.shardingsphere.distsql.handler.ral.updatable.variable;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.InvalidVariableValueException;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedVariableException;
import org.apache.shardingsphere.infra.props.TypedPropertyKey;
import org.apache.shardingsphere.infra.props.TypedPropertyValue;
import org.apache.shardingsphere.infra.props.exception.TypedPropertyValueException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.quartz.CronExpression;

import java.util.Properties;

/**
 * Set dist variable statement executor.
 */
public final class SetDistVariableExecutor implements DistSQLUpdateExecutor<SetDistVariableStatement> {
    
    @Override
    public void executeUpdate(final SetDistVariableStatement sqlStatement, final ContextManager contextManager) {
        ShardingSpherePreconditions.checkState(getEnumType(sqlStatement.getName()) instanceof TypedPropertyKey, () -> new UnsupportedVariableException(sqlStatement.getName()));
        handleConfigurationProperty(contextManager, (TypedPropertyKey) getEnumType(sqlStatement.getName()), sqlStatement.getValue());
    }
    
    private Enum<?> getEnumType(final String name) {
        try {
            return ConfigurationPropertyKey.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            try {
                return TemporaryConfigurationPropertyKey.valueOf(name.toUpperCase());
            } catch (final IllegalArgumentException ignored) {
                throw new UnsupportedVariableException(name);
            }
        }
    }
    
    private void handleConfigurationProperty(final ContextManager contextManager, final TypedPropertyKey propertyKey, final String value) {
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        Properties props = new Properties();
        props.putAll(metaDataContexts.getMetaData().getProps().getProps());
        props.putAll(metaDataContexts.getMetaData().getTemporaryProps().getProps());
        props.put(propertyKey.getKey(), getValue(propertyKey, value));
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterProperties(props);
    }
    
    private Object getValue(final TypedPropertyKey propertyKey, final String value) {
        try {
            Object propertyValue = new TypedPropertyValue(propertyKey, value).getValue();
            checkProxyMetaDataCollectorCron(propertyKey, value);
            if (Enum.class.isAssignableFrom(propertyKey.getType())) {
                return propertyValue.toString();
            }
            return TypedSPI.class.isAssignableFrom(propertyKey.getType()) ? ((TypedSPI) propertyValue).getType().toString() : propertyValue;
        } catch (final TypedPropertyValueException ignored) {
            throw new InvalidVariableValueException(value);
        }
    }
    
    private void checkProxyMetaDataCollectorCron(final TypedPropertyKey propertyKey, final String value) {
        if (TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON == propertyKey) {
            ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(value) && CronExpression.isValidExpression(value), () -> new InvalidVariableValueException(value));
        }
    }
    
    @Override
    public Class<SetDistVariableStatement> getType() {
        return SetDistVariableStatement.class;
    }
}
