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

OR
    : O R
    ;

ORDER
    : O R D E R
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

NO
    : N O
    ;

ON
    : O N
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

SELECT
    : S E L E C T
    ;

INSERT
    : I N S E R T
    ;

UPDATE
    : U P D A T E
    ;

WRITE
    : W R I T E
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

SEQUENCEE
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

VIEW
    : V I E W
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

CASE
    : C A S E
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

ELSE
    : E L S E
    ;

ENCRYPT
    : E N C R Y P T
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

INTO
    : I N T O
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

MINING
    : M I N I N G
    ;

MINVALUE
    : M I N V A L U E
    ;

MODEL
    : M O D E L
    ;

MODIFY
    : M O D I F Y
    ;

MONTH
    : M O N T H
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

NOMAXVALUE
    : N O M A X V A L U E
    ;

NOMINVALUE
    : N O M I N V A L U E
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

SAVEPOINT
    : S A V E P O I N T
    ;

SCOPE
    : S C O P E
    ;

SECOND
    : S E C O N D
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

THEN
    : T H E N
    ;

TRANSLATION
    : T R A N S L A T I O N
    ;

TREAT
    : T R E A T
    ;

TYPE
    : T Y P E
    ;

UNUSED
    : U N U S E D
    ;

USING
    : U S I N G
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

WHEN
    : W H E N
    ;

ZONE
    : Z O N E
    ;
