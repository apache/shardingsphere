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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommandExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintManagerHolder;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintType;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetCommand;

/**
 * Hint Set command executor.
 *
 * @author liya
 */
@RequiredArgsConstructor
public final class HintSetExecutor implements HintCommandExecutor {
    
    private final HintSetCommand command;
    
    @Override
    public BackendResponse execute() {
        HintType hintType = command.getHintType();
        switch (hintType) {
            case MASTER_ONLY:
                HintManagerHolder.get().setMasterRouteOnly();
                break;
            default:
                break;
        }
        return new UpdateResponse();
    }
}
