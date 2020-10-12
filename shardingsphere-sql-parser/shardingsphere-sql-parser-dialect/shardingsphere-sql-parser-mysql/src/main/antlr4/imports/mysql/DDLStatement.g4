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

grammar DDLStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule, DMLStatement, DALStatement;

createTable
    : CREATE TEMPORARY? TABLE notExistClause_? tableName (createDefinitionClause? tableOptions_? partitionClause? duplicateAsQueryExpression? | createLikeClause)
    ;

partitionClause
    : PARTITION BY partitionTypeDef (PARTITIONS NUMBER_)? subPartitions? partitionDefinitions_?
    ;

partitionTypeDef
    : LINEAR? KEY partitionKeyAlgorithm? columnNames
    | LINEAR? HASH LP_ bitExpr RP_
    | (RANGE | LIST) (LP_ bitExpr RP_ | COLUMNS columnNames )
    ;

subPartitions
    : SUBPARTITION BY LINEAR? ( HASH LP_ bitExpr RP_ | KEY partitionKeyAlgorithm? columnNames ) (SUBPARTITIONS NUMBER_)?
    ;

partitionKeyAlgorithm
    : ALGORITHM EQ_ NUMBER_
    ;

duplicateAsQueryExpression
    : (REPLACE | IGNORE)? AS? LP_? select RP_?
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause? partitionOption?
    ;

partitionOptions
    :partitionOption partitionOption*
    ;

partitionOption
    : ADD PARTITION LP_ partitionDefinition_ RP_
    | DROP PARTITION partitionNames
    | DISCARD PARTITION (partitionNames | ALL) TABLESPACE
    | IMPORT PARTITION (partitionNames | ALL) TABLESPACE
    | TRUNCATE PARTITION (partitionNames | ALL)
    | COALESCE PARTITION NUMBER_
    | REORGANIZE PARTITION partitionNames INTO LP_ partitionDefinitions_ RP_
    | EXCHANGE PARTITION partitionName WITH TABLE tableName ((WITH | WITHOUT) VALIDATION)
    | ANALYZE PARTITION (partitionNames | ALL)
    | CHECK PARTITION (partitionNames | ALL)
    | OPTIMIZE PARTITION (partitionNames | ALL)
    | REBUILD PARTITION (partitionNames | ALL)
    | REPAIR PARTITION (partitionNames | ALL)
    | REMOVE PARTITIONING
    | partitionClause
    ;

partitionNames
    : partitionName (COMMA_ partitionName)*
    ;

dropTable
    : DROP dropTableSpecification_ TABLE existClause_? tableNames (RESTRICT | CASCADE)?
    ;

dropIndex
    : DROP INDEX indexName (ON tableName)?
    (algorithmOption | lockOption)*
    ;

algorithmOption
    : ALGORITHM EQ_? (DEFAULT | INPLACE | COPY)
    ;

lockOption
    : LOCK EQ_? (DEFAULT | NONE | SHARED | EXCLUSIVE)
    ;

truncateTable
    : TRUNCATE TABLE? tableName
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX indexName indexType_? ON tableName keyParts_ indexOption_? 
    (algorithmOption | lockOption)*
    ;

createDatabase
    : CREATE (DATABASE | SCHEMA) notExistClause_? schemaName createDatabaseSpecification_*
    ;

alterDatabase
    : ALTER (DATABASE | SCHEMA) schemaName? alterDatabaseSpecification_*
    ;

createDatabaseSpecification_
    : DEFAULT? (CHARACTER SET | CHARSET) EQ_? characterSetName_
    | DEFAULT? COLLATE EQ_? collationName_
    | DEFAULT? ENCRYPTION EQ_? y_or_n=STRING_
    ;
    
alterDatabaseSpecification_
    : createDatabaseSpecification_ 
    | READ ONLY EQ_? (DEFAULT | NUMBER_)
    ;

dropDatabase
    : DROP (DATABASE | SCHEMA) existClause_? schemaName
    ;

alterInstance
    : ALTER INSTANCE instanceAction
    ;

