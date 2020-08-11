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

lexer grammar PostgreSQLKeyword;

import Alphabet;

ADMIN
    : A D M I N
    ;

BINARY
    : B I N A R Y
    ;

ESCAPE
    : E S C A P E
    ;

EXISTS
    : E X I S T S
    ;

EXCLUDE
    : E X C L U D E
    ;

MOD
    : M O D
    ;

PARTITION
    : P A R T I T I O N
    ;

ROW
    : R O W
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

GENERATED
    : G E N E R A T E D
    ;

ISOLATION
    : I S O L A T I O N
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

PRIVILEGES
    : P R I V I L E G E S
    ;

READ
    : R E A D
    ;

REFERENCES
    : R E F E R E N C E S
    ;

ROLE
    : R O L E
    ;

ROWS
    : R O W S
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

ACTION
    : A C T I O N
    ;

CACHE
    : C A C H E
    ;

CHARACTERISTICS
    : C H A R A C T E R I S T I C S
    ;

CLUSTER
    : C L U S T E R
    ;

COLLATE
    : C O L L A T E
    ;

COMMENTS
    : C O M M E N T S
    ;

CONCURRENTLY
    : C O N C U R R E N T L Y
    ;

CONNECT
    : C O N N E C T
    ;

CONSTRAINTS
    : C O N S T R A I N T S
    ;

CURRENT_TIMESTAMP
    : C U R R E N T UL_ T I M E S T A M P
    ;

CYCLE
    : C Y C L E
    ;

DATA
    : D A T A
    ;

DATABASE
    : D A T A B A S E
    ;

DEFAULTS
    : D E F A U L T S
    ;

DEFERRABLE
    : D E F E R R A B L E
    ;

DEFERRED
    : D E F E R R E D
    ;

DEPENDS
    : D E P E N D S
    ;

DOMAIN
    : D O M A I N
    ;

EXCLUDING
    : E X C L U D I N G
    ;

EXECUTE
    : E X E C U T E
    ;

EXTENDED
    : E X T E N D E D
    ;

EXTENSION
    : E X T E N S I O N
    ;

EXTERNAL
    : E X T E R N A L
    ;

EXTRACT
    : E X T R A C T
    ;

FILTER
    : F I L T E R
    ;

FIRST
    : F I R S T
    ;

FOLLOWING
    : F O L L O W I N G
    ;

FORCE
    : F O R C E
    ;

GLOBAL
    : G L O B A L
    ;

IDENTITY
    : I D E N T I T Y
    ;

IMMEDIATE
    : I M M E D I A T E
    ;

INCLUDING
    : I N C L U D I N G
    ;

INCREMENT
    : I N C R E M E N T
    ;

INDEXES
    : I N D E X E S
    ;

INHERIT
    : I N H E R I T
    ;

INHERITS
    : I N H E R I T S
    ;

INITIALLY
    : I N I T I A L L Y
    ;

INCLUDE
    : I N C L U D E
    ;

LANGUAGE
    : L A N G U A G E
    ;

LARGE
    : L A R G E
    ;

LAST
    : L A S T
    ;

LOGGED
    : L O G G E D
    ;

MAIN
    : M A I N
    ;

MATCH
    : M A T C H
    ;

MAXVALUE
    : M A X V A L U E
    ;

MINVALUE
    : M I N V A L U E
    ;

NOTHING
    : N O T H I N G
    ;

NULLS
    : N U L L S
    ;

OBJECT
    : O B J E C T
    ;

OIDS
    : O I D S
    ;

ONLY
    : O N L Y
    ;

OVER
    : O V E R
    ;

OWNED
    : O W N E D
    ;

OWNER
    : O W N E R
    ;

PARTIAL
    : P A R T I A L
    ;

PLAIN
    : P L A I N
    ;

PRECEDING
    : P R E C E D I N G
    ;

RANGE
    : R A N G E
    ;

RENAME
    : R E N A M E
    ;

REPLICA
    : R E P L I C A
    ;

RESET
    : R E S E T
    ;

RESTART
    : R E S T A R T
    ;

RESTRICT
    : R E S T R I C T
    ;

ROUTINE
    : R O U T I N E
    ;

RULE
    : R U L E
    ;

SECURITY
    : S E C U R I T Y
    ;

SEQUENCE
    : S E Q U E N C E
    ;

SESSION
    : S E S S I O N
    ;

