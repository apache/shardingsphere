package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors;

import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

public class FirebirdBatchSyncCommandExecutor implements CommandExecutor {

    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
}
