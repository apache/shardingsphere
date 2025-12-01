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

import DMLStatement, DALStatement, TCLStatement;

alterStatement
    : alterTable
    | alterDatabase
    | alterProcedure
    | alterFunction
    | alterEvent
    | alterView
    | alterLogfileGroup
    | alterInstance
    | alterServer
    ;

createTable
    // DORIS CHANGED BEGIN
    : CREATE TEMPORARY? TABLE ifNotExists? tableName (createDefinitionClause? createTableOptions? partitionClause? duplicateAsQueryExpression? startTransaction? duplicatekeyClause? commentClause? distributedbyClause? propertiesClause? | createLikeClause)
    // DORIS CHANGED END
    ;

// DORIS ADDED BEGIN
duplicatekeyClause
    : DUPLICATE KEY (LP_ columnName RP_)
    ;

commentClause
    : COMMENT EQ_? literals
    ;

distributedbyClause
    : DISTRIBUTED BY HASH (LP_ columnName RP_) BUCKETS NUMBER_
    ;

propertiesClause
    : PROPERTIES LP_ properties RP_
    ;

properties
    : property (COMMA_ property)*
    ;

property
    : (identifier | SINGLE_QUOTED_TEXT) EQ_? literals
    ;
// DORIS ADDED END

startTransaction
    : START TRANSACTION
    ;

partitionClause
    : PARTITION BY partitionTypeDef (PARTITIONS NUMBER_)? subPartitions? partitionDefinitions?
    ;

partitionTypeDef
    : LINEAR? KEY partitionKeyAlgorithm? LP_ columnNames? RP_
    | LINEAR? HASH LP_ bitExpr RP_
    | (RANGE | LIST) (LP_ bitExpr RP_ | COLUMNS LP_ columnNames RP_ )
    ;

subPartitions
    : SUBPARTITION BY LINEAR? ( HASH LP_ bitExpr RP_ | KEY partitionKeyAlgorithm? LP_ columnNames RP_ ) (SUBPARTITIONS NUMBER_)?
    ;

partitionKeyAlgorithm
    : ALGORITHM EQ_ NUMBER_
    ;

duplicateAsQueryExpression
    : (REPLACE | IGNORE)? AS? LP_? select RP_?
    ;

alterTable
    : ALTER TABLE tableName alterTableActions?
    | ALTER TABLE tableName standaloneAlterTableAction
    ;

standaloneAlterTableAction
    : (alterCommandsModifierList COMMA_)? standaloneAlterCommands
    ;

alterTableActions
    : alterCommandList alterTablePartitionOptions?
    | alterTablePartitionOptions
    ;

alterTablePartitionOptions
    : partitionClause | REMOVE PARTITIONING
    ;

alterCommandList
    : alterCommandsModifierList
    | (alterCommandsModifierList COMMA_)? alterList
    ;

alterList
    : (alterListItem | createTableOptionsSpaceSeparated) (COMMA_ (alterListItem | alterCommandsModifier| createTableOptionsSpaceSeparated))*
    ;

createTableOptionsSpaceSeparated
    : createTableOption+
    ;

alterListItem
    : ADD COLUMN? (columnDefinition place? | LP_ tableElementList RP_)  # addColumn
    | ADD tableConstraintDef  # addTableConstraint
    | CHANGE COLUMN? columnInternalRef=identifier columnDefinition place?  # changeColumn
    | MODIFY COLUMN? columnInternalRef=identifier fieldDefinition place?   # modifyColumn
    | DROP (COLUMN? columnInternalRef=identifier restrict? | FOREIGN KEY columnInternalRef=identifier | PRIMARY KEY | keyOrIndex indexName | CHECK identifier | CONSTRAINT identifier)  # alterTableDrop
    | DISABLE KEYS  # disableKeys
    | ENABLE KEYS   # enableKeys
    | ALTER COLUMN? columnInternalRef=identifier (SET DEFAULT (LP_ expr RP_| literals)| SET visibility | DROP DEFAULT) # alterColumn
    | ALTER INDEX indexName visibility  # alterIndex
    | ALTER CHECK constraintName constraintEnforcement  # alterCheck
    | ALTER CONSTRAINT constraintName constraintEnforcement # alterConstraint
    | RENAME COLUMN oldColumn TO newColumn  # renameColumn
    | RENAME (TO | AS)? tableName # alterRenameTable
    | RENAME keyOrIndex indexName TO indexName  # renameIndex
    | CONVERT TO charset charsetName collateClause?  # alterConvert
    | FORCE  # alterTableForce
    | ORDER BY alterOrderList  # alterTableOrder
    ;

