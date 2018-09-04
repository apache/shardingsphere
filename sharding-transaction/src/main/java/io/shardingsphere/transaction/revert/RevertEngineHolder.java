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

package io.shardingsphere.transaction.revert;

import lombok.Getter;
import lombok.Setter;

/**
 * revert engine holder.
 * not thread safe
 *
 * @author yangyi
 */
public class RevertEngineHolder {
    
    private static final RevertEngineHolder INSTANCE = new RevertEngineHolder();
    
    @Setter
    @Getter
    private RevertEngine revertEngine;
    
    /**
     * get holder.
     *
     * @return holder
     */
    public static RevertEngineHolder getInstance() {
        return INSTANCE;
    }
    
}
