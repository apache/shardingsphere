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

lexer grammar SQL92Keyword;

import Alphabet, Keyword;

WS
    : [ \t\r\n] + ->skip
    ;

ADA
    : A D A
    ;

C92
    : C
    ;

CATALOG_NAME
    : C A T A L O G UL_ N A M E
    ;

CHARACTER_SET_CATALOG
    : C H A R A C T E R UL_ S E T UL_ C A T A L O G
    ;

CHARACTER_SET_NAME
    : C H A R A C T E R UL_ S E T UL_ N A M E
    ;

CHARACTER_SET_SCHEMA
    : C H A R A C T E R UL_ S E T UL_ S C H E M A
    ;

CLASS_ORIGIN
    : C L A S S UL_ O R I G I N
    ;

COBOL
    : C O B O L
    ;

COLLATION_CATALOG
    : C O L L A T I O N UL_ C A T A L O G
    ;

COLLATION_NAME
    : C O L L A T I O N UL_ N A M E
    ;

COLLATION_SCHEMA
    : C O L L A T I O N UL_ S C H E M A
    ;

COLUMN_NAME
    : C O L U M N UL_ N A M E 
    ;

COMMAND_FUNCTION
    : C O M M A N D UL_ F U N C T I O N
    ;

COMMITTED
    : C O M M I T T E D 
    ;

CONDITION_NUMBER
    : C O N D I T I O N UL_ N U M B E R
    ;

CONNECTION_NAME
    : C O N N E C T I O N UL_ N A M E
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

CURSOR_NAME
    : C U R S O R UL_ N A M E
    ;

DATA
    : D A T A
    ;

DATETIME_INTERVAL_CODE
    : D A T E T I M E UL_ I N T E R V A L UL_ C O D E
    ;

DATETIME_INTERVAL_PRECISION
    : D A T E T I M E UL_ I N T E R V A L UL_ P R E C I S I O N
    ;

DYNAMIC_FUNCTION
    : D Y N A M I C UL_ F U N C T I O N
    ;

FORTRAN
    : F O R T R A N
    ;

LENGTH
    : L E N G T H
    ;

MESSAGE_LENGTH
    : M E S S A G E UL_ L E N G T H
    ;

MESSAGE_OCTET_LENGTH
    : M E S S A G E UL_ O C T E T UL_ L E N G T H
    ;

MESSAGE_TEXT
    : M E S S A G E UL_ T E X T
    ;

MORE92
    : M O R E
    ;

MUMPS
    : M U M P S
    ;

NULLABLE
    : N U L L A B L E
    ;

NUMBER
    : N U M B E R
    ;

PASCAL
    : P A S C A L
    ;

PLI
    : P L I
    ;

REPEATABLE
    : R E P E A T A B L E
    ;

RETURNED_LENGTH
    : R E T U R N E D UL_ L E N G T H
    ;

RETURNED_OCTET_LENGTH
    : R E T U R N E D UL_ O C T E T UL_ L E N G T H
    ;

RETURNED_SQLSTATE
    : R E T U R N E D UL_ S Q L S T A T E
    ;

ROW_COUNT
    : R O W UL_ C O U N T
    ;

SCALE
    : S C A L E 
    ;

SCHEMA_NAME
    : S C H E M A UL_ N A M E
    ;

SERIALIZABLE
    : S E R I A L I Z A B L E
    ;

SERVER_NAME
    : S E R V E R UL_ N A M E
    ;

SUBCLASS_ORIGIN
    : S U B C L A S S UL_ O R I G I N
    ;

TABLE_NAME
    : T A B L E UL_ N A M E
    ;

UNCOMMITTED
    : U N C O M M I T T E D
    ;

UNNAMED
    : U N N A M E D
    ;

ABSOLUTE
    : A B S O L U T E
    ;

ACTION
    : A C T I O N
    ;

ALLOCATE
    : A L L O C A T E
    ;

ARE
    : A R E 
    ;

ASSERTION
    : A S S E R T I O N
    ;

AT
    : A T 
    ;

AUTHORIZATION
    : A U T H O R I Z A T I O N
    ;

BIT
    : B I T
    ;

BIT_LENGTH
    : B I T UL_ L E N G T H
    ;

BOTH
    : B O T H
    ;

CASCADE
    : C A S C A D E
    ;

CATALOG
    : C A T A L O G
    ;

CHAR_LENGTH
    : C H A R UL_ L E N G T H
    ;

CHARACTER_LENGTH
    : C H A R A C T E R UL_ L E N G T H
    ;

CHECK
    : C H E C K
    ;

COALESCE
    : C O A L E S C E
    ;

COLLATE
    : C O L L A T E
    ;

CONNECT
    : C O N N E C T
    ;

CONNECTION
    : C O N N E C T I O N
    ;

CONSTRAINTS
    : C O N S T R A I N T S
    ;

CONTINUE
    : C O N T I N U E
    ;

