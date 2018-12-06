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

package io.shardingsphere.shardingproxy.frontend.mysql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command executor context.
 *
 * @author wuxu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandExecutorContext {
    
    private static final CommandExecutorContext INSTANCE = new CommandExecutorContext();
    
    @Getter
    private final CommandExecuteEngine commandExecuteEngine = new CommandExecuteEngine();
    
    /**
     * Get command executor context instance.
     *
     * @return instance of executor context
     */
    public static CommandExecutorContext getInstance() {
        return INSTANCE;
    }
}
