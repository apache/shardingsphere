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

lexer grammar OracleKeyword;

import Alphabet;

ABSENT
    : A B S E N T
    ;

BEQUEATH
    : B E Q U E A T H
    ;

BINARY
    : B I N A R Y
    ;

ESCAPE
    : E S C A P E
    ;

MOD
    : M O D
    ;

XOR
    : X O R
    ;

ROW
    : R O W
    ;

ROWS
    : R O W S
    ;

UNKNOWN
    : U N K N O W N
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

CONVERSION
    : C O N V E R S I O N
    ;

GENERATED
    : G E N E R A T E D
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

START
    : S T A R T
    ;

TRANSACTION
    : T R A N S A C T I O N
    ;

USER
    : U S E R
    ;

ROLE
    : R O L E
    ;

VISIBLE
    : V I S I B L E
    ;

INVISIBLE
    : I N V I S I B L E
    ;

EXECUTE
    : E X E C U T E
    ;

USE
    : U S E
    ;

DEBUG
    : D E B U G
    ;

UNDER
    : U N D E R
    ;

FLASHBACK
    : F L A S H B A C K
    ;

ARCHIVE
    : A R C H I V E
    ;

REFRESH
    : R E F R E S H
    ;

QUERY
    : Q U E R Y
    ;

REWRITE
    : R E W R I T E
    ;

KEEP
    : K E E P
    ;

SEQUENCE
    : S E Q U E N C E
    ;

INHERIT
    : I N H E R I T
    ;

TRANSLATE
    : T R A N S L A T E
    ;

MERGE
    : M E R G E
    ;

AT
    : A T
    ;

BITMAP
    : B I T M A P
    ;

CACHE
    : C A C H E
    ;

NOCACHE
    : N O C A C H E
    ;

CHECKPOINT
    : C H E C K P O I N T
    ;

CONNECT
    : C O N N E C T
    ;

CONSTRAINTS
    : C O N S T R A I N T S
    ;

CYCLE
    : C Y C L E
    ;

NOCYCLE
    : N O C Y C L E
    ;

DBTIMEZONE
    : D B T I M E Z O N E
    ;

ENCRYPT
    : E N C R Y P T
    ;

DECRYPT
    : D E C R Y P T
    ;

DEFERRABLE
    : D E F E R R A B L E
    ;

DEFERRED
    : D E F E R R E D
    ;

DIRECTORY
    : D I R E C T O R Y
    ;

DIRECT_PATH
    : D I R E C T UL_ P A T H
    ;

CREDENTIALS
    : C R E D E N T I A L S
    ;

EDITION
    : E D I T I O N
    ;

ELEMENT
    : E L E M E N T
    ;

END
    : E N D
    ;

EXCEPT
    : E X C E P T
    ;

EXCEPTIONS
    : E X C E P T I O N S
    ;

FORCE
    : F O R C E
    ;

NOFORCE
    : N O F O R C E
    ;

GLOBAL
    : G L O B A L
    ;

IDENTIFIED
    : I D E N T I F I E D
    ;

IDENTITY
    : I D E N T I T Y
    ;

IMMEDIATE
    : I M M E D I A T E
    ;

INCREMENT
    : I N C R E M E N T
    ;

INITIALLY
    : I N I T I A L L Y
    ;

INVALIDATE
    : I N V A L I D A T E
    ;

JAVA
    : J A V A
    ;

LEVELS
    : L E V E L S
    ;

MAXVALUE
    : M A X V A L U E
    ;

MINVALUE
    : M I N V A L U E
    ;

NOMAXVALUE
    : N O M A X V A L U E
    ;

NOMINVALUE
    : N O M I N V A L U E
    ;

NOSORT
    : N O S O R T
    ;

MINING
    : M I N I N G
    ;

MODEL
    : M O D E L
    ;

MODIFY
    : M O D I F Y
    ;

NATIONAL
    : N A T I O N A L
    ;

NEW
    : N E W
    ;

NOORDER
    : N O O R D E R
    ;

NORELY
    : N O R E L Y
    ;

OF
    : O F
    ;

ONLY
    : O N L Y
    ;

PRIOR
    : P R I O R
    ;

PROFILE
    : P R O F I L E
    ;

REF
    : R E F
    ;

REKEY
    : R E K E Y
    ;

RELY
    : R E L Y
    ;

RENAME
    : R E N A M E
    ;

REPLACE
    : R E P L A C E
    ;

RESOURCE
    : R E S O U R C E
    ;

REVERSE
    : R E V E R S E
    ;

ROWID
    : R O W I D
    ;

SALT
    : S A L T
    ;

SCOPE
    : S C O P E
    ;

SORT
    : S O R T
    ;

SOURCE
    : S O U R C E
    ;

SUBSTITUTABLE
    : S U B S T I T U T A B L E
    ;

TABLESPACE
    : T A B L E S P A C E
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

TRANSLATION
    : T R A N S L A T I O N
    ;

TREAT
    : T R E A T
    ;

NO
    : N O
    ;

UNUSED
    : U N U S E D
    ;

VALIDATE
    : V A L I D A T E
    ;

NOVALIDATE
    : N O V A L I D A T E
    ;

VALUE
    : V A L U E
    ;

VARYING
    : V A R Y I N G
    ;

VIRTUAL
    : V I R T U A L
    ;

ZONE
    : Z O N E
    ;

PUBLIC
    : P U B L I C
    ;

SESSION
    : S E S S I O N
    ;

COMMENT
    : C O M M E N T
    ;

LOCK
    : L O C K
    ;

ADVISOR
    : A D V I S O R
    ;

ADMINISTER
    : A D M I N I S T E R
    ;

TUNING
    : T U N I N G
    ;

MANAGE
    : M A N A G E
    ;

MANAGEMENT
    : M A N A G E M E N T
    ;

OBJECT
    : O B J E C T
    ;

CLUSTER
    : C L U S T E R
    ;

CONTEXT
    : C O N T E X T
    ;

EXEMPT
    : E X E M P T
    ;

REDACTION
    : R E D A C T I O N
    ;

POLICY
    : P O L I C Y
    ;

DATABASE
    : D A T A B A S E
    ;

SYSTEM
    : S Y S T E M
    ;

AUDIT
    : A U D I T
    ;

LINK
    : L I N K
    ;

ANALYZE
    : A N A L Y Z E
    ;

DICTIONARY
    : D I C T I O N A R Y
    ;

DIMENSION
    : D I M E N S I O N
    ;

INDEXTYPE
    : I N D E X T Y P E
    ;

INDEXTYPES
    : I N D E X T Y P E S
    ;

EXTERNAL
    : E X T E R N A L
    ;

JOB
    : J O B
    ;

CLASS
    : C L A S S
    ;

PROGRAM
    : P R O G R A M
    ;

SCHEDULER
    : S C H E D U L E R
    ;

LIBRARY
    : L I B R A R Y
    ;

LOGMINING
    : L O G M I N I N G
    ;

MATERIALIZED
    : M A T E R I A L I Z E D
    ;

CUBE
    : C U B E
    ;

MEASURE
    : M E A S U R E
    ;

FOLDER
    : F O L D E R
    ;

BUILD
    : B U I L D
    ;

PROCESS
    : P R O C E S S
    ;

OPERATOR
    : O P E R A T O R
    ;

OUTLINE
    : O U T L I N E
    ;

PLUGGABLE
    : P L U G G A B L E
    ;

CONTAINER
    : C O N T A I N E R
    ;

SEGMENT
    : S E G M E N T
    ;

RESTRICT
    : R E S T R I C T
    ;

RESTRICTED
    : R E S T R I C T E D
    ;

COST
    : C O S T
    ;

SYNONYM
    : S Y N O N Y M
    ;

BACKUP
    : B A C K U P
    ;

UNLIMITED
    : U N L I M I T E D
    ;

BECOME
    : B E C O M E
    ;

CHANGE
    : C H A N G E
    ;

NOTIFICATION
    : N O T I F I C A T I O N
    ;

ACCESS
    : A C C E S S
    ;

PRIVILEGE
    : P R I V I L E G E
    ;

PURGE
    : P U R G E
    ;

RESUMABLE
    : R E S U M A B L E
    ;

SYSGUID
    : S Y S G U I D
    ;

SYSBACKUP
    : S Y S B A C K U P
    ;

SYSDBA
    : S Y S D B A
    ;

SYSDG
    : S Y S D G
    ;

SYSKM
    : S Y S K M
    ;

SYSOPER
    : S Y S O P E R
    ;

RECYCLEBIN
    : R E C Y C L E B I N
    ;

DBA_RECYCLEBIN
    : D B A UL_ R E C Y C L E B I N
    ;

FIRST
    : F I R S T
    ;

NCHAR
    : N C H A R
    ;

RAW
    : R A W
    ;

VARCHAR
    : V A R C H A R
    ;

VARCHAR2
    : V A R C H A R [2]
    ;

STRING
    : S T R I N G
    ;

NVARCHAR2
    : N V A R C H A R [2]
    ;

LONG
    : L O N G
    ;

BLOB
    : B L O B
    ;

CLOB
    : C L O B
    ;

NCLOB
    : N C L O B
    ;

BINARY_FLOAT
    : B I N A R Y UL_ F L O A T
    ;

BINARY_DOUBLE
    : B I N A R Y UL_ D O U B L E
    ;

PLS_INTEGER
    : P L S UL_ I N T E G E R
    ;

BINARY_INTEGER
    : B I N A R Y UL_ I N T E G E R
    ;

NUMBER
    : N U M B E R
    ;

NATURALN
    : N A T U R A L N
    ;

POSITIVE
    : P O S I T I V E
    ;

POSITIVEN
    : P O S I T I V E N
    ;

SIGNTYPE
    : S I G N T Y P E
    ;

SIMPLE_INTEGER
    : S I M P L E UL_ I N T E G E R
    ;

BFILE
    : B F I L E
    ;

MLSLABEL
    : M L S L A B E L
    ;

UROWID
    : U R O W I D
    ;

JSON
    : J S O N
    ;

DEC
    : D E C
    ;

SHARING
    : S H A R I N G
    ;

PRIVATE
    : P R I V A T E
    ;

SHARDED
    : S H A R D E D
    ;

SHARD
    : S H A R D
    ;

DUPLICATED
    : D U P L I C A T E D
    ;

METADATA
    : M E T A D A T A
    ;

DATA
    : D A T A
    ;

EXTENDED
    : E X T E N D E D
    ;

NONE
    : N O N E
    ;

MEMOPTIMIZE
    : M E M O P T I M I Z E
    ;

PARENT
    : P A R E N T
    ;

IDENTIFIER
    : I D E N T I F I E R
    ;

WORK
    : W O R K
    ;

CONTAINER_MAP
    : C O N T A I N E R UL_ M A P
    ;

CONTAINERS_DEFAULT
    : C O N T A I N E R S UL_ D E F A U L T
    ;

WAIT
    : W A I T
    ;

NOWAIT
    : N O W A I T
    ;

BATCH
    : B A T C H
    ;

BLOCK
    : B L O C K
    ;

REBUILD
    : R E B U I L D
    ;

INVALIDATION
    : I N V A L I D A T I O N
    ;

COMPILE
    : C O M P I L E
    ;

USABLE
    : U S A B L E
    ;

UNUSABLE
    : U N U S A B L E
    ;

ONLINE
    : O N L I N E
    ;

MONITORING
    : M O N I T O R I N G
    ;

NOMONITORING
    : N O M O N I T O R I N G
    ;

USAGE
    : U S A G E
    ;

COALESCE
    : C O A L E S C E
    ;

CLEANUP
    : C L E A N U P
    ;

PARALLEL
    : P A R A L L E L
    ;

NOPARALLEL
    : N O P A R A L L E L
    ;

LOG
    : L O G
    ;

REUSE
    : R E U S E
    ;

SETTINGS
    : S E T T I N G S
    ;

STORAGE
    : S T O R A G E
    ;

MATCHED
    : M A T C H E D
    ;

ERRORS
    : E R R O R S
    ;

REJECT
    : R E J E C T
    ;

RETENTION
    : R E T E N T I O N
    ;

CHUNK
    : C H U N K
    ;

PCTVERSION
    : P C T V E R S I O N
    ;

FREEPOOLS
    : F R E E P O O L S
    ;

AUTO
    : A U T O
    ;

DEDUPLICATE
    : D E D U P L I C A T E
    ;

KEEP_DUPLICATES
    : K E E P UL_ D U P L I C A T E S
    ;

COMPRESS
    : C O M P R E S S
    ;

HIGH
    : H I G H
    ;

MEDIUM
    : M E D I U M
    ;

LOW
    : L O W
    ;

NOCOMPRESS
    : N O C O M P R E S S
    ;

READS
    : R E A D S
    ;

CREATION
    : C R E A T I O N
    ;

PCTFREE
    : P C T F R E E
    ;

PCTUSED
    : P C T U S E D
    ;

INITRANS
    : I N I T R A N S
    ;

LOGGING
    : L O G G I N G
    ;

NOLOGGING
    : N O L O G G I N G
    ;

FILESYSTEM_LIKE_LOGGING
    : F I L E S Y S T E M UL_ L I K E UL_ L O G G I N G
    ;

INITIAL
    : I N I T I A L
    ;

MINEXTENTS
    : M I N E X T E N T S
    ;

MAXEXTENTS
    : M A X E X T E N T S
    ;

BASIC
    : B A S I C
    ;

ADVANCED
    : A D V A N C E D
    ;

PCTINCREASE
    : P C T I N C R E A S E
    ;

FREELISTS
    : F R E E L I S T S
    ;

DML
    : D M L
    ;

DDL
    : D D L
    ;

CAPACITY
    : C A P A C I T Y
    ;

FREELIST
    : F R E E L I S T
    ;

GROUPS
    : G R O U P S
    ;

OPTIMAL
    : O P T I M A L
    ;

BUFFER_POOL
    : B U F F E R UL_ P O O L
    ;

RECYCLE
    : R E C Y C L E
    ;

FLASH_CACHE
    : F L A S H UL_ C A C H E
    ;

CELL_FLASH_CACHE
    : C E L L UL_ F L A S H UL_ C A C H E
    ;

MAXSIZE
    : M A X S I Z E
    ;

MAX_AUDIT_SIZE
    : M A X UL_ A U D I T UL_ S I Z E
    ;

MAX_DIAG_SIZE
    : M A X UL_ D I A G UL_ S I Z E
    ;

STORE
    : S T O R E
    ;

LEVEL
    : L E V E L
    ;

LOCKING
    : L O C K I N G
    ;

INMEMORY
    : I N M E M O R Y
    ;

MEMCOMPRESS
    : M E M C O M P R E S S
    ;

PRIORITY
    : P R I O R I T Y
    ;

CRITICAL
    : C R I T I C A L
    ;

DISTRIBUTE
    : D I S T R I B U T E
    ;

RANGE
    : R A N G E
    ;

PARTITION
    : P A R T I T I O N
    ;

SUBPARTITION
    : S U B P A R T I T I O N
    ;

SERVICE
    : S E R V I C E
    ;

DUPLICATE
    : D U P L I C A T E
    ;

ILM
    : I L M
    ;

DELETE_ALL
    : D E L E T E UL_ A L L
    ;

ENABLE_ALL
    : E N A B L E UL_ A L L
    ;

DISABLE_ALL
    : D I S A B L E UL_ A L L
    ;

AFTER
    : A F T E R
    ;

MODIFICATION
    : M O D I F I C A T I O N
    ;

DAYS
    : D A Y S
    ;

MONTHS
    : M O N T H S
    ;

YEARS
    : Y E A R S
    ;

TIER
    : T I E R
    ;

ORGANIZATION
    : O R G A N I Z A T I O N
    ;

HEAP
    : H E A P
    ;

PCTTHRESHOLD
    : P C T T H R E S H O L D
    ;

PARAMETERS
    : P A R A M E T E R S
    ;

LOCATION
    : L O C A T I O N
    ;

MAPPING
    : M A P P I N G
    ;

NOMAPPING
    : N O M A P P I N G
    ;

INCLUDING
    : I N C L U D I N G
    ;

OVERFLOW
    : O V E R F L O W
    ;

ATTRIBUTE
    : A T T R I B U T E
    ;

ATTRIBUTES
    : A T T R I B U T E S
    ;

RESULT_CACHE
    : R E S U L T UL_ C A C H E
    ;

ROWDEPENDENCIES
    : R O W D E P E N D E N C I E S
    ;

NOROWDEPENDENCIES
    : N O R O W D E P E N D E N C I E S
    ;

ARCHIVAL
    : A R C H I V A L
    ;

EXCHANGE
    : E X C H A N G E
    ;

INDEXING
    : I N D E X I N G
    ;

OFF
    : O F F
    ;

LESS
    : L E S S
    ;

INTERNAL
    : I N T E R N A L
    ;

VARRAY
    : V A R R A Y
    ;

NESTED
    : N E S T E D
    ;

COLUMN_VALUE
    : C O L U M N UL_ V A L U E
    ;

RETURN
    : R E T U R N
    ;

LOCATOR
    : L O C A  T O R
    ;

MODE
    : M O D E
    ;

LOB
    : L O B
    ;

SECUREFILE
    : S E C U R E F I L E
    ;

BASICFILE
    : B A S I C F I L E
    ;

THAN
    : T H A N
    ;

LIST
    : L I S T
    ;

AUTOMATIC
    : A U T O M A T I C
    ;

