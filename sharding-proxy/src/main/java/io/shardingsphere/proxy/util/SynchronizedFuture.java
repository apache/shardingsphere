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

package io.shardingsphere.proxy.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;

/**
 * sync get multiple netty return.
 *
 * @author wangkai
 */
public class SynchronizedFuture<T> implements Future<CommandResponsePackets> {
    private boolean merged;
    
    private CountDownLatch latch;
    
    private List<CommandResponsePackets> responses;
    
    private long beginTime = System.currentTimeMillis();
    
    public SynchronizedFuture(final int resultSize) {
        latch = new CountDownLatch(resultSize);
        responses = Lists.newArrayListWithCapacity(resultSize);
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public boolean isDone() {
        return merged ? true : false;
    }
    
    @Override
    public CommandResponsePackets get() throws InterruptedException {
        latch.await();
        return merge();
    }
    
    /**
     * wait for responses.
     * @param timeout wait timeout.
     * @param unit time unit
     * @return responses.
     */
    @Override
    public CommandResponsePackets get(final long timeout, final TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            //TODO
        }
        return merge();
    }
    
    /**
     * set response and count down.
     * @param response sql command result.
     */
    public void setResponse(final CommandResponsePackets response) {
        responses.add(response);
        latch.countDown();
    }
    
    private CommandResponsePackets merge() {
        boolean isOkPacket = false;
        int affectedRows = 0;
        long lastInsertId = 0;
        int sequenceId = 0;
        CommandResponsePackets headPackets = new CommandResponsePackets();
        for (CommandResponsePackets each : responses) {
            headPackets.addPacket(each.getHeadPacket());
        }
        
        for (DatabaseProtocolPacket each : headPackets.getDatabaseProtocolPackets()) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
            if (each instanceof OKPacket) {
                isOkPacket = true;
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
                lastInsertId = okPacket.getLastInsertId();
                sequenceId = okPacket.getSequenceId();
            }
        }
        if (isOkPacket) {
            return new CommandResponsePackets(new OKPacket(sequenceId, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        
        CommandResponsePackets result = new CommandResponsePackets();
        FieldCountPacket fieldCountPacket = (FieldCountPacket) headPackets.getHeadPacket();
        result.addPacket(fieldCountPacket);
        boolean isHeadReaded = false;
        for (CommandResponsePackets each : responses) {
            Iterator<DatabaseProtocolPacket> databaseProtocolPackets = each.getDatabaseProtocolPackets().iterator();
            
            List<DatabaseProtocolPacket> columnDefinitionAndEOFPackets = Collections.emptyList();
            for (int i = 0; i < fieldCountPacket.getColumnCount(); i++) {
                MySQLPacket columnDefinitionPacket = (MySQLPacket) databaseProtocolPackets.next();
                columnDefinitionPacket.setSequenceId(++sequenceId);
                columnDefinitionAndEOFPackets.add(columnDefinitionPacket); // ColumnDefinition41Packet
            }
            MySQLPacket eofPacket = (MySQLPacket) databaseProtocolPackets.next();
            eofPacket.setSequenceId(++sequenceId);
            columnDefinitionAndEOFPackets.add(eofPacket); // EofPacket
            
            List<DatabaseProtocolPacket> textResultSetRowPackets = Collections.emptyList();
            if (databaseProtocolPackets.hasNext()) {
                while (databaseProtocolPackets.hasNext()) {
                    MySQLPacket textResultSetRowPacket = (MySQLPacket) databaseProtocolPackets.next();
                    textResultSetRowPacket.setSequenceId(++sequenceId);
                    textResultSetRowPackets.add(textResultSetRowPacket); // TextResultSetRowPacket
                }
                textResultSetRowPackets.remove(textResultSetRowPackets.size() - 1); // remove EofPacket
            }
            
            if (!isHeadReaded) {
                isHeadReaded = true;
                result.addPackets(columnDefinitionAndEOFPackets);
            } else {
                result.addPackets(textResultSetRowPackets);
            }
        }
        result.addPacket(new EofPacket(++sequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        
        merged = true;
        return result;
    }
}