alterOrderList
    : columnRef direction? (COMMA_ columnRef direction?)*
    ;

tableConstraintDef
    : keyOrIndex indexName? indexTypeClause? keyListWithExpression indexOption*
    | FULLTEXT keyOrIndex? indexName? keyListWithExpression fulltextIndexOption*
    | SPATIAL keyOrIndex? indexName? keyListWithExpression commonIndexOption*
    | constraintClause? (PRIMARY KEY | UNIQUE keyOrIndex?) indexName? indexTypeClause? keyListWithExpression indexOption*
    | constraintClause? FOREIGN KEY indexName? keyParts referenceDefinition
    | constraintClause? checkConstraint (constraintEnforcement)?
    ;

alterCommandsModifierList
    : alterCommandsModifier (COMMA_ alterCommandsModifier)*
    ;

alterCommandsModifier
    : alterAlgorithmOption
    | alterLockOption
    | withValidation
    ;

withValidation
    : (WITH | WITHOUT) VALIDATION
    ;

standaloneAlterCommands
    : DISCARD TABLESPACE
    | IMPORT TABLESPACE
    | alterPartition
    | (SECONDARY_LOAD | SECONDARY_UNLOAD)
    ;

alterPartition
    : ADD PARTITION noWriteToBinLog? (partitionDefinitions | PARTITIONS NUMBER_)
    | DROP PARTITION identifierList
    | REBUILD PARTITION noWriteToBinLog? allOrPartitionNameList
    | OPTIMIZE PARTITION noWriteToBinLog? allOrPartitionNameList noWriteToBinLog?
    | ANALYZE PARTITION noWriteToBinLog? allOrPartitionNameList
    | CHECK PARTITION allOrPartitionNameList checkType*
    | REPAIR PARTITION noWriteToBinLog? allOrPartitionNameList repairType*
    | COALESCE PARTITION noWriteToBinLog? NUMBER_
    | TRUNCATE PARTITION allOrPartitionNameList
    | REORGANIZE PARTITION noWriteToBinLog? (identifierList INTO partitionDefinitions)?
    | EXCHANGE PARTITION identifier WITH TABLE tableName withValidation?
    | DISCARD PARTITION allOrPartitionNameList TABLESPACE
    | IMPORT PARTITION allOrPartitionNameList TABLESPACE
    ;

constraintClause
    : CONSTRAINT constraintName?
    ;

tableElementList
    : tableElement (COMMA_ tableElement)*
    ;

tableElement
    : columnDefinition
    | tableConstraintDef
    ;

restrict
    : RESTRICT | CASCADE
    ;

fulltextIndexOption
    : commonIndexOption
    | WITH PARSER identifier
    ;

dropTable
    : DROP TEMPORARY? tableOrTables ifExists? tableList restrict?
    ;

dropIndex
    : DROP INDEX indexName (ON tableName)? algorithmOptionAndLockOption?
    ;

algorithmOptionAndLockOption
    : alterLockOption
    | alterAlgorithmOption
    | alterLockOption alterAlgorithmOption
    | alterAlgorithmOption alterLockOption
    ;

alterAlgorithmOption
    : ALGORITHM EQ_? (DEFAULT | INSTANT | INPLACE | COPY)
    ;

