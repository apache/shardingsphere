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

 grammar ClickHouseKeyword;

import Alphabet, Keyword, Number;

ADD
    : A D D
    ;

AFTER
    : A F T E R
    ;

ALIAS
    : A L I A S
    ;

ALL
    : A L L
    ;

ALTER
    : A L T E R
    ;

AND
    : A N D
    ;

ANTI
    : A N T I
    ;

ANY
    : A N Y
    ;

ARRAY
    : A R R A Y
    ;

AS
    : A S
    ;

ASCENDING
    : A S C | A S C E N D I N G
    ;

ASOF
    : A S O F
    ;

AST
    : A S T
    ;

ASYNC
    : A S Y N C
    ;

ATTACH
    : A T T A C H
    ;

BETWEEN
    : B E T W E E N
    ;

BOTH
    : B O T H
    ;

BY
    : B Y
    ;

CASE
    : C A S E
    ;

CAST
    : C A S T
    ;

CHECK
    : C H E C K
    ;

CLEAR
    : C L E A R
    ;

CLUSTER
    : C L U S T E R
    ;

CODEC
    : C O D E C
    ;

COLLATE
    : C O L L A T E
    ;

COLUMN
    : C O L U M N
    ;

COMMENT
    : C O M M E N T
    ;

CONSTRAINT
    : C O N S T R A I N T
    ;

CREATE
    : C R E A T E
    ;

CROSS
    : C R O S S
    ;

CUBE
    : C U B E
    ;

CURRENT
    : C U R R E N T
    ;

DATABASE
    : D A T A B A S E
    ;

DATABASES
    : D A T A B A S E S
    ;

DATE
    : D A T E
    ;

DAY
    : D A Y
    ;

DEDUPLICATE
    : D E D U P L I C A T E
    ;

DEFAULT
    : D E F A U L T
    ;

DELAY
    : D E L A Y
    ;

DELETE
    : D E L E T E
    ;

DESC
    : D E S C
    ;

DESCENDING
    : D E S C E N D I N G
    ;

DESCRIBE
    : D E S C R I B E
    ;

DETACH
    : D E T A C H
    ;

DICTIONARIES
    : D I C T I O N A R I E S
    ;

DICTIONARY
    : D I C T I O N A R Y
    ;

DISK
    : D I S K
    ;

DISTINCT
    : D I S T I N C T
    ;

DISTRIBUTED
    : D I S T R I B U T E D
    ;

DROP
    : D R O P
    ;

ELSE
    : E L S E
    ;

END
    : E N D
    ;

ENGINE
    : E N G I N E
    ;

EVENTS
    : E V E N T S
    ;

EXISTS
    : E X I S T S
    ;

EXPLAIN
    : E X P L A I N
    ;

EXPRESSION
    : E X P R E S S I O N
    ;

EXTRACT
    : E X T R A C T
    ;

FETCHES
    : F E T C H E S
    ;

FINAL
    : F I N A L
    ;

FIRST
    : F I R S T
    ;

FLUSH
    : F L U S H
    ;

FOLLOWING
    : F O L L O W I N G
    ;

FOR
    : F O R
    ;

FORMAT
    : F O R M A T
    ;

FREEZE
    : F R E E Z E
    ;

FROM
    : F R O M
    ;

FULL
    : F U L L
    ;

FUNCTION
    : F U N C T I O N
    ;

GLOBAL
    : G L O B A L
    ;

GRANULARITY
    : G R A N U L A R I T Y
    ;

GROUP
    : G R O U P
    ;

HAVING
    : H A V I N G
    ;

HIERARCHICAL
    : H I E R A R C H I C A L
    ;

HOUR
    : H O U R
    ;

ID
    : I D
    ;

IF
    : I F
    ;

ILIKE
    : I L I K E
    ;

IN
    : I N
    ;

INDEX
    : I N D E X
    ;

INF
    : I N F | I N F I N I T Y
    ;

INJECTIVE
    : I N J E C T I V E
    ;

INNER
    : I N N E R
    ;

INSERT
    : I N S E R T
    ;

INTERVAL
    : I N T E R V A L
    ;

INTO
    : I N T O
    ;

IS
    : I S
    ;

IS_OBJECT_ID
    : I S UL_ O B J E C T UL_ I D
    ;

JOIN
    : J O I N
    ;

KEY
    : K E Y
    ;

KILL
    : K I L L
    ;

LAST
    : L A S T
    ;

LAYOUT
    : L A Y O U T
    ;

LEADING
    : L E A D I N G
    ;

LEFT
    : L E F T
    ;

LIFETIME
    : L I F E T I M E
    ;

LIKE
    : L I K E
    ;

LIMIT
    : L I M I T
    ;

LIVE
    : L I V E
    ;

LOCAL
    : L O C A L
    ;

LOGS
    : L O G S
    ;

MATERIALIZE
    : M A T E R I A L I Z E
    ;

MATERIALIZED
    : M A T E R I A L I Z E D
    ;

MAX
    : M A X
    ;

MERGES
    : M E R G E S
    ;

MIN
    : M I N
    ;

MINUTE
    : M I N U T E
    ;

MODIFY
    : M O D I F Y
    ;

