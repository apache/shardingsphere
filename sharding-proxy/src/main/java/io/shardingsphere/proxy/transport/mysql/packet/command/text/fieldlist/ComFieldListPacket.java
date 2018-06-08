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

package io.shardingsphere.proxy.transport.mysql.packet.command.text.fieldlist;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingsphere.proxy.backend.common.SQLPacketsBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import lombok.extern.slf4j.Slf4j;

/**
 * COM_FIELD_LIST command packet.
 *
 * @author zhangliang
 * @author wangkai
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-field-list.html">COM_FIELD_LIST</a>
 */
@Slf4j
public final class ComFieldListPacket extends CommandPacket {
    
    private final String table;
    
    private final String fieldWildcard;
    
    public ComFieldListPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId, connectionId);
        table = mysqlPacketPayload.readStringNul();
        fieldWildcard = mysqlPacketPayload.readStringEOF();
        setSql(String.format("SHOW COLUMNS FROM %s FROM %s", table, ShardingConstant.LOGIC_SCHEMA_NAME));
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(CommandPacketType.COM_FIELD_LIST.getValue());
        mysqlPacketPayload.writeStringNul(table);
        mysqlPacketPayload.writeStringEOF(fieldWildcard);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("table name received for Sharding-Proxy: {}", table);
        log.debug("field wildcard received for Sharding-Proxy: {}", fieldWildcard);
        // TODO use common database type
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            return new SQLPacketsBackendHandler(this, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL()).execute();
        } else {
            return new SQLExecuteBackendHandler(getSql(), DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL()).execute();
        }
    }
    
    @Override
    public boolean hasMoreResultValue() {
        return false;
    }
    
    @Override
    public DatabaseProtocolPacket getResultValue() {
        return null;
    }
}
