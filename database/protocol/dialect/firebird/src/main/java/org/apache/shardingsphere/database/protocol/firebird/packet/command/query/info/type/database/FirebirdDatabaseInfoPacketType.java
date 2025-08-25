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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird database info packet type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdDatabaseInfoPacketType implements FirebirdInfoPacketType {
    
    DB_ID(4),
    READS(5),
    WRITES(6),
    FETCHES(7),
    MARKS(8),
    ISC_IMPLEMENTATION(11),
    VERSION(12),
    BASE_LEVEL(13),
    PAGE_SIZE(14),
    NUM_BUFFERS(15),
    LIMBO(16),
    CURRENT_MEMORY(17),
    MAX_MEMORY(18),
    WINDOW_TURNS(19),
    LICENSE(20),
    ALLOCATION(21),
    ATTACHMENT_ID(22),
    READ_SEQ_COUNT(23),
    READ_IDX_COUNT(24),
    INSERT_COUNT(25),
    UPDATE_COUNT(26),
    DELETE_COUNT(27),
    BACKOUT_COUNT(28),
    PURGE_COUNT(29),
    EXPUNGE_COUNT(30),
    SWEEP_INTERVAL(31),
    ODS_VERSION(32),
    ODS_MINOR_VERSION(33),
    NO_RESERVE(34),
    /* Begin deprecated WAL and JOURNAL items. */
    LOGFILE(35),
    CUR_LOGFILE_NAME(36),
    CUR_LOG_PART_OFFSET(37),
    NUM_WAL_BUFFERS(38),
    WAL_BUFFER_SIZE(39),
    WAL_CKPT_LENGTH(40),
    WAL_CUR_CKPT_INTERVAL(41),
    WAL_PRV_CKPT_FNAME(42),
    WAL_PRV_CKPT_POFFSET(43),
    WAL_RECV_CKPT_FNAME(44),
    WAL_RECV_CKPT_POFFSET(45),
    WAL_GRPC_WAIT_USECS(47),
    WAL_NUM_IO(48),
    WAL_AVG_IO_SIZE(49),
    WAL_NUM_COMMITS(50),
    WAL_AVG_GRPC_SIZE(51),
    /* End deprecated WAL and JOURNAL items. */
    FORCED_WRITES(52),
    USER_NAMES(53),
    PAGE_ERRORS(54),
    RECORD_ERRORS(55),
    BPAGE_ERRORS(56),
    DPAGE_ERRORS(57),
    IPAGE_ERRORS(58),
    PPAGE_ERRORS(59),
    TPAGE_ERRORS(60),
    SET_PAGE_BUFFERS(61),
    DB_SQL_DIALECT(62),
    DB_READ_ONLY(63),
    DB_SIZE_IN_PAGES(64),
    
    /* Values 65-100 unused to avoid conflict with InterBase */
    
    ATT_CHARSET(101),
    DB_CLASS(102),
    FIREBIRD_VERSION(103),
    OLDEST_TRANSACTION(104),
    OLDEST_ACTIVE(105),
    OLDEST_SNAPSHOT(106),
    NEXT_TRANSACTION(107),
    DB_PROVIDER(108),
    ACTIVE_TRANSACTIONS(109),
    ACTIVE_TRAN_COUNT(110),
    CREATION_DATE(111),
    PAGE_CONTENTS(113),
    FB_IMPLEMENTATION(114),
    PAGE_WARNS(115),
    RECORD_WARNS(116),
    BPAGE_WARNS(117),
    DPAGE_WARNS(118),
    IPAGE_WARNS(119),
    PPAGE_WARNS(120),
    TPAGE_WARNS(121),
    PIP_ERRORS(122),
    PIP_WARNS(123),
    PAGES_USED(124),
    PAGES_FREE(125),
    // codes 126 and 127 are used for special purposes do not use them here
    SES_IDLE_TIMEOUT_DB(129),
    SES_IDLE_TIMEOUT_ATT(130),
    SES_IDLE_TIMEOUT_RUN(131),
    CONN_FLAGS(132),
    CRYPT_KEY(133),
    CRYPT_STATE(134),
    STATEMENT_TIMEOUT_DB(135),
    STATEMENT_TIMEOUT_ATT(136),
    PROTOCOL_VERSION(137),
    CRYPT_PLUGIN(138),
    CREATION_TIMESTAMP_TZ(139),
    WIRE_CRYPT(140),
    // Return list of features supported by provider of current connection
    FEATURES(141),
    NEXT_ATTACHMENT(142),
    NEXT_STATEMENT(143),
    DB_GUID(144),
    DB_FILE_ID(145),
    REPLICA_MODE(146),
    USERNAME(147),
    SQLROLE(148);
    
    private static final Map<Integer, FirebirdDatabaseInfoPacketType> FIREBIRD_DATABASE_INFO_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    static {
        for (FirebirdDatabaseInfoPacketType each : values()) {
            FIREBIRD_DATABASE_INFO_TYPE_CACHE.put(each.code, each);
        }
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird dpb type
     */
    public static FirebirdDatabaseInfoPacketType valueOf(final int code) {
        return FIREBIRD_DATABASE_INFO_TYPE_CACHE.get(code);
    }
    
    /**
     * Creates info packet of this type.
     *
     * @param payload Firebird packet payload
     * @return Firebird database info packet
     */
    public static FirebirdInfoPacket createPacket(final FirebirdPacketPayload payload) {
        return new FirebirdInfoPacket(payload, FirebirdDatabaseInfoPacketType::valueOf);
    }
    
    @Override
    public boolean isCommon() {
        return false;
    }
}
