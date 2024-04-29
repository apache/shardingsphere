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

package org.apache.shardingsphere.agent.plugin.tracing.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;

import java.lang.reflect.Method;

/**
 * Tracing SQL parser engine advice executor.
 * 
 * @param <T> type of root span
 */
public abstract class TracingSQLParserEngineAdvice<T> extends AbstractInstanceMethodAdvice {
    
    protected static final String OPERATION_NAME = "/ShardingSphere/parseSQL/";
    
    @Override
    public final void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        recordSQLParseInfo(RootSpanContext.get(), target, String.valueOf(args[0]));
    }
    
    protected abstract Object recordSQLParseInfo(T parentSpan, TargetAdviceObject target, String sql);
}
