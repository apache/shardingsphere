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

lexer grammar SQLServerKeyword;

import Alphabet;

BINARY
    : B I N A R Y
    ;

ESCAPE
    : E S C A P E
    ;

HIDDEN_
    : H I D D E N
    ;

MOD
    : M O D
    ;

PARTITION
    : P A R T I T I O N
    ;

PARTITIONS
    : P A R T I T I O N S
    ;

TOP
    : T O P
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

XOR
    : X O R
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

GENERATED
    : G E N E R A T E D
    ;

NO
    : N O
    ;

OPTION
    : O P T I O N
    ;

PRIVILEGES
    : P R I V I L E G E S
    ;

REFERENCES
    : R E F E R E N C E S
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

ACTION
    : A C T I O N
    ;

ALGORITHM
    : A L G O R I T H M
    ;

AUTO
    : A U T O
    ;

BLOCKERS
    : B L O C K E R S
    ;

CLUSTERED
    : C L U S T E R E D
    ;

NONCLUSTERED
    : N O N C L U S T E R E D
    ;

COLLATE
    : C O L L A T E
    ;

COLUMNSTORE
    : C O L U M N S T O R E
    ;

CONTENT
    : C O N T E N T
    ;

CONVERT
    : C O N V E R T
    ;

DATABASE
    : D A T A B A S E
    ;

YEARS
    : Y E A R S
    ;

MONTHS
    : M O N T H S
    ;

WEEKS
    : W E E K S
    ;

DAYS
    : D A Y S
    ;

MINUTES
    : M I N U T E S
    ;

DENY
    : D E N Y
    ;

DETERMINISTIC
    : D E T E R M I N I S T I C
    ;

DISTRIBUTION
    : D I S T R I B U T I O N
    ;

DOCUMENT
    : D O C U M E N T
    ;

DURABILITY
    : D U R A B I L I T Y
    ;

ENCRYPTED
    : E N C R Y P T E D
    ;

END
    : E N D
    ;

FILESTREAM
    : F I L E S T R E A M
    ;

FILETABLE
    : F I L E T A B L E
    ;

FILLFACTOR
    : F I L L F A C T O R
    ;

FOLLOWING
    : F O L L O W I N G
    ;

HASH
    : H A S H
    ;

HEAP
    : H E A P
    ;

IDENTITY
    : I D E N T I T Y
    ;

INBOUND
    : I N B O U N D
    ;

OUTBOUND
    : O U T B O U N D
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

INFINITE
    : I N F I N I T E
    ;

LOGIN
    : L O G I N
    ;

MASKED
    : M A S K E D
    ;

MAXDOP
    : M A X D O P
    ;

MOVE
    : M O V E
    ;

NOCHECK
    : N O C H E C K
    ;

NONE
    : N O N E
    ;

OBJECT
    : O B J E C T
    ;

OFF
    : O F F
    ;

ONLINE
    : O N L I N E
    ;

OVER
    : O V E R
    ;

PAGE
    : P A G E
    ;

PAUSED
    : P A U S E D
    ;

PERIOD
    : P E R I O D
    ;

PERSISTED
    : P E R S I S T E D
    ;

PRECEDING
    : P R E C E D I N G
    ;

RANDOMIZED
    : R A N D O M I Z E D
    ;

RANGE
    : R A N G E
    ;

REBUILD
    : R E B U I L D
    ;

REPLICATE
    : R E P L I C A T E
    ;

REPLICATION
    : R E P L I C A T I O N
    ;

RESUMABLE
    : R E S U M A B L E
    ;

ROWGUIDCOL
    : R O W G U I D C O L
    ;

SAVE
    : S A V E
    ;

SCHEMA
    : S C H E M A
    ;

SELF
    : S E L F
    ;

SPARSE
    : S P A R S E
    ;

SWITCH
    : S W I T C H
    ;

TRAN
    : T R A N
    ;

TRANCOUNT
    : T R A N C O U N T
    ;

ZONE
    : Z O N E
    ;

EXECUTE
    : E X E C U T E
    ;

EXEC
    : E X E C
    ;

SESSION
    : S E S S I O N
    ;

CONNECT
    : C O N N E C T
    ;

CONNECTION
    : C O N N E C T I O N
    ;

CATALOG
    : C A T A L O G
    ;

CONTROL
    : C O N T R O L
    ;

CONCAT
    : C O N C A T
    ;

TAKE
    : T A K E
    ;

OWNERSHIP
    : O W N E R S H I P
    ;

DEFINITION
    : D E F I N I T I O N
    ;

APPLICATION
    : A P P L I C A T I O N
    ;

ASSEMBLY
    : A S S E M B L Y
    ;

SYMMETRIC
    : S Y M M E T R I C
    ;

ASYMMETRIC
    : A S Y M M E T R I C
    ;

SERVER
    : S E R V E R
    ;

RECEIVE
    : R E C E I V E
    ;

CHANGE
    : C H A N G E
    ;

TRACE
    : T R A C E
    ;

TRACKING
    : T R A C K I N G
    ;

RESOURCES
    : R E S O U R C E S
    ;

