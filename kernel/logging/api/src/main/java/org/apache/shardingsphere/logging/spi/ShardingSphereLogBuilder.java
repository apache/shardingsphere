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

package org.apache.shardingsphere.logging.spi;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;

import java.util.Collection;

/**
 * ShardingSphere log builder.
 * 
 * @param <T> type of logger context
 */
@SingletonSPI
public interface ShardingSphereLogBuilder<T> extends TypedSPI {
    
    /**
     * Get default loggers.
     *
     * @param loggerContext logger context
     * @return default loggers
     */
    Collection<ShardingSphereLogger> getDefaultLoggers(T loggerContext);
    
    /**
     * Get default appenders.
     *
     * @param loggerContext logger context
     * @return default appenders
     */
    Collection<ShardingSphereAppender> getDefaultAppenders(T loggerContext);
    
    @Override
    Class<T> getType();
}