SESSION_USER
    : S E S S I O N UL_ U S E R
    ;

SHOW
    : S H O W
    ;

SIMPLE
    : S I M P L E
    ;

STATISTICS
    : S T A T I S T I C S
    ;

STORAGE
    : S T O R A G E
    ;

TABLESPACE
    : T A B L E S P A C E
    ;

TEMP
    : T E M P
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

UNLOGGED
    : U N L O G G E D
    ;

USAGE
    : U S A G E
    ;

VALID
    : V A L I D
    ;

VALIDATE
    : V A L I D A T E
    ;

WITHIN
    : W I T H I N
    ;

WITHOUT
    : W I T H O U T
    ;

ZONE
    : Z O N E
    ;

OF
    : O F
    ;

UESCAPE
    : U E S C A P E
    ;

GROUPS
    : G R O U P S
    ;

RECURSIVE
    : R E C U R S I V E
    ;

INT
    : I N T
    ;

INT2
    : I N T [2]
    ;

INT4
    : I N T [4]
    ;

INT8
    : I N T [8]
    ;

FLOAT
    : F L O A T
    ;

FLOAT4
    : F L O A T [4]
    ;

FLOAT8
    : F L O A T [8]
    ;

SMALLSERIAL
    : S M A L L S E R I A L
    ;

SERIAL
    : S E R I A L
    ;

BIGSERIAL
    : B I G S E R I A L
    ;

MONEY
    : M O N E Y
    ;

VARCHAR
    : V A R C H A R
    ;

BYTEA
    : B Y T E A
    ;

ENUM
    : E N U M
    ;

POINT
    : P O I N T
    ;

LINE
    : L I N E
    ;

LSEG
    : L S E G
    ;

BOX
    : B O X
    ;

PATH
    : P A T H
    ;

POLYGON
    : P O L Y G O N
    ;

CIRCLE
    : C I R C L E
    ;

CIDR
    : C I D R
    ;

INET
    : I N E T
    ;

MACADDR
    : M A C A D D R
    ;

MACADDR8
    : M A C A D D R [8]
    ;

BIT
    : B I T
    ;

VARBIT
    : V A R B I T
    ;

TSVECTOR
    : T S V E C T O R
    ;

TSQUERY
    : T S Q U E R Y
    ;

UUID
    : U U I D
    ;

XML
    : X M L
    ;

JSON
    : J S O N
    ;

INT4RANGE
    : I N T [4] R A N G E
    ;

INT8RANGE
    : I N T [8] R A N G E
    ;

NUMRANGE
    : N U M R A N G E
    ;

TSRANGE
    : T S R A N G E
    ;

TSTZRANGE
    : T S T Z R A N G E
    ;

DATERANGE
    : D A T E R A N G E
    ;

TABLESAMPLE
    : T A B L E S A M P L E
    ;

ORDINALITY
    : O R D I N A L I T Y
    ;

CURRENT_ROLE
    : C U R R E N T UL_ R O L E
    ;

CURRENT_CATALOG
    : C U R R E N T UL_ C A T A L O G
    ;

CURRENT_SCHEMA
    : C U R R E N T UL_ S C H E M A
    ;

NORMALIZE
    : N O R M A L I Z E
    ;

OVERLAY
    : O V E R L A Y
    ;

XMLCONCAT
	: X M L C O N C A T
	;

XMLELEMENT
	: X M L E L E M E N T
	;

XMLEXISTS
	: X M L E X I S T S
	;

XMLFOREST
	: X M L F O R E S T
	;

XMLPARSE
	: X M L P A R S E
	;

XMLPI
	: X M L P I
	;

XMLROOT
	: X M L R O O T
	;

XMLSERIALIZE
	: X M L S E R I A L I Z E
	;

TREAT
    : T R E A T
    ;

SETOF
    : S E T O F
    ;

NFC
    : N F C
    ;

NFD
    : N F D
    ;

NFKC
    : N F K C
    ;

NFKD
    : N F K D
    ;

XMLATTRIBUTES
    : X M L A T T R I B U T E S
    ;

REF
    : R E F
    ;

PASSING
    : P A S S I N G
    ;

VERSION
    : V E R S I O N
    ;

YES
    : Y E S
    ;

STANDALONE
    : S T A N D A L O N E
    ;

GREATEST
    : G R E A T E S T
    ;

LEAST
    : L E A S T
    ;