instanceAction
    : (ENABLE | DISABLE) INNODB_ REDO_LOG_ 
    | ROTATE INNODB_ MASTER KEY 
    | ROTATE BINLOG MASTER KEY 
    | RELOAD TLS (FOR CHANNEL channel)? (NO ROLLBACK ON ERROR)?
    ;

channel
    : MYSQL_MAIN | MYSQL_ADMIN
    ;

createEvent
    : CREATE ownerStatement? EVENT notExistClause_? eventName
      ON SCHEDULE scheduleExpression_
      (ON COMPLETION NOT? PRESERVE)? 
      (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT STRING_)?
      DO routineBody
    ;

alterEvent
    : ALTER ownerStatement? EVENT eventName
      (ON SCHEDULE scheduleExpression_)?
      (ON COMPLETION NOT? PRESERVE)?
      (RENAME TO eventName)? (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT STRING_)?
      (DO routineBody)?
    ;

dropEvent
    :  DROP EVENT existClause_? eventName
    ;

createFunction
    : CREATE ownerStatement?
      FUNCTION functionName LP_ (identifier dataType)? (COMMA_ identifier dataType)* RP_
      RETURNS dataType
      routineOption_*
      routineBody
    ;

alterFunction
    : ALTER FUNCTION functionName routineOption_*
    ;

dropFunction
    : DROP FUNCTION existClause_? functionName
    ;

createProcedure
    : CREATE ownerStatement?
      PROCEDURE functionName LP_ procedureParameter_? (COMMA_ procedureParameter_)* RP_
      routineOption_*
      routineBody
    ;

alterProcedure
    : ALTER PROCEDURE functionName routineOption_*
    ;

dropProcedure
    : DROP PROCEDURE existClause_? functionName
    ;

createServer
    : CREATE SERVER serverName
      FOREIGN DATA WRAPPER wrapperName
      OPTIONS LP_ serverOption_ (COMMA_ serverOption_)* RP_
    ;

alterServer
    : ALTER SERVER serverName OPTIONS
      LP_ serverOption_ (COMMA_ serverOption_)* RP_
    ;

dropServer
    : DROP SERVER existClause_? serverName
    ;

createView
    : CREATE (OR REPLACE)?
      (ALGORITHM EQ_ (UNDEFINED | MERGE | TEMPTABLE))?
      ownerStatement?
      (SQL SECURITY (DEFINER | INVOKER))?
      VIEW viewName (LP_ columnNames RP_)?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

alterView
    : ALTER (ALGORITHM EQ_ (UNDEFINED | MERGE | TEMPTABLE))?
      ownerStatement?
      (SQL SECURITY (DEFINER | INVOKER))?
      VIEW viewName (LP_ columnNames RP_)?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

dropView
    : DROP VIEW existClause_? viewName (COMMA_ viewName)* (RESTRICT | CASCADE)?
    ;

createTablespaceInnodb
    : CREATE (UNDO)? TABLESPACE identifier
      ADD DATAFILE STRING_
      (FILE_BLOCK_SIZE EQ_ fileSizeLiteral_)?
      (ENCRYPTION EQ_ y_or_n=STRING_)?
      (ENGINE EQ_? STRING_)?
    ;

createTablespaceNdb
    : CREATE ( UNDO )? TABLESPACE identifier
      ADD DATAFILE STRING_
      USE LOGFILE GROUP identifier
      (EXTENT_SIZE EQ_? fileSizeLiteral_)?
      (INITIAL_SIZE EQ_? fileSizeLiteral_)?
      (AUTOEXTEND_SIZE EQ_? fileSizeLiteral_)?
      (MAX_SIZE EQ_? fileSizeLiteral_)?
      (NODEGROUP EQ_? identifier)?
      WAIT?
      (COMMENT EQ_? STRING_)?
      (ENGINE EQ_? identifier)?
    ;

alterTablespaceNdb
    : ALTER UNDO? TABLESPACE identifier
      (ADD | DROP) DATAFILE STRING_
      (INITIAL_SIZE EQ_ fileSizeLiteral_)?
      WAIT? (RENAME TO identifier)?
      (ENGINE EQ_? identifier)?
    ;

