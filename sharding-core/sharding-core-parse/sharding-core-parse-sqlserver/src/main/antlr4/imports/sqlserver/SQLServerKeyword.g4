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
