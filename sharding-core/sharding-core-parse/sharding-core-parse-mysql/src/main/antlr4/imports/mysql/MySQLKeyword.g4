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

OFFLINE
    : O F F L I N E
    ;

ONLINE
    : O N L I N E
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