HASH
    : H A S H
    ;

PARTITIONS
    : P A R T I T I O N S
    ;

SUBPARTITIONS
    : S U B P A R T I T I O N S
    ;

TEMPLATE
    : T E M P L A T E
    ;

PARTITIONSET
    : P A R T I T I O N S E T
    ;

REFERENCE
    : R E F E R E N C E
    ;

CONSISTENT
    : C O N S I S T E N T
    ;

CLUSTERING
    : C L U S T E R I N G
    ;

LINEAR
    : L I N E A R
    ;

INTERLEAVED
    : I N T E R L E A V E D
    ;

YES
    : Y E S
    ;

LOAD
    : L O A D
    ;

MOVEMENT
    : M O V E M E N T
    ;

ZONEMAP
    : Z O N E M A P
    ;

WITHOUT
    : W I T H O U T
    ;

XMLTYPE
    : X M L T Y P E
    ;

RELATIONAL
    : R E L A T I O N A L
    ;

XML
    : X M L
    ;

VARRAYS
    : V A R R A Y S
    ;

LOBS
    : L O B S
    ;

TABLES
    : T A B L E S
    ;

ALLOW
    : A L L O W
    ;

DISALLOW
    : D I S A L L O W
    ;

NONSCHEMA
    : N O N S C H E M A
    ;

ANYSCHEMA
    : A N Y S C H E M A
    ;

XMLSCHEMA
    : X M L S C H E M A
    ;

COLUMNS
    : C O L U M N S
    ;

OIDINDEX
    : O I D I N D E X
    ;

EDITIONABLE
    : E D I T I O N A B L E
    ;

NONEDITIONABLE
    : N O N E D I T I O N A B L E
    ;

DEPENDENT
    : D E P E N D E N T
    ;

INDEXES
    : I N D E X E S
    ;

SHRINK
    : S H R I N K
    ;

SPACE
    : S P A C E
    ;

COMPACT
    : C O M P A C T
    ;

SUPPLEMENTAL
    : S U P P L E M E N T A L
    ;

ADVISE
    : A D V I S E
    ;

NOTHING
    : N O T H I N G
    ;

GUARD
    : G U A R D
    ;

SYNC
    : S Y N C
    ;

VISIBILITY
    : V I S I B I L I T Y
    ;

ACTIVE
    : A C T I V E
    ;

DEFAULT_COLLATION
    : D E F A U L T UL_ C O L L A T I O N
    ;

MOUNT
    : M O U N T
    ;

STANDBY
    : S T A N D B Y
    ;

CLONE
    : C L O N E
    ;

RESETLOGS
    : R E S E T L O G S
    ;

NORESETLOGS
    : N O R E S E T L O G S
    ;

UPGRADE
    : U P G R A D E
    ;

DOWNGRADE
    : D O W N G R A D E
    ;

RECOVER
    : R E C O V E R
    ;

LOGFILE
    : L O G F I L E
    ;

TEST
    : T E S T
    ;

CORRUPTION
    : C O R R U P T I O N
    ;

CONTINUE
    : C O N T I N U E
    ;

CANCEL
    : C A N C E L
    ;

UNTIL
    : U N T I L
    ;

CONTROLFILE
    : C O N T R O L F I L E
    ;

SNAPSHOT
    : S N A P S H O T
    ;

DATAFILE
    : D A T A F I L E
    ;

MANAGED
    : M A N A G E D
    ;

ARCHIVED
    : A R C H I V E D
    ;

DISCONNECT
    : D I S C O N N E C T
    ;

NODELAY
    : N O D E L A Y
    ;

INSTANCES
    : I N S T A N C E S
    ;

FINISH
    : F I N I S H
    ;

LOGICAL
    : L O G I C A L
    ;

FILE
    : F I L E
    ;

SIZE
    : S I Z E
    ;

AUTOEXTEND
    : A U T O E X T E N D
    ;

BLOCKSIZE
    : B L O C K S I Z E
    ;

OFFLINE
    : O F F L I N E
    ;

RESIZE
    : R E S I Z E
    ;

TEMPFILE
    : T E M P F I L E
    ;

DATAFILES
    : D A T A F I L E S
    ;

ARCHIVELOG
    : A R C H I V E L O G
    ;

MANUAL
    : M A N U A L
    ;

NOARCHIVELOG
    : N O A R C H I V E L O G
    ;

AVAILABILITY
    : A V A I L A B I L I T Y
    ;

PERFORMANCE
    : P E R F O R M A N C E
    ;

CLEAR
    : C L E A R
    ;

UNARCHIVED
    : U N A R C H I V E D
    ;

UNRECOVERABLE
    : U N R E C O V E R A B L E
    ;

THREAD
    : T H R E A D
    ;

MEMBER
    : M E M B E R
    ;

PHYSICAL
    : P H Y S I C A L
    ;

FAR
    : F A R
    ;

TRACE
    : T R A C E
    ;

DISTRIBUTED
    : D I S T R I B U T E D
    ;

RECOVERY
    : R E C O V E R Y
    ;

FLUSH
    : F L U S H
    ;

NOREPLY
    : N O R E P L Y
    ;

SWITCH
    : S W I T C H
    ;

LOGFILES
    : L O G F I L E S
    ;

PROCEDURAL
    : P R O C E D U R A L
    ;

REPLICATION
    : R E P L I C A T I O N
    ;

SUBSET
    : S U B S E T
    ;

ACTIVATE
    : A C T I V A T E
    ;

APPLY
    : A P P L Y
    ;

APPROX_RANK
    : A P P R O X UL_ R A N K
    ;

MAXIMIZE
    : M A X I M I Z E
    ;

PROTECTION
    : P R O T E C T I O N
    ;

SUSPEND
    : S U S P E N D
    ;

RESUME
    : R E S U M E
    ;

QUIESCE
    : Q U I E S C E
    ;

UNQUIESCE
    : U N Q U I E S C E
    ;

SHUTDOWN
    : S H U T D O W N
    ;

REGISTER
    : R E G I S T E R
    ;

PREPARE
    : P R E P A R E
    ;

SWITCHOVER
    : S W I T C H O V E R
    ;

FAILED
    : F A I L E D
    ;

SKIP_SYMBOL
    : S K I P
    ;

STOP
    : S T O P
    ;

ABORT
    : A B O R T
    ;

VERIFY
    : V E R I F Y
    ;

CONVERT
    : C O N V E R T
    ;

FAILOVER
    : F A I L O V E R
    ;

BIGFILE
    : B I G F I L E
    ;

SMALLFILE
    : S M A L L F I L E
    ;

TRACKING
    : T R A C K I N G
    ;

CACHING
    : C A C H I N G
    ;

CONTAINERS
    : C O N T A I N E R S
    ;

TARGET
    : T A R G E T
    ;

UNDO
    : U N D O
    ;

MOVE
    : M O V E
    ;

MIRROR
    : M I R R O R
    ;

COPY
    : C O P Y
    ;

UNPROTECTED
    : U N P R O T E C T E D
    ;

REDUNDANCY
    : R E D U N D A N C Y
    ;

REMOVE
    : R E M O V E
    ;

LOST
    : L O S T
    ;

LEAD_CDB
    : L E A D UL_ C D B
    ;

LEAD_CDB_URI
    : L E A D UL_ C D B UL_ U R I
    ;

PROPERTY
    : P R O P E R T Y
    ;

DEFAULT_CREDENTIAL
    : D E F A U L T UL_ C R E D E N T I A L
    ;

TIME_ZONE
    : T I M E UL_ Z O N E
    ;

RESET
    : R E S E T
    ;

RELOCATE
    : R E L O C A T E
    ;

CLIENT
    : C L I E N T
    ;

PASSWORDFILE_METADATA_CACHE
    : P A S S W O R D F I L E UL_ M E T A D A T A UL_ C A C H E
    ;

NOSWITCH
    : N O S W I T C H
    ;

POST_TRANSACTION
    : P O S T UL_ T R A N S A C T I O N
    ;

KILL
    : K I L L
    ;

ROLLING
    : R O L L I N G
    ;

MIGRATION
    : M I G R A T I O N
    ;

PATCH
    : P A T C H
    ;

ENCRYPTION
    : E N C R Y P T I O N
    ;

WALLET
    : W A L L E T
    ;

AFFINITY
    : A F F I N I T Y
    ;

MEMORY
    : M E M O R Y
    ;

SPFILE
    : S P F I L E
    ;

PFILE
    : P F I L E
    ;

BOTH
    : B O T H
    ;

SID
    : S I D
    ;

SHARED_POOL
    : S H A R E D UL_ P O O L
    ;

BUFFER_CACHE
    : B U F F E R UL_ C A C H E
    ;

REDO
    : R E D O
    ;

CONFIRM
    : C O N F I R M
    ;

MIGRATE
    : M I G R A T E
    ;

USE_STORED_OUTLINES
    : U S E UL_ S T O R E D UL_ O U T L I N E S
    ;

GLOBAL_TOPIC_ENABLED
    : G L O B A L UL_ T O P I C UL_ E N A B L E D
    ;

INTERSECT
    : I N T E R S E C T
    ;

MINUS
    : M I N U S
    ;

LOCKED
    : L O C K E D
    ;

FETCH
    : F E T C H
    ;

PERCENT
    : P E R C E N T
    ;

TIES
    : T I E S
    ;

SIBLINGS
    : S I B L I N G S
    ;

NULLS
    : N U L L S
    ;

LAST
    : L A S T
    ;

ISOLATION
    : I S O L A T I O N
    ;

SERIALIZABLE
    : S E R I A L I Z A B L E
    ;

COMMITTED
    : C O M M I T T E D
    ;

FILTER
    : F I L T E R
    ;

FACT
    : F A C T
    ;

DETERMINISTIC
    : D E T E R M I N I S T I C
    ;

PIPELINED
    : P I P E L I N E D
    ;

PARALLEL_ENABLE
    : P A R A L L E L UL_ E N A B L E
    ;

OUT
    : O U T
    ;

NOCOPY
    : N O C O P Y
    ;

ACCESSIBLE
    : A C C E S S I B L E
    ;

PACKAGE
    : P A C K A G E
    ;

PACKAGES
    : P A C K A G E S
    ;

USING_NLS_COMP
    : U S I N G UL_ N L S UL_ C O M P
    ;

AUTHID
    : A U T H I D
    ;

SEARCH
    : S E A R C H
    ;

DEPTH
    : D E P T H
    ;

BREADTH
    : B R E A D T H
    ;

ANALYTIC
    : A N A L Y T I C
    ;

HIERARCHIES
    : H I E R A R C H I E S
    ;

MEASURES
    : M E A S U R E S
    ;

OVER
    : O V E R
    ;

LAG
    : L A G
    ;

LAG_DIFF
    : L A G UL_ D I F F
    ;

LAG_DIF_PERCENT
    : L A G UL_ D I F UL_ P E R C E N T
    ;

LEAD
    : L E A D
    ;

LEAD_DIFF
    : L E A D UL_ D I F F
    ;

LEAD_DIFF_PERCENT
    : L E A D UL_ D I F F UL_ P E R C E N T
    ;

HIERARCHY
    : H I E R A R C H Y
    ;

WITHIN
    : W I T H I N
    ;

ACROSS
    : A C R O S S
    ;

ANCESTOR
    : A N C E S T O R
    ;

BEGINNING
    : B E G I N N I N G
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

PRECEDING
    : P R E C E D I N G
    ;

FOLLOWING
    : F O L L O W I N G
    ;

RANK
    : R A N K
    ;

DENSE_RANK
    : D E N S E UL_ R A N K
    ;

AVERAGE_RANK
    : A V E R A G E UL_ R A N K
    ;

ROW_NUMBER
    : R O W UL_ N U M B E R
    ;

SHARE_OF
    : S H A R E UL_ O F
    ;

HIER_ANCESTOR
    : H I E R UL_ A N C E S T O R
    ;

HIER_PARENT
    : H I E R UL_ P A R E N T
    ;

HIER_LEAD
    : H I E R UL_ L E A D
    ;

HIER_LAG
    : H I E R UL_ L A G
    ;

QUALIFY
    : Q U A L I F Y
    ;

HIER_CAPTION
    : H I E R UL_ C A P T I O N
    ;

HIER_DEPTH
    : H I E R UL_ D E P T H
    ;

HIER_DESCRIPTION
    : H I E R UL_ D E S C R I P T I O N
    ;

HIER_LEVEL
    : H I E R UL_ L E V E L
    ;

HIER_MEMBER_NAME
    : H I E R UL_ M E M B E R UL_ N A M E
    ;

HIER_MEMBER_UNIQUE_NAME
    : H I E R UL_ M E M B E R UL_ U N I Q U E UL_ N A M E
    ;

CHAINED
    : C H A I N E D
    ;

STATISTICS
    : S T A T I S T I C S
    ;

DANGLING
    : D A N G L I N G
    ;

STRUCTURE
    : S T R U C T U R E
    ;

FAST
    : F A S T
    ;

COMPLETE
    : C O M P L E T E
    ;

ASSOCIATE
    : A S S O C I A T E
    ;

DISASSOCIATE
    : D I S A S S O C I A T E
    ;

FUNCTIONS
    : F U N C T I O N S
    ;

TYPES
    : T Y P E S
    ;

SELECTIVITY
    : S E L E C T I V I T Y
    ;

RETURNING
    : R E T U R N I N G
    ;

VERSIONS
    : V E R S I O N S
    ;

SCN
    : S C N
    ;

PERIOD
    : P E R I O D
    ;

LATERAL
    : L A T E R A L
    ;

BADFILE
    : B A D F I L E
    ;

DISCARDFILE
    : D I S C A R D F I L E
    ;

PIVOT
    : P I V O T
    ;

UNPIVOT
    : U N P I V O T
    ;

INCLUDE
    : I N C L U D E
    ;

EXCLUDE
    : E X C L U D E
    ;

SAMPLE
    : S A M P L E
    ;

SEED
    : S E E D
    ;

OPTION
    : O P T I O N
    ;

SHARDS
    : S H A R D S
    ;

MATCH_RECOGNIZE
    : M A T C H UL_ R E C O G N I Z E
    ;

PATTERN
    : P A T T E R N
    ;

DEFINE
    : D E F I N E
    ;

ONE
    : O N E
    ;

PER
    : P E R
    ;

MATCH
    : M A T C H
    ;

PAST
    : P A S T
    ;

PERMUTE
    : P E R M U T E
    ;

CLASSIFIER
    : C L A S S I F I E R
    ;

MATCH_NUMBER
    : M A T C H UL_ N U M B E R
    ;

RUNNING
    : R U N N I N G
    ;

FINAL
    : F I N A L
    ;

PREV
    : P R E V
    ;

NOAUDIT
    : N O A U D I T
    ;

WHENEVER
    : W H E N E V E R
    ;

SUCCESSFUL
    : S U C C E S S F U L
    ;

USERS
    : U S E R S
    ;

GRANTED
    : G R A N T E D
    ;

ROLES
    : R O L E S
    ;

NAMESPACE
    : N A M E S P A C E
    ;

ROLLUP
    : R O L L U P
    ;

GROUPING
    : G R O U P I N G
    ;

SETS
    : S E T S
    ;

DECODE
    : D E C O D E
    ;

RESTORE
    : R E S T O R E
    ;

POINT
    : P O I N T
    ;

BEFORE
    : B E F O R E
    ;

IGNORE
    : I G N O R E
    ;

NAV
    : N A V
    ;

SINGLE
    : S I N G L E
    ;

UPDATED
    : U P D A T E D
    ;

MAIN
    : M A I N
    ;

RULES
    : R U L E S
    ;

UPSERT
    : U P S E R T
    ;

SEQUENTIAL
    : S E Q U E N T I A L
    ;

ITERATE
    : I T E R A T E
    ;

DECREMENT
    : D E C R E M E N T
    ;

SOME
    : S O M E
    ;

NAN
    : N A N
    ;

INFINITE
    : I N F I N I T E
    ;

PRESENT
    : P R E S E N T
    ;

EMPTY
    : E M P T Y
    ;

SUBMULTISET
    : S U B M U L T I S E T
    ;

LIKEC
    : L I K E C
    ;

LIKE2
    : L I K E '2'
    ;

LIKE4
    : L I K E '4'
    ;

REGEXP_LIKE
    : R E G E X P UL_ L I K E
    ;

EQUALS_PATH
    : E Q U A L S UL_ P A T H
    ;

UNDER_PATH
    : U N D E R UL_ P A T H
    ;

FORMAT
    : F O R M A T
    ;

STRICT
    : S T R I C T
    ;

LAX
    : L A X
    ;

KEYS
    : K E Y S
    ;

JSON_EQUAL
    : J S O N UL_ E Q U A L
    ;

JSON_EXISTS
    : J S O N UL_ E X I S T S
    ;

PASSING
    : P A S S I N G
    ;

ERROR
    : E R R O R
    ;

JSON_TEXTCONTAINS
    : J S O N UL_ T E X T C O N T A I N S
    ;

HAS
    : H A S
    ;

STARTS
    : S T A R T S
    ;

LIKE_REGEX
    : L I K E UL_ R E G E X
    ;

EQ_REGEX
    : E Q UL_ R E G E X
    ;

SYS
    : S Y S
    ;

MAXDATAFILES
    : M A X D A T A F I L E S
    ;

MAXINSTANCES
    : M A X I N S T A N C E S
    ;

AL32UTF8
    : A L '3' '2' U T F '8'
    ;

