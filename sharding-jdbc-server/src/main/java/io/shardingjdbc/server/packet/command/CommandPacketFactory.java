package io.shardingjdbc.server.packet.command;

/**
 * Command packet factory.
 *
 * @author zhangliang
 */
public final class CommandPacketFactory {
    
    public static CommandPacket getCommandPacket(final int commandPacketTypeValue) {
        CommandPacketType type = CommandPacketType.valueOf(commandPacketTypeValue);
        switch (type) {
            case COM_QUERY:
                return new ComQueryPacket();
            case COM_STMT_EXECUTE:
                return new ComStatExecutePacket();
            case COM_SLEEP:
            case COM_QUIT:
            case COM_INIT_DB:
            case COM_FIELD_LIST:
            case COM_CREATE_DB:
            case COM_DROP_DB:
            case COM_REFRESH:
            case COM_SHUTDOWN:
            case COM_STATISTICS:
            case COM_PROCESS_INFO:
            case COM_CONNECT:
            case COM_PROCESS_KILL:
            case COM_DEBUG:
            case COM_PING:
            case COM_TIME:
            case COM_DELAYED_INSERT:
            case COM_CHANGE_USER:
            case COM_BINLOG_DUMP:
            case COM_TABLE_DUMP:
            case COM_CONNECT_OUT:
            case COM_REGISTER_SLAVE:
            case COM_STMT_PREPARE:
            case COM_STMT_SEND_LONG_DATA:
            case COM_STMT_CLOSE:
            case COM_STMT_RESET:
            case COM_SET_OPTION:
            case COM_STMT_FETCH:
            case COM_DAEMON:
            case COM_BINLOG_DUMP_GTID:
            case COM_RESET_CONNECTION:
                return new UnsupportedCommandPacket(type);
            default:
                return new UnsupportedCommandPacket(type);
        }
    }
}