alterTablespaceInnodb
    : ALTER UNDO? TABLESPACE identifier
      (SET (ACTIVE | INACTIVE))? (ENCRYPTION EQ_? y_or_n=STRING_)
      (RENAME TO identifier)?
      (ENGINE EQ_? identifier)?
    ;

dropTablespace
    : DROP UNDO? TABLESPACE identifier (ENGINE EQ_? identifier)?
    ;

createLogfileGroup
    : CREATE LOGFILE GROUP identifier
      ADD UNDOFILE STRING_
      (INITIAL_SIZE EQ_? fileSizeLiteral_)?
      (UNDO_BUFFER_SIZE EQ_? fileSizeLiteral_)?
      (REDO_BUFFER_SIZE EQ_? fileSizeLiteral_)?
      (NODEGROUP EQ_? identifier)?
      WAIT?
      (COMMENT EQ_? STRING_)?
      (ENGINE EQ_? identifier)?
    ;

alterLogfileGroup
    : ALTER LOGFILE GROUP identifier
      ADD UNDOFILE STRING_
      (INITIAL_SIZE EQ_? fileSizeLiteral_)?
      WAIT? 
      (ENGINE EQ_? identifier)?
    ;

dropLogfileGroup
    : DROP LOGFILE GROUP identifier (ENGINE EQ_? identifier)?
    ;

createTrigger
    :  CREATE ownerStatement? TRIGGER triggerName triggerTime triggerEvent ON tableName FOR EACH ROW triggerOrder? routineBody
    ;

dropTrigger
    : DROP TRIGGER existClause_? (schemaName DOT_)? triggerName
    ;

renameTable
    : RENAME TABLE tableName TO tableName (tableName TO tableName)*
    ;

createDefinitionClause
    : LP_ createDefinition (COMMA_ createDefinition)* RP_
    ;

createDefinition
    : columnDefinition | indexDefinition_ | constraintDefinition | checkConstraintDefinition
    ;

columnDefinition
    : columnName dataType (storageOption* | generatedOption*)
    ;

storageOption
    : dataTypeGenericOption
    | AUTO_INCREMENT
    | DEFAULT expr
    | COLUMN_FORMAT (FIXED | DYNAMIC | DEFAULT)
    | STORAGE (DISK | MEMORY | DEFAULT)
    ;

generatedOption
    : dataTypeGenericOption
    | (GENERATED ALWAYS)? AS expr
    | (VIRTUAL | STORED)
    ;

dataTypeGenericOption
    : primaryKey | UNIQUE KEY? | NOT? NULL | collateClause_ | checkConstraintDefinition | referenceDefinition 
    | COMMENT STRING_ | ON UPDATE CURRENT_TIMESTAMP (LP_ NUMBER_ RP_)*
    ;

checkConstraintDefinition
    : (CONSTRAINT ignoredIdentifier_?)? CHECK LP_ expr RP_ (NOT? ENFORCED)?
    ;

referenceDefinition
    : REFERENCES tableName keyParts_ (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (UPDATE | DELETE) referenceOption_)*
    ;

referenceOption_
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

indexDefinition_
    : (FULLTEXT | SPATIAL)? (INDEX | KEY)? indexName? indexType_? keyParts_ indexOption_*
    ;

indexType_
    : USING (BTREE | HASH)
    ;

keyParts_
    : LP_ keyPart_ (COMMA_ keyPart_)* RP_
    ;

keyPart_
    : (columnName (LP_ NUMBER_ RP_)? | expr) (ASC | DESC)?
    ;

indexOption_
    : KEY_BLOCK_SIZE EQ_? NUMBER_ 
    | indexType_ 
    | WITH PARSER identifier 
    | COMMENT STRING_ 
    | (VISIBLE | INVISIBLE)
    ;

constraintDefinition
    : (CONSTRAINT ignoredIdentifier_?)? (primaryKeyOption | uniqueOption_ | foreignKeyOption)
    ;

primaryKeyOption
    : primaryKey indexType_? keyParts_ indexOption_*
    ;

primaryKey
    : PRIMARY? KEY
    ;

