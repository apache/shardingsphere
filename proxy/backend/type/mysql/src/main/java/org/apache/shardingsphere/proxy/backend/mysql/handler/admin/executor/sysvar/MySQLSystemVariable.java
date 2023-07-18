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
import org.apache.shardingsphere.db.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.dialect.mysql.exception.ErrorGlobalVariableException;
import org.apache.shardingsphere.dialect.mysql.exception.ErrorLocalVariableException;
import org.apache.shardingsphere.dialect.mysql.exception.IncorrectGlobalLocalVariableException;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
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
 * MySQL system variable.
 */
@RequiredArgsConstructor
public enum MySQLSystemVariable {
    
    ACTIVATE_ALL_ROLES_ON_LOGIN(Flag.GLOBAL, "0"),
    
    ADMIN_ADDRESS(Flag.GLOBAL | Flag.READONLY, ""),
    
    ADMIN_PORT(Flag.GLOBAL | Flag.READONLY, "33062"),
    
    ADMIN_SSL_CA(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CAPATH(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CERT(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CIPHER(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CRL(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_CRLPATH(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_SSL_KEY(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_TLS_CIPHERSUITES(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    ADMIN_TLS_VERSION(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "TLSv1.2,TLSv1.3"),
    
    AUTHENTICATION_POLICY(Flag.GLOBAL, "*,,"),
    
    AUTO_GENERATE_CERTS(Flag.GLOBAL | Flag.READONLY, "1"),
    
    AUTO_INCREMENT_INCREMENT(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    AUTO_INCREMENT_OFFSET(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    // TODO Get or set session value.
    AUTOCOMMIT(Flag.SESSION, "1"),
    
    AUTOMATIC_SP_PRIVILEGES(Flag.GLOBAL, "1"),
    
    AVOID_TEMPORAL_UPGRADE(Flag.GLOBAL, "0"),
    
    BACK_LOG(Flag.GLOBAL | Flag.READONLY, "0"),
    
    BASEDIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    BIG_TABLES(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    BIND_ADDRESS(Flag.GLOBAL | Flag.READONLY, "*"),
    
    BINLOG_CACHE_SIZE(Flag.GLOBAL, "32768"),
    
    BINLOG_CHECKSUM(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "CRC32"),
    
    BINLOG_DIRECT_NON_TRANSACTIONAL_UPDATES(Flag.SESSION, "0"),
    
    BINLOG_ENCRYPTION(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "0"),
    
    BINLOG_ERROR_ACTION(Flag.GLOBAL, "ABORT_SERVER"),
    
    BINLOG_EXPIRE_LOGS_AUTO_PURGE(Flag.GLOBAL, "1"),
    
    BINLOG_EXPIRE_LOGS_SECONDS(Flag.GLOBAL, "2592000"),
    
    BINLOG_FORMAT(Flag.SESSION, "ROW"),
    
    BINLOG_GROUP_COMMIT_SYNC_DELAY(Flag.GLOBAL, "0"),
    
    BINLOG_GROUP_COMMIT_SYNC_NO_DELAY_COUNT(Flag.GLOBAL, "0"),
    
    BINLOG_GTID_SIMPLE_RECOVERY(Flag.GLOBAL | Flag.READONLY, "1"),
    
    BINLOG_MAX_FLUSH_QUEUE_TIME(Flag.GLOBAL, "0"),
    
    BINLOG_ORDER_COMMITS(Flag.GLOBAL, "1"),
    
    BINLOG_ROTATE_ENCRYPTION_MASTER_KEY_AT_STARTUP(Flag.GLOBAL | Flag.READONLY, "0"),
    
    BINLOG_ROW_EVENT_MAX_SIZE(Flag.GLOBAL | Flag.READONLY, "8192"),
    
    BINLOG_ROW_IMAGE(Flag.SESSION, "FULL"),
    
    BINLOG_ROW_METADATA(Flag.GLOBAL, "MINIMAL"),
    
    BINLOG_ROW_VALUE_OPTIONS(Flag.SESSION, ""),
    
    BINLOG_ROWS_QUERY_LOG_EVENTS(Flag.SESSION, "0"),
    
    BINLOG_STMT_CACHE_SIZE(Flag.GLOBAL, "32768"),
    
    BINLOG_TRANSACTION_COMPRESSION(Flag.SESSION, "0"),
    
    BINLOG_TRANSACTION_COMPRESSION_LEVEL_ZSTD(Flag.SESSION, "3"),
    
    BINLOG_TRANSACTION_DEPENDENCY_HISTORY_SIZE(Flag.GLOBAL, "25000"),
    
    BINLOG_TRANSACTION_DEPENDENCY_TRACKING(Flag.GLOBAL, "COMMIT_ORDER"),
    
    BLOCK_ENCRYPTION_MODE(Flag.SESSION, "aes-128-ecb"),
    
    BUILD_ID(Flag.GLOBAL | Flag.READONLY, ""),
    
    BULK_INSERT_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "8388608"),
    
    // TODO Properly handling character set of session.
    CHARACTER_SET_CLIENT(Flag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_CONNECTION(Flag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_DATABASE(Flag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_FILESYSTEM(Flag.SESSION, "binary"),
    
    CHARACTER_SET_RESULTS(Flag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_SERVER(Flag.SESSION, "utf8mb4"),
    
    CHARACTER_SET_SYSTEM(Flag.GLOBAL | Flag.READONLY, "utf8mb4"),
    
    CHARACTER_SETS_DIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    CHECK_PROXY_USERS(Flag.GLOBAL, "0"),
    
    COLLATION_CONNECTION(Flag.SESSION, "utf8mb4_unicode_ci"),
    
    COLLATION_DATABASE(Flag.SESSION, "utf8mb4_unicode_ci"),
    
    COLLATION_SERVER(Flag.SESSION, "utf8mb4_unicode_ci"),
    
    COMPLETION_TYPE(Flag.SESSION, "NO_CHAIN"),
    
    CONCURRENT_INSERT(Flag.GLOBAL, "AUTO"),
    
    CONNECT_TIMEOUT(Flag.GLOBAL, "10"),
    
    CONNECTION_MEMORY_CHUNK_SIZE(Flag.SESSION, "8912"),
    
    CONNECTION_MEMORY_LIMIT(Flag.SESSION, "18446744073709551615"),
    
    CORE_FILE(Flag.GLOBAL | Flag.READONLY, "0"),
    
    CREATE_ADMIN_LISTENER_THREAD(Flag.GLOBAL | Flag.READONLY, "0"),
    
    CTE_MAX_RECURSION_DEPTH(Flag.SESSION | Flag.HINT_UPDATEABLE, "1000"),
    
    DATADIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    // DEBUG(Flag.SESSION, "TODO"),
    
    DEBUG_SENSITIVE_SESSION_STRING(Flag.SESSION | Flag.SENSITIVE, ""),
    
    // DEBUG_SYNC(Flag.ONLY_SESSION, "TODO"),
    
    DEFAULT_AUTHENTICATION_PLUGIN(Flag.GLOBAL | Flag.READONLY, "caching_sha2_password"),
    
    DEFAULT_COLLATION_FOR_UTF8MB4(Flag.SESSION, "utf8mb4_unicode_ci"),
    
    DEFAULT_PASSWORD_LIFETIME(Flag.GLOBAL, "0"),
    
    DEFAULT_STORAGE_ENGINE(Flag.SESSION, ""),
    
    DEFAULT_TABLE_ENCRYPTION(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    DEFAULT_TMP_STORAGE_ENGINE(Flag.SESSION | Flag.HINT_UPDATEABLE, ""),
    
    DEFAULT_WEEK_FORMAT(Flag.SESSION, "0"),
    
    DELAY_KEY_WRITE(Flag.GLOBAL, "ON"),
    
    DELAYED_INSERT_LIMIT(Flag.GLOBAL, "100"),
    
    DELAYED_INSERT_TIMEOUT(Flag.GLOBAL, "300"),
    
    DELAYED_QUEUE_SIZE(Flag.GLOBAL, "1000"),
    
    DISABLED_STORAGE_ENGINES(Flag.GLOBAL | Flag.READONLY, ""),
    
    DISCONNECT_ON_EXPIRED_PASSWORD(Flag.GLOBAL | Flag.READONLY, "1"),
    
    DIV_PRECISION_INCREMENT(Flag.SESSION | Flag.HINT_UPDATEABLE, "4"),
    
    END_MARKERS_IN_JSON(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    ENFORCE_GTID_CONSISTENCY(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "FALSE"),
    
    EQ_RANGE_INDEX_DIVE_LIMIT(Flag.SESSION | Flag.HINT_UPDATEABLE, "200"),
    
    ERROR_COUNT(Flag.ONLY_SESSION | Flag.READONLY, "0"),
    
    EVENT_SCHEDULER(Flag.GLOBAL, "ON"),
    
    EXPIRE_LOGS_DAYS(Flag.GLOBAL, "0"),
    
    // EXPLAIN_FORMAT(Flag.SESSION, "TODO"),
    
    EXPLICIT_DEFAULTS_FOR_TIMESTAMP(Flag.SESSION, "1"),
    
    EXTERNAL_USER(Flag.ONLY_SESSION | Flag.READONLY, ""),
    
    FLUSH(Flag.GLOBAL, "0"),
    
    FLUSH_TIME(Flag.GLOBAL, "0"),
    
    FOREIGN_KEY_CHECKS(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    FT_BOOLEAN_SYNTAX(Flag.GLOBAL, "+ -><()~*:\"\"&|"),
    
    FT_MAX_WORD_LEN(Flag.GLOBAL | Flag.READONLY, "84"),
    
    FT_MIN_WORD_LEN(Flag.GLOBAL | Flag.READONLY, "4"),
    
    FT_QUERY_EXPANSION_LIMIT(Flag.GLOBAL | Flag.READONLY, "20"),
    
    FT_STOPWORD_FILE(Flag.GLOBAL | Flag.READONLY, ""),
    
    GENERAL_LOG(Flag.GLOBAL, "0"),
    
    GENERAL_LOG_FILE(Flag.GLOBAL, ""),
    
    GENERATED_RANDOM_PASSWORD_LENGTH(Flag.SESSION, "20"),
    
    GLOBAL_CONNECTION_MEMORY_LIMIT(Flag.GLOBAL, "18446744073709551615"),
    
    GLOBAL_CONNECTION_MEMORY_TRACKING(Flag.SESSION, "0"),
    
    GROUP_CONCAT_MAX_LEN(Flag.SESSION | Flag.HINT_UPDATEABLE, "1024"),
    
    // GROUP_REPLICATION_CONSISTENCY(Flag.SESSION, "TODO"),
    
    // GTID_EXECUTED(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    GTID_EXECUTED_COMPRESSION_PERIOD(Flag.GLOBAL, "0"),
    
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
    
    HAVE_SSL(Flag.GLOBAL | Flag.READONLY, "NO"),
    
    // HAVE_STATEMENT_TIMEOUT(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    // HAVE_SYMLINK(Flag.GLOBAL | Flag.READONLY, "TODO"),
    
    HISTOGRAM_GENERATION_MAX_MEM_SIZE(Flag.SESSION, "20000000"),
    
    HOST_CACHE_SIZE(Flag.GLOBAL, "128"),
    
    HOSTNAME(Flag.GLOBAL | Flag.READONLY, ""),
    
    IDENTITY(Flag.ONLY_SESSION, "0"),
    
    IMMEDIATE_SERVER_VERSION(Flag.ONLY_SESSION, "999999"),
    
    INFORMATION_SCHEMA_STATS_EXPIRY(Flag.SESSION, "86400"),
    
    INIT_CONNECT(Flag.GLOBAL, ""),
    
    INIT_FILE(Flag.GLOBAL | Flag.READONLY, ""),
    
    INIT_REPLICA(Flag.GLOBAL, ""),
    
    INIT_SLAVE(Flag.GLOBAL, ""),
    
    INSERT_ID(Flag.ONLY_SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    INTERACTIVE_TIMEOUT(Flag.SESSION, "28800"),
    
    // INTERNAL_TMP_MEM_STORAGE_ENGINE(Flag.SESSION | Flag.HINT_UPDATEABLE, "TODO"),
    
    JOIN_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "262144"),
    
    KEEP_FILES_ON_CREATE(Flag.SESSION, "0"),
    
    // KEY_BUFFER_SIZE(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_AGE_THRESHOLD(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_BLOCK_SIZE(Flag.GLOBAL, "TODO"),
    
    // KEY_CACHE_DIVISION_LIMIT(Flag.GLOBAL, "TODO"),
    
    KEYRING_OPERATIONS(Flag.GLOBAL, "1"),
    
    LARGE_FILES_SUPPORT(Flag.GLOBAL | Flag.READONLY, "1"),
    
    LARGE_PAGE_SIZE(Flag.GLOBAL | Flag.READONLY, "0"),
    
    LARGE_PAGES(Flag.GLOBAL | Flag.READONLY, "0"),
    
    LAST_INSERT_ID(Flag.ONLY_SESSION, "0"),
    
    LC_MESSAGES(Flag.SESSION, ""),
    
    LC_MESSAGES_DIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    LC_TIME_NAMES(Flag.SESSION, ""),
    
    LICENSE(Flag.GLOBAL | Flag.READONLY, "GPL"),
    
    LOCAL_INFILE(Flag.GLOBAL, "0"),
    
    LOCK_WAIT_TIMEOUT(Flag.SESSION | Flag.HINT_UPDATEABLE, "31536000"),
    
    LOCKED_IN_MEMORY(Flag.GLOBAL | Flag.READONLY, "0"),
    
    LOG_BIN(Flag.GLOBAL | Flag.READONLY, "1"),
    
    LOG_BIN_BASENAME(Flag.GLOBAL | Flag.READONLY, ""),
    
    LOG_BIN_INDEX(Flag.GLOBAL | Flag.READONLY, ""),
    
    LOG_BIN_TRUST_FUNCTION_CREATORS(Flag.GLOBAL, "0"),
    
    LOG_BIN_USE_V1_ROW_EVENTS(Flag.GLOBAL, "0"),
    
    LOG_ERROR(Flag.GLOBAL | Flag.READONLY, "0"),
    
    LOG_ERROR_SERVICES(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "log_filter_internal; log_sink_internal"),
    
    LOG_ERROR_SUPPRESSION_LIST(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    LOG_ERROR_VERBOSITY(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "2"),
    
    // LOG_OUTPUT(Flag.GLOBAL, "TODO"),
    
    LOG_QUERIES_NOT_USING_INDEXES(Flag.GLOBAL, "0"),
    
    LOG_RAW(Flag.GLOBAL, "0"),
    
    LOG_REPLICA_UPDATES(Flag.GLOBAL | Flag.READONLY, "1"),
    
    LOG_SLAVE_UPDATES(Flag.GLOBAL | Flag.READONLY, "1"),
    
    LOG_SLOW_ADMIN_STATEMENTS(Flag.GLOBAL, "0"),
    
    LOG_SLOW_EXTRA(Flag.GLOBAL, "0"),
    
    LOG_SLOW_REPLICA_STATEMENTS(Flag.GLOBAL, "0"),
    
    LOG_SLOW_SLAVE_STATEMENTS(Flag.GLOBAL, "0"),
    
    LOG_STATEMENTS_UNSAFE_FOR_BINLOG(Flag.GLOBAL, "1"),
    
    LOG_THROTTLE_QUERIES_NOT_USING_INDEXES(Flag.GLOBAL, "0"),
    
    // LOG_TIMESTAMPS(Flag.GLOBAL, "TODO"),
    
    LONG_QUERY_TIME(Flag.SESSION, "4.62182e+18"),
    
    LOW_PRIORITY_UPDATES(Flag.SESSION, "0"),
    
    LOWER_CASE_FILE_SYSTEM(Flag.GLOBAL | Flag.READONLY, "0"),
    
    LOWER_CASE_TABLE_NAMES(Flag.GLOBAL | Flag.READONLY, "0"),
    
    MANDATORY_ROLES(Flag.GLOBAL, ""),
    
    // MASTER_INFO_REPOSITORY(Flag.GLOBAL, "TODO"),
    
    MASTER_VERIFY_CHECKSUM(Flag.GLOBAL, "0"),
    
    MAX_ALLOWED_PACKET(Flag.SESSION, "67108864"),
    
    MAX_BINLOG_CACHE_SIZE(Flag.GLOBAL, "18446744073709547520"),
    
    MAX_BINLOG_SIZE(Flag.GLOBAL, "1073741824"),
    
    MAX_BINLOG_STMT_CACHE_SIZE(Flag.GLOBAL, "18446744073709547520"),
    
    MAX_CONNECT_ERRORS(Flag.GLOBAL, "100"),
    
    MAX_CONNECTIONS(Flag.GLOBAL, "151"),
    
    MAX_DELAYED_THREADS(Flag.SESSION, "20"),
    
    MAX_DIGEST_LENGTH(Flag.GLOBAL | Flag.READONLY, "1024"),
    
    MAX_ERROR_COUNT(Flag.SESSION | Flag.HINT_UPDATEABLE, "1024"),
    
    MAX_EXECUTION_TIME(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    MAX_HEAP_TABLE_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "16777216"),
    
    MAX_INSERT_DELAYED_THREADS(Flag.SESSION, "20"),
    
    MAX_JOIN_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "18446744073709551615"),
    
    MAX_LENGTH_FOR_SORT_DATA(Flag.SESSION | Flag.HINT_UPDATEABLE, "4096"),
    
    MAX_POINTS_IN_GEOMETRY(Flag.SESSION | Flag.HINT_UPDATEABLE, "65536"),
    
    MAX_PREPARED_STMT_COUNT(Flag.GLOBAL, "16382"),
    
    MAX_RELAY_LOG_SIZE(Flag.GLOBAL, "0"),
    
    MAX_SEEKS_FOR_KEY(Flag.SESSION | Flag.HINT_UPDATEABLE, "18446744073709551615"),
    
    MAX_SORT_LENGTH(Flag.SESSION | Flag.HINT_UPDATEABLE, "1024"),
    
    MAX_SP_RECURSION_DEPTH(Flag.SESSION, "0"),
    
    MAX_USER_CONNECTIONS(Flag.SESSION, "0"),
    
    MAX_WRITE_LOCK_COUNT(Flag.GLOBAL, "18446744073709551615"),
    
    MIN_EXAMINED_ROW_LIMIT(Flag.SESSION, "0"),
    
    MYSQL_NATIVE_PASSWORD_PROXY_USERS(Flag.GLOBAL, "0"),
    
    NET_BUFFER_LENGTH(Flag.SESSION, "16384"),
    
    NET_READ_TIMEOUT(Flag.SESSION, "30"),
    
    NET_RETRY_COUNT(Flag.SESSION, "10"),
    
    NET_WRITE_TIMEOUT(Flag.SESSION, "60"),
    
    NEW(Flag.SESSION, "0"),
    
    OFFLINE_MODE(Flag.GLOBAL, "0"),
    
    OLD(Flag.GLOBAL | Flag.READONLY, "0"),
    
    OLD_ALTER_TABLE(Flag.SESSION, "0"),
    
    OPEN_FILES_LIMIT(Flag.GLOBAL | Flag.READONLY, "0"),
    
    OPTIMIZER_MAX_SUBGRAPH_PAIRS(Flag.SESSION | Flag.HINT_UPDATEABLE, "100000"),
    
    OPTIMIZER_PRUNE_LEVEL(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    OPTIMIZER_SEARCH_DEPTH(Flag.SESSION | Flag.HINT_UPDATEABLE, "62"),
    
    // OPTIMIZER_SWITCH(Flag.SESSION | Flag.HINT_UPDATEABLE, "TODO"),
    
    // OPTIMIZER_TRACE(Flag.SESSION, "TODO"),
    
    // OPTIMIZER_TRACE_FEATURES(Flag.SESSION, "TODO"),
    
    OPTIMIZER_TRACE_LIMIT(Flag.SESSION, "1"),
    
    OPTIMIZER_TRACE_MAX_MEM_SIZE(Flag.SESSION, "1048576"),
    
    OPTIMIZER_TRACE_OFFSET(Flag.SESSION, "-1"),
    
    ORIGINAL_COMMIT_TIMESTAMP(Flag.ONLY_SESSION, "36028797018963968"),
    
    ORIGINAL_SERVER_VERSION(Flag.ONLY_SESSION, "999999"),
    
    PARSER_MAX_MEM_SIZE(Flag.SESSION, "18446744073709551615"),
    
    PARTIAL_REVOKES(Flag.GLOBAL, "0"),
    
    PASSWORD_HISTORY(Flag.GLOBAL, "0"),
    
    PASSWORD_REQUIRE_CURRENT(Flag.GLOBAL, "0"),
    
    PASSWORD_REUSE_INTERVAL(Flag.GLOBAL, "0"),
    
    PERFORMANCE_SCHEMA(Flag.GLOBAL | Flag.READONLY, "1"),
    
    PERFORMANCE_SCHEMA_ACCOUNTS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_CURRENT(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_HISTORY(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STAGES_HISTORY_LONG(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_CPU(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_CURRENT(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_HISTORY(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_STATEMENTS_HISTORY_LONG(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_CURRENT(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_HISTORY(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_TRANSACTIONS_HISTORY_LONG(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_CURRENT(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_HISTORY(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_EVENTS_WAITS_HISTORY_LONG(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "0"),
    
    PERFORMANCE_SCHEMA_CONSUMER_GLOBAL_INSTRUMENTATION(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_STATEMENTS_DIGEST(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_CONSUMER_THREAD_INSTRUMENTATION(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    PERFORMANCE_SCHEMA_DIGESTS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_ERROR_SIZE(Flag.GLOBAL | Flag.READONLY, "5242"),
    
    PERFORMANCE_SCHEMA_EVENTS_STAGES_HISTORY_LONG_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STAGES_HISTORY_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STATEMENTS_HISTORY_LONG_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_STATEMENTS_HISTORY_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_TRANSACTIONS_HISTORY_LONG_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_TRANSACTIONS_HISTORY_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_WAITS_HISTORY_LONG_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_EVENTS_WAITS_HISTORY_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_HOSTS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_INSTRUMENT(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, ""),
    
    PERFORMANCE_SCHEMA_MAX_COND_CLASSES(Flag.GLOBAL | Flag.READONLY, "150"),
    
    PERFORMANCE_SCHEMA_MAX_COND_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_DIGEST_LENGTH(Flag.GLOBAL | Flag.READONLY, "1024"),
    
    PERFORMANCE_SCHEMA_MAX_DIGEST_SAMPLE_AGE(Flag.GLOBAL, "60"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_CLASSES(Flag.GLOBAL | Flag.READONLY, "80"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_HANDLES(Flag.GLOBAL | Flag.READONLY, "32768"),
    
    PERFORMANCE_SCHEMA_MAX_FILE_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_INDEX_STAT(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_MEMORY_CLASSES(Flag.GLOBAL | Flag.READONLY, "450"),
    
    PERFORMANCE_SCHEMA_MAX_METADATA_LOCKS(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_MUTEX_CLASSES(Flag.GLOBAL | Flag.READONLY, "350"),
    
    PERFORMANCE_SCHEMA_MAX_MUTEX_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_PREPARED_STATEMENTS_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_PROGRAM_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_RWLOCK_CLASSES(Flag.GLOBAL | Flag.READONLY, "60"),
    
    PERFORMANCE_SCHEMA_MAX_RWLOCK_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_SOCKET_CLASSES(Flag.GLOBAL | Flag.READONLY, "10"),
    
    PERFORMANCE_SCHEMA_MAX_SOCKET_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_SQL_TEXT_LENGTH(Flag.GLOBAL | Flag.READONLY, "1024"),
    
    PERFORMANCE_SCHEMA_MAX_STAGE_CLASSES(Flag.GLOBAL | Flag.READONLY, "175"),
    
    PERFORMANCE_SCHEMA_MAX_STATEMENT_CLASSES(Flag.GLOBAL | Flag.READONLY, "219"),
    
    PERFORMANCE_SCHEMA_MAX_STATEMENT_STACK(Flag.GLOBAL | Flag.READONLY, "10"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_HANDLES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_TABLE_LOCK_STAT(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_MAX_THREAD_CLASSES(Flag.GLOBAL | Flag.READONLY, "100"),
    
    PERFORMANCE_SCHEMA_MAX_THREAD_INSTANCES(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SESSION_CONNECT_ATTRS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SETUP_ACTORS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SETUP_OBJECTS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERFORMANCE_SCHEMA_SHOW_PROCESSLIST(Flag.GLOBAL, "0"),
    
    PERFORMANCE_SCHEMA_USERS_SIZE(Flag.GLOBAL | Flag.READONLY, "-1"),
    
    PERSIST_ONLY_ADMIN_X509_SUBJECT(Flag.GLOBAL | Flag.READONLY, ""),
    
    PERSIST_SENSITIVE_VARIABLES_IN_PLAINTEXT(Flag.GLOBAL | Flag.READONLY, "1"),
    
    PERSISTED_GLOBALS_LOAD(Flag.GLOBAL | Flag.READONLY, "1"),
    
    PID_FILE(Flag.GLOBAL | Flag.READONLY, ""),
    
    PLUGIN_DIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    PORT(Flag.GLOBAL | Flag.READONLY, "0"),
    
    PRELOAD_BUFFER_SIZE(Flag.SESSION, "32768"),
    
    PRINT_IDENTIFIED_WITH_AS_HEX(Flag.SESSION, "0"),
    
    PROFILING(Flag.SESSION, "0"),
    
    PROFILING_HISTORY_SIZE(Flag.SESSION, "15"),
    
    PROTOCOL_COMPRESSION_ALGORITHMS(Flag.GLOBAL, "zlib,zstd,uncompressed"),
    
    PROTOCOL_VERSION(Flag.GLOBAL | Flag.READONLY, "10"),
    
    PROXY_USER(Flag.ONLY_SESSION | Flag.READONLY, ""),
    
    PSEUDO_REPLICA_MODE(Flag.ONLY_SESSION, "0"),
    
    PSEUDO_SLAVE_MODE(Flag.ONLY_SESSION, "0"),
    
    PSEUDO_THREAD_ID(Flag.ONLY_SESSION, "0"),
    
    QUERY_ALLOC_BLOCK_SIZE(Flag.SESSION, "8192"),
    
    QUERY_PREALLOC_SIZE(Flag.SESSION, "8192"),
    
    RAND_SEED1(Flag.ONLY_SESSION, "0"),
    
    RAND_SEED2(Flag.ONLY_SESSION, "0"),
    
    RANGE_ALLOC_BLOCK_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "4096"),
    
    RANGE_OPTIMIZER_MAX_MEM_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "8388608"),
    
    // RBR_EXEC_MODE(Flag.SESSION, "TODO"),
    
    READ_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "131072"),
    
    READ_ONLY(Flag.GLOBAL, "0"),
    
    READ_RND_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "262144"),
    
    REGEXP_STACK_LIMIT(Flag.GLOBAL, "8000000"),
    
    REGEXP_TIME_LIMIT(Flag.GLOBAL, "32"),
    
    RELAY_LOG(Flag.GLOBAL | Flag.READONLY, ""),
    
    RELAY_LOG_BASENAME(Flag.GLOBAL | Flag.READONLY, ""),
    
    RELAY_LOG_INDEX(Flag.GLOBAL | Flag.READONLY, ""),
    
    RELAY_LOG_INFO_FILE(Flag.GLOBAL | Flag.READONLY, ""),
    
    // RELAY_LOG_INFO_REPOSITORY(Flag.GLOBAL, "TODO"),
    
    RELAY_LOG_PURGE(Flag.GLOBAL, "1"),
    
    RELAY_LOG_RECOVERY(Flag.GLOBAL | Flag.READONLY, "0"),
    
    RELAY_LOG_SPACE_LIMIT(Flag.GLOBAL | Flag.READONLY, "0"),
    
    REPLICA_ALLOW_BATCHING(Flag.GLOBAL, "1"),
    
    REPLICA_CHECKPOINT_GROUP(Flag.GLOBAL, "512"),
    
    REPLICA_CHECKPOINT_PERIOD(Flag.GLOBAL, "300"),
    
    REPLICA_COMPRESSED_PROTOCOL(Flag.GLOBAL, "0"),
    
    // REPLICA_EXEC_MODE(Flag.GLOBAL, "TODO"),
    
    REPLICA_LOAD_TMPDIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    REPLICA_MAX_ALLOWED_PACKET(Flag.GLOBAL, "1073741824"),
    
    REPLICA_NET_TIMEOUT(Flag.GLOBAL, "60"),
    
    // REPLICA_PARALLEL_TYPE(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "TODO"),
    
    REPLICA_PARALLEL_WORKERS(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "4"),
    
    REPLICA_PENDING_JOBS_SIZE_MAX(Flag.GLOBAL, "134217728"),
    
    REPLICA_PRESERVE_COMMIT_ORDER(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "1"),
    
    REPLICA_SKIP_ERRORS(Flag.GLOBAL | Flag.READONLY, ""),
    
    REPLICA_SQL_VERIFY_CHECKSUM(Flag.GLOBAL, "1"),
    
    REPLICA_TRANSACTION_RETRIES(Flag.GLOBAL, "10"),
    
    // REPLICA_TYPE_CONVERSIONS(Flag.GLOBAL, "TODO"),
    
    REPLICATION_OPTIMIZE_FOR_STATIC_PLUGIN_CONFIG(Flag.GLOBAL, "0"),
    
    REPLICATION_SENDER_OBSERVE_COMMIT_ONLY(Flag.GLOBAL, "0"),
    
    REPORT_HOST(Flag.GLOBAL | Flag.READONLY, ""),
    
    REPORT_PASSWORD(Flag.GLOBAL | Flag.READONLY, ""),
    
    REPORT_PORT(Flag.GLOBAL | Flag.READONLY, "0"),
    
    REPORT_USER(Flag.GLOBAL | Flag.READONLY, ""),
    
    REQUIRE_ROW_FORMAT(Flag.ONLY_SESSION, "0"),
    
    REQUIRE_SECURE_TRANSPORT(Flag.GLOBAL, "0"),
    
    // RESULTSET_METADATA(Flag.ONLY_SESSION, "TODO"),
    
    RPL_READ_SIZE(Flag.GLOBAL, "8192"),
    
    RPL_STOP_REPLICA_TIMEOUT(Flag.GLOBAL, "31536000"),
    
    RPL_STOP_SLAVE_TIMEOUT(Flag.GLOBAL, "31536000"),
    
    SCHEMA_DEFINITION_CACHE(Flag.GLOBAL, "256"),
    
    SECONDARY_ENGINE_COST_THRESHOLD(Flag.SESSION | Flag.HINT_UPDATEABLE, "4.68161e+18"),
    
    SECURE_FILE_PRIV(Flag.GLOBAL | Flag.READONLY, "NULL"),
    
    SELECT_INTO_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "131072"),
    
    SELECT_INTO_DISK_SYNC(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SELECT_INTO_DISK_SYNC_DELAY(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SERVER_ID(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "1"),
    
    SERVER_ID_BITS(Flag.GLOBAL, "32"),
    
    SERVER_UUID(Flag.GLOBAL | Flag.READONLY, ""),
    
    // SESSION_TRACK_GTIDS(Flag.SESSION, "TODO"),
    
    SESSION_TRACK_SCHEMA(Flag.SESSION, "1"),
    
    SESSION_TRACK_STATE_CHANGE(Flag.SESSION, "0"),
    
    SESSION_TRACK_SYSTEM_VARIABLES(Flag.SESSION, "time_zone,autocommit,character_set_client,character_set_results,character_set_connection"),
    
    // SESSION_TRACK_TRANSACTION_INFO(Flag.SESSION, "TODO"),
    
    SHA256_PASSWORD_PROXY_USERS(Flag.GLOBAL, "0"),
    
    SHOW_CREATE_TABLE_SKIP_SECONDARY_ENGINE(Flag.ONLY_SESSION, "0"),
    
    SHOW_CREATE_TABLE_VERBOSITY(Flag.SESSION, "0"),
    
    SHOW_GIPK_IN_CREATE_TABLE_AND_INFORMATION_SCHEMA(Flag.SESSION, "1"),
    
    SHOW_OLD_TEMPORALS(Flag.SESSION, "0"),
    
    SKIP_EXTERNAL_LOCKING(Flag.GLOBAL | Flag.READONLY, "1"),
    
    SKIP_NAME_RESOLVE(Flag.GLOBAL | Flag.READONLY, "0"),
    
    SKIP_NETWORKING(Flag.GLOBAL | Flag.READONLY, "0"),
    
    SKIP_REPLICA_START(Flag.GLOBAL | Flag.READONLY, "0"),
    
    SKIP_SHOW_DATABASE(Flag.GLOBAL | Flag.READONLY, "0"),
    
    SKIP_SLAVE_START(Flag.GLOBAL | Flag.READONLY, "0"),
    
    SLAVE_ALLOW_BATCHING(Flag.GLOBAL, "1"),
    
    SLAVE_CHECKPOINT_GROUP(Flag.GLOBAL, "512"),
    
    SLAVE_CHECKPOINT_PERIOD(Flag.GLOBAL, "300"),
    
    SLAVE_COMPRESSED_PROTOCOL(Flag.GLOBAL, "0"),
    
    SLAVE_EXEC_MODE(Flag.GLOBAL, "STRICT"),
    
    SLAVE_LOAD_TMPDIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    SLAVE_MAX_ALLOWED_PACKET(Flag.GLOBAL, "1073741824"),
    
    SLAVE_NET_TIMEOUT(Flag.GLOBAL, "60"),
    
    SLAVE_PARALLEL_TYPE(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "LOGICAL_CLOCK"),
    
    SLAVE_PARALLEL_WORKERS(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "4"),
    
    SLAVE_PENDING_JOBS_SIZE_MAX(Flag.GLOBAL, "134217728"),
    
    SLAVE_PRESERVE_COMMIT_ORDER(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "1"),
    
    SLAVE_ROWS_SEARCH_ALGORITHMS(Flag.GLOBAL, "INDEX_SCAN,HASH_SCAN"),
    
    SLAVE_SKIP_ERRORS(Flag.GLOBAL | Flag.READONLY, ""),
    
    SLAVE_SQL_VERIFY_CHECKSUM(Flag.GLOBAL, "1"),
    
    SLAVE_TRANSACTION_RETRIES(Flag.GLOBAL, "10"),
    
    SLAVE_TYPE_CONVERSIONS(Flag.GLOBAL, ""),
    
    SLOW_LAUNCH_TIME(Flag.GLOBAL, "2"),
    
    SLOW_QUERY_LOG(Flag.GLOBAL, "0"),
    
    SLOW_QUERY_LOG_FILE(Flag.GLOBAL, ""),
    
    SOCKET(Flag.GLOBAL | Flag.READONLY, ""),
    
    SORT_BUFFER_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "262144"),
    
    SOURCE_VERIFY_CHECKSUM(Flag.GLOBAL, "0"),
    
    SQL_AUTO_IS_NULL(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SQL_BIG_SELECTS(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SQL_BUFFER_RESULT(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SQL_GENERATE_INVISIBLE_PRIMARY_KEY(Flag.SESSION, "0"),
    
    SQL_LOG_BIN(Flag.ONLY_SESSION, "1"),
    
    SQL_LOG_OFF(Flag.SESSION, "0"),
    
    SQL_MODE(Flag.SESSION | Flag.HINT_UPDATEABLE, "ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION"),
    
    SQL_NOTES(Flag.SESSION, "1"),
    
    SQL_QUOTE_SHOW_CREATE(Flag.SESSION, "1"),
    
    SQL_REPLICA_SKIP_COUNTER(Flag.GLOBAL, "0"),
    
    SQL_REQUIRE_PRIMARY_KEY(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SQL_SAFE_UPDATES(Flag.SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    SQL_SELECT_LIMIT(Flag.SESSION | Flag.HINT_UPDATEABLE, "18446744073709551615"),
    
    SQL_SLAVE_SKIP_COUNTER(Flag.GLOBAL, "0"),
    
    SQL_WARNINGS(Flag.SESSION, "0"),
    
    SSL_CA(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CAPATH(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CERT(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CIPHER(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CRL(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_CRLPATH(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_FIPS_MODE(Flag.GLOBAL, "OFF"),
    
    SSL_KEY(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    SSL_SESSION_CACHE_MODE(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "1"),
    
    SSL_SESSION_CACHE_TIMEOUT(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "300"),
    
    STORED_PROGRAM_CACHE(Flag.GLOBAL, "256"),
    
    STORED_PROGRAM_DEFINITION_CACHE(Flag.GLOBAL, "256"),
    
    SUPER_READ_ONLY(Flag.GLOBAL, "0"),
    
    SYNC_BINLOG(Flag.GLOBAL, "1"),
    
    SYNC_MASTER_INFO(Flag.GLOBAL, "10000"),
    
    SYNC_RELAY_LOG(Flag.GLOBAL, "10000"),
    
    SYNC_RELAY_LOG_INFO(Flag.GLOBAL, "10000"),
    
    SYNC_SOURCE_INFO(Flag.GLOBAL, "10000"),
    
    // TODO Retrieve proper system time zone.
    SYSTEM_TIME_ZONE(Flag.GLOBAL | Flag.READONLY, "UTC"),
    
    TABLE_DEFINITION_CACHE(Flag.GLOBAL, "400"),
    
    TABLE_ENCRYPTION_PRIVILEGE_CHECK(Flag.GLOBAL, "0"),
    
    TABLE_OPEN_CACHE(Flag.GLOBAL, "4000"),
    
    TABLE_OPEN_CACHE_INSTANCES(Flag.GLOBAL | Flag.READONLY, "16"),
    
    TABLESPACE_DEFINITION_CACHE(Flag.GLOBAL, "256"),
    
    TEMPTABLE_MAX_MMAP(Flag.GLOBAL, "1073741824"),
    
    TEMPTABLE_MAX_RAM(Flag.GLOBAL, "1073741824"),
    
    TEMPTABLE_USE_MMAP(Flag.GLOBAL, "1"),
    
    TERMINOLOGY_USE_PREVIOUS(Flag.SESSION, "NONE"),
    
    THREAD_CACHE_SIZE(Flag.GLOBAL, "0"),
    
    THREAD_HANDLING(Flag.GLOBAL | Flag.READONLY, "one-thread-per-connection"),
    
    THREAD_STACK(Flag.GLOBAL | Flag.READONLY, "1048576"),
    
    TIME_ZONE(Flag.SESSION | Flag.HINT_UPDATEABLE, "SYSTEM"),
    
    TIMESTAMP(Flag.ONLY_SESSION | Flag.HINT_UPDATEABLE, "0"),
    
    TLS_CIPHERSUITES(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, ""),
    
    TLS_VERSION(Flag.GLOBAL | Flag.PERSIST_AS_READ_ONLY, "TLSv1.2,TLSv1.3"),
    
    TMP_TABLE_SIZE(Flag.SESSION | Flag.HINT_UPDATEABLE, "16777216"),
    
    TMPDIR(Flag.GLOBAL | Flag.READONLY, ""),
    
    TRANSACTION_ALLOC_BLOCK_SIZE(Flag.SESSION, "8192"),
    
    TRANSACTION_ALLOW_BATCHING(Flag.ONLY_SESSION, "0"),
    
    TRANSACTION_ISOLATION(Flag.SESSION | Flag.TRI_LEVEL, "REPEATABLE-READ", new TransactionIsolationValueProvider()),
    
    TRANSACTION_PREALLOC_SIZE(Flag.SESSION, "4096"),
    
    TRANSACTION_READ_ONLY(Flag.SESSION | Flag.TRI_LEVEL, "0", new TransactionReadOnlyValueProvider()),
    
    TRANSACTION_WRITE_SET_EXTRACTION(Flag.SESSION, "XXHASH64"),
    
    UNIQUE_CHECKS(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    UPDATABLE_VIEWS_WITH_LIMIT(Flag.SESSION | Flag.HINT_UPDATEABLE, "YES"),
    
    USE_SECONDARY_ENGINE(Flag.ONLY_SESSION | Flag.HINT_UPDATEABLE, "ON"),
    
    VALIDATE_USER_PLUGINS(Flag.GLOBAL | Flag.READONLY | Flag.INVISIBLE, "1"),
    
    VERSION(Flag.GLOBAL | Flag.READONLY, DatabaseProtocolServerInfo.getDefaultProtocolVersion(TypedSPILoader.getService(DatabaseType.class, "MySQL")), new VersionValueProvider()),
    
    VERSION_COMMENT(Flag.GLOBAL | Flag.READONLY, "Source distribution"),
    
    VERSION_COMPILE_MACHINE(Flag.GLOBAL | Flag.READONLY, "x86_64"),
    
    VERSION_COMPILE_OS(Flag.GLOBAL | Flag.READONLY, "Linux"),
    
    VERSION_COMPILE_ZLIB(Flag.GLOBAL | Flag.READONLY, "1.2.13"),
    
    WAIT_TIMEOUT(Flag.SESSION, "28800"),
    
    WARNING_COUNT(Flag.ONLY_SESSION | Flag.READONLY, "0"),
    
    WINDOWING_USE_HIGH_PRECISION(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    // The following variables are from MySQL 5.7
    
    XA_DETACH_ON_PREPARE(Flag.SESSION | Flag.HINT_UPDATEABLE, "1"),
    
    DATE_FORMAT(Flag.GLOBAL | Flag.READONLY, "%Y-%m-%d"),
    
    DATETIME_FORMAT(Flag.GLOBAL | Flag.READONLY, "%Y-%m-%d %H:%i:%s"),
    
    HAVE_CRYPT(Flag.GLOBAL | Flag.READONLY, "NO"),
    
    IGNORE_BUILTIN_INNODB(Flag.GLOBAL | Flag.READONLY, "0"),
    
    IGNORE_DB_DIRS(Flag.GLOBAL | Flag.READONLY, ""),
    
    INTERNAL_TMP_DISK_STORAGE_ENGINE(Flag.GLOBAL, "InnoDB"),
    
    LOG_BUILTIN_AS_IDENTIFIED_BY_PASSWORD(Flag.GLOBAL, "0"),
    
    LOG_SYSLOG(Flag.GLOBAL, "0"),
    
    LOG_SYSLOG_FACILITY(Flag.GLOBAL, "daemon"),
    
    LOG_SYSLOG_INCLUDE_PID(Flag.GLOBAL, "1"),
    
    LOG_SYSLOG_TAG(Flag.GLOBAL, ""),
    
    LOG_WARNINGS(Flag.GLOBAL, "2"),
    
    MAX_TMP_TABLES(Flag.SESSION, "32"),
    
    METADATA_LOCKS_CACHE_SIZE(Flag.GLOBAL | Flag.READONLY, "1024"),
    
    METADATA_LOCKS_HASH_INSTANCES(Flag.GLOBAL | Flag.READONLY, "8"),
    
    MULTI_RANGE_COUNT(Flag.SESSION, "256"),
    
    OLD_PASSWORDS(Flag.SESSION, "0"),
    
    QUERY_CACHE_LIMIT(Flag.GLOBAL, "1048576"),
    
    QUERY_CACHE_MIN_RES_UNIT(Flag.GLOBAL, "4096"),
    
    QUERY_CACHE_SIZE(Flag.GLOBAL, "1048576"),
    
    QUERY_CACHE_TYPE(Flag.SESSION, "OFF"),
    
    QUERY_CACHE_WLOCK_INVALIDATE(Flag.SESSION, "0"),
    
    SECURE_AUTH(Flag.GLOBAL, "1"),
    
    SHOW_COMPATIBILITY_56(Flag.GLOBAL, "0"),
    
    SYNC_FRM(Flag.GLOBAL, "1"),
    
    TIME_FORMAT(Flag.GLOBAL | Flag.READONLY, "%H:%i:%s"),
    
    TX_ISOLATION(Flag.SESSION | Flag.TRI_LEVEL, "REPEATABLE-READ", new TransactionIsolationValueProvider()),
    
    TX_READ_ONLY(Flag.SESSION | Flag.TRI_LEVEL, "0", new TransactionReadOnlyValueProvider()),
    
    // The following variables are from MySQL 5.6
    
    BINLOGGING_IMPOSSIBLE_MODE(Flag.GLOBAL, "IGNORE_ERROR"),
    
    SIMPLIFIED_BINLOG_GTID_RECOVERY(Flag.READONLY, "0"),
    
    STORAGE_ENGINE(Flag.SESSION, ""),
    
    THREAD_CONCURRENCY(Flag.READONLY, "10"),
    
    TIMED_MUTEXES(Flag.GLOBAL, "0");
    
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
    public String getValue(final Scope scope, final ConnectionSession connectionSession) {
        validateGetScope(scope);
        return variableValueProvider.get(scope, connectionSession, this);
    }
    
    private void validateGetScope(final Scope scope) {
        if (Scope.GLOBAL == scope) {
            ShardingSpherePreconditions.checkState(0 == (Flag.ONLY_SESSION & scope()), () -> new IncorrectGlobalLocalVariableException(name().toLowerCase(), Scope.SESSION.name()));
        }
        if (Scope.SESSION == scope) {
            ShardingSpherePreconditions.checkState(0 != ((Flag.SESSION | Flag.ONLY_SESSION) & scope()), () -> new IncorrectGlobalLocalVariableException(name().toLowerCase(), Scope.GLOBAL.name()));
        }
    }
    
    /**
     * Validate scope of set operation.
     * @param scope set scope
     */
    public void validateSetTargetScope(final Scope scope) {
        if (Scope.GLOBAL == scope) {
            ShardingSpherePreconditions.checkState(0 == (Flag.ONLY_SESSION & scope()), () -> new ErrorLocalVariableException(name().toLowerCase()));
        }
        if (Scope.SESSION == scope) {
            ShardingSpherePreconditions.checkState(0 != ((Flag.SESSION | Flag.ONLY_SESSION) & scope()), () -> new ErrorGlobalVariableException(name().toLowerCase()));
        }
    }
    
    private int scope() {
        return Flag.SCOPE_MASK & flag;
    }
}
