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

lexer grammar DorisKeyword;

import Alphabet;

ACCESSIBLE
    : A C C E S S I B L E
    ;

ACCOUNT
    : A C C O U N T
    ;

ACTION
    : A C T I O N
    ;

ACTIVE
    : A C T I V E
    ;

ADD
    : A D D
    ;

ADMIN
    : A D M I N
    ;

AFTER
    : A F T E R
    ;

AGAINST
    : A G A I N S T
    ;

AGGREGATE
    : A G G R E G A T E
    ;

ALGORITHM
    : A L G O R I T H M
    ;

ALL
    : A L L
    ;

ALTER
    : A L T E R
    ;

ALWAYS
    : A L W A Y S
    ;

ANALYZE
    : A N A L Y Z E
    ;

AND
    : A N D
    ;

ANY
    : A N Y
    ;

ARRAY
    : A R R A Y
    ;

AS
    : A S
    ;

ASC
    : A S C
    ;

ASCII
    : A S C I I
    ;

ASENSITIVE
    : A S E N S I T I V E
    ;

AT
    : A T
    ;

ATTRIBUTE
    : A T T R I B U T E
    ;

AUTO
    : A U T O
    ;

AUTOEXTEND_SIZE
    : A U T O E X T E N D UL_ S I Z E
    ;

AUTO_INCREMENT
    : A U T O UL_ I N C R E M E N T
    ;

AVG
    : A V G
    ;

ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS
    : A S S I G N UL_ G T I D S UL_ T O UL_ A N O N Y M O U S UL_ T R A N S A C T I O N S
    ;

ALIAS
    : A L I A S
    ;

// DORIS ADDED BEGIN
BITXOR
    : B I T X O R
    ;
// DORIS ADDED END

BIT_XOR
    : B I T UL_ X O R
    ;

AVG_ROW_LENGTH
    : A V G UL_ R O W UL_ L E N G T H
    ;

BACKUP
    : B A C K U P
    ;

BEFORE
    : B E F O R E
    ;

BEGIN
    : B E G I N
    ;

BETWEEN
    : B E T W E E N
    ;

BIGINT
    : B I G I N T
    ;

BINARY
    : B I N A R Y
    ;

BINLOG
    : B I N L O G
    ;

BIT
    : B I T
    ;

BLOB
    : B L O B
    ;

BLOCK
    : B L O C K
    ;

BOOL
    : B O O L
    ;

BOOLEAN
    : B O O L E A N
    ;

BOTH
    : B O T H
    ;

BTREE
    : B T R E E
    ;

BUCKETS
    : B U C K E T S
    ;

BUILD
    : B U I L D
    ;

BY
    : B Y
    ;

BYTE
    : B Y T E
    ;

CACHE
    : C A C H E
    ;

CALL
    : C A L L
    ;

CASCADE
    : C A S C A D E
    ;

CASCADED
    : C A S C A D E D
    ;

CASE
    : C A S E
    ;

CATALOG
    : C A T A L O G
    ;

CATALOG_NAME
    : C A T A L O G UL_ N A M E
    ;

CHAIN
    : C H A I N
    ;

CHANGE
    : C H A N G E
    ;

CHANGED
    : C H A N G E D
    ;

CHANNEL
    : C H A N N E L
    ;

CHAR
    : C H A R
    ;

CHAR_VARYING
    : CHAR ' ' VARYING
    ;

CHARACTER
    : C H A R A C T E R
    ;

CHARACTER_VARYING
    : CHARACTER ' ' VARYING
    ;

CHARSET
    : C H A R S E T
    ;

CHECK
    : C H E C K
    ;

CHECKSUM
    : C H E C K S U M
    ;

CIPHER
    : C I P H E R
    ;

CLASS_ORIGIN
    : C L A S S UL_ O R I G I N
    ;

CLIENT
    : C L I E N T
    ;

CLONE
    : C L O N E
    ;

CLOSE
    : C L O S E
    ;

COALESCE
    : C O A L E S C E
    ;

CODE
    : C O D E
    ;

COLLATE
    : C O L L A T E
    ;

COLLATION
    : C O L L A T I O N
    ;

COLUMN
    : C O L U M N
    ;

COLUMNS
    : C O L U M N S
    ;

COLUMN_FORMAT
    : C O L U M N UL_ F O R M A T
    ;

COLUMN_NAME
    : C O L U M N UL_ N A M E
    ;

COMMENT
    : C O M M E N T
    ;

COMMIT
    : C O M M I T
    ;

COMMITTED
    : C O M M I T T E D
    ;

COMPACT
    : C O M P A C T
    ;

COMPLETION
    : C O M P L E T I O N
    ;

// DORIS ADDED BEGIN
COMPLETE
    : C O M P L E T E
    ;
// DORIS ADDED END

COMPONENT
    : C O M P O N E N T
    ;

COMPRESSED
    : C O M P R E S S E D
    ;

COMPRESSION
    : C O M P R E S S I O N
    ;

CONCURRENT
    : C O N C U R R E N T
    ;

CONDITION
    : C O N D I T I O N
    ;

CONNECTION
    : C O N N E C T I O N
    ;

CONSISTENT
    : C O N S I S T E N T
    ;

CONSTRAINT
    : C O N S T R A I N T
    ;

CONSTRAINT_CATALOG
    : C O N S T R A I N T UL_ C A T A L O G
    ;

CONSTRAINT_NAME
    : C O N S T R A I N T UL_ N A M E
    ;

CONSTRAINT_SCHEMA
    : C O N S T R A I N T UL_ S C H E M A
    ;

CONTAINS
    : C O N T A I N S
    ;

CONTEXT
    : C O N T E X T
    ;

CONTINUE
    : C O N T I N U E
    ;

CONVERT
    : C O N V E R T
    ;

CPU
    : C P U
    ;

CREATE
    : C R E A T E
    ;

CROSS
    : C R O S S
    ;

CUBE
    : C U B E
    ;

CUME_DIST
    : C U M E UL_ D I S T
    ;

CURRENT
    : C U R R E N T
    ;

CURRENT_DATE
    : C U R R E N T UL_ D A T E
    ;

CURRENT_TIME
    : C U R R E N T UL_ T I M E
    ;

CURRENT_TIMESTAMP
    : C U R R E N T UL_ T I M E S T A M P
    ;

CURRENT_USER
    : C U R R E N T UL_ U S E R
    ;

CURSOR
    : C U R S O R
    ;

CURSOR_NAME
    : C U R S O R UL_ N A M E
    ;