SETTINGS
    : S E T T I N G S
    ;

STATE
    : S T A T E
    ;

AVAILABILITY
    : A V A I L A B I L I T Y
    ;

CREDENTIAL
    : C R E D E N T I A L
    ;

ENDPOINT
    : E N D P O I N T
    ;

EVENT
    : E V E N T
    ;

NOTIFICATION
    : N O T I F I C A T I O N
    ;

LINKED
    : L I N K E D
    ;

AUDIT
    : A U D I T
    ;

DDL
    : D D L
    ;

XML
    : X M L
    ;

IMPERSONATE
    : I M P E R S O N A T E
    ;

SECURABLES
    : S E C U R A B L E S
    ;

AUTHENTICATE
    : A U T H E N T I C A T E
    ;

EXTERNAL
    : E X T E R N A L
    ;

ACCESS
    : A C C E S S
    ;

ADMINISTER
    : A D M I N I S T E R
    ;

BULK
    : B U L K
    ;

OPERATIONS
    : O P E R A T I O N S
    ;

UNSAFE
    : U N S A F E
    ;

SHUTDOWN
    : S H U T D O W N
    ;

SCOPED
    : S C O P E D
    ;

CONFIGURATION
    : C O N F I G U R A T I O N
    ;

DATASPACE
    : D A T A S P A C E
    ;

SERVICE
    : S E R V I C E
    ;

CERTIFICATE
    : C E R T I F I C A T E
    ;

CONTRACT
    : C O N T R A C T
    ;

ENCRYPTION
    : E N C R Y P T I O N
    ;

MASTER
    : M A S T E R
    ;

DATA
    : D A T A
    ;

SOURCE
    : S O U R C E
    ;

FILE
    : F I L E
    ;

FORMAT
    : F O R M A T
    ;

LIBRARY
    : L I B R A R Y
    ;

FULLTEXT
    : F U L L T E X T
    ;

MASK
    : M A S K
    ;

UNMASK
    : U N M A S K
    ;

MESSAGE
    : M E S S A G E
    ;

REMOTE
    : R E M O T E
    ;

BINDING
    : B I N D I N G
    ;

ROUTE
    : R O U T E
    ;

SECURITY
    : S E C U R I T Y
    ;

POLICY
    : P O L I C Y
    ;

AGGREGATE
    : A G G R E G A T E
    ;

QUEUE
    : Q U E U E
    ;

RULE
    : R U L E
    ;

SYNONYM
    : S Y N O N Y M
    ;

COLLECTION
    : C O L L E C T I O N
    ;

SCRIPT
    : S C R I P T
    ;

KILL
    : K I L L
    ;

BACKUP
    : B A C K U P
    ;

LOG
    : L O G
    ;

SHOWPLAN
    : S H O W P L A N
    ;

SUBSCRIBE
    : S U B S C R I B E
    ;

QUERY
    : Q U E R Y
    ;

NOTIFICATIONS
    : N O T I F I C A T I O N S
    ;

CHECKPOINT
    : C H E C K P O I N T
    ;

SEQUENCE
    : S E Q U E N C E
    ;

ABORT_AFTER_WAIT
    : A B O R T UL_ A F T E R UL_ W A I T
    ;

ALLOW_PAGE_LOCKS
    : A L L O W UL_ P A G E UL_ L O C K S
    ;

ALLOW_ROW_LOCKS
    : A L L O W UL_ R O W UL_ L O C K S
    ;

ALL_SPARSE_COLUMNS
    : A L L UL_ S P A R S E UL_ C O L U M N S
    ;

BUCKET_COUNT
    : B U C K E T UL_ C O U N T
    ;

COLUMNSTORE_ARCHIVE
    : C O L U M N S T O R E UL_ A R C H I V E
    ;

COLUMN_ENCRYPTION_KEY
    : C O L U M N UL_ E N C R Y P T I O N UL_ K E Y
    ;

COLUMN_SET
    : C O L U M N UL_ S E T
    ;

COMPRESSION_DELAY
    : C O M P R E S S I O N UL_ D E L A Y
    ;

DATABASE_DEAULT
    : D A T A B A S E UL_ D E A U L T
    ;

DATA_COMPRESSION
    : D A T A UL_ C O M P R E S S I O N
    ;

DATA_CONSISTENCY_CHECK
    : D A T A UL_ C O N S I S T E N C Y UL_ C H E C K
    ;

ENCRYPTION_TYPE
    : E N C R Y P T I O N UL_ T Y P E
    ;

SYSTEM_TIME
    : S Y S T E M UL_ T I M E
    ;

SYSTEM_VERSIONING
    : S Y S T E M UL_ V E R S I O N I N G
    ;

TEXTIMAGE_ON
    : T E X T I M A G E UL_ O N
    ;

WAIT_AT_LOW_PRIORITY
    : W A I T UL_ A T UL_ L O W UL_ P R I O R I T Y
    ;

STATISTICS_INCREMENTAL
    : S T A T I S T I C S UL_ I N C R E M E N T A L
    ;

