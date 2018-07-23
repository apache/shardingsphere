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

package io.shardingsphere.proxy.util;

import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executor context.
 *
 * @author zhangliang
 */
public final class ExecutorContext {
    
    private static final ExecutorContext INSTANCE = new ExecutorContext();
    
    @Getter
    private final ExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(RuleRegistry.getInstance().getMaxWorkingThreads()));
    
    /**
     * Get executor context instance.
     * 
     * @return instance of executor context
     */
    public static ExecutorContext getInstance() {
        return INSTANCE;
    }
}