DATA
    : D A T A
    ;

DATABASE
    : D A T A B A S E
    ;

DATABASES
    : D A T A B A S E S
    ;

DATAFILE
    : D A T A F I L E
    ;

DATE
    : D A T E
    ;

DATETIME
    : D A T E T I M E
    ;

DAY
    : D A Y
    ;

DAY_HOUR
    : D A Y UL_ H O U R
    ;

DAY_MICROSECOND
    : D A Y UL_ M I C R O S E C O N D
    ;

DAY_MINUTE
    : D A Y UL_ M I N U T E
    ;

DAY_SECOND
    : D A Y UL_ S E C O N D
    ;

DEALLOCATE
    : D E A L L O C A T E
    ;

DEC
    : D E C
    ;

DECIMAL
    : D E C I M A L
    ;

// DORIS ADDED BEGIN
DECIMAL64
    : D E C I M A L '64'
    ;
// DORIS ADDED END

DECLARE
    : D E C L A R E
    ;

DEFAULT
    : D E F A U L T
    ;

DEFAULT_AUTH
    : D E F A U L T UL_ A U T H
    ;

// DORIS ADDED BEGIN
DEFERRED
    : D E F E R R E D
    ;
// DORIS ADDED END

DEFINER
    : D E F I N E R
    ;

DEFINITION
    : D E F I N I T I O N
    ;

DELAYED
    : D E L A Y E D
    ;

DELAY_KEY_WRITE
    : D E L A Y UL_ K E Y UL_ W R I T E
    ;

DELETE
    : D E L E T E
    ;

DENSE_RANK
    : D E N S E UL_ R A N K
    ;

DESC
    : D E S C
    ;

DESCRIBE
    : D E S C R I B E
    ;

DESCRIPTION
    : D E S C R I P T I O N
    ;

DETERMINISTIC
    : D E T E R M I N I S T I C
    ;

DIAGNOSTICS
    : D I A G N O S T I C S
    ;

DIRECTORY
    : D I R E C T O R Y
    ;

DISABLE
    : D I S A B L E
    ;

DISCARD
    : D I S C A R D
    ;

DISK
    : D I S K
    ;

DISTINCT
    : D I S T I N C T
    ;

DISTINCTROW
    : D I S T I N C T R O W
    ;

DIV
    : D I V
    ;

DO
    : D O
    ;

DOUBLE
    : D O U B L E
    ;

DROP
    : D R O P
    ;

DUAL
    : D U A L
    ;

DUMPFILE
    : D U M P F I L E
    ;

DUPLICATE
    : D U P L I C A T E
    ;

// DORIS ADDED BEGIN
DISTRIBUTED
    : D I S T R I B U T E D
    ;
// DORIS ADDED END

DYNAMIC
    : D Y N A M I C
    ;

EACH
    : E A C H
    ;

ELSE
    : E L S E
    ;

ELSEIF
    : E L S E I F
    ;

EMPTY
    : E M P T Y
    ;

ENABLE
    : E N A B L E
    ;

ENCLOSED
    : E N C L O S E D
    ;

ENCRYPTION
    : E N C R Y P T I O N
    ;

ENCRYPTKEY
    : E N C R Y P T K E Y
    ;

END
    : E N D
    ;

ENDS
    : E N D S
    ;

ENFORCED
    : E N F O R C E D
    ;

ENGINE
    : E N G I N E
    ;

ENGINES
    : E N G I N E S
    ;

ENGINE_ATTRIBUTE
    : E N G I N E UL_ A T T R I B U T E
    ;

ENUM
    : E N U M
    ;

ERROR
    : E R R O R
    ;

ERRORS
    : E R R O R S
    ;

ESCAPE
    : E S C A P E
    ;

ESCAPED
    : E S C A P E D
    ;

EVENT
    : E V E N T
    ;

EVENTS
    : E V E N T S
    ;

EVERY
    : E V E R Y
    ;

EXCEPT
    : E X C E P T
    ;

EXCHANGE
    : E X C H A N G E
    ;

EXCLUDE
    : E X C L U D E
    ;

EXECUTE
    : E X E C U T E
    ;

EXISTS
    : E X I S T S
    ;

EXIT
    : E X I T
    ;

EXPANSION
    : E X P A N S I O N
    ;

EXPIRE
    : E X P I R E
    ;

EXPLAIN
    : E X P L A I N
    ;

EXPORT
    : E X P O R T
    ;

EXTENDED
    :  E X T E N D E D
    ;

// DORIS ADDED BEGIN
EXTRACT_URL_PARAMETER
    : E X T R A C T UL_ U R L UL_ P A R A M E T E R
    ;
// DORIS ADDED END

EXTENT_SIZE
    : E X T E N T UL_ S I Z E
    ;

FAILED_LOGIN_ATTEMPTS
    : F A I L E D UL_ L O G I N UL_ A T T E M P T S
    ;

FALSE
    : F A L S E
    ;

FAST
    : F A S T
    ;

FAULTS
    : F A U L T S
    ;

FETCH
    : F E T C H
    ;

FIELDS
    : F I E L D S  -> type(COLUMNS)
    ;

FILE
    : F I L E
    ;

FILE_BLOCK_SIZE
    : F I L E UL_ B L O C K UL_ S I Z E
    ;

FILTER
    : F I L T E R
    ;

FIRST
    : F I R S T
    ;

FIRST_VALUE
    : F I R S T UL_ V A L U E
    ;

FIXED
    : F I X E D
    ;

FLOAT
    : F L O A T
    ;

FLOAT4
    : F L O A T '4'
    ;

FLOAT8
    : F L O A T '8'
    ;

FLUSH
    : F L U S H
    ;

FOLLOWING
    : F O L L O W I N G
    ;

FOLLOWS
    : F O L L O W S
    ;

FOR
    : F O R
    ;

FORCE
    : F O R C E
    ;

FOREIGN
    : F O R E I G N
    ;

FORMAT
    : F O R M A T
    ;

FOUND
    : F O U N D
    ;

FROM
    : F R O M
    ;

FULL
    : F U L L
    ;

FULLTEXT
    : F U L L T E X T
    ;

FUNCTION
    : F U N C T I O N
    ;

GENERAL
    : G E N E R A L
    ;

GENERATED
    : G E N E R A T E D
    ;

GEOMETRY
    : G E O M E T R Y
    ;

GEOMCOLLECTION
    : G E O M C O L L E C T I O N
    ;

GEOMETRYCOLLECTION
    : G E O M E T R Y C O L L E C T I O N
    ;

