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

package org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdValueFormat;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBuffer;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.FirebirdParameterBufferType;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebird database parameter buffer type.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdDatabaseParameterBufferType implements FirebirdParameterBufferType {
    
    CDD_PATHNAME(1),
    ALLOCATION(2),
    JOURNAL(3),
    PAGE_SIZE(4, FirebirdValueFormat.INT),
    NUM_BUFFERS(5, FirebirdValueFormat.INT),
    BUFFER_LENGTH(6),
    DEBUG(7, FirebirdValueFormat.INT),
    GARBAGE_COLLECT(8),
    VERIFY(9, FirebirdValueFormat.INT),
    SWEEP(10, FirebirdValueFormat.INT),
    ENABLE_JOURNAL(11),
    DISABLE_JOURNAL(12),
    DBKEY_SCOPE(13, FirebirdValueFormat.INT),
    NUMBER_OF_USERS(14),
    TRACE(15, FirebirdValueFormat.INT),
    NO_GARBAGE_COLLECT(16, FirebirdValueFormat.BOOLEAN),
    DAMAGED(17, FirebirdValueFormat.INT),
    LICENSE(18),
    SYS_USER_NAME(19),
    ENCRYPT_KEY(20),
    ACTIVATE_SHADOW(21, FirebirdValueFormat.BOOLEAN),
    SWEEP_INTERVAL(22, FirebirdValueFormat.INT),
    DELETE_SHADOW(23, FirebirdValueFormat.BOOLEAN),
    FORCE_WRITE(24, FirebirdValueFormat.INT),
    BEGIN_LOG(25),
    QUIT_LOG(26),
    NO_RESERVE(27, FirebirdValueFormat.INT),
    USER_NAME(28),
    // USER(USER_NAME.getCode()),
    PASSWORD(29),
    PASSWORD_ENC(30),
    SYS_USER_NAME_ENC(31),
    INTERP(32, FirebirdValueFormat.INT),
    ONLINE_DUMP(33),
    OLD_FILE_SIZE(34),
    OLD_NUM_FILES(35),
    OLD_FILE(36),
    OLD_START_PAGE(37),
    OLD_START_SEQNO(38),
    OLD_START_FILE(39),
    DROP_WALFILE(40),
    OLD_DUMP_ID(41),
    WAL_BACKUP_DIR(42),
    WAL_CHKPTLEN(43),
    WAL_NUMBUFS(44),
    WAL_BUFSIZE(45),
    WAL_GRP_CMT_WAIT(46),
    LC_MESSAGES(47),
    LC_CTYPE(48),
    CACHE_MANAGER(49),
    SHUTDOWN(50, FirebirdValueFormat.INT),
    ONLINE(51, FirebirdValueFormat.INT),
    SHUTDOWN_DELAY(52, FirebirdValueFormat.INT),
    RESERVED(53),
    OVERWRITE(54, FirebirdValueFormat.INT),
    SEC_ATTACH(55),
    DISABLE_WAL(56),
    CONNECT_TIMEOUT(57, FirebirdValueFormat.INT),
    DUMMY_PACKET_INTERVAL(58, FirebirdValueFormat.INT),
    GBAK_ATTACH(59),
    SQL_ROLE_NAME(60),
    SET_PAGE_BUFFERS(61, FirebirdValueFormat.INT),
    WORKING_DIRECTORY(62),
    SQL_DIALECT(63, FirebirdValueFormat.INT),
    SET_DB_READONLY(64, FirebirdValueFormat.INT),
    SET_DB_SQL_DIALECT(65, FirebirdValueFormat.INT),
    GFIX_ATTACH(66),
    GSTAT_ATTACH(67),
    SET_DB_CHARSET(68),
    
    // Firebird 2.1 constants
    GSEC_ATTACH(69),
    ADDRESS_PATH(70),
    PROCESS_ID(71, FirebirdValueFormat.INT),
    NO_DB_TRIGGERS(72, FirebirdValueFormat.INT),
    TRUSTED_AUTH(73),
    PROCESS_NAME(74),
    
    // Firebird 2.5 constants
    TRUSTED_ROLE(75),
    ORG_FILENAME(76),
    UTF8_FILENAME(77),
    EXT_CALL_DEPTH(78, FirebirdValueFormat.INT),
    
    // Firebird 3.0 constants
    AUTH_BLOCK(79),
    CLIENT_VERSION(80),
    REMOTE_PROTOCOL(81),
    HOST_NAME(82),
    OS_USER(83),
    SPECIFIC_AUTH_DATA(84),
    AUTH_PLUGIN_LIST(85),
    AUTH_PLUGIN_NAME(86),
    CONFIG(87),
    NOLINGER(88, FirebirdValueFormat.BOOLEAN),
    RESET_ICU(89, FirebirdValueFormat.BOOLEAN),
    MAP_ATTACH(90),
    
    // Firebird 4 constants
    SESSION_TIME_ZONE(91),
    SET_DB_REPLICA(92, FirebirdValueFormat.INT),
    SET_BIND(93),
    DECFLOAT_ROUND(94),
    DECFLOAT_TRAPS(95),
    CLEAR_MAP(96, FirebirdValueFormat.BOOLEAN),
    
    // Firebird 5 constants
    PARALLEL_WORKERS(100, FirebirdValueFormat.INT),
    WORKER_ATTACH(101, FirebirdValueFormat.BOOLEAN);
    
    private static final Map<Integer, FirebirdDatabaseParameterBufferType> FIREBIRD_DPB_TYPE_CACHE = new HashMap<>();
    
    private final int code;
    
    private final FirebirdValueFormat format;
    
    static {
        for (FirebirdDatabaseParameterBufferType each : values()) {
            FIREBIRD_DPB_TYPE_CACHE.put(each.code, each);
        }
    }
    
    FirebirdDatabaseParameterBufferType(final int code) {
        this(code, FirebirdValueFormat.STRING);
    }
    
    /**
     * Value of.
     *
     * @param code arch type code
     * @return Firebird dpb type
     */
    public static FirebirdDatabaseParameterBufferType valueOf(final int code) {
        FirebirdDatabaseParameterBufferType result = FIREBIRD_DPB_TYPE_CACHE.get(code);
        Preconditions.checkNotNull(result, "Cannot find code '%d' in dpb type", code);
        return result;
    }
    
    /**
     * Decides whether to use a traditional type for integers.
     *
     * @param version version of parameter buffer
     * @return is traditional type
     */
    public static boolean isTraditionalType(final int version) {
        return version == 1;
    }
    
    /**
     * Creates parameter buffer of this type.
     *
     * @return Firebird database parameter buffer
     */
    public static FirebirdParameterBuffer createBuffer() {
        return new FirebirdParameterBuffer(FirebirdDatabaseParameterBufferType::valueOf, FirebirdDatabaseParameterBufferType::isTraditionalType);
    }
}
