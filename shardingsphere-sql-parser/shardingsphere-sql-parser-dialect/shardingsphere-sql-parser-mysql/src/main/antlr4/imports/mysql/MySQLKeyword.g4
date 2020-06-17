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

lexer grammar MySQLKeyword;

import Alphabet;

USE
    : U S E
    ;

DESCRIBE
    : D E S C R I B E
    ;

SHOW
    : S H O W
    ;

DATABASES
    : D A T A B A S E S
    ;

DATABASE
    : D A T A B A S E
    ;

SCHEMAS
    : S C H E M A S
    ;

TABLES
    : T A B L E S
    ;

TABLESPACE
    : T A B L E S P A C E
    ;

COLUMNS
    : C O L U M N S
    ;

FIELDS
    : F I E L D S
    ;

INDEXES
    : I N D E X E S
    ;

STATUS
    : S T A T U S
    ;

REPLACE
    : R E P L A C E
    ;

MODIFY
    : M O D I F Y
    ;

DISTINCTROW
    : D I S T I N C T R O W
    ;

VALUE
    : V A L U E
    ;

DUPLICATE
    : D U P L I C A T E
    ;

FIRST
    : F I R S T
    ;

LAST
    : L A S T
    ;

AFTER
    : A F T E R
    ;

OJ
    : O J
    ;

WINDOW
    : W I N D O W
    ;

MOD
    : M O D
    ;

DIV
    : D I V
    ;

XOR
    : X O R
    ;

REGEXP
    : R E G E X P
    ;

RLIKE
    : R L I K E
    ;

ACCOUNT
    : A C C O U N T
    ;

USER
    : U S E R
    ;

ROLE
    : R O L E
    ;

START
    : S T A R T
    ;

TRANSACTION
    : T R A N S A C T I O N
    ;

ROW
    : R O W
    ;

ROWS
    : R O W S
    ;

WITHOUT
    : W I T H O U T
    ;

BINARY
    : B I N A R Y
    ;

ESCAPE
    : E S C A P E
    ;

GENERATED
    : G E N E R A T E D
    ;

PARTITION
    : P A R T I T I O N
    ;

SUBPARTITION
    : S U B P A R T I T I O N
    ;

STORAGE
    : S T O R A G E
    ;

STORED
    : S T O R E D
    ;

SUPER
    : S U P E R
    ;

SUBSTR
    : S U B S T R
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

THAN
    : T H A N
    ;

TRAILING
    : T R A I L I N G
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

UNLOCK
    : U N L O C K
    ;

UNSIGNED
    : U N S I G N E D
    ;

SIGNED
    : S I G N E D
    ;

UPGRADE
    : U P G R A D E
    ;

USAGE
    : U S A G E
    ;
 
VALIDATION
    : V A L I D A T I O N
    ;

VIRTUAL
    : V I R T U A L
    ;

ROLLUP
    : R O L L U P
    ;

SOUNDS
    : S O U N D S
    ;

UNKNOWN
    : U N K N O W N
    ;

OFF
    : O F F
    ;

ALWAYS
    : A L W A Y S
    ;

CASCADE
    : C A S C A D E
    ;

CHECK
    : C H E C K
    ;

COMMITTED
    : C O M M I T T E D
    ;

LEVEL
    : L E V E L
    ;

NO
    : N O
    ;

OPTION
    : O P T I O N
    ;

PASSWORD
    : P A S S W O R D
    ;

PRIVILEGES
    : P R I V I L E G E S
    ;

READ
    : R E A D
    ;

WRITE
    : W R I T E
    ;

REFERENCES
    : R E F E R E N C E S
    ;

ACTION
    : A C T I O N
    ;

ALGORITHM
    : A L G O R I T H M
    ;

ANALYZE
    : A N A L Y Z E
    ;

AUTOCOMMIT
    : A U T O C O M M I T
    ;

MAXVALUE
    : M A X V A L U E
    ;

BOTH
    : B O T H
    ;

BTREE
    : B T R E E
    ;

CHAIN
    : C H A I N
    ;

CHANGE
    : C H A N G E
    ;

CHARSET
    : C H A R S E T
    ;

CHECKSUM
    : C H E C K S U M
    ;

CIPHER
    : C I P H E R
    ;

CLIENT
    : C L I E N T
    ;

COALESCE
    : C O A L E S C E
    ;

COLLATE
    : C O L L A T E
    ;

COMMENT
    : C O M M E N T
    ;

COMPACT
    : C O M P A C T
    ;

COMPRESSED
    : C O M P R E S S E D
    ;

COMPRESSION
    : C O M P R E S S I O N
    ;

CONNECTION
    : C O N N E C T I O N
    ;

CONSISTENT
    : C O N S I S T E N T
    ;

CONVERT
    : C O N V E R T
    ;

COPY
    : C O P Y
    ;

DATA
    : D A T A
    ;

DELAYED
    : D E L A Y E D
    ;

DIRECTORY
    : D I R E C T O R Y
    ;

DISCARD
    : D I S C A R D
    ;