GET
    : G E T
    ;

GET_FORMAT
    : G E T UL_ F O R M A T
    ;

GET_MASTER_PUBLIC_KEY
    : G E T UL_ M A S T E R UL_ P U B L I C UL_ K E Y
    ;

GLOBAL
    : G L O B A L
    ;

GRANT
    : G R A N T
    ;

GRANTS
    : G R A N T S
    ;

GROUP
    : G R O U P
    ;

GROUPING
    : G R O U P I N G
    ;

GROUPS
    : G R O U P S
    ;

GROUP_REPLICATION
    : G R O U P UL_ R E P L I C A T I O N
    ;

GET_SOURCE_PUBLIC_KEY
    : G E T UL_ S O U R C E UL_ P U B L I C UL_ K E Y
    ;

GTID_ONLY
    : G T I D UL_ O N L Y
    ;

GENERATE
    : G E N E R A T E
    ;

HANDLER
    : H A N D L E R
    ;

HASH
    : H A S H
    ;

HAVING
    : H A V I N G
    ;

HELP
    : H E L P
    ;

HIGH_PRIORITY
    : H I G H UL_ P R I O R I T Y
    ;

HISTOGRAM
    : H I S T O G R A M
    ;

HISTORY
    : H I S T O R Y
    ;

HOST
    : H O S T
    ;

HOSTS
    : H O S T S
    ;

HOUR
    : H O U R
    ;

HOUR_MICROSECOND
    : H O U R UL_ M I C R O S E C O N D
    ;

HOUR_MINUTE
    : H O U R UL_ M I N U T E
    ;

HOUR_SECOND
    : H O U R UL_ S E C O N D
    ;

IDENTIFIED
    : I D E N T I F I E D
    ;

IF
    : I F
    ;

IGNORE
    : I G N O R E
    ;

IGNORE_SERVER_IDS
    : I G N O R E UL_ S E R V E R UL_ I D S
    ;

// DORIS ADDED BEGIN
IMMEDIATE
    : I M M E D I A T E
    ;
// DORIS ADDED END

IMPORT
    : I M P O R T
    ;

IN
    : I N
    ;

INACTIVE
    : I N A C T I V E
    ;

INDEX
    : I N D E X
    ;

INDEXES
    : I N D E X E S
    ;

INFILE
    : I N F I L E
    ;

INITIAL_SIZE
    : I N I T I A L UL_ S I Z E
    ;

INNER
    : I N N E R
    ;

INOUT
    : I N O U T
    ;

INSENSITIVE
    : I N S E N S I T I V E
    ;

INSERT
    : I N S E R T
    ;

INSERT_METHOD
    : I N S E R T UL_ M E T H O D
    ;

INSTALL
    : I N S T A L L
    ;

INSTANCE
    : I N S T A N C E
    ;

// DORIS ADDED BEGIN
INSTR
    : I N S T R
    ;
// DORIS ADDED END


INT
    : I N T
    ;

INT1
    : I N T '1'
    ;

INT2
    : I N T '2'
    ;

INT3
    : I N T '3'
    ;

INT4
    : I N T '4'
    ;

INT8
    : I N T '8'
    ;

INTEGER
    : I N T E G E R
    ;

INTERSECT
    : I N T E R S E C T
    ;

INTERVAL
    : I N T E R V A L
    ;

INTO
    : I N T O
    ;

INVISIBLE
    : I N V I S I B L E
    ;

INVOKER
    : I N V O K E R
    ;

IO
    : I O
    ;

IO_AFTER_GTIDS
    : I O UL_ A F T E R UL_ G T I D S
    ;

IO_BEFORE_GTIDS
    : I O UL_ B E F O R E UL_ G T I D S
    ;

IO_THREAD
    : I O UL_ T H R E A D -> type(RELAY_THREAD)
    ;

IPC
    : I P C
    ;

IS
    : I S
    ;

ISOLATION
    : I S O L A T I O N
    ;

ISSUER
    : I S S U E R
    ;

ITERATE
    : I T E R A T E
    ;

INTERMEDIATE
    : I N T E R M E D I A T E
    ;

JOIN
    : J O I N
    ;

JSON
    : J S O N
    ;

JSON_TABLE
    : J S O N UL_ T A B L E
    ;

JSON_VALUE
    : J S O N UL_ V A L U E
    ;

JOB
    : J O B
    ;

KEY
    : K E Y
    ;

KEYS
    : K E Y S
    ;

KEY_BLOCK_SIZE
    : K E Y UL_ B L O C K UL_ S I Z E
    ;

KILL
    : K I L L
    ;

LAG
    : L A G
    ;

LDAP
    : L D A P
    ;

LANGUAGE
    : L A N G U A G E
    ;

LAST
    : L A S T
    ;

LAST_VALUE
    : L A S T UL_ V A L U E
    ;

LATERAL
    : L A T E R A L
    ;

LEAD
    : L E A D
    ;

LEADING
    : L E A D I N G
    ;

LEAVE
    : L E A V E
    ;

LEAVES
    : L E A V E S
    ;

LEFT
    : L E F T
    ;

LESS
    : L E S S
    ;

LEVEL
    : L E V E L
    ;

LIKE
    : L I K E
    ;

LIMIT
    : L I M I T
    ;

LINEAR
    : L I N E A R
    ;

LINES
    : L I N E S
    ;

LINESTRING
    : L I N E S T R I N G
    ;

LIST
    : L I S T
    ;

LOAD
    : L O A D
    ;

LOCAL
    : L O C A L
    ;

LOCALTIME
    : L O C A L T I M E
    ;

LOCALTIMESTAMP
    : L O C A L T I M E S T A M P
    ;

LOCK
    : L O C K
    ;

LOCKED
    : L O C K E D
    ;

LOCKS
    : L O C K S
    ;

LOGFILE
    : L O G F I L E
    ;

LOGS
    : L O G S
    ;

LONG
    : L O N G
    ;

LONGBLOB
    : L O N G B L O B
    ;

LONGTEXT
    : L O N G T E X T
    ;

LONG_CHAR_VARYING
    : LONG ' ' CHAR ' ' VARYING
    ;

LONG_VARCHAR
    : LONG ' ' VARCHAR
    ;

LOOP
    : L O O P
    ;

LOW_PRIORITY
    : L O W UL_ P R I O R I T Y
    ;

MANUAL
    : M A N U A L
    ;

MASTER
    : M A S T E R
    ;

