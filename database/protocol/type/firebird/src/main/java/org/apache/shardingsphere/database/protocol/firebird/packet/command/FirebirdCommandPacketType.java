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

package org.apache.shardingsphere.database.protocol.firebird.packet.command;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;

import java.util.HashMap;
import java.util.Map;

/**
 * Command packet type for Firebird.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdCommandPacketType implements CommandPacketType {
    
    /* Operation (packet) types */
    VOID(0),
    CONNECT(1),
    EXIT(2),
    ACCEPT(3),
    REJECT(4),
    PROTOCOL(5),
    DISCONNECT(6),
    CREDIT(7),
    CONTINUATION(8),
    RESPONSE(9),
    
    /* Page server operations */
    OPEN_FILE(10),
    CREATE_FILE(11),
    CLOSE_FILE(12),
    READ_PAGE(13),
    WRITE_PAGE(14),
    LOCK(15),
    CONVERT_LOCK(16),
    RELEASE_LOCK(17),
    BLOCKING(18),
    
    /* Full context server operations */
    ATTACH(19),
    CREATE(20),
    DETACH(21),
    
    /* Request based operations */
    COMPILE(22),
    START(23),
    START_AND_SEND(24),
    SEND(25),
    RECEIVE(26),
    UNWIND(27),
    RELEASE(28),
    
    /* Transaction operations */
    TRANSACTION(29),
    COMMIT(30),
    ROLLBACK(31),
    PREPARE(32),
    RECONNECT(33),
    
    /* Blob operations */
    CREATE_BLOB(34),
    OPEN_BLOB(35),
    GET_SEGMENT(36),
    PUT_SEGMENT(37),
    CANCEL_BLOB(38),
    CLOSE_BLOB(39),
    
    /* Information services */
    INFO_DATABASE(40),
    INFO_REQUEST(41),
    INFO_TRANSACTION(42),
    INFO_BLOB(43),
    
    BATCH_SEGMENTS(44),
    MGR_SET_AFFINITY(45),
    MGR_CLEAR_AFFINITY(46),
    MGR_REPORT(47),
    QUE_EVENTS(48),
    CANCEL_EVENTS(49),
    COMMIT_RETAINING(50),
    PREPARE2(51),
    EVENT(52),
    CONNECT_REQUEST(53),
    AUX_CONNECT(54),
    DDL(55),
    OPEN_BLOB2(56),
    CREATE_BLOB2(57),
    GET_SLICE(58),
    PUT_SLICE(59),
    SLICE(60),
    SEEK_BLOB(61),
    
    /* DSQL operations */
    ALLOCATE_STATEMENT(62),
    EXECUTE(63),
    EXEC_IMMEDIATE(64),
    FETCH(65),
    FETCH_RESPONSE(66),
    FREE_STATEMENT(67),
    PREPARE_STATEMENT(68),
    SET_CURSOR(69),
    INFO_SQL(70),
    DUMMY(71),
    RESPONSE_PIGGYBACK(72),
    START_AND_RECEIVE(73),
    START_SEND_AND_RECEIVE(74),
    EXEC_IMMEDIATE2(75),
    EXECUTE2(76),
    INSERT(77),
    SQL_RESPONSE(78),
    
    TRANSACT(79),
    TRANSACT_RESPONSE(80),
    
    DRDATABASE(81),
    
    SERVICE_ATTACH(82),
    SERVICE_DETACH(83),
    SERVICE_INFO(84),
    SERVICE_START(85),
    
    ROLLBACK_RETAINING(86),
    
    /*
     * Two following opcode are used in vulcan. No plans to implement them completely for a while, but to support protocol 11, where they are used, have them here.
     */
    UPDATE_ACCOUNT_INFO(87),
    AUTHENTICATE_USER(88),
    
    PARTIAL(89),
    TRUSTED_AUTH(90),
    
    CANCEL(91),
    
    CONT_AUTH(92),
    
    PING(93),
    
    ACCEPT_DATA(94),
    
    ABORT_AUX_CONNECTION(95),
    
    CRYPT(96),
    CRYPT_KEY_CALLBACK(97),
    COND_ACCEPT(98),
    
    BATCH_CREATE(99),
    BATCH_MSG(100),
    BATCH_EXEC(101),
    BATCH_RLS(102),
    BATCH_CS(103),
    BATCH_REGBLOB(104),
    BATCH_BLOB_STREAM(105),
    BATCH_SET_BPB(106),
    
    REPL_DATA(107),
    REPL_REQ(108),
    
    BATCH_CANCEL(109),
    BATCH_SYNC(110),
    INFO_BATCH(111),
    
    FETCH_SCROLL(112),
    INFO_CURSOR(113);
    
    private static final Map<Integer, FirebirdCommandPacketType> FIREBIRD_COMMAND_PACKET_TYPE_CACHE = new HashMap<>();
    
    private final int value;
    
    static {
        for (FirebirdCommandPacketType each : values()) {
            FIREBIRD_COMMAND_PACKET_TYPE_CACHE.put(each.value, each);
        }
    }
    
    /**
     * Value of integer.
     *
     * @param value integer value
     * @return command packet type enum
     */
    public static FirebirdCommandPacketType valueOf(final int value) {
        FirebirdCommandPacketType result = FIREBIRD_COMMAND_PACKET_TYPE_CACHE.get(value);
        Preconditions.checkNotNull(result, "Cannot find '%s' in command packet type", value);
        return result;
    }
}