DISK
    : D I S K
    ;

DYNAMIC
    : D Y N A M I C
    ;

ENCRYPTION
    : E N C R Y P T I O N
    ;

END
    : E N D
    ;

ENGINE
    : E N G I N E
    ;

EVENT
    : E V E N T
    ;

EXCEPT
    : E X C E P T
    ;

EXCHANGE
    : E X C H A N G E
    ;

EXCLUSIVE
    : E X C L U S I V E
    ;

EXECUTE
    : E X E C U T E
    ;

EXTRACT
    : E X T R A C T
    ;

FILE
    : F I L E
    ;

FIXED
    : F I X E D
    ;

FOLLOWING
    : F O L L O W I N G
    ;

FORCE
    : F O R C E
    ;

FULLTEXT
    : F U L L T E X T
    ;

GLOBAL
    : G L O B A L
    ;

HASH
    : H A S H
    ;

IDENTIFIED
    : I D E N T I F I E D
    ;

IGNORE
    : I G N O R E
    ;

IMPORT_
    : I M P O R T UL_
    ;

INPLACE
    : I N P L A C E
    ;

KEYS
    : K E Y S
    ;

LEADING
    : L E A D I N G
    ;

LESS
    : L E S S
    ;

LINEAR
    : L I N E A R
    ;

LOCK
    : L O C K
    ;

MATCH
    : M A T C H
    ;

MEMORY
    : M E M O R Y
    ;

NONE
    : N O N E
    ;

NOW
    : N O W
    ;

OPTIMIZE
    : O P T I M I Z E
    ;

OVER
    : O V E R
    ;

PARSER
    : P A R S E R
    ;

PARTIAL
    : P A R T I A L
    ;

PARTITIONING
    : P A R T I T I O N I N G
    ;

PERSIST
    : P E R S I S T
    ;

PRECEDING
    : P R E C E D I N G
    ;

PROCESS
    : P R O C E S S
    ;

PROXY
    : P R O X Y
    ;

QUICK
    : Q U I C K
    ;

RANGE
    : R A N G E
    ;

REBUILD
    : R E B U I L D
    ;

RECURSIVE
    : R E C U R S I V E
    ;

REDUNDANT
    : R E D U N D A N T
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

REPLICATION
    : R E P L I C A T I O N
    ;

REQUIRE
    : R E Q U I R E
    ;

RESTRICT
    : R E S T R I C T
    ;

REVERSE
    : R E V E R S E
    ;

ROUTINE
    : R O U T I N E
    ;

SEPARATOR
    : S E P A R A T O R
    ;

SESSION
    : S E S S I O N
    ;

SHARED
    : S H A R E D
    ;

SHUTDOWN
    : S H U T D O W N
    ;

SIMPLE
    : S I M P L E
    ;

SLAVE
    : S L A V E
    ;

SPATIAL
    : S P A T I A L
    ;


ZEROFILL
    : Z E R O F I L L
    ;

VISIBLE
    : V I S I B L E
    ;

INVISIBLE
    : I N V I S I B L E
    ;

INSTANT
    : I N S T A N T
    ;

ENFORCED
    : E N F O R C E D
    ;

AGAINST
    : A G A I N S T
    ;

LANGUAGE
    : L A N G U A G E
    ;

MODE
    : M O D E
    ;

QUERY
    : Q U E R Y
    ;

EXTENDED
    : E X T E N D E D
    ;

EXPANSION
    : E X P A N S I O N
    ;

VARIANCE
    : V A R I A N C E
    ;

MAX_ROWS
    : M A X UL_ R O W S
    ;

MIN_ROWS
    : M I N UL_ R O W S
    ;

HIGH_PRIORITY
    : H I G H UL_ P R I O R I T Y
    ;

LOW_PRIORITY
    : L O W UL_ P R I O R I T Y
    ;

SQL_BIG_RESULT
    : S Q L UL_ B I G UL_ R E S U L T
    ;

SQL_BUFFER_RESULT
    : S Q L UL_ B U F F E R UL_ R E S U L T
    ;

SQL_CACHE
    : S Q L UL_ C A C H E
    ;

SQL_CALC_FOUND_ROWS
    : S Q L UL_ C A L C UL_ F O U N D UL_ R O W S
    ;

SQL_NO_CACHE
    : S Q L UL_ N O UL_ C A C H E
    ;

SQL_SMALL_RESULT
    : S Q L UL_ S M A L L UL_ R E S U L T
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

ROLE_ADMIN
    : R O L E UL_ A D M I N
    ;

ROW_FORMAT
    : R O W UL_ F O R M A T
    ;

SET_USER_ID
    : S E T UL_ U S E R UL_ I D
    ;

REPLICATION_SLAVE_ADMIN
    : R E P L I C A T I O N UL_ S L A V E UL_ A D M I N
    ;

GROUP_REPLICATION_ADMIN
    : G R O U P UL_ R E P L I C A T I O N UL_ A D M I N
    ;

STRAIGHT_JOIN
    : S T R A I G H T UL_ J O I N
    ;

