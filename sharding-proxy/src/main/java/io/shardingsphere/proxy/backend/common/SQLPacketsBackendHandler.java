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

package io.shardingsphere.proxy.backend.common;

import io.netty.channel.Channel;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.proxy.backend.ShardingProxyClient;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.util.MySQLResultCache;
import io.shardingsphere.proxy.util.SynchronizedFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SQL packets backend handler.
 *
 * @author wangkai
 */
public final class SQLPacketsBackendHandler extends SQLExecuteBackendHandler {
    private SynchronizedFuture<CommandResponsePackets> synchronizedFuture;
    
    private final CommandPacket commandPacket;
    
    private int connectionId;
    
    public SQLPacketsBackendHandler(final CommandPacket commandPacket, final String sql, final int connectionId, final DatabaseType databaseType, final boolean showSQL) {
        super(sql, databaseType, showSQL);
        this.commandPacket = commandPacket;
        this.connectionId = connectionId;
    }
    
    @Override
    protected CommandResponsePackets executeForSharding() {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(RuleRegistry.getInstance().getShardingRule(), RuleRegistry.getInstance().getShardingMetaData(), getDatabaseType(),
                isShowSQL());
        SQLRouteResult routeResult = routingEngine.route(getSql());
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        synchronizedFuture = new SynchronizedFuture<>(routeResult.getExecutionUnits().size());
        MySQLResultCache.getInstance().put(connectionId, synchronizedFuture);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            execute(routeResult.getSqlStatement(), each.getDataSource(), each.getSqlUnit().getSql());
        }
        //TODO timeout should be set.
        CommandResponsePackets result = synchronizedFuture.get(30, TimeUnit.SECONDS);
        MySQLResultCache.getInstance().delete(connectionId);
        return result;
    }
    
    @Override
    protected CommandResponsePackets executeForMasterSlave() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected CommandResponsePackets execute(final SQLStatement sqlStatement, final String dataSourceName, final String sql) {
        Channel channel = ShardingProxyClient.getInstance().getChannelMap().get(dataSourceName);
        //MySQLResultCache.getInstance().putConnectionMap(channel.id().asShortText(), connectionId);
        switch (sqlStatement.getType()) {
            case DQL:
                executeQuery(channel, sql);
                break;
            case DML:
            case DDL:
                executeUpdate(channel, sql, sqlStatement);
                break;
            default:
                executeCommon(channel, sql);
        }
        return null;
    }
    
    private void executeQuery(final Channel channel, final String sql) {
        MySQLResultCache.getInstance().putConnectionMap(channel.id().asShortText(), connectionId);
        channel.writeAndFlush(commandPacket);
    }
    
    private void executeUpdate(final Channel channel, final String sql, final SQLStatement sqlStatement) {
        MySQLResultCache.getInstance().putConnectionMap(channel.id().asShortText(), connectionId);
        channel.writeAndFlush(commandPacket);
    }
    
    private void executeCommon(final Channel channel, final String sql) {
        MySQLResultCache.getInstance().putConnectionMap(channel.id().asShortText(), connectionId);
        channel.writeAndFlush(commandPacket);
    }
}