MATERIALIZED
    : M A T E R I A L I Z E D
    ;

OPERATOR
    : O P E R A T O R
    ;

SHARE
    : S H A R E
    ;

ROLLUP
    : R O L L U P
    ;

ILIKE
    : I L I K E
    ;

SIMILAR
    : S I M I L A R
    ;

ISNULL
    : I S N U L L
    ;

NOTNULL
    : N O T N U L L
    ;

SYMMETRIC
    : S Y M M E T R I C
    ;

DOCUMENT
    : D O C U M E N T
    ;

NORMALIZED
    : N O R M A L I Z E D
    ;

ASYMMETRIC
    : A S Y M M E T R I C
    ;

VARIADIC
    : V A R I A D I C
    ;

NOWAIT
    : N O W A I T
    ;

LOCKED
    : L O C K E D
    ;

XMLTABLE
    : X M L T A B L E
    ;

COLUMNS
    : C O L U M N S
    ;

CONTENT
    : C O N T E N T
    ;

STRIP
    : S T R I P
    ;

WHITESPACE
    : W H I T E S P A C E
    ;

XMLNAMESPACES
    : X M L N A M E S P A C E S
    ;

PLACING
    : P L A C I N G
    ;

RETURNING
    : R E T U R N  I N G
    ;

LATERAL
    : L A T E R A L
    ;

NONE
    : N O N E
    ;

ANALYSE
    : A N A L Y S E
    ;

ANALYZE
    : A N A L Y Z E
    ;

CONFLICT
    : C O N F L I C T
    ;

OVERRIDING
    : O V E R R I D I N G
    ;

SYSTEM
    : S Y S T E M
    ;

ABORT
    : A B O R T
    ;

ABSOLUTE
    : A B S O L U T E
    ;

ACCESS
    : A C C E S S
    ;

AFTER
    : A F T E R
    ;

AGGREGATE
    : A G G R E G A T E
    ;

ALSO
    : A L S O
    ;

ATTACH
    : A T T A C H
    ;

ATTRIBUTE
    : A T T R I B U T E
    ;

BACKWARD
    : B A C K W A R D
    ;

BEFORE
    : B E F O R E
    ;

ASSERTION
    : A S S E R T I O N
    ;

ASSIGNMENT
    : A S S I G N M E N T
    ;

CONTINUE
    : C O N T I N U E
    ;

CONVERSION
    : C O N V E R S I O N
    ;

COPY
    : C O P Y
    ;

COST
    : C O S T
    ;

CSV
    : C S V
    ;

CALLED
    : C A L L E D
    ;

CATALOG
    : C A T A L O G
    ;

CHAIN
    : C H A I N
    ;

CHECKPOINT
    : C H E C K P O I N T
    ;

CLASS
    : C L A S S
    ;

CONFIGURATION
    : C O N F I G U R A T I O N
    ;

COMMENT
    : C O M M E N T
    ;

DETACH
    : D E T A C H
    ;

DICTIONARY
    : D I C T I O N A R Y
    ;

EXPRESSION
    : E X P R E S S I O N
    ;

INSENSITIVE
    : I N S E N S I T I V E
    ;

DISCARD
    : D I S C A R D
    ;

OFF
    : O F F
    ;

INSTEAD
    : I N S T E A D
    ;

EXPLAIN
    : E X P L A I N
    ;

INPUT
    : I N P U T
    ;

INLINE
    : I N L I N E
    ;

PARALLEL
    : P A R A L L E L
    ;

LEAKPROOF
    : L E A K P R O O F
    ;

COMMITTED
    : C O M M I T T E D
    ;

ENCODING
    : E N C O D I N G
    ;

IMPLICIT
    : I M P L I C I T
    ;

DELIMITER
    : D E L I M I T E R
    ;

CURSOR
    : C U R S O R
    ;
EACH
    : E A C H
    ;
EVENT
    : E V E N T
    ;

DEALLOCATE
    : D E A L L O C A T E
    ;

CONNECTION
    : C O N N E C T I O N
    ;

DECLARE
    : D E C L A R E
    ;

FAMILY
    : F A M I L Y
    ;

FORWARD
    : F O R W A R D
    ;

EXCLUSIVE
    : E X C L U S I V E
    ;

FUNCTIONS
    : F U N C T I O N S
    ;

LOCATION
    : L O C A T I O N
    ;