WEIGHT_STRING
    : W E I G H T UL_ S T R I N G
    ;

COLUMN_FORMAT
    : C O L U M N UL_ F O R M A T
    ;

CONNECTION_ADMIN
    : C O N N E C T I O N UL_ A D M I N
    ;

FIREWALL_ADMIN
    : F I R E W A L L UL_ A D M I N
    ;

FIREWALL_USER
    : F I R E W A L L UL_ U S E R
    ;

INSERT_METHOD
    : I N S E R T UL_ M E T H O D
    ;

KEY_BLOCK_SIZE
    : K E Y UL_ B L O C K UL_ S I Z E
    ;

PACK_KEYS
    : P A C K UL_ K E Y S
    ;

PERSIST_ONLY
    : P E R S I S T UL_ O N L Y
    ;

BIT_AND
    : B I T UL_ A N D
    ;

BIT_OR
    : B I T UL_ O R
    ;

BIT_XOR
    : B I T UL_ X O R
    ;

GROUP_CONCAT
    : G R O U P UL_ C O N C A T
    ;

JSON_ARRAYAGG
    : J S O N UL_ A R R A Y A G G
    ;

JSON_OBJECTAGG
    : J S O N UL_ O B J E C T A G G
    ;

STD
    : S T D
    ;

STDDEV
    : S T D D E V
    ;

STDDEV_POP
    : S T D D E V UL_ P O P
    ;

STDDEV_SAMP
    : S T D D E V UL_ S A M P
    ;

VAR_POP
    : V A R UL_ P O P
    ;

VAR_SAMP
    : V A R UL_ S A M P
    ;

AUDIT_ADMIN
    : A U D I T UL_ A D M I N
    ;

AUTO_INCREMENT
    : A U T O UL_ I N C R E M E N T
    ;

AVG_ROW_LENGTH
    : A V G UL_ R O W UL_ L E N G T H
    ;

BINLOG_ADMIN
    : B I N L O G UL_ A D M I N
    ;
    
DELAY_KEY_WRITE
    : D E L A Y UL_ K E Y UL_ W R I T E
    ;

ENCRYPTION_KEY_ADMIN
    : E N C R Y P T I O N UL_ K E Y UL_ A D M I N
    ;
    
SYSTEM_VARIABLES_ADMIN
    : S Y S T E M UL_ V A R I A B L E S UL_ A D M I N
    ;

VERSION_TOKEN_ADMIN
    : V E R S I O N UL_ T O K E N UL_ A D M I N
    ;

CURRENT_TIMESTAMP
    : C U R R E N T UL_ T I M E S T A M P
    ;
    
CURRENT_DATE
    : C U R R E N T UL_ D A T E
    ;
    
CURRENT_TIME
    : C U R R E N T UL_ T I M E
    ;
    
LAST_DAY
    : L A S T UL_ D A Y
    ;    
      
YEAR_MONTH
    : D A Y UL_ M O N T H
    ;

DAY_HOUR
    : D A Y UL_ H O U R
    ;

DAY_MINUTE
    : D A Y UL_ M I N U T E
    ;

DAY_SECOND
    : D A Y UL_ S E C O N D
    ;

DAY_MICROSECOND
    : D A Y UL_ M I C R O S E C O N D
    ;

HOUR_MINUTE
    : H O U R UL_ M I N U T E
    ;

HOUR_SECOND
    : H O U R UL_ S E C O N D
    ;

HOUR_MICROSECOND
    : H O U R UL_ M I C R O S E C O N D
    ;

MINUTE_SECOND
    : M I N U T E UL_ S E C O N D
    ;

MINUTE_MICROSECOND
    : M I N U T E UL_ M I C R O S E C O N D
    ;

SECOND_MICROSECOND
    : S E C O N D UL_ M I C R O S E C O N D
    ;

UL_BINARY
    : UL_ B I N A R Y
    ;

ROTATE
    : R O T A T E
    ;

MASTER
    : M A S T E R 
    ;

BINLOG
    : B I N L O G
    ;

ERROR
    : E R R O R
    ;

SCHEDULE
    : S C H E D U L E
    ;

COMPLETION
    : C O M P L E T I O N
    ;

EVERY
    : E V E R Y
    ;

STARTS
    : S T A R T S
    ;

ENDS
    : E N D S
    ;

HOST
    : H O S T
    ;

SOCKET
    : S O C K E T
    ;

PORT
    : P O R T
    ;

SERVER
    : S E R V E R
    ;

WRAPPER
    : W R A P P E R
    ;

OPTIONS
    : O P T I O N S
    ;

OWNER
    : O W N E R
    ;

DETERMINISTIC
    : D E T E R M I N I S T I C
    ;

RETURNS
    : R E T U R N S
    ;

CONTAINS
    : C O N T A I N S
    ;

READS
    : R E A D S
    ;

MODIFIES
    : M O D I F I E S
    ;

SECURITY
    : S E C U R I T Y
    ;

INVOKER
    : I N V O K E R
    ;

