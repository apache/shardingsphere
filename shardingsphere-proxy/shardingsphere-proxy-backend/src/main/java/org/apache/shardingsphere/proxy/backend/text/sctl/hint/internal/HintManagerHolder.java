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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.hint.HintManager;

/**
 * Holder for {@code HintManager}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintManagerHolder {
    
    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();
    
    /**
     * Get a instance for {@code HintManager} from {@code ThreadLocal},if not exist,then create new one.
     *
     * @return hint manager
     */
    public static HintManager get() {
        if (HINT_MANAGER_HOLDER.get() == null) {
            HINT_MANAGER_HOLDER.set(HintManager.getInstance());
        }
        return HINT_MANAGER_HOLDER.get();
    }
    
    /**
     * remove {@code HintManager} from {@code ThreadLocal}.
     */
    public static void remove() {
        HINT_MANAGER_HOLDER.remove();
    }
}
