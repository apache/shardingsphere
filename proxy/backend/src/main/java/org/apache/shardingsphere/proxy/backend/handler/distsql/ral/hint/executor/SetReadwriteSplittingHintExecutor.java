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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.enums.HintSourceType;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.HintManagerHolder;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;

/**
 * Set readwrite-splitting hint statement executor.
 */
@RequiredArgsConstructor
public final class SetReadwriteSplittingHintExecutor extends AbstractHintUpdateExecutor<SetReadwriteSplittingHintStatement> {
    
    private final SetReadwriteSplittingHintStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() {
        HintSourceType sourceType = HintSourceType.typeOf(sqlStatement.getSource());
        switch (sourceType) {
            case AUTO:
                HintManagerHolder.get().setReadwriteSplittingAuto();
                break;
            case WRITE:
                HintManagerHolder.get().setWriteRouteOnly();
                break;
            default:
                break;
        }
        return new UpdateResponseHeader(new EmptyStatement());
    }
}