OUT
    : O U T
    ;

INOUT
    : I N O U T
    ;

TEMPTABLE
    : T E M P T A B L E
    ;

MERGE
    : M E R G E
    ;

UNDEFINED
    : U N D E F I N E D
    ;

DATAFILE
    : D A T A F I L E
    ;

FILE_BLOCK_SIZE
    : F I L E UL_ B L O C K UL_ S I Z E
    ; 

EXTENT_SIZE
    : E X T E N T UL_ S I Z E
    ;

INITIAL_SIZE
    : I N I T I A L UL_ S I Z E
    ;

AUTOEXTEND_SIZE
    : A U T O E X T E N D UL_ S I Z E
    ;

MAX_SIZE
    : M A X UL_ S I Z E
    ;

NODEGROUP
    : N O D E G R O U P
    ;

WAIT
    : W A I T
    ;

LOGFILE
    : L O G F I L E
    ;

UNDOFILE
    : U N D O F I L E
    ;

UNDO_BUFFER_SIZE
    : U N D O UL_ B U F F E R UL_ S I Z E
    ;

REDO_BUFFER_SIZE
    : R E D O UL_ B U F F E R UL_ S I Z E
    ;

HANDLER
    : H A N D L E R
    ;

PREV
    : P R E V
    ;

ORGANIZATION
    : O R G A N I Z A T I O N
    ;

DEFINITION
    : D E F I N I T I O N
    ;

DESCRIPTION
    : D E S C R I P T I O N
    ;

REFERENCE
    : R E F E R E N C E
    ;

FOLLOWS
    : F O L L O W S
    ;

PRECEDES
    : P R E C E D E S
    ;

IMPORT
    : I M P O R T
    ;

LOAD
    : L O A D
    ;

CONCURRENT
    : C O N C U R R E N T
    ;

INFILE
    : I N F I L E
    ;

LINES
    : L I N E S
    ;

STARTING
    : S T A R T I N G
    ;

TERMINATED
    : T E R M I N A T E D
    ;    

OPTIONALLY
    : O P T I O N A L L Y
    ;

ENCLOSED
    : E N C L O S E D
    ;

ESCAPED
    : E S C A P E D
    ;

XML
    : X M L
    ;

UNDO
    : U N D O
    ;

DUMPFILE
    : D U M P F I L E
    ;

OUTFILE
    : O U T F I L E
    ;

SHARE
    : S H A R E
    ;

LOGS
    : L O G S
    ;

EVENTS
    : E V E N T S
    ;

BEFORE
    : B E F O R E
    ;

EACH
    : E A C H
    ;

MUTEX
    : M U T E X
    ;

ENGINES
    : E N G I N E S
    ;

ERRORS
    : E R R O R S
    ;

CODE
    : C O D E
    ;

GRANTS
    : G R A N T S
    ;

PLUGINS
    : P L U G I N S
    ;

PROCESSLIST
    : P R O C E S S L I S T
    ;

BLOCK
    : B L O C K
    ;

IO
    : I O
    ;

CONTEXT
    : C O N T E X T
    ;

SWITCHES
    : S W I T C H E S
    ; 

CPU
    : C P U
    ;

IPC
    : I P C
    ;

PAGE
    : P A G E
    ;

FAULTS
    : F A U L T S
    ;

SOURCE
    : S O U R C E
    ;

SWAPS
    : S W A P S
    ;

PROFILE
    : P R O F I L E
    ;

PROFILES
    : P R O F I L E S
    ;

RELAYLOG
    : R E L A Y L O G
    ;

CHANNEL
    : C H A N N E L
    ;

VARIABLES
    : V A R I A B L E S
    ;

WARNINGS
    : W A R N I N G S
    ;

SSL
    : S S L
    ;

CLONE
    : C L O N E
    ;

AGGREGATE
    : A G G R E G A T E
    ;

STRING
    : S T R I N G
    ;

SONAME
    : S O N A M E
    ;

INSTALL
    : I N S T A L L
    ;

COMPONENT
    : C O M P O N E N T
    ;

PLUGIN
    : P L U G I N
    ;

UNINSTALL
    : U N I N S T A L L
    ;

NO_WRITE_TO_BINLOG
    : N O UL_ W R I T E UL_ T O UL_ B I N L O G
    ;

HISTOGRAM
    : H I S T O G R A M
    ;

BUCKETS
    : B U C K E T S
    ;

FAST 
    : F A S T
    ;

MEDIUM
    : M E D I U M
    ;

USE_FRM
    : U S E UL_ F R M
    ;

RESOURCE
    : R E S O U R C E
    ;

VCPU
    : V C P U
    ;

THREAD_PRIORITY
    : T H R E A D UL_ P R I O R I T Y
    ;

SYSTEM
    : S Y S T E M
    ;

EXPIRE
    : E X P I R E
    ;

NEVER
    : N E V E R
    ;

HISTORY
    : H I S T O R Y
    ;

OPTIONAL
    : O P T I O N A L
    ;