STATISTICS_NORECOMPUTE
    : S T A T I S T I C S UL_ N O R E C O M P U T E
    ;

ROUND_ROBIN
    : R O U N D UL_ R O B I N
    ;

SCHEMA_AND_DATA
    : S C H E M A UL_ A N D UL_ D A T A
    ;

SCHEMA_ONLY
    : S C H E M A UL_ O N L Y
    ;

SORT_IN_TEMPDB
    : S O R T UL_ I N UL_ T E M P D B
    ;

IGNORE_DUP_KEY
    : I G N O R E UL_ D U P UL_ K E Y
    ;

IMPLICIT_TRANSACTIONS
    : I M P L I C I T UL_ T R A N S A C T I O N S
    ;

MAX_DURATION
    : M A X UL_ D U R A T I O N
    ;

MEMORY_OPTIMIZED
    : M E M O R Y UL_ O P T I M I Z E D
    ;

MIGRATION_STATE
    : M I G R A T I O N UL_ S T A T E
    ;

PAD_INDEX
    : P A D UL_ I N D E X
    ;

REMOTE_DATA_ARCHIVE
    : R E M O T E UL_ D A T A UL_ A R C H I V E
    ;

FILESTREAM_ON
    : F I L E S T R E A M UL_ O N
    ;

FILETABLE_COLLATE_FILENAME
    : F I L E T A B L E UL_ C O L L A T E UL_ F I L E N A M E
    ;

FILETABLE_DIRECTORY
    : F I L E T A B L E UL_ D I R E C T O R Y
    ;

FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME
    : F I L E T A B L E UL_ F U L L P A T H UL_ U N I Q U E UL_ C O N S T R A I N T UL_ N A M E
    ;

FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME
    : F I L E T A B L E UL_ P R I M A R Y UL_ K E Y UL_ C O N S T R A I N T UL_ N A M E
    ;

FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME
    : F I L E T A B L E UL_ S T R E A M I D UL_ U N I Q U E UL_ C O N S T R A I N T UL_ N A M E
    ;

FILTER_PREDICATE
    : F I L T E R UL_ P R E D I C A T E
    ;

HISTORY_RETENTION_PERIOD
    : H I S T O R Y UL_ R E T E N T I O N UL_ P E R I O D
    ;

HISTORY_TABLE
    : H I S T O R Y UL_ T A B L E
    ;

LOCK_ESCALATION
    : L O C K UL_ E S C A L A T I O N
    ;

DROP_EXISTING
    : D R O P UL_ E X I S T I N G
    ;

ROW_NUMBER
    : R O W UL_ N U M B E R
    ;

FETCH
    : F E T C H
    ;

FIRST
    : F I R S T
    ;

ONLY
    : O N L Y
    ;

MONEY
    : M O N E Y
    ;

SMALLMONEY
    : S M A L L M O N E Y
    ;

DATETIMEOFFSET
    : D A T E T I M E O F F S E T
    ;

DATETIME
    : D A T E T I M E
    ;

DATETIME2
    : D A T E T I M E [2]
    ;

SMALLDATETIME
    : S M A L L D A T E T I M E
    ;

NCHAR
    : N C H A R
    ;

NVARCHAR
    : N V A R C H A R
    ;

NTEXT
    : N T E X T
    ;

VARBINARY
    : V A R B I N A R Y
    ;

IMAGE
    : I M A G E
    ;

SQL_VARIANT
    : S Q L UL_ V A R I A N T
    ;

UNIQUEIDENTIFIER
    : U N I Q U E I D E N T I F I E R
    ;

HIERARCHYID
    : H I E R A R C H Y I D
    ;

GEOMETRY
    : G E O M E T R Y
    ;

GEOGRAPHY
    : G E O G R A P H Y
    ;

OUTPUT
    : O U T P U T
    ;

INSERTED
    : I N S E R T E D
    ;

DELETED
    : D E L E T E D
    ;

ASSUME_JOIN_PREDICATE_DEPENDS_ON_FILTERS
    : A S S U M E UL_ J O I N UL_ P R E D I C A T E UL_ D E P E N D S UL_ O N UL_ F I L T E R S
    ;

ASSUME_MIN_SELECTIVITY_FOR_FILTER_ESTIMATES
    : A S S U M E UL_ M I N UL_ S E L E C T I V I T Y UL_ F O R UL_ F I L T E R UL_ E S T I M A T E S
    ;

DISABLE_BATCH_MODE_ADAPTIVE_JOINS
    : D I S A B L E UL_ B A T C H UL_ M O D E UL_ A D A P T I V E UL_ J O I N S
    ;

DISABLE_BATCH_MODE_MEMORY_GRANT_FEEDBACK
    : D I S A B L E UL_ B A T C H UL_ M O D E UL_ M E M O R Y UL_ G R A N T UL_ F E E D B A C K
    ;

DISABLE_DEFERRED_COMPILATION_TV
    : D I S A B L E UL_ D E F E R R E D UL_ C O M P I L A T I O N UL_ T V
    ;

