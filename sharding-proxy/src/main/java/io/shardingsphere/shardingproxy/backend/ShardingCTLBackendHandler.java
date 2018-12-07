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

package io.shardingsphere.shardingproxy.backend;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sharding CTL backend handler.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class ShardingCTLBackendHandler extends AbstractBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    @Override
    protected CommandResponsePackets execute0() {
        Optional<ShardingTCLStatement> shardingTCLStatement = new ShardingCTLParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            throw new ShardingException("please make review your sctl format, should be sctl:set xxx=yyy");
        }
        switch (shardingTCLStatement.get().key) {
            case "transaction_type":
                backendConnection.setTransactionType(TransactionType.find(shardingTCLStatement.get().value));
                break;
            default:
                throw new ShardingException(String.format("could not support this sctl grammar [%s]", sql));
        }
        return new CommandResponsePackets(new OKPacket(1));
    }
    
    public final class ShardingCTLParser {
    
        private final String regex = "sctl:set\\s+(\\S*)=(\\S*)";
        
        private Matcher matcher;
        
        ShardingCTLParser(final String sql) {
            this.matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sql);
        }
        
        Optional<ShardingTCLStatement> doParse() {
            if (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                Preconditions.checkNotNull(key, "sctl key cannot be null.");
                Preconditions.checkNotNull(value, "sctl value cannot be null.");
                return Optional.of(new ShardingTCLStatement(key, value));
            }
            return Optional.absent();
        }
    }
    
    @RequiredArgsConstructor
    private final class ShardingTCLStatement {
        
        private final String key;
        
        private final String value;
    }
}