alterLockOption
    : LOCK EQ_? (DEFAULT | NONE | SHARED | EXCLUSIVE)
    ;

truncateTable
    : TRUNCATE TABLE? tableName
    ;

createIndex
    : CREATE createIndexSpecification? INDEX indexName indexTypeClause? ON tableName keyListWithExpression indexOption? algorithmOptionAndLockOption?
    ;

createDatabase
    : CREATE (DATABASE | SCHEMA) ifNotExists? databaseName createDatabaseSpecification_*
    ;

alterDatabase
    : ALTER (DATABASE | SCHEMA) databaseName? alterDatabaseSpecification_*
    ;

createDatabaseSpecification_
    : defaultCharset
    | defaultCollation
    | defaultEncryption
    ;

alterDatabaseSpecification_
    : createDatabaseSpecification_ 
    | READ ONLY EQ_? (DEFAULT | NUMBER_)
    ;

dropDatabase
    : DROP (DATABASE | SCHEMA) ifExists? databaseName
    ;

alterInstance
    : ALTER INSTANCE instanceAction
    ;

instanceAction
    : (ENABLE | DISABLE) INNODB REDO_LOG
    | ROTATE INNODB MASTER KEY
    | ROTATE BINLOG MASTER KEY
    | RELOAD TLS (FOR CHANNEL channel)? (NO ROLLBACK ON ERROR)?
    ;

channel
    : Doris_MAIN | Doris_ADMIN
    ;

createEvent
    : CREATE ownerStatement? EVENT ifNotExists? eventName
      ON SCHEDULE scheduleExpression
      (ON COMPLETION NOT? PRESERVE)? 
      (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT string_)?
      DO routineBody
    ;

alterEvent
    : ALTER ownerStatement? EVENT eventName
      (ON SCHEDULE scheduleExpression)?
      (ON COMPLETION NOT? PRESERVE)?
      (RENAME TO eventName)? (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT string_)?
      (DO routineBody)?
    ;

dropEvent
    :  DROP EVENT ifExists? eventName
    ;

createFunction
    : CREATE ownerStatement?
      FUNCTION functionName LP_ (identifier dataType)? (COMMA_ identifier dataType)* RP_
      RETURNS dataType
      routineOption*
      routineBody
    ;

alterFunction
    : ALTER FUNCTION functionName routineOption*
    ;

dropFunction
    : DROP FUNCTION ifExists? functionName
    ;

createProcedure
    : CREATE ownerStatement?
      PROCEDURE functionName LP_ procedureParameter? (COMMA_ procedureParameter)* RP_
      routineOption*
      routineBody
    ;

alterProcedure
    : ALTER PROCEDURE functionName routineOption*
    ;

dropProcedure
    : DROP PROCEDURE ifExists? functionName
    ;

createServer
    : CREATE SERVER serverName
      FOREIGN DATA WRAPPER wrapperName
      OPTIONS LP_ serverOption (COMMA_ serverOption)* RP_
    ;

alterServer
    : ALTER SERVER serverName OPTIONS
      LP_ serverOption (COMMA_ serverOption)* RP_
    ;

dropServer
    : DROP SERVER ifExists? serverName
    ;

dropEncryptKey
    : DROP ENCRYPTKEY identifier
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

// DORIS ADDED BEGIN
createMaterializedView
    : CREATE MATERIALIZED VIEW ifNotExists? name (LP_ columnDefinition RP_)? buildMode? (REFRESH refreshMethod? refreshTrigger?)? ((DUPLICATE)? KEY keys= LP_ identifierList RP_)? commentClause? partitionClause? distributedbyClause? propertiesClause? AS select
    ;
// DORIS ADDED END

// DORIS ADDED BEGIN
buildMode
    : BUILD (IMMEDIATE | DEFERRED)
    ;
// DORIS ADDED END

// DORIS ADDED BEGIN
refreshMethod
    : COMPLETE | AUTO
    ;
// DORIS ADDED END