DISABLE_INTERLEAVED_EXECUTION_TVF
    : D I S A B L E UL_ I N T E R L E A V E D UL_ E X E C U T I O N UL_ T V F
    ;

DISABLE_OPTIMIZED_NESTED_LOOP
    : D I S A B L E UL_ O P T I M I Z E D UL_ N E S T E D UL_ L O O P
    ;

DISABLE_OPTIMIZER_ROWGOAL
    : D I S A B L E UL_ O P T I M I Z E R UL_ R O W G O A L
    ;

DISABLE_PARAMETER_SNIFFING
    : D I S A B L E UL_ P A R A M E T E R UL_ S N I F F I N G
    ;

DISABLE_ROW_MODE_MEMORY_GRANT_FEEDBACK
    : D I S A B L E UL_ R O W UL_ M O D E UL_ M E M O R Y UL_ G R A N T UL_ F E E D B A C K
    ;

DISABLE_TSQL_SCALAR_UDF_INLINING
    : D I S A B L E UL_ T S Q L UL_ S C A L A R UL_ U D F UL_ I N L I N I N G
    ;

DISALLOW_BATCH_MODE
    : D I S A L L O W UL_ B A T C H UL_ M O D E
    ;

ENABLE_HIST_AMENDMENT_FOR_ASC_KEYS
    : E N A B L E UL_ H I S T UL_ A M E N D M E N T UL_ F O R UL_ A S C UL_ K E Y S
    ;

ENABLE_QUERY_OPTIMIZER_HOTFIXES
    : E N A B L E UL_ Q U E R Y UL_ O P T I M I Z E R UL_ H O T F I X E S
    ;

FORCE_DEFAULT_CARDINALITY_ESTIMATION
    : F O R C E UL_ D E F A U L T UL_ C A R D I N A L I T Y UL_ E S T I M A T I O N
    ;

FORCE_LEGACY_CARDINALITY_ESTIMATION
    : F O R C E UL_ L E G A C Y UL_ C A R D I N A L I T Y UL_ E S T I M A T I O N
    ;

QUERY_OPTIMIZER_COMPATIBILITY_LEVEL_n
    : Q U E R Y UL_ O P T I M I Z E R UL_ C O M P A T I B I L I T Y UL_ L E V E L UL_
    ;

QUERY_PLAN_PROFILE
    : Q U E R Y UL_ P L A N UL_ P R O F I L E
    ;

EXTERNALPUSHDOWN
    : E X T E R N A L P U S H D O W N
    ;

SCALEOUTEXECUTION
    : S C A L E O U T E X E C U T I O N
    ;

IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX
    : I G N O R E UL_ N O N C L U S T E R E D UL_ C O L U M N S T O R E UL_ I N D E X
    ;

KEEPFIXED
    : K E E P F I X E D
    ;

MAX_GRANT_PERCENT
    : M A X UL_ G R A N T UL_ P E R C E N T
    ;

MIN_GRANT_PERCENT
    : M I N UL_ G R A N T UL_ P E R C E N T
    ;

MAXRECURSION
    : M A X R E C U R S I O N
    ;

NO_PERFORMANCE_SPOOL
    : N O UL_ P E R F O R M A N C E UL_ S P O O L
    ;

PARAMETERIZATION
    : P A R A M E T E R I Z A T I O N
    ;

QUERYTRACEON
    : Q U E R Y T R A C E O N
    ;

RECOMPILE
    : R E C O M P I L E
    ;

ROBUST
    : R O B U S T
    ;

OPTIMIZE_FOR_SEQUENTIAL_KEY
    : O P T I M I Z E UL_ F O R UL_ S E Q U E N T I A L UL_ K E Y
    ;

DATA_DELETION
    : D A T A UL_ D E L E T I O N
    ;

FILTER_COLUMN
    : F I L T E R UL_ C O L U M N
    ;

RETENTION_PERIOD
    : R E T E N T I O N UL_ P E R I O D
    ;

CONTAINMENT
    : C O N T A I N M E N T
    ;

PARTIAL
    : P A R T I A L
    ;

FILENAME
    : F I L E N A M E
    ;

SIZE
    : S I Z E
    ;

MAXSIZE
    : M A X S I Z E
    ;

FILEGROWTH
    : F I L E G R O W T H
    ;

UNLIMITED
    : U N L I M I T E D
    ;

KB
    : K B
    ;

MB
    : M B
    ;

GB
    : G B
    ;

TB
    : T B
    ;

CONTAINS
    : C O N T A I N S
    ;

MEMORY_OPTIMIZED_DATA
    : M E M O R Y UL_ O P T I M I Z E D UL_ D A T A
    ;

FILEGROUP
    : F I L E G R O U P
    ;

NON_TRANSACTED_ACCESS
    : N O N UL_ T R A N S A C T E D UL_ A C C E S S
    ;

DB_CHAINING
    : D B UL_ C H A I N I N G
    ;

TRUSTWORTHY
    : T R U S T W O R T H Y
    ;

FORWARD_ONLY
    : F O R W A R D UL_ O N L Y
    ;

SCROLL
    : S C R O L L
    ;