AL16UTF16
    : A L '1' '6' U T F '1' '6'
    ;

UTF8
    : U T F '8'
    ;

USER_DATA
    : U S E R UL_ D A T A
    ;

MAXLOGFILES
    : M A X L O G F I L E S
    ;

MAXLOGMEMBERS
    : M A X L O G M E M B E R S
    ;

MAXLOGHISTORY
    : M A X L O G H I S T O R Y
    ;

EXTENT
    : E X T E N T
    ;

SYSAUX
    : S Y S A U X
    ;

LEAF
    : L E A F
    ;

AUTOALLOCATE
    : A U T O A L L O C A T E
    ;

UNIFORM
    : U N I F O R M
    ;

FILE_NAME_CONVERT
    : F I L E UL_ N A M E UL_ C O N V E R T
    ;

ALLOCATE
    : A L L O C A T E
    ;

DEALLOCATE
    : D E A L L O C A T E
    ;

SHARED
    : S H A R E D
    ;

AUTHENTICATED
    : A U T H E N T I C A T E D
    ;

CHILD
    : C H I L D
    ;

DETERMINES
    : D E T E R M I N E S
    ;

RELIES_ON
    : R E L I E S UL_ O N
    ;

AGGREGATE
    : A G G R E G A T E
    ;

POLYMORPHIC
    : P O L Y M O R P H I C
    ;

SQL_MARCO
    : S Q L UL_ M A R C O
    ;

LANGUAGE
    : L A N G U A G E
    ;

AGENT
    : A G E N T
    ;

SELF
    : S E L F
    ;

TDO
    : T D O
    ;

INDICATOR
    : I N D I C A T O R
    ;

STRUCT
    : S T R U C T
    ;

LENGTH
    : L E N G T H
    ;

DURATION
    : D U R A T I O N
    ;

MAXLEN
    : M A X L E N
    ;

CHARSETID
    : C H A R S E T I D
    ;

CHARSETFORM
    : C H A R S E T F O R M
    ;

SINGLE_C
    : C
    ;

SINGLE_K
    : K
    ;

SINGLE_M
    : M
    ;

SINGLE_G
    : G
    ;

SINGLE_T
    : T
    ;

SINGLE_P
    : P
    ;

SINGLE_E
    : E
    ;

SINGLE_H
    : H
    ;

SYSTIMESTAMP
    : S Y S T I M E S T A M P
    ;

CATEGORY
    : C A T E G O R Y
    ;

ORDER
    : O R D E R
    ;

NOKEEP
    : N O K E E P
    ;

SCALE
    : S C A L E
    ;

NOSCALE
    : N O S C A L E
    ;

EXTEND
    : E X T E N D
    ;

NOEXTEND
    : N O E X T E N D
    ;

NOSHARD
    : N O S H A R D
    ;

INITIALIZED
    : I N I T I A L I Z E D
    ;

EXTERNALLY
    : E X T E R N A L L Y
    ;

GLOBALLY
    : G L O B A L L Y
    ;

ACCESSED
    : A C C E S S E D
    ;

RESTART
    : R E S T A R T
    ;

OPTIMIZE
    : O P T I M I Z E
    ;

QUOTA
    : Q U O T A
    ;

DISKGROUP
    : D I S K G R O U P
    ;

NORMAL
    : N O R M A L
    ;

FLEX
    : F L E X
    ;

SITE
    : S I T E
    ;

QUORUM
    : Q U O R U M
    ;

REGULAR
    : R E G U L A R
    ;

FAILGROUP
    : F A I L G R O U P
    ;

DISK
    : D I S K
    ;

EXCLUDING
    : E X C L U D I N G
    ;

CONTENTS
    : C O N T E N T S
    ;

LOCKDOWN
    : L O C K D O W N
    ;

CLEAN
    : C L E A N
    ;

GUARANTEE
    : G U A R A N T E E
    ;

PRUNING
    : P R U N I N G
    ;

DEMAND
    : D E M A N D
    ;

RESOLVE
   : R E S O L V E
   ;

RESOLVER
   : R E S O L V E R
   ;

SHARE
    : S H A R E
    ;

EXCLUSIVE
    : E X C L U S I V E
    ;

ANCILLARY
    : A N C I L L A R Y
    ;

BINDING
    : B I N D I N G
    ;

SCAN
    : S C A N
    ;

COMPUTE
    : C O M P U T E
    ;

UNDROP
    : U N D R O P
    ;

DISKS
    : D I S K S
    ;

COARSE
    : C O A R S E
    ;

FINE
    : F I N E
    ;

ALIAS
    : A L I A S
    ;

SCRUB
    : S C R U B
    ;

DISMOUNT
    : D I S M O U N T
    ;

REBALANCE
    : R E B A L A N C E
    ;

COMPUTATION
    : C O M P U T A T I O N
    ;

CONSIDER
    : C O N S I D E R
    ;

FRESH
    : F R E S H
    ;

MASTER
    : M A S T E R
    ;

ENFORCED
    : E N F O R C E D
    ;

TRUSTED
    : T R U S T E D
    ;

TRUST
    : T R U S T
    ;

ID
    : I D
    ;

SYNCHRONOUS
    : S Y N C H R O N O U S
    ;

ASYNCHRONOUS
    : A S Y N C H R O N O U S
    ;

REPEAT
    : R E P E A T
    ;

FEATURE
    : F E A T U R E
    ;

STATEMENT
    : S T A T E M E N T
    ;

CLAUSE
   : C L A U S E
   ;


UNPLUG
    : U N P L U G
    ;

HOST
    : H O S T
    ;

PORT
    : P O R T
    ;

EVERY
    : E V E R Y
    ;

MINUTES
    : M I N U T E S
    ;

HOURS
    : H O U R S
    ;

NORELOCATE
    : N O R E L O C A T E
    ;

SAVE
    : S A V E
    ;

DISCARD
    : D I S C A R D
    ;

STATE
    : S T A T E
    ;

APPLICATION
    : A P P L I C A T I O N
    ;

INSTALL
    : I N S T A L L
    ;

MINIMUM
    : M I N I M U M
    ;

VERSION
    : V E R S I O N
    ;

UNINSTALL
    : U N I N S T A L L
    ;

COMPATIBILITY
    : C O M P A T I B I L I T Y
    ;

MATERIALIZE
    : M A T E R I A L I Z E
    ;

SUBTYPE
    : S U B T Y P E
    ;

RECORD
    : R E C O R D
    ;

CONSTANT
    : C O N S T A N T
    ;

CURSOR
    : C U R S O R
    ;

OTHERS
    : O T H E R S
    ;

EXCEPTION
    : E X C E P T I O N
    ;

CPU_PER_SESSION
    : C P U UL_ P E R UL_ S E S S I O N
    ;

CONNECT_TIME
    : C O N N E C T UL_ T I M E
    ;

AZURE_ROLE
    : A Z U R E UL_ R O L E
    ;

AZURE_USER
    : A Z U R E UL_ U S E R
    ;

IAM_GROUP_NAME
    : I A M UL_ G R O U P UL_ N A M E
    ;

IAM_PRINCIPAL_NAME
    : I A M UL_ P R I N C I P A L UL_ N A M E
    ;

LOGICAL_READS_PER_SESSION
    : L O G I C A L UL_ R E A D S UL_ P E R UL_ S E S S I O N
    ;

PRIVATE_SGA
    : P R I V A T E UL_ S G A
    ;

PERCENT_RANK
    : P E R C E N T UL_ R A N K
    ;

LISTAGG
    : L I S T A G G
    ;

ABS
    :A B S
    ;

ACCOUNT
    :A C C O U N T
    ;

ACOS
    :A C O S
    ;

ACTIVE_COMPONENT
    :A C T I V E UL_ C O M P O N E N T
    ;

ACTIVE_FUNCTION
    :A C T I V E UL_ F U N C T I O N
    ;

ACTIVE_TAG
    :A C T I V E UL_ T A G
    ;

ADD
    :A D D
    ;

ADD_COLUMN
    :A D D UL_ C O L U M N
    ;

ADD_GROUP
    :A D D UL_ G R O U P
    ;

ADD_MONTHS
    :A D D UL_ M O N T H S
    ;

ADJ_DATE
    :A D J UL_ D A T E
    ;

ADMIN
    :A D M I N
    ;

ADMINISTRATOR
    :A D M I N I S T R A T O R
    ;

ALL
    :A L L
    ;

ALL_ROWS
    :A L L UL_ R O W S
    ;

ALTER
    :A L T E R
    ;

AND
    :A N D
    ;

AND_EQUAL
    :A N D UL_ E Q U A L
    ;

ANTIJOIN
    :A N T I J O I N
    ;

APPEND
    :A P P E N D
    ;

APPENDCHILDXML
    :A P P E N D C H I L D X M L
    ;

APPEND_VALUES
    :A P P E N D UL_ V A L U E S
    ;

AS
    :A S
    ;

ASC
    :A S C
    ;

ASCII
    :A S C I I
    ;

ASCIISTR
    :A S C I I S T R
    ;

ASIN
    :A S I N
    ;

ASSEMBLY
    :A S S E M B L Y
    ;

ASYNC
    :A S Y N C
    ;

ATAN
    :A T A N
    ;

ATAN2
    :A T A N [2]
    ;

AUTHENTICATION
    :A U T H E N T I C A T I O N
    ;

AUTHORIZATION
    :A U T H O R I Z A T I O N
    ;

BEGIN_OUTLINE_DATA
    :B E G I N UL_ O U T L I N E UL_ D A T A
    ;

BEHALF
    :B E H A L F
    ;

BETWEEN
    :B E T W E E N
    ;

BFILENAME
    :B F I L E N A M E
    ;

BINARY_DOUBLE_INFINITY
    :B I N A R Y UL_ D O U B L E UL_ I N F I N I T Y
    ;

BINARY_DOUBLE_NAN
    :B I N A R Y UL_ D O U B L E UL_ N A N
    ;

BINARY_FLOAT_INFINITY
    :B I N A R Y UL_ F L O A T UL_ I N F I N I T Y
    ;

BINARY_FLOAT_NAN
    :B I N A R Y UL_ F L O A T UL_ N A N
    ;

BIND_AWARE
    :B I N D UL_ A W A R E
    ;

BIN_TO_NUM
    :B I N UL_ T O UL_ N U M
    ;

BITAND
    :B I T A N D
    ;

BITMAPS
    :B I T M A P S
    ;

BITMAP_TREE
    :B I T M A P UL_ T R E E
    ;

BITS
    :B I T S
    ;

BLOCKS
    :B L O C K S
    ;

BLOCK_RANGE
    :B L O C K UL_ R A N G E
    ;

BOUND
    :B O U N D
    ;

BRANCH
    :B R A N C H
    ;

BROADCAST
    :B R O A D C A S T
    ;

BUFFER
    :B U F F E R
    ;

BULK
    :B U L K
    ;

BY
    :B Y
    ;

BYPASS_RECURSIVE_CHECK
    :B Y P A S S UL_ R E C U R S I V E UL_ C H E C K
    ;

BYPASS_UJVC
    :B Y P A S S UL_ U J V C
    ;

BYTE
    :B Y T E
    ;

CACHE_CB
    :C A C H E UL_ C B
    ;

CACHE_INSTANCES
    :C A C H E UL_ I N S T A N C E S
    ;

CACHE_TEMP_TABLE
    :C A C H E UL_ T E M P UL_ T A B L E
    ;

CARDINALITY
    :C A R D I N A L I T Y
    ;

CEIL
    :C E I L
    ;

CERTIFICATE
    :C E R T I F I C A T E
    ;

CFILE
    :C F I L E
    ;

CHANGE_DUPKEY_ERROR_INDEX
    :C H A N G E UL_ D U P K E Y UL_ E R R O R UL_ I N D E X
    ;

CHAR
    :C H A R
    ;

CHARTOROWID
    :C H A R T O R O W I D
    ;

CHAR_CS
    :C H A R UL_ C S
    ;

CHECK_ACL_REWRITE
    :C H E C K UL_ A C L UL_ R E W R I T E
    ;

CHOOSE
    :C H O O S E
    ;

CHR
    :C H R
    ;

CLOSE_CACHED_OPEN_CURSORS
    :C L O S E UL_ C A C H E D UL_ O P E N UL_ C U R S O R S
    ;

CLUSTERING_FACTOR
    :C L U S T E R I N G UL_ F A C T O R
    ;

CLUSTER_ID
    :C L U S T E R UL_ I D
    ;

CLUSTER_PROBABILITY
    :C L U S T E R UL_ P R O B A B I L I T Y
    ;

CLUSTER_SET
    :C L U S T E R UL_ S E T
    ;

COALESCE_SQ
    :C O A L E S C E UL_ S Q
    ;

COLD
    :C O L D
    ;

COLLECT
    :C O L L E C T
    ;

COLUMN
    :C O L U M N
    ;

COLUMNAR
    :C O L U M N A R
    ;

COLUMN_AUTH_INDICATOR
    :C O L U M N UL_ A U T H UL_ I N D I C A T O R
    ;

COLUMN_STATS
    :C O L U M N UL_ S T A T S
    ;

COMPLIANCE
    :C O M P L I A N C E
    ;

COMPOSE
    :C O M P O S E
    ;

COMPOSITE
    :C O M P O S I T E
    ;

COMPOSITE_LIMIT
    :C O M P O S I T E UL_ L I M I T
    ;

COMPOUND
    :C O M P O U N D
    ;

CONCAT
    :C O N C A T
    ;

CONFORMING
    :C O N F O R M I N G
    ;

CONNECT_BY_CB_WHR_ONLY
    :C O N N E C T UL_ B Y UL_ C B UL_ W H R UL_ O N L Y
    ;

CONNECT_BY_COMBINE_SW
    :C O N N E C T UL_ B Y UL_ C O M B I N E UL_ S W
    ;

CONNECT_BY_COST_BASED
    :C O N N E C T UL_ B Y UL_ C O S T UL_ B A S E D
    ;

CONNECT_BY_ELIM_DUPS
    :C O N N E C T UL_ B Y UL_ E L I M UL_ D U P S
    ;

CONNECT_BY_FILTERING
    :C O N N E C T UL_ B Y UL_ F I L T E R I N G
    ;

CONNECT_BY_ISCYCLE
    :C O N N E C T UL_ B Y UL_ I S C Y C L E
    ;

CONNECT_BY_ISLEAF
    :C O N N E C T UL_ B Y UL_ I S L E A F
    ;

CONNECT_BY_ROOT
    :C O N N E C T UL_ B Y UL_ R O O T
    ;

CONST
    :C O N S T
    ;

CORR
    :C O R R
    ;

CORRUPT_XID
    :C O R R U P T UL_ X I D
    ;

CORRUPT_XID_ALL
    :C O R R U P T UL_ X I D UL_ A L L
    ;

CORR_K
    :C O R R UL_ K
    ;

CORR_S
    :C O R R UL_ S
    ;

COS
    :C O S
    ;

COSH
    :C O S H
    ;

COST_XML_QUERY_REWRITE
    :C O S T UL_ X M L UL_ Q U E R Y UL_ R E W R I T E
    ;

COVAR_POP
    :C O V A R UL_ P O P
    ;

COVAR_SAMP
    :C O V A R UL_ S A M P
    ;

CO_AUTH_IND
    :C O UL_ A U T H UL_ I N D
    ;

CPU_COSTING
    :C P U UL_ C O S T I N G
    ;

CPU_PER_CALL
    :C P U UL_ P E R UL_ C A L L
    ;

CRASH
    :C R A S H
    ;

CREATE
    :C R E A T E
    ;

CREATE_STORED_OUTLINES
    :C R E A T E UL_ S T O R E D UL_ O U T L I N E S
    ;

CROSSEDITION
    :C R O S S E D I T I O N
    ;

CSCONVERT
    :C S C O N V E R T
    ;

CUBE_GB
    :C U B E UL_ G B
    ;

CUME_DIST
    :C U M E UL_ D I S T
    ;

CUME_DISTM
    :C U M E UL_ D I S T M
    ;

CURRENT
    :C U R R E N T
    ;

CURRENTV
    :C U R R E N T V
    ;

CURRENT_DATE
    :C U R R E N T UL_ D A T E
    ;

CURRENT_SCHEMA
    :C U R R E N T UL_ S C H E M A
    ;

CURRENT_TIME
    :C U R R E N T UL_ T I M E
    ;

CURRENT_TIMESTAMP
    :C U R R E N T UL_ T I M E S T A M P
    ;

CURSOR_SHARING_EXACT
    :C U R S O R UL_ S H A R I N G UL_ E X A C T
    ;

CURSOR_SPECIFIC_SEGMENT
    :C U R S O R UL_ S P E C I F I C UL_ S E G M E N T
    ;

CV
    :C V
    ;

DATABASE_DEFAULT
    :D A T A B A S E UL_ D E F A U L T
    ;

DATAOBJNO
    :D A T A O B J N O
    ;

DATAOBJ_TO_PARTITION
    :D A T A O B J UL_ T O UL_ P A R T I T I O N
    ;

DATE
    :D A T E
    ;

DATE_MODE
    :D A T E UL_ M O D E
    ;

DBA
    :D B A
    ;

DBMS_STATS
    :D B M S UL_ S T A T S
    ;

DB_ROLE_CHANGE
    :D B UL_ R O L E UL_ C H A N G E
    ;

DB_VERSION
    :D B UL_ V E R S I O N
    ;

