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

package org.apache.shardingsphere.mode.manager.lock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;

/**
 * Lock judge engine builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockJudgeEngineBuilder {
    
    static {
        ShardingSphereServiceLoader.register(LockJudgeEngine.class);
    }
    
    /**
     * Build.
     *
     * @param lockContext lock context
     * @return lock judge engine
     */
    public static LockJudgeEngine build(final LockContext lockContext) {
        LockJudgeEngine result = RequiredSPIRegistry.getRegisteredService(LockJudgeEngine.class);
        result.init(lockContext);
        return result;
    }
}