MASTER_AUTO_POSITION
    : M A S T E R UL_ A U T O UL_ P O S I T I O N
    ;

MASTER_BIND
    : M A S T E R UL_ B I N D
    ;

MASTER_COMPRESSION_ALGORITHM
    : M A S T E R UL_ C O M P R E S S I O N UL_ A L G O R I T H M S
    ;

MASTER_CONNECT_RETRY
    : M A S T E R UL_ C O N N E C T UL_ R E T R Y
    ;

MASTER_DELAY
    : M A S T E R UL_ D E L A Y
    ;

MASTER_HEARTBEAT_PERIOD
    : M A S T E R UL_ H E A R T B E A T UL_ P E R I O D
    ;

MASTER_HOST
    : M A S T E R UL_ H O S T
    ;

MASTER_LOG_FILE
    : M A S T E R UL_ L O G UL_ F I L E
    ;

MASTER_LOG_POS
    : M A S T E R UL_ L O G UL_ P O S
    ;

MASTER_PASSWORD
    : M A S T E R UL_ P A S S W O R D
    ;

MASTER_PORT
    : M A S T E R UL_ P O R T
    ;

MASTER_PUBLIC_KEY_PATH
    : M A S T E R UL_ P  U B L I C UL_ K E Y UL_ P A T H
    ;

MASTER_RETRY_COUNT
    : M A S T E R UL_ R E T R Y UL_ C O U N T
    ;

MASTER_SERVER_ID
    : M A S T E R UL_ S E R V E R UL_ I D
    ;

MASTER_SSL
    : M A S T E R UL_ S S L
    ;

MASTER_SSL_CA
    : M A S T E R UL_ S S L UL_ C A
    ;

MASTER_SSL_CAPATH
    : M A S T E R UL_ S S L UL_ C A P A T H
    ;

MASTER_SSL_CERT
    : M A S T E R UL_ S S L UL_ C E R T
    ;

MASTER_SSL_CIPHER
    : M A S T E R UL_ S S L UL_ C I P H E R
    ;

MASTER_SSL_CRL
    : M A S T E R UL_ S S L UL_ C R L
    ;

MASTER_SSL_CRLPATH
    : M A S T E R UL_ S S L UL_ C R L P A T H
    ;

MASTER_SSL_KEY
    : M A S T E R UL_ S S L UL_ K E Y
    ;

MASTER_SSL_VERIFY_SERVER_CERT
    : M A S T E R UL_ S S L UL_ V E R I F Y UL_ S E R V E R UL_ C E R T
    ;

MASTER_TLS_CIPHERSUITES
    : M A S T E R UL_ T L S UL_ C I P H E R S U I T E S
    ;

MASTER_TLS_VERSION
    : M A S T E R UL_ T L S UL_ V E R S I O N
    ;

MASTER_USER
    : M A S T E R UL_ U S E R
    ;

MASTER_ZSTD_COMPRESSION_LEVEL
    : M A S T E R UL_ Z S T D UL_ C O M P R E S S I O N UL_ L E V E L
    ;

// DORIS ADDED BEGIN
MATERIALIZED
    : M A T E R I A L I Z E D
    ;
// DORIS ADDED END

MATCH
    : M A T C H
    ;

MAXVALUE
    : M A X V A L U E
    ;

MAX_CONNECTIONS_PER_HOUR
    : M A X UL_ C O N N E C T I O N S UL_ P E R UL_ H O U R
    ;

MAX_QUERIES_PER_HOUR
    : M A X UL_ Q U E R I E S UL_ P E R UL_ H O U R
    ;

MAX_ROWS
    : M A X UL_ R O W S
    ;

MAX_SIZE
    : M A X UL_ S I Z E
    ;

MAX_UPDATES_PER_HOUR
    : M A X UL_ U P D A T E S UL_ P E R UL_ H O U R
    ;

MAX_USER_CONNECTIONS
    : M A X UL_ U S E R UL_ C O N N E C T I O N S
    ;

MEDIUM
    : M E D I U M
    ;

MEDIUMBLOB
    : M E D I U M B L O B
    ;

MEDIUMINT
    : M E D I U M I N T
    ;

MEDIUMTEXT
    : M E D I U M T E X T
    ;

MEMBER
    : M E M B E R
    ;

MEMORY
    : M E M O R Y
    ;

MERGE
    : M E R G E
    ;

MESSAGE_TEXT
    : M E S S A G E UL_ T E X T
    ;

MICROSECOND
    : M I C R O S E C O N D
    ;

MIDDLEINT
    : M I D D L E I N T
    ;

MIGRATE
    : M I G R A T E
    ;

MINUTE
    : M I N U T E
    ;

MINUTE_MICROSECOND
    : M I N U T E UL_ M I C R O S E C O N D
    ;

MINUTE_SECOND
    : M I N U T E UL_ S E C O N D
    ;

MIN_ROWS
    : M I N UL_ R O W S
    ;

MOD
    : M O D
    ;

MODE
    : M O D E
    ;

MODIFIES
    : M O D I F I E S
    ;

MODIFY
    : M O D I F Y
    ;

MONTH
    : M O N T H
    ;

MULTILINESTRING
    : M U L T I L I N E S T R I N G
    ;

MULTIPOINT
    : M U L T I P O I N T
    ;

MULTIPOLYGON
    : M U L T I P O L Y G O N
    ;

MUTEX
    : M U T E X
    ;

Doris_ERRNO
    : M Y S Q L UL_ E R R N O
    ;

NAME
    : N A M E
    ;

NAMES
    : N A M E S
    ;

NATIONAL
    : N A T I O N A L
    ;

NATIONAL_CHAR
    : NATIONAL ' ' CHAR
    ;

NATIONAL_CHAR_VARYING
    : NATIONAL ' ' CHAR_VARYING
    ;

NATURAL
    : N A T U R A L
    ;

NCHAR
    : N C H A R
    ;

NDB
    : N D B -> type(NDBCLUSTER)
    ;

NDBCLUSTER
    : N D B C L U S T E R
    ;

NESTED
    : N E S T E D
    ;

NETWORK_NAMESPACE
    : N E T W O R K UL_ N A M E S P A C E
    ;

NEVER
    : N E V E R
    ;

NEW
    : N E W
    ;

NEXT
    : N E X T
    ;

NO
    : N O
    ;

NODEGROUP
    : N O D E G R O U P
    ;

NONE
    : N O N E
    ;

SHARED
    : S H A R E D
    ;

EXCLUSIVE
    : E X C L U S I V E
    ;

NOT
    : N O T
    ;

