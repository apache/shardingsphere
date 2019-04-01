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

ALL
    : A L L
    ;

AND
    : A N D
    ;

ANY
    : A N Y
    ;

ASC
    : A S C
    ;

BETWEEN
    : B E T W E E N
    ;

BINARY
    : B I N A R Y
    ;

BY
    : B Y
    ;

DATE
    : D A T E
    ;

DESC
    : D E S C
    ;

DISTINCT
    : D I S T I N C T
    ;

ESCAPE
    : E S C A P E
    ;

EXISTS
    : E X I S T S
    ;

FALSE
    : F A L S E
    ;

FROM
    : F R O M
    ;

GROUP
    : G R O U P
    ;

HAVING
    : H A V I N G
    ;

IN
    : I N
    ;

IS
    : I S
    ;

KEY
    : K E Y
    ;

LIKE
    : L I K E
    ;

LIMIT
    : L I M I T
    ;

MOD
    : M O D
    ;

NOT
    : N O T
    ;

NULL
    : N U L L
    ;

OFFSET
    : O F F S E T
    ;

OR
    : O R
    ;

ORDER
    : O R D E R
    ;

PARTITION
    : P A R T I T I O N
    ;

PRIMARY
    : P R I M A R Y
    ;

REGEXP
    : R E G E X P
    ;

ROW
    : R O W
    ;

SET
    : S E T
    ;

SOUNDS
    : S O U N D S
    ;

TIME
    : T I M E
    ;

TIMESTAMP
    : T I M E S T A M P
    ;

TRUE
    : T R U E
    ;

UNION
    : U N I O N
    ;

UNKNOWN
    : U N K N O W N
    ;

WHERE
    : W H E R E
    ;

WITH
    : W I T H
    ;

XOR
    : X O R
    ;

ADD
    : A D D
    ;

ALTER
    : A L T E R
    ;

ALWAYS
    : A L W A Y S
    ;

AS
    : A S
    ;

CASCADE
    : C A S C A D E
    ;

CHECK
    : C H E C K
    ;

COLUMN
    : C O L U M N
    ;

COMMIT
    : C O M M I T
    ;

CONSTRAINT
    : C O N S T R A I N T
    ;

CREATE
    : C R E A T E
    ;

CURRENT
    : C U R R E N T
    ;

DAY
    : D A Y
    ;

DEFAULT
    : D E F A U L T
    ;

DELETE
    : D E L E T E
    ;

DISABLE
    : D I S A B L E
    ;

DROP
    : D R O P
    ;

ENABLE
    : E N A B L E
    ;

FOR
    : F O R
    ;

FOREIGN
    : F O R E I G N
    ;

FUNCTION
    : F U N C T I O N
    ;

GENERATED
    : G E N E R A T E D
    ;

GRANT
    : G R A N T
    ;

INDEX
    : I N D E X
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

ON
    : O N
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

REVOKE
    : R E V O K E
    ;

ROLE
    : R O L E
    ;

ROLLBACK
    : R O L L B A C K
    ;

ROWS
    : R O W S
    ;

START
    : S T A R T
    ;

TABLE
    : T A B L E
    ;

TO
    : T O
    ;

TRANSACTION
    : T R A N S A C T I O N
    ;

TRUNCATE
    : T R U N C A T E
    ;

UNIQUE
    : U N I Q U E
    ;

USER
    : U S E R
    ;

YEAR
    : Y E A R
    ;

ACTION
    : A C T I O N
    ;

ARRAY
    : A R R A Y
    ;

BEGIN
    : B E G I N
    ;

BRIN
    : B R I N
    ;

BTREE
    : B T R E E
    ;

CACHE
    : C A C H E
    ;

CAST
    : C A S T
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

CURRENT_USER
    : C U R R E N T UL_ U S E R
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

FULL
    : F U L L
    ;

GIN
    : G I N
    ;

GIST
    : G I S T
    ;

GLOBAL
    : G L O B A L
    ;

HASH
    : H A S H
    ;

HOUR
    : H O U R
    ;

IDENTITY
    : I D E N T I T Y
    ;

IF
    : I F
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

INSERT
    : I N S E R T
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

LOCAL
    : L O C A L
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

MINUTE
    : M I N U T E
    ;

MINVALUE
    : M I N V A L U E
    ;

MONTH
    : M O N T H
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

OF
    : O F
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

PROCEDURE
    : P R O C E D U R E
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

SAVEPOINT
    : S A V E P O I N T
    ;

SCHEMA
    : S C H E M A
    ;

SECOND
    : S E C O N D
    ;

SECURITY
    : S E C U R I T Y
    ;

SELECT
    : S E L E C T
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

SPGIST
    : S P G I S T
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

TRIGGER
    : T R I G G E R
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

UPDATE
    : U P D A T E
    ;

USAGE
    : U S A G E
    ;

USING
    : U S I N G
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