LABEL
    : L A B E L
    ;

DELIMITERS
    : D E L I M I T E R S
    ;

HANDLER
    : H A N D L E R
    ;

HEADER
    : H E A D E R
    ;

IMMUTABLE
    : I M M U T A B L E
    ;

GRANTED
    : G R A N T E D
    ;

HOLD
    : H O L D
    ;

MAPPING
    : M A P P I N G
    ;

OLD
    : O L D
    ;

METHOD
    : M E T H O D
    ;

LOAD
    : L O A D
    ;

LISTEN
    : L I S T E N
    ;

MODE
    : M O D E
    ;

MOVE
    : M O V E
    ;

PROCEDURAL
    : P R O C E D U R A L
    ;

PARSER
    : P A R S E R
    ;

PROCEDURES
    : P R O C E D U R E S
    ;

ENCRYPTED
    : E N C R Y P T E D
    ;

PUBLICATION
    : P U B L I C A T I O N
    ;

PROGRAM
    : P R O G R A M
    ;

REFERENCING
    : R E F E R E N C I N G
    ;

PLANS
    : P L A N S
    ;

REINDEX
    : R E I N D E X
    ;

PRIOR
    : P R I O R
    ;

PASSWORD
    : P A S S W O R D
    ;

RELATIVE
    : R E L A T I V E
    ;

QUOTE
    : Q U O T E
    ;

ROUTINES
    : R O U T I N E S
    ;

REPLACE
    : R E P L A C E
    ;

SNAPSHOT
    : S N A P S H O T
    ;

REFRESH
    : R E F R E S H
    ;

PREPARE
    : P R E P A R E
    ;

OPTIONS
    : O P T I O N S
    ;

IMPORT
    : I M P O R T
    ;

INVOKER
    : I N V O K E R
    ;

NEW
    : N E W
    ;

//SKIP
//    : S K I P
//    ;

PREPARED
    : P R E P A R E D
    ;

SCROLL
    : S C R O L L
    ;

SEQUENCES
    : S E Q U E N C E S
    ;

SYSID
    : S Y S I D
    ;

REASSIGN
    : R E A S S I G N
    ;

SERVER
    : S E R V E R
    ;

SUBSCRIPTION
    : S U B S C R I P T I O N
    ;

SEARCH
    : S E A R C H
    ;

SCHEMAS
    : S C H E M A S
    ;

RECHECK
    : R E C H E C K
    ;

POLICY
    : P O L I C Y
    ;

NOTIFY
    : N O T I F Y
    ;

LOCK
    : L O C K
    ;

RELEASE
    : R E L E A S E
    ;

SERIALIZABLE
    : S E R I A L I Z A B L E
    ;

RETURNS
    : R E T U R N S
    ;

STATEMENT
    : S T A T E M E N T
    ;

STDIN
    : S T D I N
    ;

STDOUT
    : S T D O U T
    ;

TABLES
    : T A B L E S
    ;

SUPPORT
    : S U P P O R T
    ;

STABLE
    : S T A B L E
    ;

TEMPLATE
    : T E M P L A T E
    ;

UNENCRYPTED
    : U N E N C R Y P T E D
    ;

VIEWS
    : V I E W S
    ;

UNCOMMITTED
    : U N C O M M I T T E D
    ;

TRANSFORM
    : T R A N S F O R M
    ;

UNLISTEN
    : U N L I S T E N
    ;

TRUSTED
    : T R U S T E D
    ;

VALIDATOR
    : V A L I D A T O R
    ;

UNTIL
    : U N T I L
    ;

VACUUM
    : V A C U U M
    ;

VOLATILE
    : V O L A T I L E
    ;

STORED
    : S T O R E D
    ;

WRITE
    : W R I T E
    ;

STRICT
    : S T R I C T
    ;

TYPES
    : T Y P E S
    ;

WRAPPER
    : W R A P P E R
    ;

WORK
    : W O R K
    ;

FREEZE
    : F R E E Z E
    ;

AUTHORIZATION
    : A U T H O R I Z A T I O N
    ;

VERBOSE
    : V E R B O S E
    ;

PARAM
    : P A R A M
    ;

REPLACCE
    : R E P L A C C E
    ;

TMP
    : T M P
    ;

TMPORARY
    : T M P O R A R Y
    ;

OUT
    : O U T
    ;

INOUT
    : I N O U T
    ;