DEBUGGER
    :D E B U G G E R
    ;

DECIMAL
    :D E C I M A L
    ;

DECLARE
    :D E C L A R E
    ;

DECOMPOSE
    :D E C O M P O S E
    ;

DECR
    :D E C R
    ;

DEFAULT
    :D E F A U L T
    ;

DEFAULTS
    :D E F A U L T S
    ;

DEFINED
    :D E F I N E D
    ;

DEGREE
    :D E G R E E
    ;

DELAY
    :D E L A Y
    ;

DELETE
    :D E L E T E
    ;

DELETEXML
    :D E L E T E X M L
    ;

DENSE_RANKM
    :D E N S E UL_ R A N K M
    ;

DEQUEUE
    :D E Q U E U E
    ;

DEREF
    :D E R E F
    ;

DEREF_NO_REWRITE
    :D E R E F UL_ N O UL_ R E W R I T E
    ;

DESC
    :D E S C
    ;

DETACHED
    :D E T A C H E D
    ;

DIRECT_LOAD
    :D I R E C T UL_ L O A D
    ;

DISABLE_PRESET
    :D I S A B L E UL_ P R E S E T
    ;

DISABLE_RPKE
    :D I S A B L E UL_ R P K E
    ;

DISTINCT
    :D I S T I N C T
    ;

DISTINGUISHED
    :D I S T I N G U I S H E D
    ;

DML_UPDATE
    :D M L UL_ U P D A T E
    ;

DOCFIDELITY
    :D O C F I D E L I T Y
    ;

DOCUMENT
    :D O C U M E N T
    ;

DOMAIN_INDEX_FILTER
    :D O M A I N UL_ I N D E X UL_ F I L T E R
    ;

DOMAIN_INDEX_NO_SORT
    :D O M A I N UL_ I N D E X UL_ N O UL_ S O R T
    ;

DOMAIN_INDEX_SORT
    :D O M A I N UL_ I N D E X UL_ S O R T
    ;

DRIVING_SITE
    :D R I V I N G UL_ S I T E
    ;

DROP
    :D R O P
    ;

DROP_COLUMN
    :D R O P UL_ C O L U M N
    ;

DROP_GROUP
    :D R O P UL_ G R O U P
    ;

DST_UPGRADE_INSERT_CONV
    :D S T UL_ U P G R A D E UL_ I N S E R T UL_ C O N V
    ;

DUMP
    :D U M P
    ;

DYNAMIC
    :D Y N A M I C
    ;

DYNAMIC_SAMPLING
    :D Y N A M I C UL_ S A M P L I N G
    ;

DYNAMIC_SAMPLING_EST_CDN
    :D Y N A M I C UL_ S A M P L I N G UL_ E S T UL_ C D N
    ;

EACH
    :E A C H
    ;

EDITIONING
    :E D I T I O N I N G
    ;

EDITIONS
    :E D I T I O N S
    ;

ELIMINATE_JOIN
    :E L I M I N A T E UL_ J O I N
    ;

ELIMINATE_OBY
    :E L I M I N A T E UL_ O B Y
    ;

ELIMINATE_OUTER_JOIN
    :E L I M I N A T E UL_ O U T E R UL_ J O I N
    ;

ELSE
    :E L S E
    ;

EMPTY_BLOB
    :E M P T Y UL_ B L O B
    ;

EMPTY_CLOB
    :E M P T Y UL_ C L O B
    ;

ENABLE_PRESET
    :E N A B L E UL_ P R E S E T
    ;

ENCODING
    :E N C O D I N G
    ;

END_OUTLINE_DATA
    :E N D UL_ O U T L I N E UL_ D A T A
    ;

ENFORCE
    :E N F O R C E
    ;

ENQUEUE
    :E N Q U E U E
    ;

ENTERPRISE
    :E N T E R P R I S E
    ;

ENTITYESCAPING
    :E N T I T Y E S C A P I N G
    ;

ENTRY
    :E N T R Y
    ;

ERROR_ARGUMENT
    :E R R O R UL_ A R G U M E N T
    ;

ERROR_ON_OVERLAP_TIME
    :E R R O R UL_ O N UL_ O V E R L A P UL_ T I M E
    ;

ESTIMATE
    :E S T I M A T E
    ;

EVALUATION
    :E V A L U A T I O N
    ;

EVENTS
    :E V E N T S
    ;

EXISTS
    :E X I S T S
    ;

EXISTSNODE
    :E X I S T S N O D E
    ;

EXP
    :E X P
    ;

EXPAND_GSET_TO_UNION
    :E X P A N D UL_ G S E T UL_ T O UL_ U N I O N
    ;

EXPAND_TABLE
    :E X P A N D UL_ T A B L E
    ;

EXPIRE
    :E X P I R E
    ;

EXPLAIN
    :E X P L A I N
    ;

EXPLOSION
    :E X P L O S I O N
    ;

EXPORT
    :E X P O R T
    ;

EXPR_CORR_CHECK
    :E X P R UL_ C O R R UL_ C H E C K
    ;

EXTENDS
    :E X T E N D S
    ;

EXTENTS
    :E X T E N T S
    ;

EXTRA
    :E X T R A
    ;

EXTRACT
    :E X T R A C T
    ;

EXTRACTVALUE
    :E X T R A C T V A L U E
    ;

FACILITY
    :F A C I L I T Y
    ;

FACTORIZE_JOIN
    :F A C T O R I Z E UL_ J O I N
    ;

FAILED_LOGIN_ATTEMPTS
    :F A I L E D UL_ L O G I N UL_ A T T E M P T S
    ;

FBTSCAN
    :F B T S C A N
    ;

FEATURE_ID
    :F E A T U R E UL_ I D
    ;

FEATURE_SET
    :F E A T U R E UL_ S E T
    ;

FEATURE_VALUE
    :F E A T U R E UL_ V A L U E
    ;

FIRSTM
    :F I R S T M
    ;

FIRST_ROWS
    :F I R S T UL_ R O W S
    ;

FIRST_VALUE
    :F I R S T UL_ V A L U E
    ;

FLAGGER
    :F L A G G E R
    ;

FLOAT
    :F L O A T
    ;

FLOB
    :F L O B
    ;

FLOOR
    :F L O O R
    ;

FOLLOWS
    :F O L L O W S
    ;

FOR
    :F O R
    ;

FORCE_XML_QUERY_REWRITE
    :F O R C E UL_ X M L UL_ Q U E R Y UL_ R E W R I T E
    ;

FOREVER
    :F O R E V E R
    ;

FORWARD
    :F O R W A R D
    ;

FROM
    :F R O M
    ;

FROM_TZ
    :F R O M UL_ T Z
    ;

GATHER_PLAN_STATISTICS
    :G A T H E R UL_ P L A N UL_ S T A T I S T I C S
    ;

GBY_CONC_ROLLUP
    :G B Y UL_ C O N C UL_ R O L L U P
    ;

GBY_PUSHDOWN
    :G B Y UL_ P U S H D O W N
    ;

GRANT
    :G R A N T
    ;

GREATEST
    :G R E A T E S T
    ;

GROUP
    :G R O U P
    ;

GROUPING_ID
    :G R O U P I N G UL_ I D
    ;

GROUP_BY
    :G R O U P UL_ B Y
    ;

GROUP_ID
    :G R O U P UL_ I D
    ;

GUARANTEED
    :G U A R A N T E E D
    ;

HASHKEYS
    :H A S H K E Y S
    ;

HASH_AJ
    :H A S H UL_ A J
    ;

HASH_SJ
    :H A S H UL_ S J
    ;

HAVING
    :H A V I N G
    ;

HEADER
    :H E A D E R
    ;

HELP
    :H E L P
    ;

HEXTORAW
    :H E X T O R A W
    ;

HEXTOREF
    :H E X T O R E F
    ;

HIDE
    :H I D E
    ;

HINTSET_BEGIN
    :H I N T S E T UL_ B E G I N
    ;

HINTSET_END
    :H I N T S E T UL_ E N D
    ;

HOT
    :H O T
    ;

HWM_BROKERED
    :H W M UL_ B R O K E R E D
    ;

HYBRID
    :H Y B R I D
    ;

IDGENERATORS
    :I D G E N E R A T O R S
    ;

IDLE_TIME
    :I D L E UL_ T I M E
    ;

IGNORE_OPTIM_EMBEDDED_HINTS
    :I G N O R E UL_ O P T I M UL_ E M B E D D E D UL_ H I N T S
    ;

IGNORE_ROW_ON_DUPKEY_INDEX
    :I G N O R E UL_ R O W UL_ O N UL_ D U P K E Y UL_ I N D E X
    ;

IGNORE_WHERE_CLAUSE
    :I G N O R E UL_ W H E R E UL_ C L A U S E
    ;

IMPACT
    :I M P A C T
    ;

IMPORT
    :I M P O R T
    ;

IN
    :I N
    ;

INCLUDE_VERSION
    :I N C L U D E UL_ V E R S I O N
    ;

INCR
    :I N C R
    ;

INCREMENTAL
    :I N C R E M E N T A L
    ;

INDENT
    :I N D E N T
    ;

INDEX
    :I N D E X
    ;

INDEXED
    :I N D E X E D
    ;

INDEX_ASC
    :I N D E X UL_ A S C
    ;

INDEX_COMBINE
    :I N D E X UL_ C O M B I N E
    ;

INDEX_DESC
    :I N D E X UL_ D E S C
    ;

INDEX_FFS
    :I N D E X UL_ F F S
    ;

INDEX_FILTER
    :I N D E X UL_ F I L T E R
    ;

INDEX_JOIN
    :I N D E X UL_ J O I N
    ;

INDEX_ROWS
    :I N D E X UL_ R O W S
    ;

INDEX_RRS
    :I N D E X UL_ R R S
    ;

INDEX_RS
    :I N D E X UL_ R S
    ;

INDEX_RS_ASC
    :I N D E X UL_ R S UL_ A S C
    ;

INDEX_RS_DESC
    :I N D E X UL_ R S UL_ D E S C
    ;

INDEX_SCAN
    :I N D E X UL_ S C A N
    ;

INDEX_SKIP_SCAN
    :I N D E X UL_ S K I P UL_ S C A N
    ;

INDEX_SS
    :I N D E X UL_ S S
    ;

INDEX_SS_ASC
    :I N D E X UL_ S S UL_ A S C
    ;

INDEX_SS_DESC
    :I N D E X UL_ S S UL_ D E S C
    ;

INDEX_STATS
    :I N D E X UL_ S T A T S
    ;

INFORMATIONAL
    :I N F O R M A T I O N A L
    ;

INITCAP
    :I N I T C A P
    ;

INLINE
    :I N L I N E
    ;

INLINE_XMLTYPE_NT
    :I N L I N E UL_ X M L T Y P E UL_ N T
    ;

INSERT
    :I N S E R T
    ;

INSERTCHILDXML
    :I N S E R T C H I L D X M L
    ;

INSERTCHILDXMLAFTER
    :I N S E R T C H I L D X M L A F T E R
    ;

INSERTCHILDXMLBEFORE
    :I N S E R T C H I L D X M L B E F O R E
    ;

INSERTXMLAFTER
    :I N S E R T X M L A F T E R
    ;

INSERTXMLBEFORE
    :I N S E R T X M L B E F O R E
    ;

INSTANTIABLE
    :I N S T A N T I A B L E
    ;

INSTANTLY
    :I N S T A N T L Y
    ;

INSTEAD
    :I N S T E A D
    ;

INSTR
    :I N S T R
    ;

INSTR2
    :I N S T R [2]
    ;

INSTR4
    :I N S T R [4]
    ;

INSTRB
    :I N S T R B
    ;

INSTRC
    :I N S T R C
    ;

INTEGER
    :I N T E G E R
    ;

INTERMEDIATE
    :I N T E R M E D I A T E
    ;

INTERNAL_CONVERT
    :I N T E R N A L UL_ C O N V E R T
    ;

INTERNAL_USE
    :I N T E R N A L UL_ U S E
    ;

INTERPRETED
    :I N T E R P R E T E D
    ;

INTO
    :I N T O
    ;

IN_MEMORY_METADATA
    :I N UL_ M E M O R Y UL_ M E T A D A T A
    ;

IN_XQUERY
    :I N UL_ X Q U E R Y
    ;

IS
    :I S
    ;

ISOLATION_LEVEL
    :I S O L A T I O N UL_ L E V E L
    ;

ITERATION_NUMBER
    :I T E R A T I O N UL_ N U M B E R
    ;

INACTIVE_ACCOUNT_TIME
    : I N A C T I V E UL_ A C C O U N T UL_ T I M E
    ;

KERBEROS
    :K E R B E R O S
    ;

KEYSIZE
    :K E Y S I Z E
    ;

KEY_LENGTH
    :K E Y UL_ L E N G T H
    ;

LAST_DAY
    :L A S T UL_ D A Y
    ;

LAST_VALUE
    :L A S T UL_ V A L U E
    ;

LAYER
    :L A Y E R
    ;

LDAP_REGISTRATION
    :L D A P UL_ R E G I S T R A T I O N
    ;

LDAP_REGISTRATION_ENABLED
    :L D A P UL_ R E G I S T R A T I O N UL_ E N A B L E D
    ;

LDAP_REG_SYNC_INTERVAL
    :L D A P UL_ R E G UL_ S Y N C UL_ I N T E R V A L
    ;

LEADING
    :L E A D I N G
    ;

LEAST
    :L E A S T
    ;

LENGTH2
    :L E N G T H [2]
    ;

LENGTH4
    :L E N G T H [4]
    ;

LENGTHB
    :L E N G T H B
    ;

LENGTHC
    :L E N G T H C
    ;

LIFE
    :L I F E
    ;

LIFETIME
    :L I F E T I M E
    ;

LIKE
    :L I K E
    ;

LIKE_EXPAND
    :L I K E UL_ E X P A N D
    ;

LN
    :L N
    ;

LNNVL
    :L N N V L
    ;

LOBNVL
    :L O B N V L
    ;

LOCAL_INDEXES
    :L O C A L UL_ I N D E X E S
    ;

LOGICAL_READS_PER_CALL
    :L O G I C A L UL_ R E A D S UL_ P E R UL_ C A L L
    ;

LOGOFF
    :L O G O F F
    ;

LOGON
    :L O G O N
    ;

LOWER
    :L O W E R
    ;

LTRIM
    :L T R I M
    ;

MAKE_REF
    :M A K E UL_ R E F
    ;

MAXARCHLOGS
    :M A X A R C H L O G S
    ;

MAXTRANS
    :M A X T R A N S
    ;

MEDIAN
    :M E D I A N
    ;

MERGE_AJ
    :M E R G E UL_ A J
    ;

MERGE_CONST_ON
    :M E R G E UL_ C O N S T UL_ O N
    ;

MERGE_SJ
    :M E R G E UL_ S J
    ;

METHOD
    :M E T H O D
    ;

MINIMIZE
    :M I N I M I Z E
    ;

MINUS_NULL
    :M I N U S UL_ N U L L
    ;

MIRRORCOLD
    :M I R R O R C O L D
    ;

MIRRORHOT
    :M I R R O R H O T
    ;

MODEL_COMPILE_SUBQUERY
    :M O D E L UL_ C O M P I L E UL_ S U B Q U E R Y
    ;

MODEL_DONTVERIFY_UNIQUENESS
    :M O D E L UL_ D O N T V E R I F Y UL_ U N I Q U E N E S S
    ;

MODEL_DYNAMIC_SUBQUERY
    :M O D E L UL_ D Y N A M I C UL_ S U B Q U E R Y
    ;

MODEL_MIN_ANALYSIS
    :M O D E L UL_ M I N UL_ A N A L Y S I S
    ;

MODEL_NO_ANALYSIS
    :M O D E L UL_ N O UL_ A N A L Y S I S
    ;

MODEL_PBY
    :M O D E L UL_ P B Y
    ;

MODEL_PUSH_REF
    :M O D E L UL_ P U S H UL_ R E F
    ;

MONITOR
    :M O N I T O R
    ;

MONTHS_BETWEEN
    :M O N T H S UL_ B E T W E E N
    ;

MOUNTPATH
    :M O U N T P A T H
    ;

MULTISET
    :M U L T I S E T
    ;

MV_MERGE
    :M V UL_ M E R G E
    ;

NAMED
    :N A M E D
    ;

NANVL
    :N A N V L
    ;

NATIVE
    :N A T I V E
    ;

NATIVE_FULL_OUTER_JOIN
    :N A T I V E UL_ F U L L UL_ O U T E R UL_ J O I N
    ;

NCHAR_CS
    :N C H A R UL_ C S
    ;

NCHR
    :N C H R
    ;

NEEDED
    :N E E D E D
    ;

NESTED_TABLE_FAST_INSERT
    :N E S T E D UL_ T A B L E UL_ F A S T UL_ I N S E R T
    ;

NESTED_TABLE_GET_REFS
    :N E S T E D UL_ T A B L E UL_ G E T UL_ R E F S
    ;

NESTED_TABLE_ID
    :N E S T E D UL_ T A B L E UL_ I D
    ;

NESTED_TABLE_SET_REFS
    :N E S T E D UL_ T A B L E UL_ S E T UL_ R E F S
    ;

NESTED_TABLE_SET_SETID
    :N E S T E D UL_ T A B L E UL_ S E T UL_ S E T I D
    ;

NETWORK
    :N E T W O R K
    ;

NEVER
    :N E V E R
    ;

