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
    OP_VOID(0), /* Packet has been voided */
    OP_CONNECT(1), /* Connect to remote server */
    OP_EXIT(2), /* Remote end has exited */
    OP_ACCEPT(3), /* Server accepts connection */
    OP_REJECT(4), /* Server rejects connection */
    OP_PROTOCOL(5), /* Protocol selection */
    OP_DISCONNECT(6), /* Connect is going away */
    OP_CREDIT(7), /* Grant (buffer) credits */
    OP_CONTINUATION(8), /* Continuation packet */
    OP_RESPONSE(9), /* Generic response block */

    /* Page server operations */
    OP_OPEN_FILE(10), /* Open file for page service */
    OP_CREATE_FILE(11), /* Create file for page service */
    OP_CLOSE_FILE(12), /* Close file for page service */
    OP_READ_PAGE(13), /* optionally lock and read page */
    OP_WRITE_PAGE(14), /* write page and optionally release lock */
    OP_LOCK(15), /* seize lock */
    OP_CONVERT_LOCK(16), /* convert existing lock */
    OP_RELEASE_LOCK(17), /* release existing lock */
    OP_BLOCKING(18), /* blocking lock message */

    /* Full context server operations */
    OP_ATTACH(19), /* Attach database */
    OP_CREATE(20), /* Create database */
    OP_DETACH(21), /* Detach database */

    /* Request based operations */
    OP_COMPILE(22),
    OP_START(23),
    OP_START_AND_SEND(24),
    OP_SEND(25),
    OP_RECEIVE(26),
    OP_UNWIND(27),
    OP_RELEASE(28),

    /* Transaction operations */
    OP_TRANSACTION(29),
    OP_COMMIT(30),
    OP_ROLLBACK(31),
    OP_PREPARE(32),
    OP_RECONNECT(33),

    /* Blob operations */
    OP_CREATE_BLOB(34),
    OP_OPEN_BLOB(35),
    OP_GET_SEGMENT(36),
    OP_PUT_SEGMENT(37),
    OP_CANCEL_BLOB(38),
    OP_CLOSE_BLOB(39),

    /* Information services */
    OP_INFO_DATABASE(40),
    OP_INFO_REQUEST(41),
    OP_INFO_TRANSACTION(42),
    OP_INFO_BLOB(43),

    OP_BATCH_SEGMENTS(44), /* Put a bunch of blob segments */
    OP_MGR_SET_AFFINITY(45), /* Establish server affinity */
    OP_MGR_CLEAR_AFFINITY(46), /* Break server affinity */
    OP_MGR_REPORT(47), /* Report on server */
    OP_QUE_EVENTS(48), /* Queue event notification request */
    OP_CANCEL_EVENTS(49), /* Cancel event notification request */
    OP_COMMIT_RETAINING(50), /* Commit retaining (what else) */
    OP_PREPARE2(51), /* Message form of prepare */
    OP_EVENT(52), /* Completed event request (asynchronous) */
    OP_CONNECT_REQUEST(53), /* Request to establish connection */
    OP_AUX_CONNECT(54), /* Establish auxiliary connection */
    OP_DDL(55), /* DDL call */
    OP_OPEN_BLOB2(56),
    OP_CREATE_BLOB2(57),
    OP_GET_SLICE(58),
    OP_PUT_SLICE(59),
    OP_SLICE(60), /* Successful response to int op_get_slice */
    OP_SEEK_BLOB(61), /* Blob seek operation */

    /* DSQL operations */
    OP_ALLOCATE_STATEMENT(62), /* allocate a statement handle */
    OP_EXECUTE(63), /* execute a prepared statement */
    OP_EXEC_IMMEDIATE(64), /* execute a statement */
    OP_FETCH(65), /* fetch a record */
    OP_FETCH_RESPONSE(66), /* response for record fetch */
    OP_FREE_STATEMENT(67), /* free a statement */
    OP_PREPARE_STATEMENT(68), /* prepare a statement */
    OP_SET_CURSOR(69), /* set a cursor name */
    OP_INFO_SQL(70),
    OP_DUMMY(71), /* dummy packet to detect loss of client */
    OP_RESPONSE_PIGGYBACK(72), /* response block for piggybacked messages */
    OP_START_AND_RECEIVE(73),
    OP_START_SEND_AND_RECEIVE(74),
    OP_EXEC_IMMEDIATE2(75), /* execute an immediate statement with msgs */
    OP_EXECUTE2(76), /* execute a statement with msgs */
    OP_INSERT(77),
    OP_SQL_RESPONSE(78), /* response from execute; exec immed; insert */

    OP_TRANSACT(79),
    OP_TRANSACT_RESPONSE(80),

    OP_DROP_DATABASE(81),

    OP_SERVICE_ATTACH(82),
    OP_SERVICE_DETACH(83),
    OP_SERVICE_INFO(84),
    OP_SERVICE_START(85),

    OP_ROLLBACK_RETAINING(86),

    /* Two following opcode are used in vulcan.
       No plans to implement them completely for a while, but to
       support protocol 11, where they are used, have them here. */
    OP_UPDATE_ACCOUNT_INFO(87),
    OP_AUTHENTICATE_USER(88),

    OP_PARTIAL(89),   /* packet is not complete - delay processing */
    OP_TRUSTED_AUTH(90),

    OP_CANCEL(91),

    OP_CONT_AUTH(92),

    OP_PING(93),

    OP_ACCEPT_DATA(94),   /* Server accepts connection and returns some data to client */

    OP_ABORT_AUX_CONNECTION(95),   /* Async operation - stop waiting for async connection to arrive */

    OP_CRYPT(96),
    OP_CRYPT_KEY_CALLBACK(97),
    OP_COND_ACCEPT(98),  /* Server accepts connection, returns some data to client
                                 and asks client to continue authentication before attach call */

    OP_BATCH_CREATE(99),
    OP_BATCH_MSG(100),
    OP_BATCH_EXEC(101),
    OP_BATCH_RLS(102),
    OP_BATCH_CS(103),
    OP_BATCH_REGBLOB(104),
    OP_BATCH_BLOB_STREAM(105),
    OP_BATCH_SET_BPB(106),

    OP_REPL_DATA(107),
    OP_REPL_REQ(108),

    OP_BATCH_CANCEL(109),
    OP_BATCH_SYNC(110),
    OP_INFO_BATCH(111),

    OP_FETCH_SCROLL(112),
    OP_INFO_CURSOR(113);

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