uniqueOption_
    : UNIQUE (INDEX | KEY)? indexName? indexType_? keyParts_ indexOption_*
    ;

foreignKeyOption
    : FOREIGN KEY indexName? columnNames referenceDefinition
    ;

createLikeClause
    : LP_? LIKE tableName RP_?
    ;

createIndexSpecification_
    : (UNIQUE | FULLTEXT | SPATIAL)?
    ;

alterDefinitionClause
    : alterSpecification (COMMA_ alterSpecification)*
    ;

alterSpecification
    : tableOptions_
    | addColumnSpecification
    | addIndexSpecification
    | addConstraintSpecification
    | ADD checkConstraintDefinition
    | DROP CHECK ignoredIdentifier_
    | ALTER CHECK ignoredIdentifier_ NOT? ENFORCED
    | ALGORITHM EQ_? (DEFAULT | INSTANT | INPLACE | COPY)
    | ALTER COLUMN? columnName (SET DEFAULT expr | DROP DEFAULT)
    | ALTER INDEX indexName (VISIBLE | INVISIBLE)
    | changeColumnSpecification
    | modifyColumnSpecification
    | DEFAULT? characterSet_ collateClause_?
    | CONVERT TO characterSet_ collateClause_?
    | (DISABLE | ENABLE) KEYS
    | (DISCARD | IMPORT) TABLESPACE
    | dropColumnSpecification
    | dropIndexSpecification
    | dropPrimaryKeySpecification
    | DROP FOREIGN KEY ignoredIdentifier_
    | FORCE
    | lockOption
    // TODO investigate ORDER BY col_name [, col_name] ...
    | ORDER BY columnNames
    | renameColumnSpecification
    | renameIndexSpecification
    | renameTableSpecification
    | (WITHOUT | WITH) VALIDATION
    | ADD PARTITION LP_ partitionDefinition_ RP_
    | DROP PARTITION ignoredIdentifiers_
    | DISCARD PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
    | IMPORT PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
    | TRUNCATE PARTITION (ignoredIdentifiers_ | ALL)
    | COALESCE PARTITION NUMBER_
    | REORGANIZE PARTITION ignoredIdentifiers_ INTO partitionDefinitions_
    | EXCHANGE PARTITION ignoredIdentifier_ WITH TABLE tableName ((WITH | WITHOUT) VALIDATION)?
    | ANALYZE PARTITION (ignoredIdentifiers_ | ALL)
    | CHECK PARTITION (ignoredIdentifiers_ | ALL)
    | OPTIMIZE PARTITION (ignoredIdentifiers_ | ALL)
    | REBUILD PARTITION (ignoredIdentifiers_ | ALL)
    | REPAIR PARTITION (ignoredIdentifiers_ | ALL)
    | REMOVE PARTITIONING
    | UPGRADE PARTITIONING
    ;

tableOptions_
    : tableOption_ (COMMA_? tableOption_)*
    ;