NEW_TIME
    :N E W UL_ T I M E
    ;

NEXT_DAY
    :N E X T UL_ D A Y
    ;

NLJ_BATCHING
    :N L J UL_ B A T C H I N G
    ;

NLJ_INDEX_FILTER
    :N L J UL_ I N D E X UL_ F I L T E R
    ;

NLJ_INDEX_SCAN
    :N L J UL_ I N D E X UL_ S C A N
    ;

NLJ_PREFETCH
    :N L J UL_ P R E F E T C H
    ;

NLSSORT
    :N L S S O R T
    ;

NLS_CALENDAR
    :N L S UL_ C A L E N D A R
    ;

NLS_CHARACTERSET
    :N L S UL_ C H A R A C T E R S E T
    ;

NLS_CHARSET_DECL_LEN
    :N L S UL_ C H A R S E T UL_ D E C L UL_ L E N
    ;

NLS_CHARSET_ID
    :N L S UL_ C H A R S E T UL_ I D
    ;

NLS_CHARSET_NAME
    :N L S UL_ C H A R S E T UL_ N A M E
    ;

NLS_COMP
    :N L S UL_ C O M P
    ;

NLS_CURRENCY
    :N L S UL_ C U R R E N C Y
    ;

NLS_DATE_FORMAT
    :N L S UL_ D A T E UL_ F O R M A T
    ;

NLS_DATE_LANGUAGE
    :N L S UL_ D A T E UL_ L A N G U A G E
    ;

NLS_INITCAP
    :N L S UL_ I N I T C A P
    ;

NLS_ISO_CURRENCY
    :N L S UL_ I S O UL_ C U R R E N C Y
    ;

NLS_LANG
    :N L S UL_ L A N G
    ;

NLS_LANGUAGE
    :N L S UL_ L A N G U A G E
    ;

NLS_LENGTH_SEMANTICS
    :N L S UL_ L E N G T H UL_ S E M A N T I C S
    ;

NLS_LOWER
    :N L S UL_ L O W E R
    ;

NLS_NCHAR_CONV_EXCP
    :N L S UL_ N C H A R UL_ C O N V UL_ E X C P
    ;

NLS_NUMERIC_CHARACTERS
    :N L S UL_ N U M E R I C UL_ C H A R A C T E R S
    ;

NLS_SORT
    :N L S UL_ S O R T
    ;

NLS_SPECIAL_CHARS
    :N L S UL_ S P E C I A L UL_ C H A R S
    ;

NLS_TERRITORY
    :N L S UL_ T E R R I T O R Y
    ;

NLS_UPPER
    :N L S UL_ U P P E R
    ;

NL_AJ
    :N L UL_ A J
    ;

NL_SJ
    :N L UL_ S J
    ;

NOAPPEND
    :N O A P P E N D
    ;

NOCPU_COSTING
    :N O C P U UL_ C O S T I N G
    ;

NOENTITYESCAPING
    :N O E N T I T Y E S C A P I N G
    ;

NOGUARANTEE
    :N O G U A R A N T E E
    ;

NOLOCAL
    :N O L O C A L
    ;

NOMINIMIZE
    :N O M I N I M I Z E
    ;

NOOVERRIDE
    :N O O V E R R I D E
    ;

NOPARALLEL_INDEX
    :N O P A R A L L E L UL_ I N D E X
    ;

NOREPAIR
    :N O R E P A I R
    ;

NOREVERSE
    :N O R E V E R S E
    ;

NOREWRITE
    :N O R E W R I T E
    ;

NOSCHEMACHECK
    :N O S C H E M A C H E C K
    ;

NOSEGMENT
    :N O S E G M E N T
    ;

NOSTRICT
    :N O S T R I C T
    ;

NOT
    :N O T
    ;

NO_ACCESS
    :N O UL_ A C C E S S
    ;

NO_BASETABLE_MULTIMV_REWRITE
    :N O UL_ B A S E T A B L E UL_ M U L T I M V UL_ R E W R I T E
    ;

NO_BIND_AWARE
    :N O UL_ B I N D UL_ A W A R E
    ;

NO_BUFFER
    :N O UL_ B U F F E R
    ;

NO_CARTESIAN
    :N O UL_ C A R T E S I A N
    ;

NO_CHECK_ACL_REWRITE
    :N O UL_ C H E C K UL_ A C L UL_ R E W R I T E
    ;

NO_COALESCE_SQ
    :N O UL_ C O A L E S C E UL_ S Q
    ;

NO_CONNECT_BY_CB_WHR_ONLY
    :N O UL_ C O N N E C T UL_ B Y UL_ C B UL_ W H R UL_ O N L Y
    ;

NO_CONNECT_BY_COMBINE_SW
    :N O UL_ C O N N E C T UL_ B Y UL_ C O M B I N E UL_ S W
    ;

NO_CONNECT_BY_COST_BASED
    :N O UL_ C O N N E C T UL_ B Y UL_ C O S T UL_ B A S E D
    ;

NO_CONNECT_BY_ELIM_DUPS
    :N O UL_ C O N N E C T UL_ B Y UL_ E L I M UL_ D U P S
    ;

NO_CONNECT_BY_FILTERING
    :N O UL_ C O N N E C T UL_ B Y UL_ F I L T E R I N G
    ;

NO_COST_XML_QUERY_REWRITE
    :N O UL_ C O S T UL_ X M L UL_ Q U E R Y UL_ R E W R I T E
    ;

NO_CPU_COSTING
    :N O UL_ C P U UL_ C O S T I N G
    ;

NO_DOMAIN_INDEX_FILTER
    :N O UL_ D O M A I N UL_ I N D E X UL_ F I L T E R
    ;

NO_DST_UPGRADE_INSERT_CONV
    :N O UL_ D S T UL_ U P G R A D E UL_ I N S E R T UL_ C O N V
    ;

NO_ELIMINATE_JOIN
    :N O UL_ E L I M I N A T E UL_ J O I N
    ;

NO_ELIMINATE_OBY
    :N O UL_ E L I M I N A T E UL_ O B Y
    ;

NO_ELIMINATE_OUTER_JOIN
    :N O UL_ E L I M I N A T E UL_ O U T E R UL_ J O I N
    ;

NO_EXPAND
    :N O UL_ E X P A N D
    ;

NO_EXPAND_GSET_TO_UNION
    :N O UL_ E X P A N D UL_ G S E T UL_ T O UL_ U N I O N
    ;

NO_EXPAND_TABLE
    :N O UL_ E X P A N D UL_ T A B L E
    ;

NO_FACT
    :N O UL_ F A C T
    ;

NO_FACTORIZE_JOIN
    :N O UL_ F A C T O R I Z E UL_ J O I N
    ;

NO_FILTERING
    :N O UL_ F I L T E R I N G
    ;

NO_GBY_PUSHDOWN
    :N O UL_ G B Y UL_ P U S H D O W N
    ;

NO_INDEX
    :N O UL_ I N D E X
    ;

NO_INDEX_FFS
    :N O UL_ I N D E X UL_ F F S
    ;

NO_INDEX_SS
    :N O UL_ I N D E X UL_ S S
    ;

NO_LOAD
    :N O UL_ L O A D
    ;

NO_MERGE
    :N O UL_ M E R G E
    ;

NO_MODEL_PUSH_REF
    :N O UL_ M O D E L UL_ P U S H UL_ R E F
    ;

NO_MONITOR
    :N O UL_ M O N I T O R
    ;

NO_MONITORING
    :N O UL_ M O N I T O R I N G
    ;

NO_MULTIMV_REWRITE
    :N O UL_ M U L T I M V UL_ R E W R I T E
    ;

NO_NATIVE_FULL_OUTER_JOIN
    :N O UL_ N A T I V E UL_ F U L L UL_ O U T E R UL_ J O I N
    ;

NO_NLJ_BATCHING
    :N O UL_ N L J UL_ B A T C H I N G
    ;

NO_NLJ_PREFETCH
    :N O UL_ N L J UL_ P R E F E T C H
    ;

NO_ORDER_ROLLUPS
    :N O UL_ O R D E R UL_ R O L L U P S
    ;

NO_OUTER_JOIN_TO_INNER
    :N O UL_ O U T E R UL_ J O I N UL_ T O UL_ I N N E R
    ;

NO_PARALLEL
    :N O UL_ P A R A L L E L
    ;

NO_PARALLEL_INDEX
    :N O UL_ P A R A L L E L UL_ I N D E X
    ;

NO_PARTIAL_COMMIT
    :N O UL_ P A R T I A L UL_ C O M M I T
    ;

NO_PLACE_DISTINCT
    :N O UL_ P L A C E UL_ D I S T I N C T
    ;

NO_PLACE_GROUP_BY
    :N O UL_ P L A C E UL_ G R O U P UL_ B Y
    ;

NO_PQ_MAP
    :N O UL_ P Q UL_ M A P
    ;

NO_PRUNE_GSETS
    :N O UL_ P R U N E UL_ G S E T S
    ;

NO_PULL_PRED
    :N O UL_ P U L L UL_ P R E D
    ;

NO_PUSH_PRED
    :N O UL_ P U S H UL_ P R E D
    ;

NO_PUSH_SUBQ
    :N O UL_ P U S H UL_ S U B Q
    ;

NO_PX_JOIN_FILTER
    :N O UL_ P X UL_ J O I N UL_ F I L T E R
    ;

NO_QKN_BUFF
    :N O UL_ Q K N UL_ B U F F
    ;

NO_QUERY_TRANSFORMATION
    :N O UL_ Q U E R Y UL_ T R A N S F O R M A T I O N
    ;

NO_REF_CASCADE
    :N O UL_ R E F UL_ C A S C A D E
    ;

NO_RESULT_CACHE
    :N O UL_ R E S U L T UL_ C A C H E
    ;

NO_REWRITE
    :N O UL_ R E W R I T E
    ;

NO_SEMIJOIN
    :N O UL_ S E M I J O I N
    ;

NO_SET_TO_JOIN
    :N O UL_ S E T UL_ T O UL_ J O I N
    ;

NO_SQL_TUNE
    :N O UL_ S Q L UL_ T U N E
    ;

NO_STAR_TRANSFORMATION
    :N O UL_ S T A R UL_ T R A N S F O R M A T I O N
    ;

NO_STATEMENT_QUEUING
    :N O UL_ S T A T E M E N T UL_ Q U E U I N G
    ;

NO_STATS_GSETS
    :N O UL_ S T A T S UL_ G S E T S
    ;

NO_SUBQUERY_PRUNING
    :N O UL_ S U B Q U E R Y UL_ P R U N I N G
    ;

NO_SUBSTRB_PAD
    :N O UL_ S U B S T R B UL_ P A D
    ;

NO_SWAP_JOIN_INPUTS
    :N O UL_ S W A P UL_ J O I N UL_ I N P U T S
    ;

NO_TEMP_TABLE
    :N O UL_ T E M P UL_ T A B L E
    ;

NO_TRANSFORM_DISTINCT_AGG
    :N O UL_ T R A N S F O R M UL_ D I S T I N C T UL_ A G G
    ;

NO_UNNEST
    :N O UL_ U N N E S T
    ;

NO_USE_HASH
    :N O UL_ U S E UL_ H A S H
    ;

NO_USE_HASH_AGGREGATION
    :N O UL_ U S E UL_ H A S H UL_ A G G R E G A T I O N
    ;

NO_USE_INVISIBLE_INDEXES
    :N O UL_ U S E UL_ I N V I S I B L E UL_ I N D E X E S
    ;

NO_USE_MERGE
    :N O UL_ U S E UL_ M E R G E
    ;

NO_USE_NL
    :N O UL_ U S E UL_ N L
    ;

NO_XMLINDEX_REWRITE
    :N O UL_ X M L I N D E X UL_ R E W R I T E
    ;

NO_XMLINDEX_REWRITE_IN_SELECT
    :N O UL_ X M L I N D E X UL_ R E W R I T E UL_ I N UL_ S E L E C T
    ;

NO_XML_DML_REWRITE
    :N O UL_ X M L UL_ D M L UL_ R E W R I T E
    ;

NO_XML_QUERY_REWRITE
    :N O UL_ X M L UL_ Q U E R Y UL_ R E W R I T E
    ;

NTH_VALUE
    :N T H UL_ V A L U E
    ;

NTILE
    :N T I L E
    ;

NULL
    :N U L L
    ;

NULLIF
    :N U L L I F
    ;

NUMTODSINTERVAL
    :N U M T O D S I N T E R V A L
    ;

NUMTOYMINTERVAL
    :N U M T O Y M I N T E R V A L
    ;

NUM_INDEX_KEYS
    :N U M UL_ I N D E X UL_ K E Y S
    ;

NVL
    :N V L
    ;

NVL2
    :N V L [2]
    ;

OBJECTTOXML
    :O B J E C T T O X M L
    ;

OBJNO
    :O B J N O
    ;

OBJNO_REUSE
    :O B J N O UL_ R E U S E
    ;

OCCURENCES
    :O C C U R E N C E S
    ;

OID
    :O I D
    ;

OLAP
    :O L A P
    ;

OLD
    :O L D
    ;

OLD_PUSH_PRED
    :O L D UL_ P U S H UL_ P R E D
    ;

OLTP
    :O L T P
    ;

ON
    :O N
    ;

OPAQUE
    :O P A Q U E
    ;

OPAQUE_TRANSFORM
    :O P A Q U E UL_ T R A N S F O R M
    ;

OPAQUE_XCANONICAL
    :O P A Q U E UL_ X C A N O N I C A L
    ;

OPCODE
    :O P C O D E
    ;

OPERATIONS
    :O P E R A T I O N S
    ;

OPTIMIZER_FEATURES_ENABLE
    :O P T I M I Z E R UL_ F E A T U R E S UL_ E N A B L E
    ;

OPTIMIZER_GOAL
    :O P T I M I Z E R UL_ G O A L
    ;

OPT_ESTIMATE
    :O P T UL_ E S T I M A T E
    ;

OPT_PARAM
    :O P T UL_ P A R A M
    ;

OR
    :O R
    ;

ORADEBUG
    :O R A D E B U G
    ;

ORA_BRANCH
    :O R A UL_ B R A N C H
    ;

ORA_CHECKACL
    :O R A UL_ C H E C K A C L
    ;

ORA_DST_AFFECTED
    :O R A UL_ D S T UL_ A F F E C T E D
    ;

ORA_DST_CONVERT
    :O R A UL_ D S T UL_ C O N V E R T
    ;

ORA_DST_ERROR
    :O R A UL_ D S T UL_ E R R O R
    ;

ORA_GET_ACLIDS
    :O R A UL_ G E T UL_ A C L I D S
    ;

ORA_GET_PRIVILEGES
    :O R A UL_ G E T UL_ P R I V I L E G E S
    ;

ORA_HASH
    :O R A UL_ H A S H
    ;

ORA_ROWSCN
    :O R A UL_ R O W S C N
    ;

ORA_ROWSCN_RAW
    :O R A UL_ R O W S C N UL_ R A W
    ;

ORA_ROWVERSION
    :O R A UL_ R O W V E R S I O N
    ;

ORA_TABVERSION
    :O R A UL_ T A B V E R S I O N
    ;

ORDERED
    :O R D E R E D
    ;

ORDERED_PREDICATES
    :O R D E R E D UL_ P R E D I C A T E S
    ;

ORDINALITY
    :O R D I N A L I T Y
    ;

OR_EXPAND
    :O R UL_ E X P A N D
    ;

OR_PREDICATES
    :O R UL_ P R E D I C A T E S
    ;

OTHER
    :O T H E R
    ;

OUTER_JOIN_TO_INNER
    :O U T E R UL_ J O I N UL_ T O UL_ I N N E R
    ;

OUTLINE_LEAF
    :O U T L I N E UL_ L E A F
    ;

OUT_OF_LINE
    :O U T UL_ O F UL_ L I N E
    ;

OVERFLOW_NOMOVE
    :O V E R F L O W UL_ N O M O V E
    ;

OVERLAPS
    :O V E R L A P S
    ;

OWN
    :O W N
    ;

OWNER
    :O W N E R
    ;

OWNERSHIP
    :O W N E R S H I P
    ;

PARALLEL_INDEX
    :P A R A L L E L UL_ I N D E X
    ;

PARAM
    :P A R A M
    ;

PARITY
    :P A R I T Y
    ;

PARTIAL
    : P A R T I A L
    ;

PARTIALLY
    :P A R T I A L L Y
    ;

PARTITION_HASH
    :P A R T I T I O N UL_ H A S H
    ;

PARTITION_LIST
    :P A R T I T I O N UL_ L I S T
    ;

PARTITION_RANGE
    :P A R T I T I O N UL_ R A N G E
    ;

PASSWORD
    :P A S S W O R D
    ;

PASSWORD_GRACE_TIME
    :P A S S W O R D UL_ G R A C E UL_ T I M E
    ;

PASSWORD_LIFE_TIME
    :P A S S W O R D UL_ L I F E UL_ T I M E
    ;

PASSWORD_LOCK_TIME
    :P A S S W O R D UL_ L O C K UL_ T I M E
    ;

PASSWORD_REUSE_MAX
    :P A S S W O R D UL_ R E U S E UL_ M A X
    ;

PASSWORD_REUSE_TIME
    :P A S S W O R D UL_ R E U S E UL_ T I M E
    ;

PASSWORD_VERIFY_FUNCTION
    :P A S S W O R D UL_ V E R I F Y UL_ F U N C T I O N
    ;