STATIC
    : S T A T I C
    ;

KEYSET
    : K E Y S E T
    ;

DYNAMIC
    : D Y N A M I C
    ;

FAST_FORWARD
    : F A S T UL_ F O R W A R D
    ;

READ_ONLY
    : R E A D UL_ O N L Y
    ;

SCROLL_LOCKS
    : S C R O L L UL_ L O C K S
    ;

OPTIMISTIC
    : O P T I M I S T I C
    ;

TYPE_WARNING
    : T Y P E UL_ W A R N I N G
    ;

SCHEMABINDING
    : S C H E M A B I N D I N G
    ;

CALLER
    : C A L L E R
    ;

INPUT
    : I N P U T
    ;

CALLED
    : C A L L E D
    ;

VARYING
    : V A R Y I N G
    ;

OUT
    : O U T
    ;

OWNER
    : O W N E R
    ;

ATOMIC
    : A T O M I C
    ;

LANGUAGE
    : L A N G U A G E
    ;

LEVEL
    : L E V E L
    ;

ISOLATION
    : I S O L A T I O N
    ;

SNAPSHOT
    : S N A P S H O T
    ;

REPEATABLE
    : R E P E A T A B L E
    ;

READ
    : R E A D
    ;

SERIALIZABLE
    : S E R I A L I Z A B L E
    ;

NATIVE_COMPILATION
    : N A T I V E UL_ C O M P I L A T I O N
    ;

VIEW_METADATA
    : V I E W UL_ M E T A D A T A
    ;

AFTER
    : A F T E R
    ;

INSTEAD
    : I N S T E A D
    ;

APPEND
    : A P P E N D
    ;

INCREMENT
    : I N C R E M E N T
    ;

CYCLE
    : C Y C L E
    ;

CACHE
    : C A C H E
    ;

MINVALUE
    : M I N V A L U E
    ;

MAXVALUE
    : M A X V A L U E
    ;

RESTART
    : R E S T A R T
    ;

LOB_COMPACTION
    : L O B UL_ C O M P A C T I O N
    ;

COMPRESS_ALL_ROW_GROUPS
    : C O M P R E S S UL_ A L L UL_ R O W UL_ G R O U P S
    ;

REORGANIZE
    : R E O R G A N I Z E
    ;

RESUME
    : R E S U M E
    ;

PAUSE
    : P A U S E
    ;

ABORT
    : A B O R T
    ;

INCLUDE
    : I N C L U D E
    ;

DISTRIBUTED
    : D I S T R I B U T E D
    ;

MARK
    : M A R K
    ;

WORK
    : W O R K
    ;

REMOVE
    : R E M O V E
    ;

AUTOGROW_SINGLE_FILE
    : A U T O G R O W UL_ S I N G L E UL_ F I L E
    ;

AUTOGROW_ALL_FILES
    : A U T O G R O W UL_ A L L UL_ F I L E S
    ;

READWRITE
    : R E A D W R I T E
    ;

READ_WRITE
    : R E A D UL_ W R I T E
    ;

MODIFY
    : M O D I F Y
    ;

ACCELERATED_DATABASE_RECOVERY
    : A C C E L E R A T E D UL_ D A T A B A S E UL_ R E C O V E R Y
    ;

PERSISTENT_VERSION_STORE_FILEGROUP
    : P E R S I S T E N T UL_ V E R S I O N UL_ S T O R E UL_ F I L E G R O U P
    ;

IMMEDIATE
    : I M M E D I A T E
    ;

NO_WAIT
    : N O UL_ W A I T
    ;

TARGET_RECOVERY_TIME
    : T A R G E T UL_ R E C O V E R Y UL_ T I M E
    ;

SECONDS
    : S E C O N D S
    ;

HONOR_BROKER_PRIORITY
    : H O N O R UL_ B R O K E R UL_ P R I O R I T Y
    ;

ERROR_BROKER_CONVERSATIONS
    : E R R O R UL_ B R O K E R UL_ C O N V E R S A T I O N S
    ;

NEW_BROKER
    : N E W UL_ B R O K E R
    ;

DISABLE_BROKER
    : D I S A B L E UL_ B R O K E R
    ;

ENABLE_BROKER
    : E N A B L E UL_ B R O K E R
    ;

MEMORY_OPTIMIZED_ELEVATE_TO_SNAPSHOT
    : M E M O R Y UL_ O P T I M I Z E D UL_ E L E V A T E UL_ T O UL_ S N A P S H O T
    ;

READ_COMMITTED_SNAPSHOT
    : R E A D UL_ C O M M I T T E D UL_ S N A P S H O T
    ;

ALLOW_SNAPSHOT_ISOLATION
    : A L L O W UL_ S N A P S H O T UL_ I S O L A T I O N
    ;

RECURSIVE_TRIGGERS
    : R E C U R S I V E UL_ T R I G G E R S
    ;

QUOTED_IDENTIFIER
    : Q U O T E D UL_ I D E N T I F I E R
    ;

NUMERIC_ROUNDABORT
    : N U M E R I C UL_ R O U N D A B O R T
    ;