NOWAIT
    : N O W A I T
    ;

NO_WAIT
    : N O UL_ W A I T
    ;

NO_WRITE_TO_BINLOG
    : N O UL_ W R I T E UL_ T O UL_ B I N L O G
    ;

NTH_VALUE
    : N T H UL_ V A L U E
    ;

NTILE
    : N T I L E
    ;

NULL
    : N U L L
    ;

NULLS
    : N U L L S
    ;

NUMBER
    : N U M B E R
    ;

NUMERIC
    : N U M E R I C
    ;

NVARCHAR
    : N V A R C H A R
    ;

OF
    : O F
    ;

OFF
    : O F F
    ;

OFFSET
    : O F F S E T
    ;

OJ
    : O J
    ;

OLD
    : O L D
    ;

ON
    : O N
    ;

ONE
    : O N E
    ;

ONLY
    : O N L Y
    ;

OPEN
    : O P E N
    ;

OPTIMIZE
    : O P T I M I Z E
    ;

OPTIMIZER_COSTS
    : O P T I M I Z E R UL_ C O S T S
    ;

OPTION
    : O P T I O N
    ;

OPTIONAL
    : O P T I O N A L
    ;

OPTIONALLY
    : O P T I O N A L L Y
    ;

OPTIONS
    : O P T I O N S
    ;

OR
    : O R
    ;

ORDER
    : O R D E R
    ;

ORDINALITY
    : O R D I N A L I T Y
    ;

ORGANIZATION
    : O R G A N I Z A T I O N
    ;

OTHERS
    : O T H E R S
    ;

OUT
    : O U T
    ;

OUTER
    : O U T E R
    ;

OUTFILE
    : O U T F I L E
    ;

OVER
    : O V E R
    ;

OWNER
    : O W N E R
    ;

PACK_KEYS
    : P A C K UL_ K E Y S
    ;

PAGE
    : P A G E
    ;

PARSER
    : P A R S E R
    ;

PARTIAL
    : P A R T I A L
    ;

PARTITION
    : P A R T I T I O N
    ;

PARTITIONING
    : P A R T I T I O N I N G
    ;

PARTITIONS
    : P A R T I T I O N S
    ;

PASSWORD
    : P A S S W O R D
    ;

PASSWORD_LOCK_TIME
    : P A S S W O R D UL_ L O C K UL_ T I M E
    ;

PATH
    : P A T H
    ;

PERCENT_RANK
    : P E R C E N T UL_ R A N K
    ;

PERSIST
    : P E R S I S T
    ;

PERSIST_ONLY
    : P E R S I S T UL_ O N L Y
    ;

PHASE
    : P H A S E
    ;

PLUGIN
    : P L U G I N
    ;

PLUGINS
    : P L U G I N S
    ;

PLUGIN_DIR
    : P L U G I N UL_ D I R
    ;

POINT
    : P O I N T
    ;

POLYGON
    : P O L Y G O N
    ;

PORT
    : P O R T
    ;

PRECEDES
    : P R E C E D E S
    ;

PRECEDING
    : P R E C E D I N G
    ;

PRECISION
    : P R E C I S I O N
    ;

PREPARE
    : P R E P A R E
    ;

PRESERVE
    : P R E S E R V E
    ;

PREV
    : P R E V
    ;

PRIMARY
    : P R I M A R Y
    ;

PRIVILEGES
    : P R I V I L E G E S
    ;

PRIVILEGE_CHECKS_USER
    : P R I V I L E G E UL_ C H E C K S UL_ U S E R
    ;

PROCEDURE
    : P R O C E D U R E
    ;

PROCESS
    : P R O C E S S
    ;

PROCESSLIST
    : P R O C E S S L I S T
    ;

PROFILE
    : P R O F I L E
    ;

PROFILES
    : P R O F I L E S
    ;

// DORIS ADDED BEGIN
PROPERTIES
    : P R O P E R T I E S
    ;
// DORIS ADDED END

PROXY
    : P R O X Y
    ;

PURGE
    : P U R G E
    ;

PARAMETER
    : P A R A M E T E R
    ;

QUARTER
    : Q U A R T E R
    ;

QUERY
    : Q U E R Y
    ;

QUICK
    : Q U I C K
    ;

RANDOM
    : R A N D O M
    ;

RANGE
    : R A N G E
    ;

RANK
    : R A N K
    ;

READ
    : R E A D
    ;

READS
    : R E A D S
    ;

READ_ONLY
    : R E A D UL_ O N L Y
    ;

READ_WRITE
    : R E A D UL_ W R I T E
    ;

REAL
    : R E A L
    ;

REBUILD
    : R E B U I L D
    ;

RECOVER
    : R E C O V E R
    ;

RECURSIVE
    : R E C U R S I V E
    ;

REDO_BUFFER_SIZE
    : R E D O UL_ B U F F E R UL_ S I Z E
    ;

REDUNDANT
    : R E D U N D A N T
    ;

REFERENCE
    : R E F E R E N C E
    ;

REFERENCES
    : R E F E R E N C E S
    ;

REFRESH
    : R E F R E S H
    ;

REGEXP
    : R E G E X P
    ;

RELAY
    : R E L A Y
    ;

RELAYLOG
    : R E L A Y L O G
    ;

RELAY_LOG_FILE
    : R E L A Y UL_ L O G UL_ F I L E
    ;

RELAY_LOG_POS
    : R E L A Y UL_ L O G UL_ P O S
    ;

RELAY_THREAD
    : R E L A Y UL_ T H R E A D
    ;

RELEASE
    : R E L E A S E
    ;

RELOAD
    : R E L O A D
    ;

REMOVE
    : R E M O V E
    ;

RENAME
    : R E N A M E
    ;

REORGANIZE
    : R E O R G A N I Z E
    ;

REPAIR
    : R E P A I R
    ;

REPEAT
    : R E P E A T
    ;

REPEATABLE
    : R E P E A T A B L E
    ;

REPLACE
    : R E P L A C E
    ;

REPLICA
    : R E P L I C A
    ;

REPLICAS
    : R E P L I C A S
    ;

REPLICATE_DO_DB
    : R E P L I C A T E UL_ D O UL_ D B
    ;

REPLICATE_DO_TABLE
    : R E P L I C A T E UL_ D O UL_ T A B L E
    ;

REPLICATE_IGNORE_DB
    : R E P L I C A T E UL_ I G N O R E UL_ D B
    ;

REPLICATE_IGNORE_TABLE
    : R E P L I C A T E UL_ I G N O R E UL_ T A B L E
    ;