// DORIS ADDED BEGIN
refreshTrigger
    : ON MANUAL | ON SCHEDULE refreshSchedule | ON COMMIT
    ;
// DORIS ADDED END

// DORIS ADDED BEGIN
refreshSchedule
    : EVERY intervalValue (STARTS timestampValue)?
    ;
// DORIS ADDED END

alterView
    : ALTER (ALGORITHM EQ_ (UNDEFINED | MERGE | TEMPTABLE))?
      ownerStatement?
      (SQL SECURITY (DEFINER | INVOKER))?
      VIEW viewName (LP_ columnNames RP_)?
      AS select
      (WITH (CASCADED | LOCAL)? CHECK OPTION)?
    ;

dropView
    : DROP VIEW ifExists? viewNames restrict?
    ;

createTablespace
    : CREATE UNDO? TABLESPACE identifier (createTablespaceInnodb | createTablespaceNdb | createTablespaceInnodbAndNdb)*
    ;

createTablespaceInnodb
    : FILE_BLOCK_SIZE EQ_ fileSizeLiteral
    | ENCRYPTION EQ_ y_or_n=string_
    | ENGINE_ATTRIBUTE EQ_? jsonAttribute = string_
    ;

createTablespaceNdb
    : USE LOGFILE GROUP identifier
    | EXTENT_SIZE EQ_? fileSizeLiteral
    | INITIAL_SIZE EQ_? fileSizeLiteral
    | MAX_SIZE EQ_? fileSizeLiteral
    | NODEGROUP EQ_? identifier
    | WAIT
    | COMMENT EQ_? string_
    ;

createTablespaceInnodbAndNdb
    : ADD DATAFILE string_
    | AUTOEXTEND_SIZE EQ_? fileSizeLiteral
    | ENGINE EQ_? engineRef
    ;

alterTablespace
    : alterTablespaceInnodb | alterTablespaceNdb
    ;

alterTablespaceNdb
    : ALTER UNDO? TABLESPACE tablespace=identifier
      (ADD | DROP) DATAFILE string_
      (INITIAL_SIZE EQ_? fileSizeLiteral)?
      WAIT? (RENAME TO renameTableSpace=identifier)?
      (ENGINE EQ_? identifier)?
    ;

alterTablespaceInnodb
    : ALTER UNDO? TABLESPACE tablespace=identifier
      (SET (ACTIVE | INACTIVE))?
      (AUTOEXTEND_SIZE EQ_? fileSizeLiteral)?
      (ENCRYPTION EQ_? y_or_n=string_)?
      (ENGINE_ATTRIBUTE EQ_? jsonAttribute = string_)?
      (RENAME TO renameTablespace=identifier)?
      (ENGINE EQ_? identifier)?
    ;

dropTablespace
    : DROP UNDO? TABLESPACE identifier (ENGINE EQ_? identifier)?
    ;

createLogfileGroup
    : CREATE LOGFILE GROUP identifier
      ADD UNDOFILE string_
      (INITIAL_SIZE EQ_? fileSizeLiteral)?
      (UNDO_BUFFER_SIZE EQ_? fileSizeLiteral)?
      (REDO_BUFFER_SIZE EQ_? fileSizeLiteral)?
      (NODEGROUP EQ_? identifier)?
      WAIT?
      (COMMENT EQ_? string_)?
      (ENGINE EQ_? identifier)?
    ;

alterLogfileGroup
    : ALTER LOGFILE GROUP identifier
      ADD UNDOFILE string_
      (INITIAL_SIZE EQ_? fileSizeLiteral)?
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
    : DROP TRIGGER ifExists? (databaseName DOT_)? triggerName
    ;

renameTable
    : RENAME (TABLE | TABLES) tableName TO tableName (COMMA_ tableName TO tableName)*
    ;

createDefinitionClause
    : LP_ tableElementList RP_
    ;

columnDefinition
    // DORIS CHANGED BEGIN
    : column_name=identifier fieldDefinition referenceDefinition? | NAME EQ_ columnName
    // DORIS CHANGED END
    ;

