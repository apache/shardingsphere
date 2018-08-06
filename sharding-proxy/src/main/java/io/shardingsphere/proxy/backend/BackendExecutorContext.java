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

package io.shardingsphere.proxy.backend;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.Getter;

import java.util.concurrent.Executors;

/**
 * Backend executor context.
 *
 * @author zhangliang
 */
public final class BackendExecutorContext {
    
    private static final BackendExecutorContext INSTANCE = new BackendExecutorContext();
    
    @Getter
    private final ListeningExecutorService executorService;
    
    private BackendExecutorContext() {
        int executorSize = RuleRegistry.getInstance().getExecutorSize();
        executorService = 0 == executorSize ? MoreExecutors.listeningDecorator(Executors.newCachedThreadPool()) : MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(executorSize));
    }
    
    /**
     * Get backend executor context instance.
     * 
     * @return instance of executor context
     */
    public static BackendExecutorContext getInstance() {
        return INSTANCE;
    }
}