PASSWORD_ROLLOVER_TIME
    : P A S S W O R D UL_ R O L L O V E R UL_ T I M E
    ;

PATH
    :P A T H
    ;

PATHS
    :P A T H S
    ;

PBL_HS_BEGIN
    :P B L UL_ H S UL_ B E G I N
    ;

PBL_HS_END
    :P B L UL_ H S UL_ E N D
    ;

PENDING
    :P E N D I N G
    ;

PERCENTILE_CONT
    :P E R C E N T I L E UL_ C O N T
    ;

PERCENTILE_DISC
    :P E R C E N T I L E UL_ D I S C
    ;

PERCENT_RANKM
    :P E R C E N T UL_ R A N K M
    ;

PERMANENT
    :P E R M A N E N T
    ;

PERMISSION
    :P E R M I S S I O N
    ;

PIKEY
    :P I K E Y
    ;

PIV_GB
    :P I V UL_ G B
    ;

PIV_SSF
    :P I V UL_ S S F
    ;

PLACE_DISTINCT
    :P L A C E UL_ D I S T I N C T
    ;

PLACE_GROUP_BY
    :P L A C E UL_ G R O U P UL_ B Y
    ;

PLAN
    :P L A N
    ;

PLSCOPE_SETTINGS
    :P L S C O P E UL_ S E T T I N G S
    ;

PLSQL_CCFLAGS
    :P L S Q L UL_ C C F L A G S
    ;

PLSQL_CODE_TYPE
    :P L S Q L UL_ C O D E UL_ T Y P E
    ;

PLSQL_DEBUG
    :P L S Q L UL_ D E B U G
    ;

PLSQL_OPTIMIZE_LEVEL
    :P L S Q L UL_ O P T I M I Z E UL_ L E V E L
    ;

PLSQL_WARNINGS
    :P L S Q L UL_ W A R N I N G S
    ;

POWER
    :P O W E R
    ;

POWERMULTISET
    :P O W E R M U L T I S E T
    ;

POWERMULTISET_BY_CARDINALITY
    :P O W E R M U L T I S E T UL_ B Y UL_ C A R D I N A L I T Y
    ;

PQ_DISTRIBUTE
    :P Q UL_ D I S T R I B U T E
    ;

PQ_MAP
    :P Q UL_ M A P
    ;

PQ_NOMAP
    :P Q UL_ N O M A P
    ;

PREBUILT
    :P R E B U I L T
    ;

PRECEDES
    :P R E C E D E S
    ;

PRECOMPUTE_SUBQUERY
    :P R E C O M P U T E UL_ S U B Q U E R Y
    ;

PREDICATE_REORDERS
    :P R E D I C A T E UL_ R E O R D E R S
    ;

PREDICTION
    :P R E D I C T I O N
    ;

PREDICTION_BOUNDS
    :P R E D I C T I O N UL_ B O U N D S
    ;

PREDICTION_COST
    :P R E D I C T I O N UL_ C O S T
    ;

PREDICTION_DETAILS
    :P R E D I C T I O N UL_ D E T A I L S
    ;

PREDICTION_PROBABILITY
    :P R E D I C T I O N UL_ P R O B A B I L I T Y
    ;

PREDICTION_SET
    :P R E D I C T I O N UL_ S E T
    ;

PRESENTNNV
    :P R E S E N T N N V
    ;

PRESENTV
    :P R E S E N T V
    ;

PRESERVE_OID
    :P R E S E R V E UL_ O I D
    ;

PREVIOUS
    :P R E V I O U S
    ;

PROJECT
    :P R O J E C T
    ;

PROPAGATE
    :P R O P A G A T E
    ;

PROTECTED
    :P R O T E C T E D
    ;

PULL_PRED
    :P U L L UL_ P R E D
    ;

PUSH_PRED
    :P U S H UL_ P R E D
    ;

PUSH_SUBQ
    :P U S H UL_ S U B Q
    ;

PX_GRANULE
    :P X UL_ G R A N U L E
    ;

PX_JOIN_FILTER
    :P X UL_ J O I N UL_ F I L T E R
    ;

QB_NAME
    :Q B UL_ N A M E
    ;

QUERY_BLOCK
    :Q U E R Y UL_ B L O C K
    ;

QUEUE
    :Q U E U E
    ;

QUEUE_CURR
    :Q U E U E UL_ C U R R
    ;

QUEUE_ROWP
    :Q U E U E UL_ R O W P
    ;

RANDOM
    :R A N D O M
    ;

RANDOM_LOCAL
    :R A N D O M UL_ L O C A L
    ;

RANKM
    :R A N K M
    ;

RAPIDLY
    :R A P I D L Y
    ;

RATIO_TO_REPORT
    :R A T I O UL_ T O UL_ R E P O R T
    ;

RAWTOHEX
    :R A W T O H E X
    ;

RAWTONHEX
    :R A W T O N H E X
    ;

RBA
    :R B A
    ;

RBO_OUTLINE
    :R B O UL_ O U T L I N E
    ;

RDBA
    :R D B A
    ;

RECORDS_PER_BLOCK
    :R E C O R D S UL_ P E R UL_ B L O C K
    ;

RECOVERABLE
    :R E C O V E R A B L E
    ;

REDUCED
    :R E D U C E D
    ;

REFERENCED
    :R E F E R E N C E D
    ;

REFERENCING
    :R E F E R E N C I N G
    ;

REFTOHEX
    :R E F T O H E X
    ;

REF_CASCADE_CURSOR
    :R E F UL_ C A S C A D E UL_ C U R S O R
    ;

REGEXP_COUNT
    :R E G E X P UL_ C O U N T
    ;

REGEXP_INSTR
    :R E G E X P UL_ I N S T R
    ;

REGEXP_REPLACE
    :R E G E X P UL_ R E P L A C E
    ;

REGEXP_SUBSTR
    :R E G E X P UL_ S U B S T R
    ;

REGR_AVGX
    :R E G R UL_ A V G X
    ;

REGR_AVGY
    :R E G R UL_ A V G Y
    ;

REGR_COUNT
    :R E G R UL_ C O U N T
    ;

REGR_INTERCEPT
    :R E G R UL_ I N T E R C E P T
    ;

REGR_R2
    :R E G R UL_ R [2]
    ;

REGR_SLOPE
    :R E G R UL_ S L O P E
    ;

REGR_SXX
    :R E G R UL_ S X X
    ;

REGR_SXY
    :R E G R UL_ S X Y
    ;

REGR_SYY
    :R E G R UL_ S Y Y
    ;

REMAINDER
    :R E M A I N D E R
    ;

REMOTE_MAPPED
    :R E M O T E UL_ M A P P E D
    ;

REPAIR
    :R E P A I R
    ;

REQUIRED
    :R E Q U I R E D
    ;

RESPECT
    :R E S P E C T
    ;

RESTORE_AS_INTERVALS
    :R E S T O R E UL_ A S UL_ I N T E R V A L S
    ;

RESTRICT_ALL_REF_CONS
    :R E S T R I C T UL_ A L L UL_ R E F UL_ C O N S
    ;

RETRY_ON_ROW_CHANGE
    :R E T R Y UL_ O N UL_ R O W UL_ C H A N G E
    ;

REVOKE
    :R E V O K E
    ;

REWRITE_OR_ERROR
    :R E W R I T E UL_ O R UL_ E R R O R
    ;

ROUND
    :R O U N D
    ;

ROWIDTOCHAR
    :R O W I D T O C H A R
    ;

ROWIDTONCHAR
    :R O W I D T O N C H A R
    ;

ROWNUM
    :R O W N U M
    ;

ROW_LENGTH
    :R O W UL_ L E N G T H
    ;

RPAD
    :R P A D
    ;

RTRIM
    :R T R I M
    ;

RULE
    :R U L E
    ;

SAVE_AS_INTERVALS
    :S A V E UL_ A S UL_ I N T E R V A L S
    ;

SB4
    :S B [4]
    ;

SCALE_ROWS
    :S C A L E UL_ R O W S
    ;

SCAN_INSTANCES
    :S C A N UL_ I N S T A N C E S
    ;

SCHEMACHECK
    :S C H E M A C H E C K
    ;

SCN_ASCENDING
    :S C N UL_ A S C E N D I N G
    ;

SD_ALL
    :S D UL_ A L L
    ;

SD_INHIBIT
    :S D UL_ I N H I B I T
    ;

SD_SHOW
    :S D UL_ S H O W
    ;

SECUREFILE_DBA
    :S E C U R E F I L E UL_ D B A
    ;

SECURITY
    :S E C U R I T Y
    ;

SEG_BLOCK
    :S E G UL_ B L O C K
    ;

SEG_FILE
    :S E G UL_ F I L E
    ;

SELECT
    :S E L E C T
    ;

SEMIJOIN
    :S E M I J O I N
    ;

SEMIJOIN_DRIVER
    :S E M I J O I N UL_ D R I V E R
    ;

SEQUENCED
    :S E Q U E N C E D
    ;

SERVERERROR
    :S E R V E R E R R O R
    ;

SESSIONS_PER_USER
    :S E S S I O N S UL_ P E R UL_ U S E R
    ;

SESSIONTZNAME
    :S E S S I O N T Z N A M E
    ;

SESSION_CACHED_CURSORS
    :S E S S I O N UL_ C A C H E D UL_ C U R S O R S
    ;

SET
    :S E T
    ;

SET_TO_JOIN
    :S E T UL_ T O UL_ J O I N
    ;

SEVERE
    :S E V E R E
    ;

SHOW
    :S H O W
    ;

SIGN
    :S I G N
    ;

SIGNAL_COMPONENT
    :S I G N A L UL_ C O M P O N E N T
    ;

SIGNAL_FUNCTION
    :S I G N A L UL_ F U N C T I O N
    ;

SIMPLE
    :S I M P L E
    ;

SIN
    :S I N
    ;

SINGLETASK
    :S I N G L E T A S K
    ;

SINH
    :S I N H
    ;

SKIP_EXT_OPTIMIZER
    :S K I P UL_ E X T UL_ O P T I M I Z E R
    ;

SKIP_UNQ_UNUSABLE_IDX
    :S K I P UL_ U N Q UL_ U N U S A B L E UL_ I D X
    ;

SKIP_UNUSABLE_INDEXES
    :S K I P UL_ U N U S A B L E UL_ I N D E X E S
    ;

SMALLINT
    :S M A L L I N T
    ;

SOUNDEX
    :S O U N D E X
    ;

SPLIT
    :S P L I T
    ;

SPREADSHEET
    :S P R E A D S H E E T
    ;

SQLLDR
    :S Q L L D R
    ;

SQL_TRACE
    :S Q L UL_ T R A C E
    ;

SQRT
    :S Q R T
    ;

STALE
    :S T A L E
    ;

STANDALONE
    :S T A N D A L O N E
    ;

STANDBY_MAX_DATA_DELAY
    :S T A N D B Y UL_ M A X UL_ D A T A UL_ D E L A Y
    ;

STAR
    :S T A R
    ;

STARTUP
    :S T A R T U P
    ;

STAR_TRANSFORMATION
    :S T A R UL_ T R A N S F O R M A T I O N
    ;

STATEMENTS
    :S T A T E M E N T S
    ;

STATEMENT_ID
    :S T A T E M E N T UL_ I D
    ;

STATEMENT_QUEUING
    :S T A T E M E N T UL_ Q U E U I N G
    ;

PRAGMA
    : P R A G M A
    ;

RESTRICT_REFERENCES
    : R E S T R I C T UL_ R E F E R E N C E S
    ;

RNDS
    : R N D S
    ;

WNDS
    : W N D S
    ;

RNPS
    : R N P S
    ;

WNPS
    : W N P S
    ;

OVERRIDING
    : O V E R R I D I N G
    ;

STATIC
    :S T A T I C
    ;

STATS_BINOMIAL_TEST
    :S T A T S UL_ B I N O M I A L UL_ T E S T
    ;

STATS_CROSSTAB
    :S T A T S UL_ C R O S S T A B
    ;

STATS_F_TEST
    :S T A T S UL_ F UL_ T E S T
    ;

STATS_KS_TEST
    :S T A T S UL_ K S UL_ T E S T
    ;

STATS_MODE
    :S T A T S UL_ M O D E
    ;

STATS_MW_TEST
    :S T A T S UL_ M W UL_ T E S T
    ;

STATS_ONE_WAY_ANOVA
    :S T A T S UL_ O N E UL_ W A Y UL_ A N O V A
    ;

STATS_T_TEST_INDEP
    :S T A T S UL_ T UL_ T E S T UL_ I N D E P
    ;

STATS_T_TEST_INDEPU
    :S T A T S UL_ T UL_ T E S T UL_ I N D E P U
    ;

STATS_T_TEST_ONE
    :S T A T S UL_ T UL_ T E S T UL_ O N E
    ;

STATS_T_TEST_PAIRED
    :S T A T S UL_ T UL_ T E S T UL_ P A I R E D
    ;

STATS_WSR_TEST
    :S T A T S UL_ W S R UL_ T E S T
    ;

STDDEV
    :S T D D E V
    ;

STDDEV_POP
    :S T D D E V UL_ P O P
    ;

STDDEV_SAMP
    :S T D D E V UL_ S A M P
    ;

STREAMS
    :S T R E A M S
    ;

STRIP
    :S T R I P
    ;

STRIPE_COLUMNS
    :S T R I P E UL_ C O L U M N S
    ;

STRIPE_WIDTH
    :S T R I P E UL_ W I D T H
    ;

SUBPARTITION_REL
    :S U B P A R T I T I O N UL_ R E L
    ;

SUBQUERIES
    :S U B Q U E R I E S
    ;

SUBQUERY_PRUNING
    :S U B Q U E R Y UL_ P R U N I N G
    ;

SUBSTR
    :S U B S T R
    ;

SUBSTR2
    :S U B S T R [2]
    ;

SUBSTR4
    :S U B S T R [4]
    ;

SUBSTRB
    :S U B S T R B
    ;

SUBSTRC
    :S U B S T R C
    ;

SUMMARY
    :S U M M A R Y
    ;

SWAP_JOIN_INPUTS
    :S W A P UL_ J O I N UL_ I N P U T S
    ;

SYSASM
    :S Y S A S M
    ;

SYSDATE
    :S Y S D A T E
    ;

SYSTEM_DEFINED
    :S Y S T E M UL_ D E F I N E D
    ;

SYS_AUDIT
    :S Y S UL_ A U D I T
    ;

SYS_CHECKACL
    :S Y S UL_ C H E C K A C L
    ;

SYS_CONNECT_BY_PATH
    :S Y S UL_ C O N N E C T UL_ B Y UL_ P A T H
    ;

SYS_CONTEXT
    :S Y S UL_ C O N T E X T
    ;

SYS_DBURIGEN
    :S Y S UL_ D B U R I G E N
    ;

SYS_DL_CURSOR
    :S Y S UL_ D L UL_ C U R S O R
    ;

SYS_DM_RXFORM_CHR
    :S Y S UL_ D M UL_ R X F O R M UL_ C H R
    ;

SYS_DM_RXFORM_NUM
    :S Y S UL_ D M UL_ R X F O R M UL_ N U M
    ;

SYS_DOM_COMPARE
    :S Y S UL_ D O M UL_ C O M P A R E
    ;

SYS_DST_PRIM2SEC
    :S Y S UL_ D S T UL_ P R I M [2] S E C
    ;

SYS_DST_SEC2PRIM
    :S Y S UL_ D S T UL_ S E C [2] P R I M
    ;

SYS_ET_BFILE_TO_RAW
    :S Y S UL_ E T UL_ B F I L E UL_ T O UL_ R A W
    ;

SYS_ET_BLOB_TO_IMAGE
    :S Y S UL_ E T UL_ B L O B UL_ T O UL_ I M A G E
    ;

SYS_ET_IMAGE_TO_BLOB
    :S Y S UL_ E T UL_ I M A G E UL_ T O UL_ B L O B
    ;

SYS_ET_RAW_TO_BFILE
    :S Y S UL_ E T UL_ R A W UL_ T O UL_ B F I L E
    ;

SYS_EXTPDTXT
    :S Y S UL_ E X T P D T X T
    ;

SYS_EXTRACT_UTC
    :S Y S UL_ E X T R A C T UL_ U T C
    ;

SYS_FBT_INSDEL
    :S Y S UL_ F B T UL_ I N S D E L
    ;

SYS_FILTER_ACLS
    :S Y S UL_ F I L T E R UL_ A C L S
    ;

SYS_GETTOKENID
    :S Y S UL_ G E T T O K E N I D
    ;

SYS_GET_ACLIDS
    :S Y S UL_ G E T UL_ A C L I D S
    ;

SYS_GET_PRIVILEGES
    :S Y S UL_ G E T UL_ P R I V I L E G E S
    ;

SYS_GUID
    :S Y S UL_ G U I D
    ;

SYS_MAKEXML
    :S Y S UL_ M A K E X M L
    ;

SYS_MAKE_XMLNODEID
    :S Y S UL_ M A K E UL_ X M L N O D E I D
    ;

SYS_MKXMLATTR
    :S Y S UL_ M K X M L A T T R
    ;

SYS_OPTLOBPRBSC
    :S Y S UL_ O P T L O B P R B S C
    ;

SYS_OPTXICMP
    :S Y S UL_ O P T X I C M P
    ;

SYS_OPTXQCASTASNQ
    :S Y S UL_ O P T X Q C A S T A S N Q
    ;

