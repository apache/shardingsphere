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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.ErrorLocalVariableException;
import org.apache.shardingsphere.database.exception.mysql.exception.IncorrectGlobalLocalVariableException;
import org.apache.shardingsphere.database.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider.TransactionIsolationValueProvider;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider.TransactionReadOnlyValueProvider;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider.VersionValueProvider;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * System variable for MySQL.
 */
@RequiredArgsConstructor
public enum MySQLSystemVariable {
    
    ACTIVATE_ALL_ROLES_ON_LOGIN(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    ADMIN_ADDRESS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    ADMIN_PORT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "33062"),
    
    ADMIN_SSL_CA(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CAPATH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CERT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CIPHER(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CRL(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CRLPATH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_KEY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_TLS_CIPHERSUITES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_TLS_VERSION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "TLSv1.2,TLSv1.3"),
    
    AUTHENTICATION_POLICY(MySQLSystemVariableFlag.GLOBAL, "*,,"),
    
    AUTO_GENERATE_CERTS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    AUTO_INCREMENT_INCREMENT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    AUTO_INCREMENT_OFFSET(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    // TODO Get or set session value.
    AUTOCOMMIT(MySQLSystemVariableFlag.SESSION, "1"),
    
    AUTOMATIC_SP_PRIVILEGES(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    AVOID_TEMPORAL_UPGRADE(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    BACK_LOG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    BASEDIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    BIG_TABLES(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    BIND_ADDRESS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "*"),
    
    BINLOG_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "32768"),
    
    BINLOG_CHECKSUM(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "CRC32"),
    
    BINLOG_DIRECT_NON_TRANSACTIONAL_UPDATES(MySQLSystemVariableFlag.SESSION, "0"),
    
    BINLOG_ENCRYPTION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "0"),
    
    BINLOG_ERROR_ACTION(MySQLSystemVariableFlag.GLOBAL, "ABORT_SERVER"),
    
    BINLOG_EXPIRE_LOGS_AUTO_PURGE(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    BINLOG_EXPIRE_LOGS_SECONDS(MySQLSystemVariableFlag.GLOBAL, "2592000"),
    
    BINLOG_FORMAT(MySQLSystemVariableFlag.SESSION, "ROW"),
    
    BINLOG_GROUP_COMMIT_SYNC_DELAY(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    BINLOG_GROUP_COMMIT_SYNC_NO_DELAY_COUNT(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    BINLOG_GTID_SIMPLE_RECOVERY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    BINLOG_MAX_FLUSH_QUEUE_TIME(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    BINLOG_ORDER_COMMITS(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    BINLOG_ROTATE_ENCRYPTION_MASTER_KEY_AT_STARTUP(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    BINLOG_ROW_EVENT_MAX_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "8192"),
    
    BINLOG_ROW_IMAGE(MySQLSystemVariableFlag.SESSION, "FULL"),
    
    BINLOG_ROW_METADATA(MySQLSystemVariableFlag.GLOBAL, "MINIMAL"),
    
    BINLOG_ROW_VALUE_OPTIONS(MySQLSystemVariableFlag.SESSION, ""),
    
    BINLOG_ROWS_QUERY_LOG_EVENTS(MySQLSystemVariableFlag.SESSION, "0"),
    
    BINLOG_STMT_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "32768"),
    
    BINLOG_TRANSACTION_COMPRESSION(MySQLSystemVariableFlag.SESSION, "0"),
    
    BINLOG_TRANSACTION_COMPRESSION_LEVEL_ZSTD(MySQLSystemVariableFlag.SESSION, "3"),
    
    BINLOG_TRANSACTION_DEPENDENCY_HISTORY_SIZE(MySQLSystemVariableFlag.GLOBAL, "25000"),
    
    BINLOG_TRANSACTION_DEPENDENCY_TRACKING(MySQLSystemVariableFlag.GLOBAL, "COMMIT_ORDER"),
    
    BLOCK_ENCRYPTION_MODE(MySQLSystemVariableFlag.SESSION, "aes-128-ecb"),
    
    BUILD_ID(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    BULK_INSERT_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "8388608"),
    
    // TODO Properly handling character set of session.
    CHARACTER_SET_CLIENT(MySQLSystemVariableFlag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_CONNECTION(MySQLSystemVariableFlag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_DATABASE(MySQLSystemVariableFlag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_FILESYSTEM(MySQLSystemVariableFlag.SESSION, "binary"),
    
    CHARACTER_SET_RESULTS(MySQLSystemVariableFlag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_SERVER(MySQLSystemVariableFlag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_SYSTEM(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "utf8mb4"),
    
    CHARACTER_SETS_DIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    CHECK_PROXY_USERS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    COLLATION_CONNECTION(MySQLSystemVariableFlag.SESSION, "utf8mb4_unicode_ci"),
    
    COLLATION_DATABASE(MySQLSystemVariableFlag.SESSION, "utf8mb4_unicode_ci"),
    
    COLLATION_SERVER(MySQLSystemVariableFlag.SESSION, "utf8mb4_unicode_ci"),
    
    COMPLETION_TYPE(MySQLSystemVariableFlag.SESSION, "NO_CHAIN"),
    
    CONCURRENT_INSERT(MySQLSystemVariableFlag.GLOBAL, "AUTO"),
    
    CONNECT_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "10"),
    
    CONNECTION_MEMORY_CHUNK_SIZE(MySQLSystemVariableFlag.SESSION, "8912"),
    
    CONNECTION_MEMORY_LIMIT(MySQLSystemVariableFlag.SESSION, "18446744073709551615"),
    
    CORE_FILE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    CREATE_ADMIN_LISTENER_THREAD(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    CTE_MAX_RECURSION_DEPTH(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1000"),
    
    DATADIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    // DEBUG(Flag.SESSION, "TODO"),
    
    DEBUG_SENSITIVE_SESSION_STRING(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.SENSITIVE, ""),
    
    // DEBUG_SYNC(Flag.ONLY_SESSION, "TODO"),
    
    DEFAULT_AUTHENTICATION_PLUGIN(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "caching_sha2_password"),
    
    DEFAULT_COLLATION_FOR_UTF8MB4(MySQLSystemVariableFlag.SESSION, "utf8mb4_unicode_ci"),
    
    DEFAULT_PASSWORD_LIFETIME(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    DEFAULT_STORAGE_ENGINE(MySQLSystemVariableFlag.SESSION, ""),
    
    DEFAULT_TABLE_ENCRYPTION(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    DEFAULT_TMP_STORAGE_ENGINE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, ""),
    
    DEFAULT_WEEK_FORMAT(MySQLSystemVariableFlag.SESSION, "0"),
    
    DELAY_KEY_WRITE(MySQLSystemVariableFlag.GLOBAL, "ON"),
    
    DELAYED_INSERT_LIMIT(MySQLSystemVariableFlag.GLOBAL, "100"),
    
    DELAYED_INSERT_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "300"),
    
    DELAYED_QUEUE_SIZE(MySQLSystemVariableFlag.GLOBAL, "1000"),
    
    DISABLED_STORAGE_ENGINES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    DISCONNECT_ON_EXPIRED_PASSWORD(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    DIV_PRECISION_INCREMENT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "4"),
    
    END_MARKERS_IN_JSON(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    ENFORCE_GTID_CONSISTENCY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "FALSE"),
    
    EQ_RANGE_INDEX_DIVE_LIMIT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "200"),
    
    ERROR_COUNT(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.READONLY, "0"),
    
    EVENT_SCHEDULER(MySQLSystemVariableFlag.GLOBAL, "ON"),
    
    EXPIRE_LOGS_DAYS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    // EXPLAIN_FORMAT(Flag.SESSION, "TODO"),
    
    EXPLICIT_DEFAULTS_FOR_TIMESTAMP(MySQLSystemVariableFlag.SESSION, "1"),
    
    EXTERNAL_USER(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.READONLY, ""),
    
    FLUSH(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    FLUSH_TIME(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    FOREIGN_KEY_CHECKS(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    FT_BOOLEAN_SYNTAX(MySQLSystemVariableFlag.GLOBAL, "+ -><()~*:\"\"&|"),
    
    FT_MAX_WORD_LEN(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "84"),
    
    FT_MIN_WORD_LEN(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "4"),
    
    FT_QUERY_EXPANSION_LIMIT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "20"),
    
    FT_STOPWORD_FILE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    GENERAL_LOG(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    GENERAL_LOG_FILE(MySQLSystemVariableFlag.GLOBAL, ""),
    
    GENERATED_RANDOM_PASSWORD_LENGTH(MySQLSystemVariableFlag.SESSION, "20"),
    
    GLOBAL_CONNECTION_MEMORY_LIMIT(MySQLSystemVariableFlag.GLOBAL, "18446744073709551615"),
    
    GLOBAL_CONNECTION_MEMORY_TRACKING(MySQLSystemVariableFlag.SESSION, "0"),
    
    GROUP_CONCAT_MAX_LEN(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1024"),
    
    // GROUP_REPLICATION_CONSISTENCY(Flag.SESSION, "TODO"),
    
    // GTID_EXECUTED(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    GTID_EXECUTED_COMPRESSION_PERIOD(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    // GTID_MODE(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "TODO"),
    
    // GTID_NEXT(Flag.ONLY_SESSION, "TODO"),
    
    // GTID_OWNED(Flag.SESSION | Flag.READONLY, "TODO"),
    
    // GTID_PURGED(Flag.GLOBAL, "TODO"),
    
    // HAVE_COMPRESS(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_DYNAMIC_LOADING(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_GEOMETRY(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_OPENSSL(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_PROFILING(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_QUERY_CACHE(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_RTREE_KEYS(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    HAVE_SSL(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "NO"),
    
    // HAVE_STATEMENT_TIMEOUT(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_SYMLINK(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    HISTOGRAM_GENERATION_MAX_MEM_SIZE(MySQLSystemVariableFlag.SESSION, "20000000"),
    
    HOST_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "128"),
    
    HOSTNAME(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    IDENTITY(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    IMMEDIATE_SERVER_VERSION(MySQLSystemVariableFlag.ONLY_SESSION, "999999"),
    
    INFORMATION_SCHEMA_STATS_EXPIRY(MySQLSystemVariableFlag.SESSION, "86400"),
    
    INIT_CONNECT(MySQLSystemVariableFlag.GLOBAL, ""),
    
    INIT_FILE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    INIT_REPLICA(MySQLSystemVariableFlag.GLOBAL, ""),
    
    INIT_SLAVE(MySQLSystemVariableFlag.GLOBAL, ""),
    
    INSERT_ID(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    INTERACTIVE_TIMEOUT(MySQLSystemVariableFlag.SESSION, "28800"),
    
    // INTERNAL_TMP_MEM_STORAGE_ENGINE(Flag.SESSION | Flag.HINT_UPDATEABLE, "TODO"),
    
    JOIN_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "262144"),
    
    KEEP_FILES_ON_CREATE(MySQLSystemVariableFlag.SESSION, "0"),
    
    // KEY_BUFFER_SIZE(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_AGE_THRESHOLD(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_BLOCK_SIZE(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_DIVISION_LIMIT(Flag.GLOBAL, "TODO"),
    
    KEYRING_OPERATIONS(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    LARGE_FILES_SUPPORT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    LARGE_PAGE_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    LARGE_PAGES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    LAST_INSERT_ID(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    LC_MESSAGES(MySQLSystemVariableFlag.SESSION, ""),
    
    LC_MESSAGES_DIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    LC_TIME_NAMES(MySQLSystemVariableFlag.SESSION, ""),
    
    LICENSE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "GPL"),
    
    LOCAL_INFILE(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOCK_WAIT_TIMEOUT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "31536000"),
    
    LOCKED_IN_MEMORY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    LOG_BIN(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    LOG_BIN_BASENAME(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    LOG_BIN_INDEX(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    LOG_BIN_TRUST_FUNCTION_CREATORS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_BIN_USE_V1_ROW_EVENTS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_ERROR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    LOG_ERROR_SERVICES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "log_filter_internal; log_sink_internal"),
    
    LOG_ERROR_SUPPRESSION_LIST(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    LOG_ERROR_VERBOSITY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "2"),
    
    // LOG_OUTPUT(Flag.GLOBAL, "TODO"),
    
    LOG_QUERIES_NOT_USING_INDEXES(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_RAW(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_REPLICA_UPDATES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    LOG_SLAVE_UPDATES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    LOG_SLOW_ADMIN_STATEMENTS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_SLOW_EXTRA(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_SLOW_REPLICA_STATEMENTS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_SLOW_SLAVE_STATEMENTS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_STATEMENTS_UNSAFE_FOR_BINLOG(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    LOG_THROTTLE_QUERIES_NOT_USING_INDEXES(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    // LOG_TIMESTAMPS(Flag.GLOBAL, "TODO"),
    
    LONG_QUERY_TIME(MySQLSystemVariableFlag.SESSION, "4.62182e+18"),
    
    LOW_PRIORITY_UPDATES(MySQLSystemVariableFlag.SESSION, "0"),
    
    LOWER_CASE_FILE_SYSTEM(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    LOWER_CASE_TABLE_NAMES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    MANDATORY_ROLES(MySQLSystemVariableFlag.GLOBAL, ""),
    
    // MASTER_INFO_REPOSITORY(Flag.GLOBAL, "TODO"),
    
    MASTER_VERIFY_CHECKSUM(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    MAX_ALLOWED_PACKET(MySQLSystemVariableFlag.SESSION, "67108864"),
    
    MAX_BINLOG_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "18446744073709547520"),
    
    MAX_BINLOG_SIZE(MySQLSystemVariableFlag.GLOBAL, "1073741824"),
    
    MAX_BINLOG_STMT_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "18446744073709547520"),
    
    MAX_CONNECT_ERRORS(MySQLSystemVariableFlag.GLOBAL, "100"),
    
    MAX_CONNECTIONS(MySQLSystemVariableFlag.GLOBAL, "151"),
    
    MAX_DELAYED_THREADS(MySQLSystemVariableFlag.SESSION, "20"),
    
    MAX_DIGEST_LENGTH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1024"),
    
    MAX_ERROR_COUNT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1024"),
    
    MAX_EXECUTION_TIME(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    MAX_HEAP_TABLE_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "16777216"),
    
    MAX_INSERT_DELAYED_THREADS(MySQLSystemVariableFlag.SESSION, "20"),
    
    MAX_JOIN_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "18446744073709551615"),
    
    MAX_LENGTH_FOR_SORT_DATA(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "4096"),
    
    MAX_POINTS_IN_GEOMETRY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "65536"),
    
    MAX_PREPARED_STMT_COUNT(MySQLSystemVariableFlag.GLOBAL, "16382"),
    
    MAX_RELAY_LOG_SIZE(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    MAX_SEEKS_FOR_KEY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "18446744073709551615"),
    
    MAX_SORT_LENGTH(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1024"),
    
    MAX_SP_RECURSION_DEPTH(MySQLSystemVariableFlag.SESSION, "0"),
    
    MAX_USER_CONNECTIONS(MySQLSystemVariableFlag.SESSION, "0"),
    
    MAX_WRITE_LOCK_COUNT(MySQLSystemVariableFlag.GLOBAL, "18446744073709551615"),
    
    MIN_EXAMINED_ROW_LIMIT(MySQLSystemVariableFlag.SESSION, "0"),
    
    MYSQL_NATIVE_PASSWORD_PROXY_USERS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    NET_BUFFER_LENGTH(MySQLSystemVariableFlag.SESSION, "16384"),
    
    NET_READ_TIMEOUT(MySQLSystemVariableFlag.SESSION, "30"),
    
    NET_RETRY_COUNT(MySQLSystemVariableFlag.SESSION, "10"),
    
    NET_WRITE_TIMEOUT(MySQLSystemVariableFlag.SESSION, "60"),
    
    NEW(MySQLSystemVariableFlag.SESSION, "0"),
    
    OFFLINE_MODE(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    OLD(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    OLD_ALTER_TABLE(MySQLSystemVariableFlag.SESSION, "0"),
    
    OPEN_FILES_LIMIT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    OPTIMIZER_MAX_SUBGRAPH_PAIRS(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "100000"),
    
    OPTIMIZER_PRUNE_LEVEL(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    OPTIMIZER_SEARCH_DEPTH(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "62"),
    
    // OPTIMIZER_SWITCH(Flag.SESSION | Flag.HINT_UPDATEABLE, "TODO"),
    
    // OPTIMIZER_TRACE(Flag.SESSION, "TODO"),
    
    // OPTIMIZER_TRACE_FEATURES(Flag.SESSION, "TODO"),
    
    OPTIMIZER_TRACE_LIMIT(MySQLSystemVariableFlag.SESSION, "1"),
    
    OPTIMIZER_TRACE_MAX_MEM_SIZE(MySQLSystemVariableFlag.SESSION, "1048576"),
    
    OPTIMIZER_TRACE_OFFSET(MySQLSystemVariableFlag.SESSION, "-1"),
    
    ORIGINAL_COMMIT_TIMESTAMP(MySQLSystemVariableFlag.ONLY_SESSION, "36028797018963968"),
    
    ORIGINAL_SERVER_VERSION(MySQLSystemVariableFlag.ONLY_SESSION, "999999"),
    
    PARSER_MAX_MEM_SIZE(MySQLSystemVariableFlag.SESSION, "18446744073709551615"),
    
    PARTIAL_REVOKES(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    PASSWORD_HISTORY(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    PASSWORD_REQUIRE_CURRENT(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    PASSWORD_REUSE_INTERVAL(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    PERFORMANCE_SCHEMA(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    PERFORMANCE_SCHEMA_ACCOUNTS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_CURRENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_HISTORY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_HISTORY_LONG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_CPU(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_CURRENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_HISTORY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_HISTORY_LONG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_CURRENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_HISTORY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_HISTORY_LONG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_CURRENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_HISTORY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_HISTORY_LONG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_GLOBAL_INSTRUMENTATION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_STATEMENTS_DIGEST(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_THREAD_INSTRUMENTATION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_DIGESTS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_ERROR_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "5242"),
    
    PERFORMANCE_SCHEMA_EVENTS_STAGES_HISTORY_LONG_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STAGES_HISTORY_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STATEMENTS_HISTORY_LONG_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STATEMENTS_HISTORY_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_TRANSACTIONS_HISTORY_LONG_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_TRANSACTIONS_HISTORY_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_WAITS_HISTORY_LONG_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_WAITS_HISTORY_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_HOSTS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_INSTRUMENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, ""),
    
    PERFORMANCE_SCHEMA_MAX_COND_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "150"),
    
    PERFORMANCE_SCHEMA_MAX_COND_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_DIGEST_LENGTH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1024"),
    
    PERFORMANCE_SCHEMA_MAX_DIGEST_SAMPLE_AGE(MySQLSystemVariableFlag.GLOBAL, "60"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "80"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_HANDLES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "32768"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_INDEX_STAT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_MEMORY_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "450"),
    
    PERFORMANCE_SCHEMA_MAX_METADATA_LOCKS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_MUTEX_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "350"),
    
    PERFORMANCE_SCHEMA_MAX_MUTEX_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_PREPARED_STATEMENTS_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_PROGRAM_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_RWLOCK_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "60"),
    
    PERFORMANCE_SCHEMA_MAX_RWLOCK_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_SOCKET_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "10"),
    
    PERFORMANCE_SCHEMA_MAX_SOCKET_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_SQL_TEXT_LENGTH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1024"),
    
    PERFORMANCE_SCHEMA_MAX_STAGE_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "175"),
    
    PERFORMANCE_SCHEMA_MAX_STATEMENT_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "219"),
    
    PERFORMANCE_SCHEMA_MAX_STATEMENT_STACK(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "10"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_HANDLES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_LOCK_STAT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_THREAD_CLASSES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "100"),
    
    PERFORMANCE_SCHEMA_MAX_THREAD_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SESSION_CONNECT_ATTRS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SETUP_ACTORS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SETUP_OBJECTS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SHOW_PROCESSLIST(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    PERFORMANCE_SCHEMA_USERS_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "-1"),
    
    PERSIST_ONLY_ADMIN_X509_SUBJECT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    PERSIST_SENSITIVE_VARIABLES_IN_PLAINTEXT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    PERSISTED_GLOBALS_LOAD(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    PID_FILE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    PLUGIN_DIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    PORT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    PRELOAD_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION, "32768"),
    
    PRINT_IDENTIFIED_WITH_AS_HEX(MySQLSystemVariableFlag.SESSION, "0"),
    
    PROFILING(MySQLSystemVariableFlag.SESSION, "0"),
    
    PROFILING_HISTORY_SIZE(MySQLSystemVariableFlag.SESSION, "15"),
    
    PROTOCOL_COMPRESSION_ALGORITHMS(MySQLSystemVariableFlag.GLOBAL, "zlib,zstd,uncompressed"),
    
    PROTOCOL_VERSION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "10"),
    
    PROXY_USER(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.READONLY, ""),
    
    PSEUDO_REPLICA_MODE(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    PSEUDO_SLAVE_MODE(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    PSEUDO_THREAD_ID(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    QUERY_ALLOC_BLOCK_SIZE(MySQLSystemVariableFlag.SESSION, "8192"),
    
    QUERY_PREALLOC_SIZE(MySQLSystemVariableFlag.SESSION, "8192"),
    
    RAND_SEED1(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    RAND_SEED2(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    RANGE_ALLOC_BLOCK_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "4096"),
    
    RANGE_OPTIMIZER_MAX_MEM_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "8388608"),
    
    // RBR_EXEC_MODE(Flag.SESSION, "TODO"),
    
    READ_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "131072"),
    
    READ_ONLY(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    READ_RND_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "262144"),
    
    REGEXP_STACK_LIMIT(MySQLSystemVariableFlag.GLOBAL, "8000000"),
    
    REGEXP_TIME_LIMIT(MySQLSystemVariableFlag.GLOBAL, "32"),
    
    RELAY_LOG(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    RELAY_LOG_BASENAME(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    RELAY_LOG_INDEX(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    RELAY_LOG_INFO_FILE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    // RELAY_LOG_INFO_REPOSITORY(Flag.GLOBAL, "TODO"),
    
    RELAY_LOG_PURGE(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    RELAY_LOG_RECOVERY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    RELAY_LOG_SPACE_LIMIT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    REPLICA_ALLOW_BATCHING(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    REPLICA_CHECKPOINT_GROUP(MySQLSystemVariableFlag.GLOBAL, "512"),
    
    REPLICA_CHECKPOINT_PERIOD(MySQLSystemVariableFlag.GLOBAL, "300"),
    
    REPLICA_COMPRESSED_PROTOCOL(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    // REPLICA_EXEC_MODE(Flag.GLOBAL, "TODO"),
    
    REPLICA_LOAD_TMPDIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    REPLICA_MAX_ALLOWED_PACKET(MySQLSystemVariableFlag.GLOBAL, "1073741824"),
    
    REPLICA_NET_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "60"),
    
    // REPLICA_PARALLEL_TYPE(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "TODO"),
    
    REPLICA_PARALLEL_WORKERS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "4"),
    
    REPLICA_PENDING_JOBS_SIZE_MAX(MySQLSystemVariableFlag.GLOBAL, "134217728"),
    
    REPLICA_PRESERVE_COMMIT_ORDER(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "1"),
    
    REPLICA_SKIP_ERRORS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    REPLICA_SQL_VERIFY_CHECKSUM(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    REPLICA_TRANSACTION_RETRIES(MySQLSystemVariableFlag.GLOBAL, "10"),
    
    // REPLICA_TYPE_CONVERSIONS(Flag.GLOBAL, "TODO"),
    
    REPLICATION_OPTIMIZE_FOR_STATIC_PLUGIN_CONFIG(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    REPLICATION_SENDER_OBSERVE_COMMIT_ONLY(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    REPORT_HOST(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    REPORT_PASSWORD(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    REPORT_PORT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    REPORT_USER(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    REQUIRE_ROW_FORMAT(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    REQUIRE_SECURE_TRANSPORT(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    // RESULTSET_METADATA(Flag.ONLY_SESSION, "TODO"),
    
    RPL_READ_SIZE(MySQLSystemVariableFlag.GLOBAL, "8192"),
    
    RPL_STOP_REPLICA_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "31536000"),
    
    RPL_STOP_SLAVE_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "31536000"),
    
    SCHEMA_DEFINITION_CACHE(MySQLSystemVariableFlag.GLOBAL, "256"),
    
    SECONDARY_ENGINE_COST_THRESHOLD(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "4.68161e+18"),
    
    SECURE_FILE_PRIV(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "NULL"),
    
    SELECT_INTO_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "131072"),
    
    SELECT_INTO_DISK_SYNC(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SELECT_INTO_DISK_SYNC_DELAY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SERVER_ID(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "1"),
    
    SERVER_ID_BITS(MySQLSystemVariableFlag.GLOBAL, "32"),
    
    SERVER_UUID(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    // SESSION_TRACK_GTIDS(Flag.SESSION, "TODO"),
    
    SESSION_TRACK_SCHEMA(MySQLSystemVariableFlag.SESSION, "1"),
    
    SESSION_TRACK_STATE_CHANGE(MySQLSystemVariableFlag.SESSION, "0"),
    
    SESSION_TRACK_SYSTEM_VARIABLES(MySQLSystemVariableFlag.SESSION, "time_zone,autocommit,character_set_client,character_set_results,character_set_connection"),
    
    // SESSION_TRACK_TRANSACTION_INFO(Flag.SESSION, "TODO"),
    
    SHA256_PASSWORD_PROXY_USERS(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SHOW_CREATE_TABLE_SKIP_SECONDARY_ENGINE(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    SHOW_CREATE_TABLE_VERBOSITY(MySQLSystemVariableFlag.SESSION, "0"),
    
    SHOW_GIPK_IN_CREATE_TABLE_AND_INFORMATION_SCHEMA(MySQLSystemVariableFlag.SESSION, "1"),
    
    SHOW_OLD_TEMPORALS(MySQLSystemVariableFlag.SESSION, "0"),
    
    SKIP_EXTERNAL_LOCKING(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1"),
    
    SKIP_NAME_RESOLVE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    SKIP_NETWORKING(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    SKIP_REPLICA_START(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    SKIP_SHOW_DATABASE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    SKIP_SLAVE_START(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    SLAVE_ALLOW_BATCHING(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    SLAVE_CHECKPOINT_GROUP(MySQLSystemVariableFlag.GLOBAL, "512"),
    
    SLAVE_CHECKPOINT_PERIOD(MySQLSystemVariableFlag.GLOBAL, "300"),
    
    SLAVE_COMPRESSED_PROTOCOL(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SLAVE_EXEC_MODE(MySQLSystemVariableFlag.GLOBAL, "STRICT"),
    
    SLAVE_LOAD_TMPDIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    SLAVE_MAX_ALLOWED_PACKET(MySQLSystemVariableFlag.GLOBAL, "1073741824"),
    
    SLAVE_NET_TIMEOUT(MySQLSystemVariableFlag.GLOBAL, "60"),
    
    SLAVE_PARALLEL_TYPE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "LOGICAL_CLOCK"),
    
    SLAVE_PARALLEL_WORKERS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "4"),
    
    SLAVE_PENDING_JOBS_SIZE_MAX(MySQLSystemVariableFlag.GLOBAL, "134217728"),
    
    SLAVE_PRESERVE_COMMIT_ORDER(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "1"),
    
    SLAVE_ROWS_SEARCH_ALGORITHMS(MySQLSystemVariableFlag.GLOBAL, "INDEX_SCAN,HASH_SCAN"),
    
    SLAVE_SKIP_ERRORS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    SLAVE_SQL_VERIFY_CHECKSUM(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    SLAVE_TRANSACTION_RETRIES(MySQLSystemVariableFlag.GLOBAL, "10"),
    
    SLAVE_TYPE_CONVERSIONS(MySQLSystemVariableFlag.GLOBAL, ""),
    
    SLOW_LAUNCH_TIME(MySQLSystemVariableFlag.GLOBAL, "2"),
    
    SLOW_QUERY_LOG(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SLOW_QUERY_LOG_FILE(MySQLSystemVariableFlag.GLOBAL, ""),
    
    SOCKET(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    SORT_BUFFER_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "262144"),
    
    SOURCE_VERIFY_CHECKSUM(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SQL_AUTO_IS_NULL(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SQL_BIG_SELECTS(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SQL_BUFFER_RESULT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SQL_GENERATE_INVISIBLE_PRIMARY_KEY(MySQLSystemVariableFlag.SESSION, "0"),
    
    SQL_LOG_BIN(MySQLSystemVariableFlag.ONLY_SESSION, "1"),
    
    SQL_LOG_OFF(MySQLSystemVariableFlag.SESSION, "0"),
    
    SQL_MODE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE,
            "ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION"),
    
    SQL_NOTES(MySQLSystemVariableFlag.SESSION, "1"),
    
    SQL_QUOTE_SHOW_CREATE(MySQLSystemVariableFlag.SESSION, "1"),
    
    SQL_REPLICA_SKIP_COUNTER(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SQL_REQUIRE_PRIMARY_KEY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SQL_SAFE_UPDATES(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    SQL_SELECT_LIMIT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "18446744073709551615"),
    
    SQL_SLAVE_SKIP_COUNTER(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SQL_WARNINGS(MySQLSystemVariableFlag.SESSION, "0"),
    
    SSL_CA(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CAPATH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CERT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CIPHER(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CRL(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CRLPATH(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_FIPS_MODE(MySQLSystemVariableFlag.GLOBAL, "OFF"),
    
    SSL_KEY(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_SESSION_CACHE_MODE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "1"),
    
    SSL_SESSION_CACHE_TIMEOUT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "300"),
    
    STORED_PROGRAM_CACHE(MySQLSystemVariableFlag.GLOBAL, "256"),
    
    STORED_PROGRAM_DEFINITION_CACHE(MySQLSystemVariableFlag.GLOBAL, "256"),
    
    SUPER_READ_ONLY(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SYNC_BINLOG(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    SYNC_MASTER_INFO(MySQLSystemVariableFlag.GLOBAL, "10000"),
    
    SYNC_RELAY_LOG(MySQLSystemVariableFlag.GLOBAL, "10000"),
    
    SYNC_RELAY_LOG_INFO(MySQLSystemVariableFlag.GLOBAL, "10000"),
    
    SYNC_SOURCE_INFO(MySQLSystemVariableFlag.GLOBAL, "10000"),
    
    // TODO Retrieve proper system time zone.
    SYSTEM_TIME_ZONE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "UTC"),
    
    TABLE_DEFINITION_CACHE(MySQLSystemVariableFlag.GLOBAL, "400"),
    
    TABLE_ENCRYPTION_PRIVILEGE_CHECK(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    TABLE_OPEN_CACHE(MySQLSystemVariableFlag.GLOBAL, "4000"),
    
    TABLE_OPEN_CACHE_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "16"),
    
    TABLESPACE_DEFINITION_CACHE(MySQLSystemVariableFlag.GLOBAL, "256"),
    
    TEMPTABLE_MAX_MMAP(MySQLSystemVariableFlag.GLOBAL, "1073741824"),
    
    TEMPTABLE_MAX_RAM(MySQLSystemVariableFlag.GLOBAL, "1073741824"),
    
    TEMPTABLE_USE_MMAP(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    TERMINOLOGY_USE_PREVIOUS(MySQLSystemVariableFlag.SESSION, "NONE"),
    
    THREAD_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    THREAD_HANDLING(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "one-thread-per-connection"),
    
    THREAD_STACK(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1048576"),
    
    TIME_ZONE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "SYSTEM"),
    
    TIMESTAMP(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "0"),
    
    TLS_CIPHERSUITES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, ""),
    
    TLS_VERSION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.PERSIST_AS_READ_ONLY, "TLSv1.2,TLSv1.3"),
    
    TMP_TABLE_SIZE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "16777216"),
    
    TMPDIR(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    TRANSACTION_ALLOC_BLOCK_SIZE(MySQLSystemVariableFlag.SESSION, "8192"),
    
    TRANSACTION_ALLOW_BATCHING(MySQLSystemVariableFlag.ONLY_SESSION, "0"),
    
    TRANSACTION_ISOLATION(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.TRI_LEVEL, "REPEATABLE-READ", new TransactionIsolationValueProvider()),
    
    TRANSACTION_PREALLOC_SIZE(MySQLSystemVariableFlag.SESSION, "4096"),
    
    TRANSACTION_READ_ONLY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.TRI_LEVEL, "0", new TransactionReadOnlyValueProvider()),
    
    TRANSACTION_WRITE_SET_EXTRACTION(MySQLSystemVariableFlag.SESSION, "XXHASH64"),
    
    UNIQUE_CHECKS(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    UPDATABLE_VIEWS_WITH_LIMIT(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "YES"),
    
    USE_SECONDARY_ENGINE(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "ON"),
    
    VALIDATE_USER_PLUGINS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY | MySQLSystemVariableFlag.INVISIBLE, "1"),
    
    VERSION(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY,
            DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")), new VersionValueProvider()),
    
    VERSION_COMMENT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "Source distribution"),
    
    VERSION_COMPILE_MACHINE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "x86_64"),
    
    VERSION_COMPILE_OS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "Linux"),
    
    VERSION_COMPILE_ZLIB(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1.2.13"),
    
    WAIT_TIMEOUT(MySQLSystemVariableFlag.SESSION, "28800"),
    
    WARNING_COUNT(MySQLSystemVariableFlag.ONLY_SESSION | MySQLSystemVariableFlag.READONLY, "0"),
    
    WINDOWING_USE_HIGH_PRECISION(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    // The following variables are from MySQL 5.7
    
    XA_DETACH_ON_PREPARE(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.HINT_UPDATEABLE, "1"),
    
    DATE_FORMAT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "%Y-%m-%d"),
    
    DATETIME_FORMAT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "%Y-%m-%d %H:%i:%s"),
    
    HAVE_CRYPT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "NO"),
    
    IGNORE_BUILTIN_INNODB(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "0"),
    
    IGNORE_DB_DIRS(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, ""),
    
    INTERNAL_TMP_DISK_STORAGE_ENGINE(MySQLSystemVariableFlag.GLOBAL, "InnoDB"),
    
    LOG_BUILTIN_AS_IDENTIFIED_BY_PASSWORD(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_SYSLOG(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    LOG_SYSLOG_FACILITY(MySQLSystemVariableFlag.GLOBAL, "daemon"),
    
    LOG_SYSLOG_INCLUDE_PID(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    LOG_SYSLOG_TAG(MySQLSystemVariableFlag.GLOBAL, ""),
    
    LOG_WARNINGS(MySQLSystemVariableFlag.GLOBAL, "2"),
    
    MAX_TMP_TABLES(MySQLSystemVariableFlag.SESSION, "32"),
    
    METADATA_LOCKS_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "1024"),
    
    METADATA_LOCKS_HASH_INSTANCES(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "8"),
    
    MULTI_RANGE_COUNT(MySQLSystemVariableFlag.SESSION, "256"),
    
    OLD_PASSWORDS(MySQLSystemVariableFlag.SESSION, "0"),
    
    QUERY_CACHE_LIMIT(MySQLSystemVariableFlag.GLOBAL, "1048576"),
    
    QUERY_CACHE_MIN_RES_UNIT(MySQLSystemVariableFlag.GLOBAL, "4096"),
    
    QUERY_CACHE_SIZE(MySQLSystemVariableFlag.GLOBAL, "1048576"),
    
    QUERY_CACHE_TYPE(MySQLSystemVariableFlag.SESSION, "OFF"),
    
    QUERY_CACHE_WLOCK_INVALIDATE(MySQLSystemVariableFlag.SESSION, "0"),
    
    SECURE_AUTH(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    SHOW_COMPATIBILITY_56(MySQLSystemVariableFlag.GLOBAL, "0"),
    
    SYNC_FRM(MySQLSystemVariableFlag.GLOBAL, "1"),
    
    TIME_FORMAT(MySQLSystemVariableFlag.GLOBAL | MySQLSystemVariableFlag.READONLY, "%H:%i:%s"),
    
    TX_ISOLATION(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.TRI_LEVEL, "REPEATABLE-READ", new TransactionIsolationValueProvider()),
    
    TX_READ_ONLY(MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.TRI_LEVEL, "0", new TransactionReadOnlyValueProvider()),
    
    // The following variables are from MySQL 5.6
    
    BINLOGGING_IMPOSSIBLE_MODE(MySQLSystemVariableFlag.GLOBAL, "IGNORE_ERROR"),
    
    SIMPLIFIED_BINLOG_GTID_RECOVERY(MySQLSystemVariableFlag.READONLY, "0"),
    
    STORAGE_ENGINE(MySQLSystemVariableFlag.SESSION, ""),
    
    THREAD_CONCURRENCY(MySQLSystemVariableFlag.READONLY, "10"),
    
    TIMED_MUTEXES(MySQLSystemVariableFlag.GLOBAL, "0");
    
    private static final Map<String, MySQLSystemVariable> ALL_VARIABLES = Arrays.stream(values()).collect(Collectors.toMap(Enum::name, Function.identity()));
    
    private final int flag;
    
    @Getter
    private final String defaultValue;
    
    private final MySQLSystemVariableValueProvider variableValueProvider;
    
    MySQLSystemVariable(final int flag, final String defaultValue) {
        this(flag, defaultValue, MySQLSystemVariableValueProvider.DEFAULT_PROVIDER);
    }
    
    /**
     * Find system variable by name.
     *
     * @param name variable name
     * @return system variable
     */
    public static Optional<MySQLSystemVariable> findSystemVariable(final String name) {
        return Optional.ofNullable(ALL_VARIABLES.get(name.toUpperCase()));
    }
    
    /**
     * Get value of system variable.
     *
     * @param scope scope
     * @param connectionSession connection session
     * @return value
     */
    public String getValue(final MySQLSystemVariableScope scope, final ConnectionSession connectionSession) {
        validateGetScope(scope);
        return variableValueProvider.get(scope, connectionSession, this);
    }
    
    private void validateGetScope(final MySQLSystemVariableScope scope) {
        if (MySQLSystemVariableScope.GLOBAL == scope) {
            ShardingSpherePreconditions.checkState(0 == (MySQLSystemVariableFlag.ONLY_SESSION & scope()),
                    () -> new IncorrectGlobalLocalVariableException(name().toLowerCase(), MySQLSystemVariableScope.SESSION.name()));
        }
        if (MySQLSystemVariableScope.SESSION == scope) {
            ShardingSpherePreconditions.checkState(
                    0 != ((MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.ONLY_SESSION) & scope()),
                    () -> new IncorrectGlobalLocalVariableException(name().toLowerCase(), MySQLSystemVariableScope.GLOBAL.name()));
        }
    }
    
    /**
     * Validate scope of set operation.
     *
     * @param scope set scope
     */
    public void validateSetTargetScope(final MySQLSystemVariableScope scope) {
        if (MySQLSystemVariableScope.GLOBAL == scope) {
            ShardingSpherePreconditions.checkState(0 == (MySQLSystemVariableFlag.ONLY_SESSION & scope()), () -> new ErrorLocalVariableException(name().toLowerCase()));
        }
        if (MySQLSystemVariableScope.SESSION == scope) {
            ShardingSpherePreconditions.checkState(0 != ((MySQLSystemVariableFlag.SESSION | MySQLSystemVariableFlag.ONLY_SESSION) & scope()),
                    () -> new ErrorGlobalVariableException(name().toLowerCase()));
        }
    }
    
    private int scope() {
        return MySQLSystemVariableFlag.SCOPE_MASK & flag;
    }
}