MONTH
    : M O N T H
    ;

MOVE
    : M O V E
    ;

MUTATION
    : M U T A T I O N
    ;

// conflicts with macro NAN
NAN_SQL
    : N A N
    ;

NO
    : N O
    ;

NOT
    : N O T
    ;

// conflicts with macro NULL
NULL_SQL
    : N U L L
    ;

NULLS
    : N U L L S
    ;

OFFSET
    : O F F S E T
    ;

ON
    : O N
    ;

OPTIMIZE
    : O P T I M I Z E
    ;

OR
    : O R
    ;

ORDER
    : O R D E R
    ;

OUTER
    : O U T E R
    ;

OUTFILE
    : O U T F I L E
    ;

OVER
    : O V E R
    ;

PARTITION
    : P A R T I T I O N
    ;

POPULATE
    : P O P U L A T E
    ;

PRECEDING
    : P R E C E D I N G
    ;

PREWHERE
    : P R E W H E R E
    ;

PRIMARY
    : P R I M A R Y
    ;

PROJECTION
    : P R O J E C T I O N
    ;

QUARTER
    : Q U A R T E R
    ;

RANGE
    : R A N G E
    ;

RELOAD
    : R E L O A D
    ;

REMOVE
    : R E M O V E
    ;

RENAME
    : R E N A M E
    ;

REPLACE
    : R E P L A C E
    ;

REPLICA
    : R E P L I C A
    ;

REPLICATED
    : R E P L I C A T E D
    ;

RIGHT
    : R I G H T
    ;

ROLLUP
    : R O L L U P
    ;

ROW
    : R O W
    ;

ROWS
    : R O W S
    ;

SAMPLE
    : S A M P L E
    ;

SECOND
    : S E C O N D
    ;

SELECT
    : S E L E C T
    ;

SEMI
    : S E M I
    ;

SENDS
    : S E N D S
    ;

SET
    : S E T
    ;

SETTINGS
    : S E T T I N G S
    ;

SHOW
    : S H O W
    ;

SOURCE
    : S O U R C E
    ;

START
    : S T A R T
    ;

STOP
    : S T O P
    ;

SUBSTRING
    : S U B S T R I N G
    ;

SYNC
    : S Y N C
    ;

SYNTAX
    : S Y N T A X
    ;

SYSTEM
    : S Y S T E M
    ;

TABLE
    : T A B L E
    ;

TABLES
    : T A B L E S
    ;

TEMPORARY
    : T E M P O R A R Y
    ;

TEST
    : T E S T
    ;

THEN
    : T H E N
    ;

TIES
    : T I E S
    ;

TIMEOUT
    : T I M E O U T
    ;

TIMESTAMP
    : T I M E S T A M P
    ;

TO
    : T O
    ;

TOP
    : T O P
    ;

TOTALS
    : T O T A L S
    ;

TRAILING
    : T R A I L I N G
    ;

TRIM
    : T R I M
    ;

TRUNCATE
    : T R U N C A T E
    ;

TTL
    : T T L
    ;

TYPE
    : T Y P E
    ;

UNBOUNDED
    : U N B O U N D E D
    ;

UNION
    : U N I O N
    ;

UPDATE
    : U P D A T E
    ;

USE
    : U S E
    ;

USING
    : U S I N G
    ;

UUID
    : U U I D
    ;

VALUES
    : V A L U E S
    ;

VIEW
    : V I E W
    ;

VOLUME
    : V O L U M E
    ;

WATCH
    : W A T C H
    ;

WEEK
    : W E E K
    ;

WHEN
    : W H E N
    ;

WHERE
    : W H E R E
    ;

WINDOW
    : W I N D O W
    ;

WITH
    : W I T H
    ;

YEAR
    : Y E A R | Y Y Y Y
    ;

ESCAPE
    : E S C A P E
    ;

JSON_FALSE
    : 'false'
    ;

JSON_TRUE
    : 'true'
    ;

UINT8
    : U I N T EIGHT_
    ;

UINT16
    : U I N T ONE_ SIX_
    ;

UINT32
    : U I N T THREE_ TWO_
    ;

UINT64
    : U I N T SIX_ FOUR_
    ;

INT8
    : I N T EIGHT_
    ;

INT16
    : I N T ONE_ SIX_
    ;

INT32
    : I N T THREE_ TWO_
    ;

INT64
    : I N T SIX_ FOUR_
    ;

FLOAT32
    : F L O A T THREE_ TWO_
    ;

FLOAT64
    : F L O A T SIX_ FOUR_
    ;

DECIMAL
    : D E C I M A L
    ;

STRING
    : S T R I N G
    ;

FIXED_STRING
    : F I X E D S T R I N G
    ;

DATETIME
    : D A T E T I M E
    ;

DATETIME64
    : D A T E T I M E SIX_ FOUR_
    ;

ENUM8
    : E N U M EIGHT_
    ;

ENUM16
    : E N U M ONE_ SIX_
    ;

TUPLE
    : T U P L E
    ;

IPV4
    : I P V FOUR_
    ;

IPV6
    : I P V SIX_
    ;

NESTED
    : N E S T E D
    ;

NULLABLE
    : N U L L A B L E
    ;