REPLICATE_REWRITE_DB
    : R E P L I C A T E UL_ R E W R I T E UL_ D B
    ;

REPLICATE_WILD_DO_TABLE
    : R E P L I C A T E UL_ W I L D UL_ D O UL_ T A B L E
    ;

REPLICATE_WILD_IGNORE_TABLE
    : R E P L I C A T E UL_ W I L D UL_ I G N O R E UL_ T A B L E
    ;

REPLICATION
    : R E P L I C A T I O N
    ;

REQUIRE
    : R E Q U I R E
    ;

REQUIRE_ROW_FORMAT
    : R E Q U I R E UL_ R O W UL_ F O R M A T
    ;

REQUIRE_TABLE_PRIMARY_KEY_CHECK
    : R E Q U I R E UL_ T A B L E UL_ P R I M A R Y UL_ K E Y UL_ C H E C K
    ;

RESET
    : R E S E T
    ;

RESIGNAL
    : R E S I G N A L
    ;

RESOURCE
    : R E S O U R C E
    ;

RESPECT
    : R E S P E C T
    ;

RESTART
    : R E S T A R T
    ;

RESTORE
    : R E S T O R E
    ;

RESTRICT
    : R E S T R I C T
    ;

RESUME
    : R E S U M E
    ;

RETAIN
    : R E T A I N
    ;

RETURN
    : R E T U R N
    ;

RETURNED_SQLSTATE
    : R E T U R N E D UL_ S Q L S T A T E
    ;

RETURNING
    : R E T U R N I N G
    ;

RETURNS
    : R E T U R N S
    ;

REUSE
    : R E U S E
    ;

REVERSE
    : R E V E R S E
    ;

REVOKE
    : R E V O K E
    ;

RIGHT
    : R I G H T
    ;

RLIKE
    : R L I K E
    ;

ROLE
    : R O L E
    ;

ROLLBACK
    : R O L L B A C K
    ;

ROLLUP
    : R O L L U P
    ;

ROTATE
    : R O T A T E
    ;

ROUTINE
    : R O U T I N E
    ;

ROW
    : R O W
    ;

ROWS
    : R O W S
    ;

ROW_COUNT
    : R O W UL_ C O U N T
    ;

ROW_FORMAT
    : R O W UL_ F O R M A T
    ;

ROW_NUMBER
    : R O W UL_ N U M B  E R
    ;

RTREE
    : R T R E E
    ;

SAVEPOINT
    : S A V E P O I N T
    ;

SCHEDULE
    : S C H E D U L E
    ;

SCHEMA
    : S C H E M A
    ;

SCHEMAS
    : S C H E M A S
    ;

SCHEMA_NAME
    : S C H E M A UL_ N A M E
    ;

SECOND
    : S E C O N D
    ;

SECONDARY
    : S E C O N D A R Y
    ;

SECONDARY_ENGINE
    : S E C O N D A R Y UL_ E N G I N E
    ;

SECONDARY_ENGINE_ATTRIBUTE
    : S E C O N D A R Y UL_ E N G I N E UL_ A T T R I B U T E
    ;

SECONDARY_LOAD
    : S E C O N D A R Y UL_ L O A D
    ;

SECONDARY_UNLOAD
    : S E C O N D A R Y UL_ U N L O A D
    ;

SECOND_MICROSECOND
    : S E C O N D UL_ M I C R O S E C O N D
    ;

SECURITY
    : S E C U R I T Y
    ;

SELECT
    : S E L E C T
    ;

SENSITIVE
    : S E N S I T I V E
    ;

SEPARATOR
    : S E P A R A T O R
    ;

SERIAL
    : S E R I A L
    ;

SERIALIZABLE
    : S E R I A L I Z A B L E
    ;

SERVER
    : S E R V E R
    ;

SESSION
    : S E S S I O N
    ;

SET
    : S E T
    ;

SHARE
    : S H A R E
    ;

SHOW
    : S H O W
    ;

SHUTDOWN
    : S H U T D O W N
    ;

SIGNAL
    : S I G N A L
    ;

SIGNED
    : S I G N E D
    ;

SIGNED_INT
    : SIGNED ' ' INT
    ;

SIGNED_INTEGER
    : SIGNED ' ' INTEGER
    ;

SIMPLE
    : S I M P L E
    ;

SKIP_SYMBOL
    : S K I P
    ;

SLAVE
    : S L A V E
    ;

SLOW
    : S L O W
    ;

SMALLINT
    : S M A L L I N T
    ;

SNAPSHOT
    : S N A P S H O T
    ;

SOCKET
    : S O C K E T
    ;

SOME
    : S O M E -> type(ANY)
    ;

SONAME
    : S O N A M E
    ;

SOUNDS
    : S O U N D S
    ;

SOURCE
    : S O U R C E
    ;

SPATIAL
    : S P A T I A L
    ;

SPECIFIC
    : S P E C I F I C
    ;

SQL
    : S Q L
    ;

SQLEXCEPTION
    : S Q L E X C E P T I O N
    ;

SQLSTATE
    : S Q L S T A T E
    ;

SQLWARNING
    : S Q L W A R N I N G
    ;

SQL_AFTER_GTIDS
    : S Q L UL_ A F T E R UL_ G T I D S
    ;

SQL_AFTER_MTS_GAPS
    : S Q L UL_ A F T E R UL_ M T S UL_ G A P S
    ;

SQL_BEFORE_GTIDS
    : S Q L UL_ B E F O R E UL_ G T I D S
    ;

SQL_BIG_RESULT
    : S Q L UL_ B I G UL_ R E S U L T
    ;

SQL_BUFFER_RESULT
    : S Q L UL_ B U F F E R UL_ R E S U L T
    ;

SQL_CALC_FOUND_ROWS
    : S Q L UL_ C A L C UL_ F O U N D UL_ R O W S
    ;

SQL_NO_CACHE
    : S Q L UL_ N O UL_ C A C H E
    ;

SQL_SMALL_RESULT
    : S Q L UL_  S M A L L UL_ R E S U L T
    ;

SQL_THREAD
    : S Q L UL_ T H R E A D
    ;

SQL_TSI_DAY
    : S Q L UL_ T S I UL_ D A Y -> type(DAY)
    ;

SQL_TSI_HOUR
    : S Q L UL_ T S I UL_ H O U R -> type(HOUR)
    ;

SQL_TSI_MINUTE
    : S Q L UL_ T S I UL_ M I N U T E -> type(MINUTE)
    ;

