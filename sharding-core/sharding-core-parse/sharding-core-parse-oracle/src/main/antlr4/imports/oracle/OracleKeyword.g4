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

REGEXP
    : R E G E X P
    ;

ROW
    : R O W
    ;

ROWS
    : R O W S
    ;

SOUNDS
    : S O U N D S
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

SQL
    : S Q L
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

LOCAL
    : L O C A L
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

NOCACHE
    : N O C A C H E
    ;

NOCYCLE
    : N O C Y C L E
    ;

NOORDER
    : N O O R D E R
    ;

NORELY
    : N O R E L Y
    ;

NOVALIDATE
    : N O V A L I D A T E
    ;

OF
    : O F
    ;

ONLY
    : O N L Y
    ;

PRESERVE
    : P R E S E R V E
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

TYPE
    : T Y P E
    ;

UNUSED
    : U N U S E D
    ;

VALIDATE
    : V A L I D A T E
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