CONVERT
    : C O N V E R T
    ;

CORRESPONDING
    : C O R R E S P O N D I N G
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

CURSOR
    : C U R S O R
    ;

DEALLOCATE
    : D E A L L O C A T E
    ;

DEC
    : D E C
    ;

DECLARE
    : D E C L A R E
    ;

DEFERRABLE
    : D E F E R R A B L E
    ;

DEFERRED
    : D E F E R R E D
    ;

DESCRIBE
    : D E S C R I B E
    ;

DESCRIPTOR
    : D E S C R I P T O R
    ;

DIAGNOSTICS
    : D I A G N O S T I C S
    ;

DISCONNECT
    : D I S C O N N E C T
    ;

DOMAIN
    : D O M A I N
    ;

END
    : E N D
    ;

END_EXEC
    : E N D '-' E X E C
    ;

ESCAPE
    : E S C A P E
    ;

EXCEPT
    : E X C E P T
    ;

EXCEPTION
    : E X C E P T I O N
    ;

EXEC
    : E X E C
    ;

EXECUTE
    : E X E C U T E
    ;

EXTERNAL
    : E X T E R N A L
    ;

EXTRACT
    : E X T R A C T
    ;

FETCH
    : F E T C H
    ;

FIRST
    : F I R S T
    ;

FOUND
    : F O U N D
    ;

GET
    : G E T
    ;

GLOBAL
    : G L O B A L
    ;

GO
    : G O
    ;

GOTO
    : G O T O
    ;

IDENTITY
    : I D E N T I T Y
    ;

IMMEDIATE
    : I M M E D I A T E
    ;

INDICATOR
    : I N D I C A T O R
    ;

INITIALLY
    : I N I T I A L L Y
    ;

INPUT
    : I N P U T
    ;

INSENSITIVE
    : I N S E N S I T I V E
    ;

INTERSECT
    : I N T E R S E C T
    ;

ISOLATION
    : I S O L A T I O N
    ;

LANGUAGE
    : L A N G U A G E
    ;

LAST
    : L A S T
    ;

LEADING
    : L E A D I N G
    ;

LEVEL
    : L E V E L
    ;

LOWER
    : L O W E R
    ;

MATCH
    : M A T C H
    ;

MODULE
    : M O D U L E
    ;

NATIONAL
    : N A T I O N A L
    ;

NCHAR
    : N C H A R
    ;

NO
    : N O
    ;

NULLIF
    : N U L L I F
    ;

NUMERIC
    : N U M E R I C
    ;

OCTET_LENGTH
    : O C T E T UL_ L E N G T H
    ;

OF
    : O F
    ;

ONLY
    : O N L Y
    ;

OPTION
    : O P T I O N
    ;

OUTPUT
    : O U T P U T
    ;

OVERLAPS
    : O V E R L A P S
    ;

PAD
    : P A D
    ;

PARTIAL
    : P A R T I A L
    ;

PREPARE
    : P R E P A R E
    ;

PRIOR
    : P R I O R
    ;

PRIVILEGES
    : P R I V I L E G E S
    ;

PUBLIC
    : P U B L I C
    ;

READ
    : R E A D
    ;

REFERENCES
    : R E F E R E N C E S
    ;

RELATIVE
    : R E L A T I V E
    ;

RESTRICT
    : R E S T R I C T
    ;

ROWS
    : R O W S
    ;

SCROLL
    : S C R O L L
    ;

SECTION
    : S E C T I O N
    ;

SESSION
    : S E S S I O N
    ;

SESSION_USER
    : S E S S I O N UL_ U S E R
    ;

SIZE
    : S I Z E
    ;

SMALLINT
    : S M A L L I N T
    ;

SOME
    : S O M E
    ;

SPACE
    : S P A C E
    ;

SQLCODE
    : S Q L C O D E
    ;

SQLERROR
    : S Q L E R R O R
    ;

SQLSTATE
    : S Q L S T A T E
    ;

SYSTEM_USER
    : S Y S T E M UL_ U S E R
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

TIMEZONE_HOUR
    : T I M E Z O N E UL_ H O U R
    ;

TIMEZONE_MINUTE
    : T I M E Z O N E UL_ M I N U T E
    ;

TRAILING
    : T R A I L I N G
    ;

TRANSACTION
    : T R A N S A C T I O N
    ;

TRANSLATE
    : T R A N S L A T E
    ;

TRANSLATION
    : T R A N S L A T I O N
    ;

UNKNOWN
    : U N K N O W N
    ;

UPPER
    : U P P E R
    ;

USAGE
    : U S A G E
    ;

USER
    : U S E R
    ;

VALUE
    : V A L U E
    ;

VARYING
    : V A R Y I N G
    ;

WHENEVER
    : W H E N E V E R
    ;

WORK
    : W O R K
    ;

WRITE
    : W R I T E
    ;

ZONE
    : Z O N E
    ;
