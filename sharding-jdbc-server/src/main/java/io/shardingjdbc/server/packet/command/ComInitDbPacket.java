package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.ok.ErrPacket;
import io.shardingjdbc.server.packet.ok.OKPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * COM_INIT_DB command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComInitDbPacket extends AbstractCommandPacket {
    
    private String schemaName;
    
    @Override
    public ComInitDbPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        schemaName = mysqlPacketPayload.readStringEOF();
        log.debug("Schema name received for Sharding-JDBC-server: {}", schemaName);
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        if (ShardingConstant.LOGIC_SCHEMA_NAME.equalsIgnoreCase(schemaName)) {
            return Collections.<AbstractMySQLSentPacket>singletonList(new OKPacket(getSequenceId() + 1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        return Collections.<AbstractMySQLSentPacket>singletonList(new ErrPacket(getSequenceId() + 1, 1049, "", "", String.format("Unknown database '%s'", schemaName)));
    }
}