tableOption_
    : AUTO_INCREMENT EQ_? NUMBER_
    | AVG_ROW_LENGTH EQ_? NUMBER_
    | DEFAULT? (characterSet_ | collateClause_)
    | CHECKSUM EQ_? NUMBER_
    | COMMENT EQ_? STRING_
    | COMPRESSION EQ_? STRING_
    | CONNECTION EQ_? STRING_
    | (DATA | INDEX) DIRECTORY EQ_? STRING_
    | DELAY_KEY_WRITE EQ_? NUMBER_
    | ENCRYPTION EQ_? STRING_
    | ENGINE EQ_? ignoredIdentifier_
    | INSERT_METHOD EQ_? (NO | FIRST | LAST)
    | KEY_BLOCK_SIZE EQ_? NUMBER_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | PACK_KEYS EQ_? (NUMBER_ | DEFAULT)
    | PASSWORD EQ_? STRING_
    | ROW_FORMAT EQ_? (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | SECONDARY_ENGINE EQ_? (NULL | STRING_)
    | STORAGE (DISK | MEMORY)
    | STATS_AUTO_RECALC EQ_? (DEFAULT | NUMBER_)
    | STATS_PERSISTENT EQ_? (DEFAULT | NUMBER_)
    | STATS_SAMPLE_PAGES EQ_? (NUMBER_ | DEFAULT)
    | TABLE_CHECKSUM EQ_ NUMBER_
    | TABLESPACE ignoredIdentifier_ (STORAGE (DISK | MEMORY | DEFAULT))?
    | UNION EQ_? LP_ tableName (COMMA_ tableName)* RP_
    ;

addColumnSpecification
    : ADD COLUMN? (columnDefinition firstOrAfterColumn? | LP_ columnDefinition (COMMA_ columnDefinition)* RP_)
    ;

firstOrAfterColumn
    : FIRST | AFTER columnName
    ;

addIndexSpecification
    : ADD indexDefinition_
    ;

addConstraintSpecification
    : ADD constraintDefinition
    ;

changeColumnSpecification
    : CHANGE COLUMN? columnName columnDefinition firstOrAfterColumn?
    ;

modifyColumnSpecification
    : MODIFY COLUMN? columnDefinition firstOrAfterColumn?
    ;

dropColumnSpecification
    : DROP COLUMN? columnName
    ;

dropIndexSpecification
    : DROP (INDEX | KEY) indexName
    ;

dropPrimaryKeySpecification
    : DROP primaryKey
    ;

renameColumnSpecification
    : RENAME COLUMN columnName TO columnName
    ;

// TODO hongjun: should support renameIndexSpecification on mysql
renameIndexSpecification
    : RENAME (INDEX | KEY) indexName TO indexName
    ;

renameTableSpecification
    : RENAME (TO | AS)? identifier
    ;

partitionDefinitions_
    : LP_ partitionDefinition_ (COMMA_ partitionDefinition_)* RP_
    ;

partitionDefinition_
    : PARTITION partitionName
    (VALUES (LESS THAN partitionLessThanValue_ | IN LP_ partitionValueList_ RP_))?
    partitionDefinitionOption_* 
    (LP_ subpartitionDefinition_ (COMMA_ subpartitionDefinition_)* RP_)?
    ;

partitionLessThanValue_
    : LP_ (expr | partitionValueList_) RP_ | MAXVALUE
    ;

partitionValueList_
    : expr (COMMA_ expr)*
    ;

partitionDefinitionOption_
    : STORAGE? ENGINE EQ_? identifier
    | COMMENT EQ_? STRING_
    | DATA DIRECTORY EQ_? STRING_
    | INDEX DIRECTORY EQ_? STRING_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? identifier
    ;

subpartitionDefinition_
    : SUBPARTITION identifier partitionDefinitionOption_*
    ;

dropTableSpecification_
    : TEMPORARY?
    ;

ownerStatement
    : DEFINER EQ_ (userName | CURRENT_USER ( '(' ')')?)
    ;

scheduleExpression_
    : AT timestampValue (PLUS_ intervalExpression)*
    | EVERY intervalValue
      (STARTS timestampValue (PLUS_ intervalExpression)*)?
      (ENDS timestampValue (PLUS_ intervalExpression)*)?     
    ;

timestampValue
    : CURRENT_TIMESTAMP | stringLiterals | numberLiterals | expr
    ;

routineBody
    : simpleStatement | compoundStatement
    ;

serverOption_
    : HOST STRING_
    | DATABASE STRING_
    | USER STRING_
    | PASSWORD STRING_
    | SOCKET STRING_
    | OWNER STRING_
    | PORT numberLiterals 
    ;

routineOption_
    : COMMENT STRING_                                       
    | LANGUAGE SQL                                              
    | NOT? DETERMINISTIC                                          
    | (CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA)
    | SQL SECURITY (DEFINER | INVOKER)                    
    ;

procedureParameter_
    : (IN | OUT | INOUT)? identifier dataType
    ;

fileSizeLiteral_
    : FILESIZE_LITERAL | numberLiterals
    ; 
    
simpleStatement
    : validStatement
    ;
    
compoundStatement
    : beginStatement
    ;

validStatement
    : (createTable | alterTable | dropTable | truncateTable 
    | insert | replace | update | delete | select | call
    | setVariable | beginStatement | declareStatement | flowControlStatement | cursorStatement | conditionHandlingStatement) SEMI_?
    ;

beginStatement
    : (labelName COLON_)? BEGIN validStatement* END labelName? SEMI_?
    ;

declareStatement
    : DECLARE variable (COMMA_ variable)* dataType (DEFAULT simpleExpr)*
    ;

flowControlStatement
    : caseStatement | ifStatement | iterateStatement | leaveStatement | loopStatement | repeatStatement | returnStatement | whileStatement
    ;
    
caseStatement
    : CASE expr? 
      (WHEN expr THEN validStatement+)+ 
      (ELSE validStatement+)? 
      END CASE
    ;
    
ifStatement
    : IF expr THEN validStatement+
      (ELSEIF expr THEN validStatement+)*
      (ELSE validStatement+)?
      END IF
    ;
    
iterateStatement
    : ITERATE labelName
    ;

leaveStatement
    : LEAVE labelName
    ;
    
loopStatement
    : (labelName COLON_)? LOOP
      validStatement+
      END LOOP labelName?
    ;
    
repeatStatement
    : (labelName COLON_)? REPEAT
      validStatement+
      UNTIL expr
      END REPEAT labelName?
    ;
    
returnStatement
    : RETURN expr
    ;   
    
whileStatement
    : (labelName COLON_)? WHILE expr DO
      validStatement+
      END WHILE labelName?
    ;
    
cursorStatement
    : cursorCloseStatement | cursorDeclareStatement | cursorFetchStatement | cursorOpenStatement 
    ;
    
cursorCloseStatement
    : CLOSE cursorName
    ;
    
cursorDeclareStatement
    : DECLARE cursorName CURSOR FOR select
    ;
    
cursorFetchStatement
    : FETCH ((NEXT)? FROM)? cursorName INTO variable (COMMA_ variable)*
    ;
    
cursorOpenStatement
    : OPEN cursorName
    ;
    
conditionHandlingStatement
    : declareConditionStatement | declareHandlerStatement | getDiagnosticsStatement | resignalStatement | signalStatement 
    ;
    
declareConditionStatement
    : DECLARE conditionName CONDITION FOR conditionValue
    ;
    
declareHandlerStatement
    : DECLARE handlerAction HANDLER FOR conditionValue (COMMA_ conditionValue)* validStatement
    ;

getDiagnosticsStatement
    : GET (CURRENT | STACKED)? DIAGNOSTICS 
      ((statementInformationItem (COMMA_ statementInformationItem)*) 
    | (CONDITION conditionNumber conditionInformationItem (COMMA_ conditionInformationItem)*))
    ;

statementInformationItem
    : variable EQ_ statementInformationItemName
    ;
    
conditionInformationItem
    : variable EQ_ conditionInformationItemName
    ;
    
conditionNumber
    : variable | numberLiterals 
    ;

statementInformationItemName
    : NUMBER
    | ROW_COUNT
    ;
    
conditionInformationItemName
    : CLASS_ORIGIN
    | SUBCLASS_ORIGIN
    | RETURNED_SQLSTATE
    | MESSAGE_TEXT
    | MYSQL_ERRNO
    | CONSTRAINT_CATALOG
    | CONSTRAINT_SCHEMA
    | CONSTRAINT_NAME
    | CATALOG_NAME
    | SCHEMA_NAME
    | TABLE_NAME
    | COLUMN_NAME
    | CURSOR_NAME
    ;
    
handlerAction
    : CONTINUE | EXIT | UNDO
    ;
    
conditionValue
    : numberLiterals | SQLSTATE (VALUE)? stringLiterals | conditionName | SQLWARNING | NOT FOUND | SQLEXCEPTION
    ;
    
resignalStatement
    : RESIGNAL conditionValue?
      (SET signalInformationItem (COMMA_ signalInformationItem)*)?
    ;
    
signalStatement
    : SIGNAL conditionValue
      (SET signalInformationItem (COMMA_ signalInformationItem)*)?
    ;
    
signalInformationItem
    : conditionInformationItemName EQ_ expr
    ;
