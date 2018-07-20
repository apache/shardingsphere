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

package io.shardingsphere.proxy.backend.common.jdbc.execute.response;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.QueryResponsePackets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JDBC execute query response.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JDBCExecuteQueryResponse implements JDBCExecuteResponse {
    
    private final QueryResponsePackets queryResponsePackets;
    
    @Getter
    private final QueryResult queryResult;
    
    @Override
    public QueryResponsePackets getCommandResponsePackets() {
        return queryResponsePackets;
    }
}
