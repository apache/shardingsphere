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

lexer grammar Keyword;

import Alphabet;

WS
    : [ \t\r\n] + ->skip
    ;

CREATE
    : C R E A T E
    ;

ALTER
    : A L T E R
    ;

DROP
    : D R O P
    ;

SHOW
    : S H O W
    ;

SHARDING
    : S H A R D I N G
    ;

RULE
    :  R U L E
    ;

FROM
    : F R O M
    ;

STORAGE_UNITS
    : S T O R A G E UL_ U N I T S
    ;

KEY_GENERATE_STRATEGY
    : K E Y UL_ G E N E R A T E UL_ S T R A T E G Y
    ;

DEFAULT_TABLE_STRATEGY
    : D E F A U L T UL_ T A B L E UL_ S T R A T E G Y
    ;

TABLE
    : T A B L E
    ;

SHARDING_COLUMN
    : S H A R D I N G UL_ C O L U M N
    ;

SHARDING_COLUMNS
    : S H A R D I N G UL_ C O L U M N S
    ;

TYPE
    : T Y P E
    ;

NAME
    : N A M E
    ;

PROPERTIES
    : P R O P E R T I E S
    ;

COLUMN
    : C O L U M N
    ;

REFERENCE
    : R E F E R E N C E
    ;

RULES
    : R U L E S
    ;

COLUMNS
    : C O L U M N S
    ;

ALGORITHM
    : A L G O R I T H M
    ;

ALGORITHMS
    : A L G O R I T H M S
    ;

HINT
    : H I N T
    ;

DEFAULT
    : D E F A U L T
    ;

DATABASE
    : D A T A B A S E
    ;

SHARDING_ALGORITHM
    : S H A R D I N G UL_ A L G O R I T H M
    ;

STRATEGY
    : S T R A T E G Y
    ;

DATANODES
    : D A T A N O D E S
    ;

DATABASE_STRATEGY
    : D A T A B A S E UL_ S T R A T E G Y
    ;

TABLE_STRATEGY
    : T A B L E UL_ S T R A T E G Y
    ;

NODES
    : N O D E S
    ;

KEY
    : K E Y
    ;

GENERATOR
    : G E N E R A T O R
    ;

GENERATORS
    : G E N E R A T O R S
    ;

KEY_GENERATOR
    : K E Y UL_ G E N E R A T O R
    ;

UNUSED
    : U N U S E D
    ;

USED
    : U S E D
    ;

IF
    : I F
    ;

EXISTS
    : E X I S T S
    ;
    
WITH
    : W I T H
    ;

COUNT
    : C O U N T
    ;

AUDITOR
    : A U D I T O R
    ;

AUDITORS
    : A U D I T O R S
    ;

AUDIT_STRATEGY
    : A U D I T UL_ S T R A T E G Y
    ;

ALLOW_HINT_DISABLE
    : A L L O W UL_ H I N T UL_ D I S A B L E
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;

MOD
    : M O D
    ;

COSID_MOD
    : C O S I D UL_ M O D
    ;

HASH_MOD
    : H A S H UL_ M O D
    ;

VOLUME_RANGE
    : V O L U M E UL_ R A N G E
    ;

BOUNDARY_RANGE
    : B O U N D A R Y UL_ R A N G E
    ;

AUTO_INTERVAL
    : A U T O UL_ I N T E R V A L
    ;

INLINE
    : I N L I N E
    ;

INTERVAL
    : I N T E R V A L
    ;

COSID_INTERVAL
    : C O S I D UL_ I N T E R V A L
    ;

COSID_INTERVAL_SNOWFLAKE
    : C O S I D UL_ I N T E R V A L UL_ S N O W F L A K E
    ;

COMPLEX_INLINE
    : C O M P L E X UL_ I N L I N E
    ;

HINT_INLINE
    : H I N T UL_ I N L I N E
    ;

CLASS_BASED
    : C L A S S UL_ B A S E D
    ;

SNOWFLAKE
    : S N O W F L A K E
    ;

NANOID
    : N A N O I D
    ;

UUID
    : U U I D
    ;

COSID
    : C O S I D
    ;

COSID_SNOWFLAKE
    : C O S I D UL_ S N O W F L A K E
    ;

STANDARD
    : S T A N D A R D
    ;

COMPLEX
    : C O M P L E X
    ;

DML_SHARDING_CONDITIONS
    : D M L UL_ S H A R D I N G UL_ C O N D I T I O N S
    ;

NOT
    : N O T
    ;

NONE
    : N O N E
    ;