SQL_TSI_MONTH
    : S Q L UL_ T S I UL_ M O N T H -> type(MONTH)
    ;

SQL_TSI_QUARTER
    : S Q L UL_ T S I UL_ Q U A R T E R -> type(QUARTER)
    ;

SQL_TSI_SECOND
    : S Q L UL_ T S I UL_ S E C O N D -> type(SECOND)
    ;

SQL_TSI_WEEK
    : S Q L UL_ T S I UL_ W E E K -> type(WEEK)
    ;

SQL_TSI_YEAR
    : S Q L UL_ T S I UL_ Y E A R -> type(YEAR)
    ;

SRID
    : S R I D
    ;

SSL
    : S S L
    ;

STACKED
    : S T A C K E D
    ;

START
    : S T A R T
    ;

STARTING
    : S T A R T I N G
    ;

STARTS
    : S T A R T S
    ;

STATS_AUTO_RECALC
    : S T A T S UL_ A U T O UL_ R E C A L C
    ;

STATS_PERSISTENT
    : S T A T S UL_ P E R S I S T E N T
    ;

STATS_SAMPLE_PAGES
    : S T A T S UL_ S A M P L E UL_ P A G E S
    ;

STATUS
    : S T A T U S
    ;

STOP
    : S T O P
    ;

STORAGE
    : S T O R A G E
    ;

STORED
    : S T O R E D
    ;

STRAIGHT_JOIN
    : S T R A I G H T UL_ J O I N
    ;

STREAM
    : S T R E A M
    ;

STRING
    : S T R I N G
    ;

// DORIS ADDED BEGIN
STRRIGHT
    : S T R R I G H T
    ;
// DORIS ADDED END

SUBCLASS_ORIGIN
    : S U B C L A S S UL_ O R I G I N
    ;

SUBJECT
    : S U B J E C T
    ;

SUBPARTITION
    : S U B P A R T I T I O N
    ;

SUBPARTITIONS
    : S U B P A R T I T I O N S
    ;

SUPER
    : S U P E R
    ;

SUSPEND
    : S U S P E N D
    ;

SWAPS
    : S W A P S
    ;

SWITCHES
    : S W I T C H E S
    ;

SYSTEM
    : S Y S T E M
    ;

SOURCE_BIND
    : S O U R C E UL_ B I N D
    ;

SOURCE_HOST
    : S O U R C E UL_ H O S T
    ;

SOURCE_USER
    : S O U R C E UL_ U S E R
    ;

SOURCE_PASSWORD
    : S O U R C E UL_ P A S S W O R D
    ;

SOURCE_PORT
    : S O U R C E UL_ P O R T
    ;

SOURCE_LOG_FILE
    : S O U R C E UL_ L O G UL_ F I L E
    ;

SOURCE_LOG_POS
    : S O U R C E UL_ L O G UL_ P O S
    ;

SOURCE_AUTO_POSITION
    : S O U R C E UL_ A U T O UL_ P O S I T I O N
    ;

SOURCE_HEARTBEAT_PERIOD
    : S O U R C E UL_ H E A R T B E A T UL_ P E R I O D
    ;

SOURCE_CONNECT_RETRY
    : S O U R C E UL_ C O N N E C T UL_ R E T R Y
    ;

SOURCE_RETRY_COUNT
    : S O U R C E UL_ R E T R Y UL_ C O U N T
    ;

SOURCE_CONNECTION_AUTO_FAILOVER
    : S O U R C E UL_ C O N N E C T I O N UL_ A U T O UL_ F A I L O V E R
    ;

SOURCE_DELAY
    : S O U R C E UL_ D E L A Y
    ;

SOURCE_COMPRESSION_ALGORITHMS
    : S O U R C E UL_ C O M P R E S S I O N UL_ A L G O R I T H M S
    ;

SOURCE_ZSTD_COMPRESSION_LEVEL
    : S O U R C E UL_ Z S T D UL_ C O M P R E S S I O N UL_ L E V E L
    ;

SOURCE_SSL
    : S O U R C E UL_ S S L
    ;

SOURCE_SSL_CA
    : S O U R C E UL_ S S L UL_ C A
    ;

SOURCE_SSL_CAPATH
    : S O U R C E UL_ S S L UL_ C A P A T H
    ;

SOURCE_SSL_CERT
    : S O U R C E UL_ S S L UL_ C E R T
    ;

SOURCE_SSL_CRL
    : S O U R C E UL_ S S L UL_ C R L
    ;

SOURCE_SSL_CRLPATH
    : S O U R C E UL_ S S L UL_ C R L P A T H
    ;

SOURCE_SSL_KEY
    : S O U R C E UL_ S S L UL_ K E Y
    ;

SOURCE_SSL_CIPHER
    : S O U R C E UL_ S S L UL_ C I P H E R
    ;

SOURCE_SSL_VERIFY_SERVER_CERT
    : S O U R C E UL_ S S L UL_ V E R I F Y UL_ S E R V E R UL_ C E R T
    ;

SOURCE_TLS_VERSION
    : S O U R C E UL_ T L S UL_ V E R S I O N
    ;

SOURCE_TLS_CIPHERSUITES
    : S O U R C E UL_ T L S UL_ C I P H E R S U I T E S
    ;

SOURCE_PUBLIC_KEY_PATH
    : S O U R C E UL_ P U B L I C UL_ K E Y UL_ P A T H
    ;

SYMBOL
    : S Y M B O L
    ;

TABLE
    : T A B L E
    ;

TABLES
    : T A B L E S
    ;

TABLESPACE
    : T A B L E S P A C E
    ;

TABLE_CHECKSUM
    : T A B L E UL_ C H E C K S U M
    ;

TABLE_NAME
    : T A B L E UL_ N A M E
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

TEMPTABLE
    : T E M P T A B L E
    ;

TERMINATED
    : T E R M I N A T E D
    ;

TEXT
    : T E X T
    ;

THAN
    : T H A N
    ;

THEN
    : T H E N
    ;

THREAD_PRIORITY
    : T H R E A D UL_ P R I O R I T Y
    ;

TIES
    : T I E S
    ;

TIME
    : T I M E
    ;

TIMESTAMP
    : T I M E S T A M P
    ;

TIMESTAMP_ADD
    : T I M E S T A M P UL_ A D D
    ;

TIMESTAMP_DIFF
    : T I M E S T A M P UL_ D I F F
    ;

TINYBLOB
    : T I N Y B L O B
    ;

TINYINT
    : T I N Y I N T
    ;