CONCAT_NULL_YIELDS_NULL
    : C O N C A T UL_ N U L L UL_ Y I E L D S UL_ N U L L
    ;

COMPATIBILITY_LEVEL
    : C O M P A T I B I L I T Y UL_ L E V E L
    ;

ARITHABORT
    : A R I T H A B O R T
    ;

ANSI_WARNINGS
    : A N S I UL_ W A R N I N G S
    ;

ANSI_PADDING
    : A N S I UL_ P A D D I N G
    ;

ANSI_NULLS
    : A N S I UL_ N U L L S
    ;

ANSI_NULL_DEFAULT
    : A N S I UL_ N U L L UL_ D E F A U L T
    ;

PAGE_VERIFY
    : P A G E UL_ V E R I F Y
    ;

CHECKSUM
    : C H E C K S U M
    ;

TORN_PAGE_DETECTION
    : T O R N UL_ P A G E UL_ D E T E C T I O N
    ;

BULK_LOGGED
    : B U L K UL_ L O G G E D
    ;

RECOVERY
    : R E C O V E R Y
    ;

TOTAL_EXECUTION_CPU_TIME_MS
    : T O T A L UL_ E X E C U T I O N UL_ C P U UL_ T I M E UL_ M S
    ;

TOTAL_COMPILE_CPU_TIME_MS
    : T O T A L UL_ C O M P I L E UL_ C P U UL_ T I M E UL_ M S
    ;

STALE_CAPTURE_POLICY_THRESHOLD
    : S T A L E UL_ C A P T U R E UL_ P O L I C Y UL_ T H R E S H O L D
    ;

EXECUTION_COUNT
    : E X E C U T I O N UL_ C O U N T
    ;

QUERY_CAPTURE_POLICY
    : Q U E R Y UL_ C A P T U R E UL_ P O L I C Y
    ;

WAIT_STATS_CAPTURE_MODE
    : W A I T UL_ S T A T S UL_ C A P T U R E UL_ M O D E
    ;

MAX_PLANS_PER_QUERY
    : M A X UL_ P L A N S UL_ P E R UL_ Q U E R Y
    ;

QUERY_CAPTURE_MODE
    : Q U E R Y UL_ C A P T U R E UL_ M O D E
    ;

SIZE_BASED_CLEANUP_MODE
    : S I Z E UL_ B A S E D UL_ C L E A N U P UL_ M O D E
    ;

INTERVAL_LENGTH_MINUTES
    : I N T E R V A L UL_ L E N G T H UL_ M I N U T E S
    ;

MAX_STORAGE_SIZE_MB
    : M A X UL_ S T O R A G E UL_ S I Z E UL_ M B
    ;

DATA_FLUSH_INTERVAL_SECONDS
    : D A T A UL_ F L U S H UL_ I N T E R V A L UL_ S E C O N D S
    ;

CLEANUP_POLICY
    : C L E A N U P UL_ P O L I C Y
    ;

CUSTOM
    : C U S T O M
    ;

STALE_QUERY_THRESHOLD_DAYS
    : S T A L E UL_ Q U E R Y UL_ T H R E S H O L D UL_ D A Y S
    ;

OPERATION_MODE
    : O P E R A T I O N UL_ M O D E
    ;

QUERY_STORE
    : Q U E R Y UL_ S T O R E
    ;

CURSOR_DEFAULT
    : C U R S O R UL_ D E F A U L T
    ;

GLOBAL
    : G L O B A L
    ;

CURSOR_CLOSE_ON_COMMIT
    : C U R S O R UL_ C L O S E UL_ O N UL_ C O M M I T
    ;

HOURS
    : H O U R S
    ;

CHANGE_RETENTION
    : C H A N G E UL_ R E T E N T I O N
    ;

AUTO_CLEANUP
    : A U T O UL_ C L E A N U P
    ;

CHANGE_TRACKING
    : C H A N G E UL_ T R A C K I N G
    ;

AUTOMATIC_TUNING
    : A U T O M A T I C UL_ T U N I N G
    ;

FORCE_LAST_GOOD_PLAN
    : F O R C E UL_ L A S T UL_ G O O D UL_ P L A N
    ;

AUTO_UPDATE_STATISTICS_ASYNC
    : A U T O UL_ U P D A T E UL_ S T A T I S T I C S UL_ A S Y N C
    ;

AUTO_UPDATE_STATISTICS
    : A U T O UL_ U P D A T E UL_ S T A T I S T I C S
    ;

AUTO_SHRINK
    : A U T O UL_ S H R I N K
    ;

AUTO_CREATE_STATISTICS
    : A U T O UL_ C R E A T E UL_ S T A T I S T I C S
    ;

INCREMENTAL
    : I N C R E M E N T A L
    ;

AUTO_CLOSE
    : A U T O UL_ C L O S E
    ;

DATA_RETENTION
    : D A T A UL_ R E T E N T I O N
    ;

TEMPORAL_HISTORY_RETENTION
    : T E M P O R A L UL_ H I S T O R Y UL_ R E T E N T I O N
    ;