SYS_OP_ADT2BIN
    :S Y S UL_ O P UL_ A D T [2] B I N
    ;

SYS_OP_ADTCONS
    :S Y S UL_ O P UL_ A D T C O N S
    ;

SYS_OP_ALSCRVAL
    :S Y S UL_ O P UL_ A L S C R V A L
    ;

SYS_OP_ATG
    :S Y S UL_ O P UL_ A T G
    ;

SYS_OP_BIN2ADT
    :S Y S UL_ O P UL_ B I N [2] A D T
    ;

SYS_OP_BITVEC
    :S Y S UL_ O P UL_ B I T V E C
    ;

SYS_OP_BL2R
    :S Y S UL_ O P UL_ B L [2] R
    ;

SYS_OP_BLOOM_FILTER
    :S Y S UL_ O P UL_ B L O O M UL_ F I L T E R
    ;

SYS_OP_BLOOM_FILTER_LIST
    :S Y S UL_ O P UL_ B L O O M UL_ F I L T E R UL_ L I S T
    ;

SYS_OP_C2C
    :S Y S UL_ O P UL_ C [2] C
    ;

SYS_OP_CAST
    :S Y S UL_ O P UL_ C A S T
    ;

SYS_OP_CEG
    :S Y S UL_ O P UL_ C E G
    ;

SYS_OP_CL2C
    :S Y S UL_ O P UL_ C L [2] C
    ;

SYS_OP_COMBINED_HASH
    :S Y S UL_ O P UL_ C O M B I N E D UL_ H A S H
    ;

SYS_OP_COMP
    :S Y S UL_ O P UL_ C O M P
    ;

SYS_OP_CONVERT
    :S Y S UL_ O P UL_ C O N V E R T
    ;

SYS_OP_COUNTCHG
    :S Y S UL_ O P UL_ C O U N T C H G
    ;

SYS_OP_CSCONV
    :S Y S UL_ O P UL_ C S C O N V
    ;

SYS_OP_CSCONVTEST
    :S Y S UL_ O P UL_ C S C O N V T E S T
    ;

SYS_OP_CSR
    :S Y S UL_ O P UL_ C S R
    ;

SYS_OP_CSX_PATCH
    :S Y S UL_ O P UL_ C S X UL_ P A T C H
    ;

SYS_OP_DECOMP
    :S Y S UL_ O P UL_ D E C O M P
    ;

SYS_OP_DESCEND
    :S Y S UL_ O P UL_ D E S C E N D
    ;

SYS_OP_DISTINCT
    :S Y S UL_ O P UL_ D I S T I N C T
    ;

SYS_OP_DRA
    :S Y S UL_ O P UL_ D R A
    ;

SYS_OP_DUMP
    :S Y S UL_ O P UL_ D U M P
    ;

SYS_OP_EXTRACT
    :S Y S UL_ O P UL_ E X T R A C T
    ;

SYS_OP_GROUPING
    :S Y S UL_ O P UL_ G R O U P I N G
    ;

SYS_OP_GUID
    :S Y S UL_ O P UL_ G U I D
    ;

SYS_OP_IIX
    :S Y S UL_ O P UL_ I I X
    ;

SYS_OP_ITR
    :S Y S UL_ O P UL_ I T R
    ;

SYS_OP_LBID
    :S Y S UL_ O P UL_ L B I D
    ;

SYS_OP_LOBLOC2BLOB
    :S Y S UL_ O P UL_ L O B L O C [2] B L O B
    ;

SYS_OP_LOBLOC2CLOB
    :S Y S UL_ O P UL_ L O B L O C [2] C L O B
    ;

SYS_OP_LOBLOC2ID
    :S Y S UL_ O P UL_ L O B L O C [2] I D
    ;

SYS_OP_LOBLOC2NCLOB
    :S Y S UL_ O P UL_ L O B L O C [2] N C L O B
    ;

SYS_OP_LOBLOC2TYP
    :S Y S UL_ O P UL_ L O B L O C [2] T Y P
    ;

SYS_OP_LSVI
    :S Y S UL_ O P UL_ L S V I
    ;

SYS_OP_LVL
    :S Y S UL_ O P UL_ L V L
    ;

SYS_OP_MAKEOID
    :S Y S UL_ O P UL_ M A K E O I D
    ;

SYS_OP_MAP_NONNULL
    :S Y S UL_ O P UL_ M A P UL_ N O N N U L L
    ;

SYS_OP_MSR
    :S Y S UL_ O P UL_ M S R
    ;

SYS_OP_NICOMBINE
    :S Y S UL_ O P UL_ N I C O M B I N E
    ;

SYS_OP_NIEXTRACT
    :S Y S UL_ O P UL_ N I E X T R A C T
    ;

SYS_OP_NII
    :S Y S UL_ O P UL_ N I I
    ;

SYS_OP_NIX
    :S Y S UL_ O P UL_ N I X
    ;

SYS_OP_NOEXPAND
    :S Y S UL_ O P UL_ N O E X P A N D
    ;

SYS_OP_NUMTORAW
    :S Y S UL_ O P UL_ N U M T O R A W
    ;

SYS_OP_OIDVALUE
    :S Y S UL_ O P UL_ O I D V A L U E
    ;

SYS_OP_OPNSIZE
    :S Y S UL_ O P UL_ O P N S I Z E
    ;

SYS_OP_PAR
    :S Y S UL_ O P UL_ P A R
    ;

SYS_OP_PARGID
    :S Y S UL_ O P UL_ P A R G I D
    ;

SYS_OP_PARGID_1
    :S Y S UL_ O P UL_ P A R G I D UL_ [1]
    ;

SYS_OP_PAR_1
    :S Y S UL_ O P UL_ P A R UL_ [1]
    ;

SYS_OP_PIVOT
    :S Y S UL_ O P UL_ P I V O T
    ;

SYS_OP_R2O
    :S Y S UL_ O P UL_ R [2] O
    ;

SYS_OP_RAWTONUM
    :S Y S UL_ O P UL_ R A W T O N U M
    ;

SYS_OP_RDTM
    :S Y S UL_ O P UL_ R D T M
    ;

SYS_OP_REF
    :S Y S UL_ O P UL_ R E F
    ;

SYS_OP_RMTD
    :S Y S UL_ O P UL_ R M T D
    ;

SYS_OP_ROWIDTOOBJ
    :S Y S UL_ O P UL_ R O W I D T O O B J
    ;

SYS_OP_RPB
    :S Y S UL_ O P UL_ R P B
    ;

SYS_OP_TOSETID
    :S Y S UL_ O P UL_ T O S E T I D
    ;

SYS_OP_TPR
    :S Y S UL_ O P UL_ T P R
    ;

SYS_OP_TRTB
    :S Y S UL_ O P UL_ T R T B
    ;

SYS_OP_UNDESCEND
    :S Y S UL_ O P UL_ U N D E S C E N D
    ;

SYS_OP_VECAND
    :S Y S UL_ O P UL_ V E C A N D
    ;

SYS_OP_VECBIT
    :S Y S UL_ O P UL_ V E C B I T
    ;

SYS_OP_VECOR
    :S Y S UL_ O P UL_ V E C O R
    ;

SYS_OP_VECXOR
    :S Y S UL_ O P UL_ V E C X O R
    ;

SYS_OP_VERSION
    :S Y S UL_ O P UL_ V E R S I O N
    ;

SYS_OP_VREF
    :S Y S UL_ O P UL_ V R E F
    ;

SYS_OP_VVD
    :S Y S UL_ O P UL_ V V D
    ;

SYS_OP_XPTHATG
    :S Y S UL_ O P UL_ X P T H A T G
    ;

SYS_OP_XPTHIDX
    :S Y S UL_ O P UL_ X P T H I D X
    ;

SYS_OP_XPTHOP
    :S Y S UL_ O P UL_ X P T H O P
    ;

SYS_OP_XTXT2SQLT
    :S Y S UL_ O P UL_ X T X T [2] S Q L T
    ;

SYS_ORDERKEY_DEPTH
    :S Y S UL_ O R D E R K E Y UL_ D E P T H
    ;

SYS_ORDERKEY_MAXCHILD
    :S Y S UL_ O R D E R K E Y UL_ M A X C H I L D
    ;

SYS_ORDERKEY_PARENT
    :S Y S UL_ O R D E R K E Y UL_ P A R E N T
    ;

SYS_PARALLEL_TXN
    :S Y S UL_ P A R A L L E L UL_ T X N
    ;

SYS_PATHID_IS_ATTR
    :S Y S UL_ P A T H I D UL_ I S UL_ A T T R
    ;

SYS_PATHID_IS_NMSPC
    :S Y S UL_ P A T H I D UL_ I S UL_ N M S P C
    ;

SYS_PATHID_LASTNAME
    :S Y S UL_ P A T H I D UL_ L A S T N A M E
    ;

SYS_PATHID_LASTNMSPC
    :S Y S UL_ P A T H I D UL_ L A S T N M S P C
    ;

SYS_PATH_REVERSE
    :S Y S UL_ P A T H UL_ R E V E R S E
    ;

SYS_PXQEXTRACT
    :S Y S UL_ P X Q E X T R A C T
    ;

SYS_RID_ORDER
    :S Y S UL_ R I D UL_ O R D E R
    ;

SYS_ROW_DELTA
    :S Y S UL_ R O W UL_ D E L T A
    ;

SYS_SC_2_XMLT
    :S Y S UL_ S C UL_ [2] UL_ X M L T
    ;

SYS_SYNRCIREDO
    :S Y S UL_ S Y N R C I R E D O
    ;

SYS_TYPEID
    :S Y S UL_ T Y P E I D
    ;

SYS_UMAKEXML
    :S Y S UL_ U M A K E X M L
    ;

SYS_XMLANALYZE
    :S Y S UL_ X M L A N A L Y Z E
    ;

SYS_XMLCONTAINS
    :S Y S UL_ X M L C O N T A I N S
    ;

SYS_XMLCONV
    :S Y S UL_ X M L C O N V
    ;

SYS_XMLEXNSURI
    :S Y S UL_ X M L E X N S U R I
    ;

SYS_XMLGEN
    :S Y S UL_ X M L G E N
    ;

SYS_XMLAGG
    :S Y S UL_ X M L A G G
    ;

SYS_XMLI_LOC_ISNODE
    :S Y S UL_ X M L I UL_ L O C UL_ I S N O D E
    ;

SYS_XMLI_LOC_ISTEXT
    :S Y S UL_ X M L I UL_ L O C UL_ I S T E X T
    ;

SYS_XMLLOCATOR_GETSVAL
    :S Y S UL_ X M L L O C A T O R UL_ G E T S V A L
    ;

SYS_XMLNODEID
    :S Y S UL_ X M L N O D E I D
    ;

SYS_XMLNODEID_GETCID
    :S Y S UL_ X M L N O D E I D UL_ G E T C I D
    ;

SYS_XMLNODEID_GETLOCATOR
    :S Y S UL_ X M L N O D E I D UL_ G E T L O C A T O R
    ;

SYS_XMLNODEID_GETOKEY
    :S Y S UL_ X M L N O D E I D UL_ G E T O K E Y
    ;

SYS_XMLNODEID_GETPATHID
    :S Y S UL_ X M L N O D E I D UL_ G E T P A T H I D
    ;

SYS_XMLNODEID_GETPTRID
    :S Y S UL_ X M L N O D E I D UL_ G E T P T R I D
    ;

SYS_XMLNODEID_GETRID
    :S Y S UL_ X M L N O D E I D UL_ G E T R I D
    ;

SYS_XMLNODEID_GETSVAL
    :S Y S UL_ X M L N O D E I D UL_ G E T S V A L
    ;

SYS_XMLNODEID_GETTID
    :S Y S UL_ X M L N O D E I D UL_ G E T T I D
    ;

SYS_XMLTRANSLATE
    :S Y S UL_ X M L T R A N S L A T E
    ;

SYS_XMLTYPE2SQL
    :S Y S UL_ X M L T Y P E [2] S Q L
    ;

SYS_XMLT_2_SC
    :S Y S UL_ X M L T UL_ [2] UL_ S C
    ;

SYS_XQBASEURI
    :S Y S UL_ X Q B A S E U R I
    ;

SYS_XQCASTABLEERRH
    :S Y S UL_ X Q C A S T A B L E E R R H
    ;

SYS_XQCODEP2STR
    :S Y S UL_ X Q C O D E P [2] S T R
    ;

SYS_XQCODEPEQ
    :S Y S UL_ X Q C O D E P E Q
    ;

SYS_XQCON2SEQ
    :S Y S UL_ X Q C O N [2] S E Q
    ;

SYS_XQCONCAT
    :S Y S UL_ X Q C O N C A T
    ;

SYS_XQDELETE
    :S Y S UL_ X Q D E L E T E
    ;

SYS_XQDFLTCOLATION
    :S Y S UL_ X Q D F L T C O L A T I O N
    ;

SYS_XQDOC
    :S Y S UL_ X Q D O C
    ;

SYS_XQDOCURI
    :S Y S UL_ X Q D O C U R I
    ;

SYS_XQED4URI
    :S Y S UL_ X Q E D [4] U R I
    ;

SYS_XQENDSWITH
    :S Y S UL_ X Q E N D S W I T H
    ;

SYS_XQERR
    :S Y S UL_ X Q E R R
    ;

SYS_XQERRH
    :S Y S UL_ X Q E R R H
    ;

SYS_XQESHTMLURI
    :S Y S UL_ X Q E S H T M L U R I
    ;

SYS_XQEXLOBVAL
    :S Y S UL_ X Q E X L O B V A L
    ;

SYS_XQEXSTWRP
    :S Y S UL_ X Q E X S T W R P
    ;

SYS_XQEXTRACT
    :S Y S UL_ X Q E X T R A C T
    ;

SYS_XQEXTRREF
    :S Y S UL_ X Q E X T R R E F
    ;

SYS_XQEXVAL
    :S Y S UL_ X Q E X V A L
    ;

SYS_XQFB2STR
    :S Y S UL_ X Q F B [2] S T R
    ;

SYS_XQFNBOOL
    :S Y S UL_ X Q F N B O O L
    ;

SYS_XQFNCMP
    :S Y S UL_ X Q F N C M P
    ;

SYS_XQFNDATIM
    :S Y S UL_ X Q F N D A T I M
    ;

SYS_XQFNLNAME
    :S Y S UL_ X Q F N L N A M E
    ;

SYS_XQFNNM
    :S Y S UL_ X Q F N N M
    ;

SYS_XQFNNSURI
    :S Y S UL_ X Q F N N S U R I
    ;

SYS_XQFNPREDTRUTH
    :S Y S UL_ X Q F N P R E D T R U T H
    ;

SYS_XQFNQNM
    :S Y S UL_ X Q F N Q N M
    ;

SYS_XQFNROOT
    :S Y S UL_ X Q F N R O O T
    ;

SYS_XQFORMATNUM
    :S Y S UL_ X Q F O R M A T N U M
    ;

SYS_XQFTCONTAIN
    :S Y S UL_ X Q F T C O N T A I N
    ;

SYS_XQFUNCR
    :S Y S UL_ X Q F U N C R
    ;

SYS_XQGETCONTENT
    :S Y S UL_ X Q G E T C O N T E N T
    ;

SYS_XQINDXOF
    :S Y S UL_ X Q I N D X O F
    ;

SYS_XQINSERT
    :S Y S UL_ X Q I N S E R T
    ;

SYS_XQINSPFX
    :S Y S UL_ X Q I N S P F X
    ;

SYS_XQIRI2URI
    :S Y S UL_ X Q I R I [2] U R I
    ;

SYS_XQLANG
    :S Y S UL_ X Q L A N G
    ;

SYS_XQLLNMFRMQNM
    :S Y S UL_ X Q L L N M F R M Q N M
    ;

SYS_XQMKNODEREF
    :S Y S UL_ X Q M K N O D E R E F
    ;

SYS_XQNILLED
    :S Y S UL_ X Q N I L L E D
    ;

SYS_XQNODENAME
    :S Y S UL_ X Q N O D E N A M E
    ;

SYS_XQNORMSPACE
    :S Y S UL_ X Q N O R M S P A C E
    ;

SYS_XQNORMUCODE
    :S Y S UL_ X Q N O R M U C O D E
    ;

SYS_XQNSP4PFX
    :S Y S UL_ X Q N S P [4] P F X
    ;

SYS_XQNSPFRMQNM
    :S Y S UL_ X Q N S P F R M Q N M
    ;

SYS_XQPFXFRMQNM
    :S Y S UL_ X Q P F X F R M Q N M
    ;

SYS_XQPOLYABS
    :S Y S UL_ X Q P O L Y A B S
    ;

SYS_XQPOLYADD
    :S Y S UL_ X Q P O L Y A D D
    ;

SYS_XQPOLYCEL
    :S Y S UL_ X Q P O L Y C E L
    ;

SYS_XQPOLYCST
    :S Y S UL_ X Q P O L Y C S T
    ;

SYS_XQPOLYCSTBL
    :S Y S UL_ X Q P O L Y C S T B L
    ;

SYS_XQPOLYDIV
    :S Y S UL_ X Q P O L Y D I V
    ;

SYS_XQPOLYFLR
    :S Y S UL_ X Q P O L Y F L R
    ;

SYS_XQPOLYMOD
    :S Y S UL_ X Q P O L Y M O D
    ;