REUSE
    : R E U S E
    ;

MAX_QUERIES_PER_HOUR
    : M A X UL_ Q U E R I E S UL_ P E R UL_ H O U R
    ;

MAX_UPDATES_PER_HOUR
    : M A X UL_ U P D A T E S UL_ P E R UL_ H O U R
    ;

MAX_CONNECTIONS_PER_HOUR
    : M A X UL_ C O N N E C T I O N S UL_ P E R UL_ H O U R
    ;

MAX_USER_CONNECTIONS
    : M A X UL_ U S E R UL_ C O N N E C T I O N S
    ; 

RETAIN
    : R E T A I N
    ;

RANDOM
    : R A N D O M
    ;

OLD
    : O L D
    ;

X509
    : X '509'
    ;

ISSUER
    : I S S U E R
    ;

SUBJECT
    : S U B J E C T
    ;

CACHE
    : C A C H E
    ;

GENERAL
    : G E N E R A L
    ;

OPTIMIZER_COSTS
    : O P T I M I Z E R UL_ C O S T S
    ;

SLOW
    : S L O W
    ;

USER_RESOURCES
    : U S E R UL_ R E S O U R C E S
    ;

EXPORT
    : E X P O R T
    ;

RELAY
    : R E L A Y
    ;

HOSTS
    : H O S T S
    ;

KILL
    : K I L L
    ;

FLUSH
    : F L U S H
    ;

RESET
    : R E S E T
    ;

RESTART
    : R E S T A R T
    ;

UNIX_TIMESTAMP
    : U N I X UL_ T I M E S T A M P
    ;

LOWER
    : L O W E R
    ;

UPPER
    : U P P E R
    ;

ADDDATE
    : A D D D A T E
    ;

ADDTIME
    : A D D T I M E
    ;

DATE_ADD
    : D A T E UL_ A D D
    ;

DATE_SUB
    : D A T E UL_ S U B
    ;

DATEDIFF
    : D A T E D I F F
    ;

DATE_FORMAT
    : D A T E UL_ F O R M A T
    ;

DAYNAME
    : D A Y N A M E
    ;

DAYOFMONTH
    : D A Y O F M O N T H
    ;

DAYOFWEEK
    : D A Y O F W E E K
    ;

DAYOFYEAR
    : D A Y O F Y E A R
    ;

STR_TO_DATE
    : S T R UL_ T O UL_ D A T E
    ;

TIMEDIFF
    : T I M E D I F F
    ;

TIMESTAMPADD
    : T I M E S T A M P A D D
    ;

TIMESTAMPDIFF
    : T I M E S T A M P D I F F
    ;

TIME_FORMAT
     : T I M E UL_ F O R M A T
     ;

TIME_TO_SEC
    : T I M E UL_ T O UL_ S E C
    ;

AES_DECRYPT
    : A E S UL_ D E C R Y P T
    ;

AES_ENCRYPT
    : A E S UL_ E N C R Y P T
    ;

FROM_BASE64
    : F R O M UL_ B A S E
    ;

TO_BASE64
    : T O UL_ B A S E
    ;

GEOMCOLLECTION
    : G E O M C O L L E C T I O N
    ;

GEOMETRYCOLLECTION
    : G E O M E T R Y C O L L E C T I O N
    ;

LINESTRING
    : L I N E S T R I N G
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

POINT
    : P O I N T
    ;

POLYGON
    : P O L Y G O N
    ;

ST_AREA
    : S T UL_ A R E A
    ;

ST_ASBINARY
    : S T UL_ A S B I N A R Y
    ;

ST_ASGEOJSON
    : S T UL_ A S G E O J S O N
    ;

ST_ASTEXT
    : S T UL_ A S T E X T
    ;

ST_ASWKB
    : S T UL_ A S W K B
    ;

ST_ASWKT
    : S T UL_ A S W K T
    ;

ST_BUFFER
    : S T UL_ B U F F E R
    ;

ST_BUFFER_STRATEGY
    : S T UL_ B U F F E R UL_ S T R A T E G Y
    ;

ST_CENTROID
    : S T UL_ C E N T R O I D
    ;

ST_CONTAINS
    : S T UL_ C O N T A I N S
    ;

ST_CONVEXHULL
    : S T UL_ C O N V E X H U L L
    ;

ST_CROSSES
    : S T UL_ C R O S S E S
    ;

ST_DIFFERENCE
    : S T UL_ D I F F E R E N C E
    ;

ST_DIMENSION
    : S T UL_ D I M E N S I O N
    ;

ST_DISJOINT
    : S T UL_ D I S J O I N T
    ;

ST_DISTANCE
    : S T UL_ D I S T A N C E
    ;

ST_DISTANCE_SPHERE
    : S T UL_ D I S T A N C E UL_ S P H E R E
    ;

ST_ENDPOINT
    : S T UL_ E N D P O I N T
    ;

ST_ENVELOPE
    : S T UL_ E N V E L O P E
    ;

ST_EQUALS
    : S T UL_ E Q U A L S
    ;

