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

package org.apache.shardingsphere.db.protocol.firebird.packet.command;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.command.CommandPacketType;

import java.util.HashMap;
import java.util.Map;

/**
 * Command packet type for Firebird.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdCommandPacketType implements CommandPacketType {

    /* Operation (packet) types */
    VOID(0), /* Packet has been voided */
    CONNECT(1), /* Connect to remote server */
    EXIT(2), /* Remote end has exited */
    ACCEPT(3), /* Server accepts connection */
    REJECT(4), /* Server rejects connection */
    PROTOCOL(5), /* Protocol selection */
    DISCONNECT(6), /* Connect is going away */
    CREDIT(7), /* Grant (buffer) credits */
    CONTINUATION(8), /* Continuation packet */
    RESPONSE(9), /* Generic response block */

    /* Page server operations */
    OPEN_FILE(10), /* Open file for page service */
    CREATE_FILE(11), /* Create file for page service */
    CLOSE_FILE(12), /* Close file for page service */
    READ_PAGE(13), /* optionally lock and read page */
    WRITE_PAGE(14), /* write page and optionally release lock */
    LOCK(15), /* seize lock */
    CONVERT_LOCK(16), /* convert existing lock */
    RELEASE_LOCK(17), /* release existing lock */
    BLOCKING(18), /* blocking lock message */

    /* Full context server operations */
    ATTACH(19), /* Attach database */
    CREATE(20), /* Create database */
    DETACH(21), /* Detach database */

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

    BATCH_SEGMENTS(44), /* Put a bunch of blob segments */
    MGR_SET_AFFINITY(45), /* Establish server affinity */
    MGR_CLEAR_AFFINITY(46), /* Break server affinity */
    MGR_REPORT(47), /* Report on server */
    QUE_EVENTS(48), /* Queue event notification request */
    CANCEL_EVENTS(49), /* Cancel event notification request */
    COMMIT_RETAINING(50), /* Commit retaining (what else) */
    PREPARE2(51), /* Message form of prepare */
    EVENT(52), /* Completed event request (asynchronous) */
    CONNECT_REQUEST(53), /* Request to establish connection */
    AUX_CONNECT(54), /* Establish auxiliary connection */
    DDL(55), /* DDL call */
    OPEN_BLOB2(56),
    CREATE_BLOB2(57),
    GET_SLICE(58),
    PUT_SLICE(59),
    SLICE(60), /* Successful response to int get_slice */
    SEEK_BLOB(61), /* Blob seek operation */

    /* DSQL operations */
    ALLOCATE_STATEMENT(62), /* allocate a statement handle */
    EXECUTE(63), /* execute a prepared statement */
    EXEC_IMMEDIATE(64), /* execute a statement */
    FETCH(65), /* fetch a record */
    FETCH_RESPONSE(66), /* response for record fetch */
    FREE_STATEMENT(67), /* free a statement */
    PREPARE_STATEMENT(68), /* prepare a statement */
    SET_CURSOR(69), /* set a cursor name */
    INFO_SQL(70),
    DUMMY(71), /* dummy packet to detect loss of client */
    RESPONSE_PIGGYBACK(72), /* response block for piggybacked messages */
    START_AND_RECEIVE(73),
    START_SEND_AND_RECEIVE(74),
    EXEC_IMMEDIATE2(75), /* execute an immediate statement with msgs */
    EXECUTE2(76), /* execute a statement with msgs */
    INSERT(77),
    SQL_RESPONSE(78), /* response from execute; exec immed; insert */

    TRANSACT(79),
    TRANSACT_RESPONSE(80),

    DRDATABASE(81),

    SERVICE_ATTACH(82),
    SERVICE_DETACH(83),
    SERVICE_INFO(84),
    SERVICE_START(85),

    ROLLBACK_RETAINING(86),

    /* Two following opcode are used in vulcan.
       No plans to implement them completely for a while, but to
       support protocol 11, where they are used, have them here. */
    UPDATE_ACCOUNT_INFO(87),
    AUTHENTICATE_USER(88),

    PARTIAL(89),   /* packet is not complete - delay processing */
    TRUSTED_AUTH(90),

    CANCEL(91),

    CONT_AUTH(92),

    PING(93),

    ACCEPT_DATA(94),   /* Server accepts connection and returns some data to client */

    ABORT_AUX_CONNECTION(95),   /* Async operation - stop waiting for async connection to arrive */

    CRYPT(96),
    CRYPT_KEY_CALLBACK(97),
    COND_ACCEPT(98),  /* Server accepts connection, returns some data to client
                                 and asks client to continue authentication before attach call */

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