SYS_XQPOLYMUL
    :S Y S UL_ X Q P O L Y M U L
    ;

SYS_XQPOLYRND
    :S Y S UL_ X Q P O L Y R N D
    ;

SYS_XQPOLYSQRT
    :S Y S UL_ X Q P O L Y S Q R T
    ;

SYS_XQPOLYSUB
    :S Y S UL_ X Q P O L Y S U B
    ;

SYS_XQPOLYUMUS
    :S Y S UL_ X Q P O L Y U M U S
    ;

SYS_XQPOLYUPLS
    :S Y S UL_ X Q P O L Y U P L S
    ;

SYS_XQPOLYVEQ
    :S Y S UL_ X Q P O L Y V E Q
    ;

SYS_XQPOLYVGE
    :S Y S UL_ X Q P O L Y V G E
    ;

SYS_XQPOLYVGT
    :S Y S UL_ X Q P O L Y V G T
    ;

SYS_XQPOLYVLE
    :S Y S UL_ X Q P O L Y V L E
    ;

SYS_XQPOLYVLT
    :S Y S UL_ X Q P O L Y V L T
    ;

SYS_XQPOLYVNE
    :S Y S UL_ X Q P O L Y V N E
    ;

SYS_XQREF2VAL
    :S Y S UL_ X Q R E F [2] V A L
    ;

SYS_XQRENAME
    :S Y S UL_ X Q R E N A M E
    ;

SYS_XQREPLACE
    :S Y S UL_ X Q R E P L A C E
    ;

SYS_XQRESVURI
    :S Y S UL_ X Q R E S V U R I
    ;

SYS_XQRNDHALF2EVN
    :S Y S UL_ X Q R N D H A L F [2] E V N
    ;

SYS_XQRSLVQNM
    :S Y S UL_ X Q R S L V Q N M
    ;

SYS_XQRYENVPGET
    :S Y S UL_ X Q R Y E N V P G E T
    ;

SYS_XQRYVARGET
    :S Y S UL_ X Q R Y V A R G E T
    ;

SYS_XQRYWRP
    :S Y S UL_ X Q R Y W R P
    ;

SYS_XQSEQ2CON
    :S Y S UL_ X Q S E Q [2] C O N
    ;

SYS_XQSEQ2CON4XC
    :S Y S UL_ X Q S E Q [2] C O N [4] X C
    ;

SYS_XQSEQDEEPEQ
    :S Y S UL_ X Q S E Q D E E P E Q
    ;

SYS_XQSEQINSB
    :S Y S UL_ X Q S E Q I N S B
    ;

SYS_XQSEQRM
    :S Y S UL_ X Q S E Q R M
    ;

SYS_XQSEQRVS
    :S Y S UL_ X Q S E Q R V S
    ;

SYS_XQSEQSUB
    :S Y S UL_ X Q S E Q S U B
    ;

SYS_XQSEQTYPMATCH
    :S Y S UL_ X Q S E Q T Y P M A T C H
    ;

SYS_XQSTARTSWITH
    :S Y S UL_ X Q S T A R T S W I T H
    ;

SYS_XQSTATBURI
    :S Y S UL_ X Q S T A T B U R I
    ;

SYS_XQSTR2CODEP
    :S Y S UL_ X Q S T R [2] C O D E P
    ;

SYS_XQSTRJOIN
    :S Y S UL_ X Q S T R J O I N
    ;

SYS_XQSUBSTRAFT
    :S Y S UL_ X Q S U B S T R A F T
    ;

SYS_XQSUBSTRBEF
    :S Y S UL_ X Q S U B S T R B E F
    ;

SYS_XQTOKENIZE
    :S Y S UL_ X Q T O K E N I Z E
    ;

SYS_XQTREATAS
    :S Y S UL_ X Q T R E A T A S
    ;

SYS_XQXFORM
    :S Y S UL_ X Q X F O R M
    ;

SYS_XQ_ASQLCNV
    :S Y S UL_ X Q UL_ A S Q L C N V
    ;

SYS_XQ_ATOMCNVCHK
    :S Y S UL_ X Q UL_ A T O M C N V C H K
    ;

SYS_XQ_NRNG
    :S Y S UL_ X Q UL_ N R N G
    ;

SYS_XQ_PKSQL2XML
    :S Y S UL_ X Q UL_ P K S Q L [2] X M L
    ;

SYS_XQ_UPKXML2SQL
    :S Y S UL_ X Q UL_ U P K X M L [2] S Q L
    ;

TABLE
    :T A B L E
    ;

TABLESPACE_NO
    :T A B L E S P A C E UL_ N O
    ;

TABLE_STATS
    :T A B L E UL_ S T A T S
    ;

TABNO
    :T A B N O
    ;

TAN
    :T A N
    ;

TANH
    :T A N H
    ;

TEMP_TABLE
    :T E M P UL_ T A B L E
    ;

THE
    :T H E
    ;

THEN
    :T H E N
    ;

THROUGH
    :T H R O U G H
    ;

TIMES
    :T I M E S
    ;

TIMEZONE_ABBR
    :T I M E Z O N E UL_ A B B R
    ;

TIMEZONE_HOUR
    :T I M E Z O N E UL_ H O U R
    ;

TIMEZONE_MINUTE
    :T I M E Z O N E UL_ M I N U T E
    ;

TIMEZONE_OFFSET
    :T I M E Z O N E UL_ O F F S E T
    ;

TIMEZONE_REGION
    :T I M E Z O N E UL_ R E G I O N
    ;

TIV_GB
    :T I V UL_ G B
    ;

TIV_SSF
    :T I V UL_ S S F
    ;

TO
    :T O
    ;

TOPLEVEL
    :T O P L E V E L
    ;

TO_BINARY_DOUBLE
    :T O UL_ B I N A R Y UL_ D O U B L E
    ;

TO_BINARY_FLOAT
    :T O UL_ B I N A R Y UL_ F L O A T
    ;

TO_BLOB
    :T O UL_ B L O B
    ;

TO_CLOB
    :T O UL_ C L O B
    ;

TO_DATE
    :T O UL_ D A T E
    ;

TO_DSINTERVAL
    :T O UL_ D S I N T E R V A L
    ;

TO_LOB
    :T O UL_ L O B
    ;

TO_MULTI_BYTE
    :T O UL_ M U L T I UL_ B Y T E
    ;

TO_NCHAR
    :T O UL_ N C H A R
    ;

TO_NCLOB
    :T O UL_ N C L O B
    ;

TO_NUMBER
    :T O UL_ N U M B E R
    ;

TO_SINGLE_BYTE
    :T O UL_ S I N G L E UL_ B Y T E
    ;

TO_TIME
    :T O UL_ T I M E
    ;

TO_TIMESTAMP
    :T O UL_ T I M E S T A M P
    ;

TO_TIMESTAMP_TZ
    :T O UL_ T I M E S T A M P UL_ T Z
    ;

TO_TIME_TZ
    :T O UL_ T I M E UL_ T Z
    ;

TO_YMINTERVAL
    :T O UL_ Y M I N T E R V A L
    ;

TRACING
    :T R A C I N G
    ;

TRAILING
    :T R A I L I N G
    ;

TRANSFORM_DISTINCT_AGG
    :T R A N S F O R M UL_ D I S T I N C T UL_ A G G
    ;

TRANSITION
    :T R A N S I T I O N
    ;

TRANSITIONAL
    :T R A N S I T I O N A L
    ;

TRIGGER
    :T R I G G E R
    ;

TRUNC
    :T R U N C
    ;

TX
    :T X
    ;

TZ_OFFSET
    :T Z UL_ O F F S E T
    ;

UB2
    :U B [2]
    ;

UBA
    :U B A
    ;

UID
    :U I D
    ;

UNBOUND
    :U N B O U N D
    ;

UNION
    :U N I O N
    ;

UNIQUE
    :U N I Q U E
    ;

UNISTR
    :U N I S T R
    ;

UNLOCK
    :U N L O C K
    ;

UNNEST
    :U N N E S T
    ;

UNPACKED
    :U N P A C K E D
    ;

UNRESTRICTED
    :U N R E S T R I C T E D
    ;

UPDATABLE
    :U P D A T A B L E
    ;

UPDATE
    :U P D A T E
    ;

UPDATEXML
    :U P D A T E X M L
    ;

UPD_INDEXES
    :U P D UL_ I N D E X E S
    ;

UPD_JOININDEX
    :U P D UL_ J O I N I N D E X
    ;

UPPER
    :U P P E R
    ;

USERENV
    :U S E R E N V
    ;

USERGROUP
    :U S E R G R O U P
    ;

USER_DEFINED
    :U S E R UL_ D E F I N E D
    ;

USER_RECYCLEBIN
    :U S E R UL_ R E C Y C L E B I N
    ;

USE_ANTI
    :U S E UL_ A N T I
    ;

USE_CONCAT
    :U S E UL_ C O N C A T
    ;

USE_HASH
    :U S E UL_ H A S H
    ;

USE_HASH_AGGREGATION
    :U S E UL_ H A S H UL_ A G G R E G A T I O N
    ;

USE_INVISIBLE_INDEXES
    :U S E UL_ I N V I S I B L E UL_ I N D E X E S
    ;

USE_MERGE
    :U S E UL_ M E R G E
    ;

USE_MERGE_CARTESIAN
    :U S E UL_ M E R G E UL_ C A R T E S I A N
    ;

USE_NL
    :U S E UL_ N L
    ;

USE_NL_WITH_INDEX
    :U S E UL_ N L UL_ W I T H UL_ I N D E X
    ;

USE_PRIVATE_OUTLINES
    :U S E UL_ P R I V A T E UL_ O U T L I N E S
    ;

USE_SEMI
    :U S E UL_ S E M I
    ;

USE_TTT_FOR_GSETS
    :U S E UL_ T T T UL_ F O R UL_ G S E T S
    ;

USE_WEAK_NAME_RESL
    :U S E UL_ W E A K UL_ N A M E UL_ R E S L
    ;

VALIDATION
    :V A L I D A T I O N
    ;

VALUES
    :V A L U E S
    ;

VARIANCE
    :V A R I A N C E
    ;

VAR_POP
    :V A R UL_ P O P
    ;

VAR_SAMP
    :V A R UL_ S A M P
    ;

VECTOR_READ
    :V E C T O R UL_ R E A D
    ;

VECTOR_READ_TRACE
    :V E C T O R UL_ R E A D UL_ T R A C E
    ;

VERSIONING
    :V E R S I O N I N G
    ;

VERSIONS_ENDSCN
    :V E R S I O N S UL_ E N D S C N
    ;

VERSIONS_ENDTIME
    :V E R S I O N S UL_ E N D T I M E
    ;

VERSIONS_OPERATION
    :V E R S I O N S UL_ O P E R A T I O N
    ;

VERSIONS_STARTSCN
    :V E R S I O N S UL_ S T A R T S C N
    ;

VERSIONS_STARTTIME
    :V E R S I O N S UL_ S T A R T T I M E
    ;

VERSIONS_XID
    :V E R S I O N S UL_ X I D
    ;

VIEW
    :V I E W
    ;

VOLUME
    :V O L U M E
    ;

VSIZE
    :V S I Z E
    ;

WELLFORMED
    :W E L L F O R M E D
    ;

WHERE
    :W H E R E
    ;

WHITESPACE
    :W H I T E S P A C E
    ;

WIDTH_BUCKET
    :W I D T H UL_ B U C K E T
    ;

WITH
    :W I T H
    ;

WRAPPED
    :W R A P P E D
    ;

XID
    :X I D
    ;

XMLATTRIBUTES
    :X M L A T T R I B U T E S
    ;

XMLCAST
    :X M L C A S T
    ;

XMLCDATA
    :X M L C D A T A
    ;

XMLCOLATTVAL
    :X M L C O L A T T V A L
    ;

XMLCOMMENT
    :X M L C O M M E N T
    ;

XMLCONCAT
    :X M L C O N C A T
    ;

XMLDIFF
    :X M L D I F F
    ;

XMLEXISTS
    :X M L E X I S T S
    ;

XMLEXISTS2
    :X M L E X I S T S [2]
    ;

XMLFOREST
    :X M L F O R E S T
    ;

XMLINDEX_REWRITE
    :X M L I N D E X UL_ R E W R I T E
    ;

XMLINDEX_REWRITE_IN_SELECT
    :X M L I N D E X UL_ R E W R I T E UL_ I N UL_ S E L E C T
    ;

XMLINDEX_SEL_IDX_TBL
    :X M L I N D E X UL_ S E L UL_ I D X UL_ T B L
    ;

XMLISNODE
    :X M L I S N O D E
    ;

XMLISVALID
    :X M L I S V A L I D
    ;

XMLNAMESPACES
    :X M L N A M E S P A C E S
    ;

XMLPARSE
    :X M L P A R S E
    ;

XMLPATCH
    :X M L P A T C H
    ;

XMLPI
    :X M L P I
    ;

XMLQUERY
    :X M L Q U E R Y
    ;

XMLROOT
    :X M L R O O T
    ;

XMLSERIALIZE
    :X M L S E R I A L I Z E
    ;

XMLTABLE
    :X M L T A B L E
    ;

XMLTOOBJECT
    :X M L T O O B J E C T
    ;

XMLTRANSFORM
    :X M L T R A N S F O R M
    ;

XMLTRANSFORMBLOB
    :X M L T R A N S F O R M B L O B
    ;

XML_DML_RWT_STMT
    :X M L UL_ D M L UL_ R W T UL_ S T M T
    ;

XPATHTABLE
    :X P A T H T A B L E
    ;

XMLSEQUENCE
    :X M L S E Q U E N C E
    ;

XS_SYS_CONTEXT
    :X S UL_ S Y S UL_ C O N T E X T
    ;

X_DYN_PRUNE
    :X UL_ D Y N UL_ P R U N E
    ;

FEATURE_COMPARE
    : F E A T U R E UL_ C O M P A R E
    ;

FEATURE_DETAILS
    : F E A T U R E UL_ D E T A I L S
    ;

CLUSTER_DETAILS
    : C L U S T E R UL_ D E T A I L S
    ;

CLUSTER_DISTANCE
    : C L U S T E R UL_ D I S T A N C E
    ;

PERSISTABLE
    : P E R S I S T A B L E
    ;

DB_RECOVERY_FILE_DEST_SIZE
    : D B UL_ R E C O V E R Y UL_ F I L E UL_ D E S T UL_ S I Z E
    ;

MANDATORY
    : M A N D A T O R Y
    ;

BACKUPS
    : B A C K U P S
    ;

NOPROMPT
    : N O P R O M P T
    ;

IMMUTABLE
    : I M M U T A B L E
    ;

BLOCKCHAIN
    : B L O C K C H A I N
    ;

IDLE
    : I D L E
    ;

HASHING
    : H A S H I N G
    ;

DEFINITION
    : D E F I N I T I O N
    ;

COLLATE
    : C O L L A T E
    ;

XDB
    : X D B
    ;

XMLINDEX
    : X M L I N D E X
    ;

INDEX_ALL_PATHS
    : I N D E X UL_ A L L UL_ P A T H S
    ;

NONBLOCKING
    : N O N B L O C K I N G
    ;

MODIFY_COLUMN_TYPE
    : M O D I F Y UL_ C O L U M N UL_ T Y P E
    ;

CERTIFICATE_DN
    : C E R T I F I C A T E UL_ D N
    ;

KERBEROS_PRINCIPAL_NAME
    : K E R B E R O S UL_ P R I N C I P A L UL_ N A M E
    ;

DELEGATE
    : D E L E G A T E
    ;

TIME_UNIT
    : T I M E UL_ U N I T
    ;

DEVICE
    : D E V I C E
    ;

PARAMETER
    :P A R A M E T E R
    ;

SHO
    : S H O
    ;

ERR
    : E R R
    ;

CON_ID
    : C O N UL_ I D
    ;

CON_NAME
    : C O N UL_ N A M E
    ;

BTI
    : B T I
    ;

BTITLE
    : B T I T L E
    ;

HISTORY
    : H I S T O R Y
    ;

LNO
    : L N O
    ;

LOBPREFETCH
    : L O B P R E F E T C H
    ;

PDBS
    : P D B S
    ;

PNO
    : P N O
    ;

RECYC
    : R E C Y C
    ;

REL
    : R E L
    ;

RELEASE
    : R E L E A S E
    ;

REPF
    : R E P F
    ;

REPFOOTER
    : R E P F O O T E R
    ;

REPH
    : R E P H
    ;

REPHEADER
    : R E P H E A D E R
    ;

ROWPREF
    : R O W P R E F
    ;

ROWPREFETCH
    : R O W P R E F E T C H
    ;

SGA
    : S G A
    ;

SPOO
    : S P O O
    ;

SPOOL
    : S P O O L
    ;

SPPARAMETER
    : S P P A R A M E T E R
    ;

SPPARAMETERS
    : S P P A R A M E T E R S
    ;

SQLCODE
    : S Q L C O D E
    ;

STATEMENTC
    : S T A T E M E N T C
    ;

STATEMENTCACHE
    : S T A T E M E N T C A C H E
    ;

TTI
    : T T I
    ;

TLE
    : T L E
    ;

XQUERY
    : X Q U E R Y
    ;

SPO
    : S P O
    ;

CRE
    : C R E
    ;

REP
    : R E P
    ;

APP
    : A P P
    ;

EXCEPTION_INIT
    : E X C E P T I O N UL_ I N I T
    ;

V1
    : V '1'
    ;