EDITION
    : E D I T I O N
    ;

MIXED_PAGE_ALLOCATION
    : M I X E D UL_ P A G E UL_ A L L O C A T I O N
    ;

DISABLED
    : D I S A B L E D
    ;

ALLOWED
    : A L L O W E D
    ;

HADR
    : H A D R
    ;

MULTI_USER
    : M U L T I UL_ U S E R
    ;

RESTRICTED_USER
    : R E S T R I C T E D UL_ U S E R
    ;

SINGLE_USER
    : S I N G L E UL_ U S E R
    ;

OFFLINE
    : O F F L I N E
    ;

EMERGENCY
    : E M E R G E N C Y
    ;

SUSPEND
    : S U S P E N D
    ;

DATE_CORRELATION_OPTIMIZATION
    : D A T E UL_ C O R R E L A T I O N UL_ O P T I M I Z A T I O N
    ;

ELASTIC_POOL
    : E L A S T I C UL_ P O O L
    ;

SERVICE_OBJECTIVE
    : S E R V I C E UL_ O B J E C T I V E
    ;

DATABASE_NAME
    : D A T A B A S E UL_ N A M E
    ;

ALLOW_CONNECTIONS
    : A L L O W UL_ C O N N E C T I O N S
    ;

GEO
    : G E O
    ;

NAMED
    : N A M E D
    ;

DATEFIRST
    : D A T E F I R S T
    ;

BACKUP_STORAGE_REDUNDANCY
    : B A C K U P UL_ S T O R A G E UL_ R E D U N D A N C Y
    ;

FORCE_FAILOVER_ALLOW_DATA_LOSS
    : F O R C E UL_ F A I L O V E R UL_ A L L O W UL_ D A T A UL_ L O S S
    ;

SECONDARY
    : S E C O N D A R Y
    ;

FAILOVER
    : F A I L O V E R
    ;

DEFAULT_FULLTEXT_LANGUAGE
    : D E F A U L T UL_ F U L L T E X T UL_ L A N G U A G E
    ;

DEFAULT_LANGUAGE
    : D E F A U L T UL_ L A N G U A G E
    ;

INLINE
    : I N L I N E
    ;

NESTED_TRIGGERS
    : N E S T E D UL_ T R I G G E R S
    ;

TRANSFORM_NOISE_WORDS
    : T R A N S F O R M UL_ N O I S E UL_ W O R D S
    ;

TWO_DIGIT_YEAR_CUTOFF
    : T W O UL_ D I G I T UL_ Y E A R UL_ C U T O F F
    ;

PERSISTENT_LOG_BUFFER
    : P E R S I S T E N T UL_ L O G UL_ B U F F E R
    ;

DIRECTORY_NAME
    : D I R E C T O R Y UL_ N A M E
    ;

DATEFORMAT
    : D A T E F O R M A T
    ;

DELAYED_DURABILITY
    : D E L A Y E D UL_ D U R A B I L I T
    ;

AUTHORIZATION
    : A U T H O R I Z A T I O N
    ;

TRANSFER
    : T R A N S F E R
    ;

EXPLAIN
    : E X P L A I N
    ;

WITH_RECOMMENDATIONS
    : W I T H UL_ R E C O M M E N D A T I O N S
    ;

BATCH_SIZE
    : B A T C H UL_ S I Z E
    ;

SETUSER
    : S E T U S E R
    ;

NORESET
    : N O R E S E T
    ;

DEFAULT_SCHEMA
    : D E F A U L T UL_ S C H E M A
    ;

ALLOW_ENCRYPTED_VALUE_MODIFICATIONS
    : A L L O W UL_ E N C R Y P T E D UL_ V A L U E UL_ M O D I F I C A T I O N S
    ;

OLD_PASSWORD
    : O L D UL_ P A S S W O R D
    ;

PROVIDER
    : P R O V I D E R
    ;

SID
    : S I D
    ;

UNCOMMITTED
    : U N C O M M I T T E D
    ;

COMMITTED
    : C O M M I T T E D
    ;

STOPLIST
    : S T O P L I S T
    ;

SEARCH
    : S E A R C H
    ;

PROPERTY
    : P R O P E R T Y
    ;

LIST
    : L I S T
    ;

SEND
    : S E N D
    ;

MEMBER
    : M E M B E R
    ;

HASHED
    : H A S H E D
    ;

MUST_CHANGE
    : M U S T UL_ C H A N G E
    ;

DEFAULT_DATABASE
    : D E F A U L T UL_ D A T A B A S E
    ;

CHECK_EXPIRATION
    : C H E C K UL_ E X P I R A T I O N
    ;

CHECK_POLICY
    : C H E C K UL_ P O L I C Y
    ;

WINDOWS
    : W I N D O W S
    ;

UNLOCK
    : U N L O C K
    ;

REVERT
    : R E V E R T
    ;

COOKIE
    : C O O K I E
    ;

BROWSE
    : B R O W S E
    ;

RAW
    : R A W
    ;

XMLDATA
    : X M L D A T A
    ;

XMLSCHEMA
    : X M L S C H E M A
    ;

