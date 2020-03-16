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

EDITION
    : E D I T I O N
    ;

ELEMENT
    : E L E M E N T
    ;

END
    : E N D
    ;

EXCEPTIONS
    : E X C E P T I O N S
    ;

FORCE
    : F O R C E
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
