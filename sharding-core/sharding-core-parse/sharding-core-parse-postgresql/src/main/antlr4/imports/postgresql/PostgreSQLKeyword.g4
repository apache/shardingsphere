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

TYPE
    : T Y P E
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