ELEMENTS
    : E L E M E N T S
    ;

XSINIL
    : X S I N I L
    ;

ABSENT
    : A B S E N T
    ;

EXPLICIT
    : E X P L I C I T
    ;

PATH
    : P A T H
    ;

BASE64
    : B A S E '6' '4'
    ;

ROOT
    : R O O T
    ;

JSON
    : J S O N
    ;

INCLUDE_NULL_VALUES
    : I N C L U D E UL_ N U L L UL_ V A L U E S
    ;

WITHOUT_ARRAY_WRAPPER
    : W I T H O U T UL_ A R R A Y UL_ W R A P P E R
    ;

XMLNAMESPACES
    : X M L N A M E S P A C E S
    ;

APPLY
    : A P P L Y
    ;

STATISTICS
    : S T A T I S T I C S
    ;

FULLSCAN
    : F U L L S C A N
    ;

SAMPLE
    : S A M P L E
    ;

RESAMPLE
    : R E S A M P L E
    ;

NORECOMPUTE
    : N O R E C O M P U T E
    ;

AUTO_DROP
    : A U T O UL_ D R O P
    ;

PERSIST_SAMPLE_PERCENT
    : P E R S I S T UL_ S A M P L E UL_ P E R C E N T
    ;

OPENJSON
    : O P E N J S O N
    ;

OPENROWSET
    : O P E N R O W S E T
    ;

TRY_CAST
    : T R Y UL_ C A S T
    ;

TRY_CONVERT
    : T R Y UL_ C O N V E R T
    ;

OPENQUERY
    : O P E N Q U E R Y
    ;

MATCH
    : M A T C H
    ;

LAST_NODE
    : L A S T UL_ N O D E
    ;

SHORTEST_PATH
    : S H O R T E S T UL_ P A T H
    ;

STRING_AGG
    : S T R I N G UL_ A G G
    ;

LAST_VALUE
    : L A S T UL_ V A L U E
    ;

GRAPH
    : G R A P H
    ;

NODES
    : N O D E S
    ;

VALUE
    : V A L U E
    ;

EXIST
    : E X I S T
    ;

CHANGETABLE
    : C H A N G E T A B L E
    ;

VERSION
    : V E R S I O N
    ;

CHANGES
    : C H A N G E S
    ;

MODEL
    : M O D E L
    ;

AI_GENERATE_EMBEDDINGS
    : A I UL_ G E N E R A T E UL_ E M B E D D I N G S
    ;

PARAMETERS
    : P A R A M E T E R S
    ;

LAG
    : L A G
    ;

LEAD
    : L E A D
    ;

FREETEXTTABLE
    : F R E E T E X T T A B L E
    ;

NTILE
    : N T I L E
    ;

RANK
    : R A N K
    ;

ROLLUP
    : R O L L U P
    ;

PIVOT
    : P I V O T
    ;

UNPIVOT
    : U N P I V O T
    ;

PARSE
    : P A R S E
    ;

TRY_PARSE
    : T R Y UL_ P A R S E
    ;

PATINDEX
    : P A T I N D E X
    ;

PERCENTILE_CONT
    : P E R C E N T I L E UL_ C O N T
    ;

PERCENTILE_DISC
    : P E R C E N T I L E UL_ D I S C
    ;

DENSE_RANK
    : D E N S E UL_ R A N K
    ;

PERCENT_RANK
    : P E R C E N T UL_ R A N K
    ;

CUME_DIST
    : C U M E UL_ D I S T
    ;

FORCESEEK
    : F O R C E S E E K
    ;

FORCESCAN
    : F O R C E S C A N
    ;

NOEXPAND
    : N O E X P A N D
    ;

SPATIAL_WINDOW_MAX_CELLS
    : S P A T I A L UL_ W I N D O W UL_ M A X UL_ C E L L S
    ;

LOOP
    : L O O P
    ;

REDUCE
    : R E D U C E
    ;

REDISTRIBUTE
    : R E D I S T R I B U T E
    ;

PRODUCT
    : P R O D U C T
    ;

CONTAINED
    : C O N T A I N E D
    ;

SYSTEM
    : S Y S T E M
    ;

TABLESAMPLE
    : T A B L E S A M P L E
    ;

LABEL
    : L A B E L
    ;

VECTOR_SEARCH
    : V E C T O R UL_ S E A R C H
    ;

COLUMN
    : C O L U M N
    ;

SIMILAR_TO
    : S I M I L A R UL_ T O
    ;

METRIC
    : M E T R I C
    ;

TOP_N
    : T O P UL_ N
    ;

CUBE
    : C U B E
    ;

GROUPING
    : G R O U P I N G
    ;

SETS
    : S E T S
    ;

DISTRIBUTED_AGG
    : D I S T R I B U T E D UL_ A G G
    ;

PREDICT
    : P R E D I C T
    ;

RUNTIME
    : R U N T I M E
    ;

ONNX
    : O N N X
    ;

WINDOW
    : W I N D O W
    ;

SINGLE
    : S I N G L E
    ;

NODE
    : N O D E
    ;