ST_EXTERIORRING
    : S T UL_ E X T E R I O R R I N G
    ;

ST_GEOHASH
    : S T UL_ G E O H A S H
    ;

ST_GEOMCOLLFROMTEXT
    : S T UL_ G E O M C O L L F R O M T E X T
    ;

ST_GEOMCOLLFROMTXT
    : S T UL_ G E O M C O L L F R O M T X T
    ;

ST_GEOMCOLLFROMWKB
    : S T UL_ G E O M C O L L F R O M W K B
    ;

ST_GEOMETRYCOLLECTIONFROMTEXT
    : S T UL_ G E O M E T R Y C O L L E C T I O N F R O M T E X T
    ;

ST_GEOMETRYCOLLECTIONFROMWKB
    : S T UL_ G E O M E T R Y C O L L E C T I O N F R O M W K B
    ;

ST_GEOMETRYFROMTEXT
    : S T UL_ G E O M E T R Y F R O M T E X T
    ;

ST_GEOMETRYFROMWKB
    : S T UL_ G E O M E T R Y F R O M W K B
    ;

ST_GEOMETRYN
    : S T UL_ G E O M E T R Y N
    ;

ST_GEOMETRYTYPE
    : S T UL_ G E O M E T R Y T Y P E
    ;

ST_GEOMFROMGEOJSON
    : S T UL_ G E O F R O M G E O J S O N
    ;

ST_GEOMFROMTEXT
    : S T UL_ G E O F R O M T E X T
    ;

ST_GEOMFROMWKB
    : S T UL_ G E O F R O M W K B
    ;

ST_INTERIORRINGN
    : S T UL_ I N T E R I O R R I N G N
    ;

ST_INTERSECTION
    : S T UL_ I N T E R S E C T I O N
    ;

ST_INTERSECTS
    : S T UL_ I N T E R S E C T S
    ;

ST_ISCLOSED
    : S T UL_ I S C L O S E D
    ;

ST_ISEMPTY
    : S T UL_ I S E M P T Y
    ;

ST_ISSIMPLE
    : S T UL_ I S S I M P L E
    ;

ST_ISVALID
    : S T UL_ I S V A L I D
    ;

ST_LATFROMGEOHASH
    : S T UL_ L A T F R O M G E O H A S H
    ;

ST_LATITUDE
    : S T UL_ L A T I T U D E
    ;

ST_LENGTH
    : S T UL_ L E N G T H
    ;

ST_LINEFROMTEXT
    : S T UL_ L I N E F R O M T E X T
    ;

ST_LINEFROMWKB
    : S T UL_ L I N E F R O M W K B
    ;

ST_LINESTRINGFROMTEXT
    : S T UL_ L I N E S T R I N G F R O M T E X T
    ;

ST_LINESTRINGFROMWKB
    : S T UL_ L I N E S T R I N G F R O M W K B
    ;

ST_LONGFROMGEOHASH
    : S T UL_ L O N G F R O M G E O H A S H
    ;

ST_LONGITUDE
    : S T UL_ L O N G I T U D E
    ;

ST_MAKEENVELOPE
    : S T UL_ M A K E E N V E L O P E
    ;

ST_MLINEFROMTEXT
    : S T UL_ M L I N E F R O M T E X T
    ;

ST_MLINEFROMWKB
    : S T UL_ M L I N E F R O M W K B
    ;

ST_MULTILINESTRINGFROMTEXT
    : S T UL_ M U L T I L I N E S T R I N G F R O M T E X T
    ;

ST_MULTILINESTRINGFROMWKB
    : S T UL_ M U L T I L I N E S T R I N G F R O M W K B
    ;

ST_MPOINTFROMTEXT
    : S T UL_ M P O I N T F R O M T E X T
    ;

ST_MPOINTFROMWKB
    : S T UL_ M P O I N T F R O M W K B
    ;

ST_MULTIPOINTFROMTEXT
    : S T UL_ M U L T I P O I N T F R O M T E X T
    ;

ST_MULTIPOINTFROMWKB
    : S T UL_ M U L T I P O I N T F R O M W K B
    ;

ST_MPOLYFROMTEXT
    : S T UL_ M P O L Y F R O M T E X T
    ;

ST_MPOLYFROMWKB
    : S T UL_ M P O L Y F R O M W K B
    ;

ST_MULTIPOLYGONFROMTEXT
    : S T UL_ M U L T I P O L Y G O N F R O M T E X T
    ;

ST_MULTIPOLYGONFROMWKB
    : S T UL_ M U L T I P O L Y G O N F R O M W K B
    ;

ST_NUMGEOMETRIES
    : S T UL_ N U M G E O M E T R I E S
    ;

ST_NUMINTERIORRING
    : S T UL_ N U M I N T E R I O R R I N G
    ;

ST_NUMINTERIORRINGS
    : S T UL_ N U M I N T E R I O R R I N G S
    ;

ST_NUMPOINTS
    : S T UL_ N U M P O I N T S
    ;

