/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.backend.common;

import io.netty.channel.Channel;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.proxy.backend.ShardingProxyClient;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingjdbc.proxy.util.MySQLResultCache;
import io.shardingjdbc.proxy.util.SynchronizedFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SQL packets backend handler.
 *
 * @author wangkai
 */
public final class SQLPacketsBackendHandler extends SQLExecuteBackendHandler {
    private SynchronizedFuture<CommandResponsePackets> synchronizedFuture;
    
    private int connectionId;
    
    public SQLPacketsBackendHandler(final String sql, final int connectionId, final DatabaseType databaseType, final boolean showSQL) {
        super(sql, databaseType, showSQL);
        this.connectionId = connectionId;
    }
    
    @Override
    public CommandResponsePackets execute() {
        SQLRouteResult routeResult = routingEngine.route(sql);
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        synchronizedFuture = new SynchronizedFuture<>(routeResult.getExecutionUnits().size());
        MySQLResultCache.getInstance().put(connectionId, synchronizedFuture);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            execute(routeResult.getSqlStatement(), each);
        }
        //TODO timeout should be set.
        List<CommandResponsePackets> result = synchronizedFuture.get(30, TimeUnit.SECONDS);
        MySQLResultCache.getInstance().delete(connectionId);
        return merge(routeResult.getSqlStatement(), result);
    }
    
    @Override
    protected CommandResponsePackets execute(final SQLStatement sqlStatement, final SQLExecutionUnit sqlExecutionUnit) {
        Channel channel = ShardingProxyClient.getInstance().getChannelMap().get(sqlExecutionUnit.getDataSource());
        //MySQLResultCache.getInstance().putConnectionMap(channel.id().asShortText(), connectionId);
        switch (sqlStatement.getType()) {
            case DQL:
                executeQuery(channel, sqlExecutionUnit.getSqlUnit().getSql());
            case DML:
            case DDL:
                executeUpdate(channel, sqlExecutionUnit.getSqlUnit().getSql(), sqlStatement);
            default:
                executeCommon(channel, sqlExecutionUnit.getSqlUnit().getSql());
        }
        return null;
    }
    
    //TODO
    private void executeQuery(final Channel channel, final String sql) {
        channel.writeAndFlush("");
        
    }
    
    //TODO
    private void executeUpdate(final Channel channel, final String sql, final SQLStatement sqlStatement) {
    }
    
    //TODO
    private void executeCommon(final Channel channel, final String sql) {
    }
}
