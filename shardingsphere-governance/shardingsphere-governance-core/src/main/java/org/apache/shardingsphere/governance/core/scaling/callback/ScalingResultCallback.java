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

package org.apache.shardingsphere.governance.core.scaling.callback;

import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.scaling.ScalingServiceHolder;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.scaling.core.service.ScalingCallback;

import java.util.concurrent.TimeUnit;

/**
 * Scaling result callback.
 */
public final class ScalingResultCallback implements ScalingCallback {
    
    private final String schemaName;
    
    private final String ruleConfigurationCacheId;
    
    public ScalingResultCallback(final String schemaName, final String ruleConfigurationCacheId) {
        this.schemaName = schemaName;
        this.ruleConfigurationCacheId = ruleConfigurationCacheId;
    }
    
    @Override
    public void onSuccess(final long jobId) {
        if (LockContext.getLockStrategy().tryLock(30L, TimeUnit.SECONDS) && LockContext.getLockStrategy().checkLock()) {
            try {
                Thread.sleep(30000L);
                if (ScalingServiceHolder.getInstance().checkScalingResult(jobId)) {
                    ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(schemaName, ruleConfigurationCacheId));
                }  
            } catch (final InterruptedException ignored) {
            } finally {
                LockContext.getLockStrategy().releaseLock();
            }
        }
    }
    
    @Override
    public void onFailure(final long jobId) {
        // TODO
    }
}