ST_OVERLAPS
    : S T UL_ O V E R L A P S
    ;

ST_POINTFROMGEOHASH
    : S T UL_ P O I N T F R O M G E O H A S H
    ;

ST_POINTFROMTEXT
    : S T UL_ P O I N T F R O M T E X T
    ;

ST_POINTFROMWKB
    : S T UL_ P O I N T F R O M W K B
    ;

ST_POINTN
    : S T UL_ P O I N T N
    ;

ST_POLYFROMTEXT
    : S T UL_ P O L Y F R O M T E X T
    ;

ST_POLYFROMWKB
    : S T UL_ P O L Y F R O M W K B
    ;

ST_POLYGONFROMTEXT
    : S T UL_ P O L Y G O N F R O M T E X T
    ;

ST_POLYGONFROMWKB
    : S T UL_ P O L Y G O N F R O M W K B
    ;

ST_SIMPLIFY
    : S T UL_ S I M P L I F Y
    ;

ST_SRID
    : S T UL_ S R I D
    ;

ST_STARTPOINT
    : S T UL_ S T A R T P O I N T
    ;

ST_SWAPXY
    : S T UL_ S W A P X Y
    ;

ST_SYMDIFFERENCE
    : S T UL_ S Y M D I F F E R E N C E
    ;

ST_TOUCHES
    : S T UL_ T O U C H E S
    ;

ST_TRANSFORM
    : S T UL_ T R A N S F O R M
    ;

ST_UNION
    : S T UL_ U N I O N
    ;

ST_VALIDATE
    : S T UL_ V A L I D A T E
    ;

ST_WITHIN
    : S T UL_ W I T H I N
    ;

ST_X
    : S T UL_ X
    ;

ST_Y
    : S T UL_ Y
    ;

BIT
    : B I T
    ;

BOOL
    : B O O L
    ;

DEC
    : D E C
    ;

VARCHAR
    : V A R C H A R
    ;

VARBINARY
    : V A R B I N A R Y
    ;

TINYBLOB
    : T I N Y B L O B
    ;

TINYTEXT
    : T I N Y T E X T
    ;

BLOB
    : B L O B
    ;

TEXT
    : T E X T
    ;

MEDIUMBLOB
    : M E D I U M B L O B
    ;

MEDIUMTEXT
    : M E D I U M T E X T
    ;

LONGBLOB
    : L O N G B L O B
    ;

LONGTEXT
    : L O N G T E X T
    ;

ENUM
    : E N U M
    ;

GEOMETRY
    : G E O M E T R Y
    ;

JSON
    : J S O N
    ;

IO_THREAD
    : I O UL_ T H R E A D
    ;

SQL_THREAD
    : S Q L UL_ T H R E A D
    ;

SQL_BEFORE_GTIDS
    : S Q L UL_ B E F O R E UL_ G T I D S
    ;

SQL_AFTER_GTIDS
    : S Q L UL_ A F T E R UL_ G T I D S
    ;

MASTER_LOG_FILE
    : M A S T E R UL_ L O G UL_ F I L E
    ;

MASTER_LOG_POS
    : M A S T E R UL_ L O G UL_ P O S
    ;

RELAY_LOG_FILE
    : R E L A Y UL_ L O G UL_ F I L E
    ;

RELAY_LOG_POS
    : R E L A Y UL_ L O G UL_ P O S
    ;

SQL_AFTER_MTS_GAPS
    : S Q L UL_ A F T E R UL_ M T S UL_ G A P S
    ;

UNTIL
    : U N T I L
    ;

DEFAULT_AUTH
    : D E F A U L T UL_ A U T H
    ;

PLUGIN_DIR
    : P L U G I N UL_ D I R
    ;

STOP
    : S T O P
    ;

FAILED_LOGIN_ATTEMPTS
    : F A I L E D UL_ L O G I N UL_ A T T E M P T S
    ;

PASSWORD_LOCK_TIME
    : P A S S W O R D UL_ L O C K UL_ T I M E
    ;

ATTRIBUTE
    : A T T R I B U T E
    ;

APPLICATION_PASSWORD_ADMIN
    : A P P L I C A T I O N UL_ P A S S W O R D UL_ A D M I N
    ;

BACKUP_ADMIN
    : B A C K U P UL_ A D M I N
    ;

BINLOG_ENCRYPTION_ADMIN
    : B I N L O G UL_ E N C R Y P T I O N UL_ A D M I N
    ;

CLONE_ADMIN
    : C L O N E UL_ A D M I N
    ;

INNODB_REDO_LOG_ARCHIVE
    : I N N O D B UL_ R E D O UL_ L O G UL_ A R C H I V E
    ;

NDB_STOPED_USER
    : N D B UL_ S T O P E D UL_ U S E R
    ;

PERSIST_RO_VARIABLES_ADMIN
    : P E R S I S T UL_ R O UL_ V A R I A B L E S UL_ A D M I N
    ;

REPLICATION_APPLIER
    : R E P L I C A T I O N UL_ A P P L I E R
    ;