fieldDefinition
    : dataType (columnAttribute* | collateClause? generatedOption? AS LP_ expr RP_ storedAttribute=(VIRTUAL | STORED)? columnAttribute*)
    ;

columnAttribute
    : NOT? NULL
    | NOT SECONDARY
    | value = DEFAULT (literals | now | LP_ expr RP_)
    | value = ON UPDATE now
    | value = AUTO_INCREMENT
    | value = SERIAL DEFAULT VALUE
    | PRIMARY? value = KEY
    | value = UNIQUE KEY?
    | value = COMMENT string_
    | collateClause
    | value = COLUMN_FORMAT columnFormat
    | value = STORAGE storageMedia
    | value = SRID NUMBER_
    | constraintClause? checkConstraint
    | constraintEnforcement
    | visibility
    | value = ENGINE_ATTRIBUTE EQ_? jsonAttribute = string_
    ;

checkConstraint
    : CHECK LP_ expr RP_
    ;

constraintEnforcement
    : NOT? ENFORCED
    ;

generatedOption
    : GENERATED ALWAYS
    ;

referenceDefinition
    : REFERENCES tableName keyParts (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? onUpdateDelete?
    ;

onUpdateDelete
    : ON UPDATE referenceOption (ON DELETE referenceOption)?
    | ON DELETE referenceOption (ON UPDATE referenceOption)?
    ;

referenceOption
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

indexType
    : BTREE | RTREE | HASH
    ;

indexTypeClause
    : (USING | TYPE) indexType
    ;

keyParts
    : LP_ keyPart (COMMA_ keyPart)* RP_
    ;

keyPart
    : columnName fieldLength? direction?
    ;

keyPartWithExpression
    : keyPart | LP_ expr RP_ direction?
    ;

keyListWithExpression
    : LP_ keyPartWithExpression (COMMA_ keyPartWithExpression)* RP_
    ;

indexOption
    : commonIndexOption | indexTypeClause
    ;

commonIndexOption
    : KEY_BLOCK_SIZE EQ_? NUMBER_
    | COMMENT stringLiterals
    | visibility
    | ENGINE_ATTRIBUTE EQ_? jsonAttribute = string_
    ;

visibility
    : VISIBLE | INVISIBLE
    ;

createLikeClause
    : LP_? LIKE tableName RP_?
    ;

createIndexSpecification
    : UNIQUE | FULLTEXT | SPATIAL
    ;

createTableOptions
    : createTableOption (COMMA_? createTableOption)*
    ;

createTableOption
    : option = ENGINE EQ_? engineRef
    | option = SECONDARY_ENGINE EQ_? (NULL | string_ | identifier)
    | option = MAX_ROWS EQ_? NUMBER_
    | option = MIN_ROWS EQ_? NUMBER_
    | option = AVG_ROW_LENGTH EQ_? NUMBER_
    | option = PASSWORD EQ_? string_
    | option = COMMENT EQ_? string_
    | option = COMPRESSION EQ_? textString
    | option = ENCRYPTION EQ_? textString
    | option = AUTO_INCREMENT EQ_? NUMBER_
    | option = PACK_KEYS EQ_? ternaryOption=(NUMBER_ | DEFAULT)
    | option = (STATS_AUTO_RECALC | STATS_PERSISTENT | STATS_SAMPLE_PAGES) EQ_? ternaryOption=(NUMBER_ | DEFAULT)
    | option = (CHECKSUM | TABLE_CHECKSUM) EQ_? NUMBER_
    | option = DELAY_KEY_WRITE EQ_? NUMBER_
    | option = ROW_FORMAT EQ_? format = (DEFAULT | DYNAMIC | FIXED | COMPRESSED | REDUNDANT | COMPACT)
    | option = UNION EQ_? LP_ tableList RP_
    | defaultCharset
    | defaultCollation
    | option = INSERT_METHOD EQ_? method = (NO| FIRST| LAST)
    | option = DATA DIRECTORY EQ_? textString
    | option = INDEX DIRECTORY EQ_? textString
    | option = TABLESPACE EQ_? identifier
    | option = STORAGE (DISK | MEMORY)
    | option = CONNECTION EQ_? textString
    | option = KEY_BLOCK_SIZE EQ_? NUMBER_
    | option = ENGINE_ATTRIBUTE EQ_? jsonAttribute = string_
    | option = SECONDARY_ENGINE_ATTRIBUTE EQ_ jsonAttribute = string_
    | option = AUTOEXTEND_SIZE EQ_? fileSizeLiteral
    ;

createSRSStatement
    : CREATE OR REPLACE SPATIAL REFERENCE SYSTEM NUMBER_ srsAttribute*
    | CREATE SPATIAL REFERENCE SYSTEM ifNotExists? NUMBER_ srsAttribute*
    ;

dropSRSStatement
    : DROP SPATIAL REFERENCE SYSTEM ifNotExists? NUMBER_
    ;

srsAttribute
    : NAME string_
    | DEFINITION string_
    | ORGANIZATION string_ IDENTIFIED BY NUMBER_
    | DESCRIPTION string_
    ;

place
    : FIRST | AFTER columnName
    ;

partitionDefinitions
    : LP_ partitionDefinition (COMMA_ partitionDefinition)* RP_
    ;

partitionDefinition
    : PARTITION partitionName
    (VALUES (LESS THAN partitionLessThanValue | IN LP_ partitionValueList RP_))?
    partitionDefinitionOption*
    (LP_ subpartitionDefinition (COMMA_ subpartitionDefinition)* RP_)?
    ;

partitionLessThanValue
    : LP_ (expr | partitionValueList) RP_ | MAXVALUE
    ;

partitionValueList
    : expr (COMMA_ expr)*
    ;

partitionDefinitionOption
    : STORAGE? ENGINE EQ_? identifier
    | COMMENT EQ_? string_
    | DATA DIRECTORY EQ_? string_
    | INDEX DIRECTORY EQ_? string_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? identifier
    ;

subpartitionDefinition
    : SUBPARTITION identifier partitionDefinitionOption*
    ;

ownerStatement
    : DEFINER EQ_ (username | CURRENT_USER ( LP_ RP_)?)
    ;

scheduleExpression
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

serverOption
    : HOST string_
    | DATABASE string_
    | USER string_
    | PASSWORD string_
    | SOCKET string_
    | OWNER string_
    | PORT numberLiterals 
    ;

routineOption
    : COMMENT string_
    | LANGUAGE SQL                                              
    | NOT? DETERMINISTIC                                          
    | (CONTAINS SQL | NO SQL | READS SQL DATA | MODIFIES SQL DATA)
    | SQL SECURITY (DEFINER | INVOKER)                    
    ;

procedureParameter
    : (IN | OUT | INOUT)? identifier dataType
    ;

fileSizeLiteral
    : FILESIZE_LITERAL | numberLiterals
    ; 

simpleStatement
    : validStatement
    ;

compoundStatement
    : beginStatement
    ;

validStatement
    : (createTable | alterTable | dropTable | dropDatabase | truncateTable
    | insert | replace | update | delete | select | call
    | createView | prepare | executeStmt | commit | deallocate
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
    : GET (CURRENT | STACKED)? DIAGNOSTICS (
        (statementInformationItem (COMMA_ statementInformationItem)*
        | (CONDITION conditionNumber conditionInformationItem (COMMA_ conditionInformationItem)*))
    )
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
    | Doris_ERRNO
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

prepare
    : PREPARE identifier FROM (stringLiterals | userVariable)
    ;

executeStmt
    : EXECUTE identifier (USING executeVarList)?
    ;

executeVarList
    : userVariable (COMMA_ userVariable)*
    ;

deallocate
    : (DEALLOCATE | DROP) PREPARE identifier
    ;