TINYTEXT
    : T I N Y T E X T
    ;

TLS
    : T L S
    ;

TO
    : T O
    ;

TRAILING
    : T R A I L I N G
    ;

TRANSACTION
    : T R A N S A C T I O N
    ;

TRIGGER
    : T R I G G E R
    ;

TRIGGERS
    : T R I G G E R S
    ;

TRUE
    : T R U E
    ;

TRUNCATE
    : T R U N C A T E
    ;

TYPE
    : T Y P E
    ;

TYPES
    : T Y P E S
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

UNCOMMITTED
    : U N C O M M I T T E D
    ;

UNDEFINED
    : U N D E F I N E D
    ;

UNDO
    : U N D O
    ;

UNDOFILE
    : U N D O F I L E
    ;

UNDO_BUFFER_SIZE
    : U N D O UL_ B U F F E R UL_ S I Z E
    ;

UNICODE
    : U N I C O D E
    ;

UNINSTALL
    : U N I N S T A L L
    ;

UNION
    : U N I O N
    ;

UNIQUE
    : U N I Q U E
    ;

UNKNOWN
    : U N K N O W N
    ;

UNLOCK
    : U N L O C K
    ;

UNSIGNED
    : U N S I G N E D
    ;

UNSIGNED_INT
    : UNSIGNED ' ' INT
    ;

UNSIGNED_INTEGER
    : UNSIGNED ' ' INTEGER
    ;

UNTIL
    : U N T I L
    ;

UPDATE
    : U P D A T E
    ;

UPGRADE
    : U P G R A D E
    ;

USAGE
    : U S A G E
    ;

USE
    : U S E
    ;

USER
    : U S E R
    ;

USER_RESOURCES
    : U S E R UL_ R E S O U R C E S
    ;

USE_FRM
    : U S E UL_ F R M
    ;

USING
    : U S I N G
    ;

UTC_DATE
    : U T C UL_ D A T E
    ;

UTC_TIME
    : U T C UL_ T I M E
    ;

UTC_TIMESTAMP
    : U T C UL_ T I M E S T A M P
    ;

VALIDATION
    : V A L I D A T I O N
    ;

VALUE
    : V A L U E
    ;

VALUES
    : V A L U E S
    ;

VARBINARY
    : V A R B I N A R Y
    ;

VARCHAR
    : V A R C H A R
    ;

VARCHARACTER
    : V A R C H A R A C T E R
    ;

VARIABLES
    : V A R I A B L E S
    ;

VARYING
    : V A R Y I N G
    ;

VCPU
    : V C P U
    ;

VIEW
    : V I E W
    ;

VIRTUAL
    : V I R T U A L
    ;

VISIBLE
    : V I S I B L E
    ;

WAIT
    : W A I T
    ;

WARNINGS
    : W A R N I N G S
    ;

WEEK
    : W E E K
    ;

WEIGHT_STRING
    : W E I G H T UL_ S T R I N G
    ;

WHEN
    : W H E N
    ;

WHERE
    : W H E R E
    ;

WHILE
    : W H I L E
    ;

WINDOW
    : W I N D O W
    ;

WITH
    : W I T H
    ;

WITHOUT
    : W I T H O U T
    ;

WORK
    : W O R K
    ;

WRAPPER
    : W R A P P E R
    ;

WRITE
    : W R I T E
    ;

X509
    : X '509'
    ;

XA
    : X A
    ;

XID
    : X I D
    ;

XML
    : X M L
    ;

XOR
    : X O R
    ;

YEAR
    : Y E A R
    ;

YEAR_MONTH
    : Y E A R UL_ M O N T H
    ;

ZEROFILL
    : Z E R O F I L L
    ;

JSON_ARRAY
    : J S O N UL_ A R R A Y
    ;

JSON_ARRAY_APPEND
    : J S O N UL_ A R R A Y UL_ A P P E N D
    ;

JSON_ARRAY_INSERT
    : J S O N UL_ A R R A Y UL_ I N S E R T
    ;

JSON_CONTAINS
    : J S O N UL_ C O N T A I N S
    ;

JSON_CONTAINS_PATH
    : J S O N UL_ C O N T A I N S UL_ P A T H
    ;
JSON_DEPTH
    : J S O N UL_ D E P T H
    ;

JSON_EXTRACT
    : J S O N UL_ E X T R A C T
    ;

JSON_INSERT
    : J S O N UL_ I N S E R T
    ;

JSON_KEYS
    : J S O N UL_ K E Y S
    ;

JSON_LENGTH
    : J S O N UL_ L E N G T H
    ;

JSON_MERGE
    : J S O N UL_ M E R G E
    ;

JSON_MERGE_PATCH
    : J S O N UL_ M E R G E UL_ P A T C H
    ;

JSON_MERGE_PRESERVE
    : J S O N UL_ M E R G E UL_ P R E S E R V E
    ;

JSON_OBJECT
    : J S O N UL_ O B J E C T
    ;

JSON_OVERLAPS
    : J S O N UL_ O V E R L A P S
    ;

JSON_PRETTY
    : J S O N UL_ P R E T T Y
    ;

JSON_QUOTE
    : J S O N UL_ Q U O T E
    ;

JSON_REMOVE
    : J S O N UL_ R E M O V E
    ;

JSON_REPLACE
    : J S O N UL_ R E P L A C E
    ;

JSON_SCHEMA_VALID
    : J S O N UL_ S C H E M A UL_ V A L I D
    ;

JSON_SCHEMA_VALIDATION_REPORT
    : J S O N UL_ S C H E M A UL_ V A L I D A T I O N UL_ R E P O R T
    ;

JSON_SEARCH
    : J S O N UL_ S E A R C H
    ;

JSON_SET
    : J S O N UL_ S E T
    ;

JSON_STORAGE_FREE
    : J S O N UL_ S T O R A G E UL_ F R E E
    ;

JSON_STORAGE_SIZE
    : J S O N UL_ S T O R A G E UL_ S I Z E
    ;

JSON_TYPE
    : J S O N UL_ T Y P E
    ;

JSON_UNQUOTE
    : J S O N UL_ U N Q U O T E
    ;

JSON_VALID
    : J S O N UL_ V A L I D
    ;

ZONE
    : Z O N E
    ;

TIMESTAMPDIFF
    : T I M E S T A M P D I F F
    ;

AUTHENTICATION_FIDO
    : A U T H E N T I C A T I O N UL_ F I D O
    ;

FACTOR
    : F A C T O R
    ;