RESOURCE_GROUP_ADMIN
    : R E S O U R C E UL_ G R O U P UL_ A D M I N
    ;

RESOURCE_GROUP_USER
    : R E S O U R C E UL_ G R O U P UL_ U S E R
    ;

SHOW_ROUTINE
    : S H O W UL_ R O U T I N E
    ;

SYSTEM_USER
    : S Y S T E M UL_ U S E R
    ;

TABLE_ENCRYPTION_ADMIN
    : T A B L E UL_ E N C R Y P T I O N UL_ A D M I N
    ;

XA_RECOVER_ADMIN
    : X A UL_ R E C O V E R UL_ A D M I N
    ;

WORK
    : W O R K
    ;

BACKUP
    : B A C K U P
    ;

RESUME
    : R E S U M E
    ;

SUSPEND
    : S U S P E N D
    ;

MIGRATE
    : M I G R A T E
    ;

PREPARE
    : P R E P A R E
    ;

ONE
    : O N E
    ;

PHASE
    : P H A S E
    ;

RECOVER
    : R E C O V E R
    ;

SNAPSHOT
    : S N A P S H O T
    ;

PURGE
    : P U R G E
    ;

MASTER_COMPRESSION_ALGORITHMS
    : M A S T E R UL_ C O M P R E S S I O N UL_ A L G O R I T H M S
    ;

MASTER_ZSTD_COMPRESSION_LEVEL
    : M A S T E R UL_ Z S T D UL_ C O M P R E S S I O N UL_ L E V E L
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

MASTER_SSL_CRL
    : M A S T E R UL_ S S L UL_ C R L
    ;

MASTER_SSL_CRLPATH
    : M A S T E R UL_ S S L UL_ C R L P A T H
    ;

MASTER_SSL_KEY
    : M A S T E R UL_ S S L UL_ K E Y
    ;

MASTER_SSL_CIPHER
    : M A S T E R UL_ S S L UL_ C I P H E R
    ;

MASTER_SSL_VERIFY_SERVER_CERT
    : M A S T E R UL_ S S L UL_ V E R I F Y UL_ S E R V E R UL_ C E R T
    ;

MASTER_TLS_VERSION
    : M A S T E R UL_ T L S UL_ V E R S I O N
    ;

MASTER_TLS_CIPHERSUITES
    : M A S T E R UL_ T L S UL_ C I P H E R S U I T E S
    ;

MASTER_PUBLIC_KEY_PATH
    : M A S T E R UL_ P U B L I C UL_ K E Y UL_ P A T H
    ;

GET_MASTER_PUBLIC_KEY
    : G E T UL_ M A S T E R UL_ P U B L I C UL_ K E Y
    ;

IGNORE_SERVER_IDS
    : I G N O R E UL_ S E R V E R UL_ I D S
    ;

MASTER_BIND
    : M A S T E R UL_ B I N D
    ;

MASTER_HOST
    : M A S T E R UL_ H O S T
    ;

MASTER_USER
    : M A S T E R UL_ U S E R
    ;

MASTER_PASSWORD
    : M A S T E R UL_ P A S S W O R D
    ;

MASTER_PORT
    : M A S T E R UL_ P O R T
    ;

PRIVILEGE_CHECKS_USER
    : P R I V I L E G E UL_ C H E C K S UL_ U S E R
    ;

REQUIRE_ROW_FORMAT
    : R E Q U I R E UL_ R O W UL_ F O R M A T
    ;

MASTER_CONNECT_RETRY
    : M A S T E R UL_ C O N N E C T UL_ R E T R Y
    ;

MASTER_RETRY_COUNT
    : M A S T E R UL_ R E T R Y UL_ C O U N T
    ;

MASTER_DELAY
    : M A S T E R UL_ D E L A Y
    ;

MASTER_HEARTBEAT_PERIOD
    : M A S T E R UL_ H E A R T B E A T UL_ P E R I O D
    ;

MASTER_AUTO_POSITION
    : M A S T E R UL_ A U T O UL_ P O S I T I O N
    ;

REPLICATE_DO_DB
    : R E P L I C A T E UL_ D O UL_ D B
    ;

REPLICATE_IGNORE_DB
    : R E P L I C A T E UL_ I G N O R E UL_ D B
    ;

REPLICATE_DO_TABLE
    : R E P L I C A T E UL_ D O UL_ T A B L E
    ;

REPLICATE_IGNORE_TABLE
    : R E P L I C A T E UL_ I G N O R E UL_ T A B L E
    ;

REPLICATE_WILD_DO_TABLE
    : R E P L I C A T E UL_ W I L D UL_ D O UL_ T A B L E
    ;

REPLICATE_WILD_IGNORE_TABLE
    : R E P L I C A T E UL_ W I L D UL_ I G N O R E UL_ T A B L E
    ;

REPLICATE_REWRITE_DB
    : R E P L I C A T E UL_ R E W R I T E UL_ D B
    ;

GROUP_REPLICATION
    : G R O U P UL_ R E P L I C A T I O N
    ;
