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

package org.apache.shardingsphere.agent.plugin.logging.file.advice;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.recorder.MethodTimeRecorder;

/**
 * Meta data contexts factory advice.
 */
@Slf4j
public final class MetaDataContextsFactoryAdvice extends AbstractInstanceMethodAdvice {
    
    private final MethodTimeRecorder methodTimeRecorder = new MethodTimeRecorder(MetaDataContextsFactoryAdvice.class);
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
        methodTimeRecorder.recordNow(method);
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
        log.info("Build meta data contexts finished, cost {} milliseconds.", methodTimeRecorder.getElapsedTimeAndClean(method));
    }
}
