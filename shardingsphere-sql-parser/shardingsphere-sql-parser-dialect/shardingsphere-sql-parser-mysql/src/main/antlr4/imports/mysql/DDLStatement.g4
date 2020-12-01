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

alterStatement
    : alterTable
    | alterDatabase
    | alterProcedure
    | alterFunction
    | alterEvent
    | alterView
    | alterTablespaceInnodb
    | alterTablespaceNdb
    | alterLogfileGroup
    | alterInstance
    | alterServer
    ;

createTable
    : CREATE TEMPORARY? TABLE notExistClause? tableName (createDefinitionClause? createTableOptions? partitionClause? duplicateAsQueryExpression? | createLikeClause)
    ;

partitionClause
    : PARTITION BY partitionTypeDef (PARTITIONS NUMBER_)? subPartitions? partitionDefinitions?
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
    | ALTER COLUMN? columnInternalRef=identifier (SET DEFAULT (LP_ expr RP_| signedLiteral)| DROP DEFAULT) # alterColumn
    | ALTER INDEX indexName visibility  # alterIndex
    | ALTER CHECK identifier constraintEnforcement  # alterCheck
    | ALTER CONSTRAINT identifier constraintEnforcement # alterConstraint
    | RENAME COLUMN columnInternalRef=identifier TO identifier  # renameColumn
    | RENAME (TO | AS)? tableName # alterRenameTable
    | RENAME keyOrIndex indexName TO indexName  # renameIndex
    | CONVERT TO charset charsetName collateClause?  # alterConvert
    | FORCE  # alterTableForce
    | ORDER BY alterOrderList  # alterTableOrder
    ;

alterOrderList
    : identifier direction? (COMMA_ identifier direction?)*
    ;

tableConstraintDef
    : keyOrIndex indexNameAndType? keyListWithExpression indexOption*
    | FULLTEXT keyOrIndex? indexName? keyListWithExpression fulltextIndexOption*
    | SPATIAL keyOrIndex? indexName? keyListWithExpression commonIndexOption*
    | constraintName? (PRIMARY KEY | UNIQUE keyOrIndex?) indexNameAndType? keyListWithExpression indexOption*
    | constraintName? FOREIGN KEY indexName? keyParts referenceDefinition
    | constraintName? checkConstraint (constraintEnforcement)?
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

constraintName
    : CONSTRAINT identifier?
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

partitionNames
    : partitionName (COMMA_ partitionName)*
    ;

dropTable
    : DROP TEMPORARY? tableOrTables existClause? tableList restrict?
    ;

dropIndex
    : DROP INDEX indexName (ON tableName)?
    (alterAlgorithmOption | alterLockOption)*
    ;

alterAlgorithmOption
    : ALGORITHM EQ_? (DEFAULT | INSTANT | INPLACE | COPY)
    ;

alterLockOption
    : LOCK EQ_? (DEFAULT | NONE | 'SHARED' | 'EXCLUSIVE')
    ;

truncateTable
    : TRUNCATE TABLE? tableName
    ;

createIndex
    : CREATE createIndexSpecification? INDEX indexName indexTypeClause? ON tableName keyListWithExpression indexOption?
    (alterAlgorithmOption | alterLockOption)*
    ;

createDatabase
    : CREATE (DATABASE | SCHEMA) notExistClause? schemaName createDatabaseSpecification_*
    ;

alterDatabase
    : ALTER (DATABASE | SCHEMA) schemaName? alterDatabaseSpecification_*
    ;

createDatabaseSpecification_
    : defaultCollation
    | defaultCollation
    | defaultEncryption
    ;
    
alterDatabaseSpecification_
    : createDatabaseSpecification_ 
    | READ ONLY EQ_? (DEFAULT | NUMBER_)
    ;

dropDatabase
    : DROP (DATABASE | SCHEMA) existClause? schemaName
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
    : CREATE ownerStatement? EVENT notExistClause? eventName
      ON SCHEDULE scheduleExpression
      (ON COMPLETION NOT? PRESERVE)? 
      (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT STRING_)?
      DO routineBody
    ;

alterEvent
    : ALTER ownerStatement? EVENT eventName
      (ON SCHEDULE scheduleExpression)?
      (ON COMPLETION NOT? PRESERVE)?
      (RENAME TO eventName)? (ENABLE | DISABLE | DISABLE ON SLAVE)?
      (COMMENT STRING_)?
      (DO routineBody)?
    ;

dropEvent
    :  DROP EVENT existClause? eventName
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
    : DROP FUNCTION existClause? functionName
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
    : DROP PROCEDURE existClause? functionName
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
    : DROP SERVER existClause? serverName
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
    : DROP VIEW existClause? viewNames restrict?
    ;

createTablespaceInnodb
    : CREATE (UNDO)? TABLESPACE identifier
      ADD DATAFILE STRING_
      (FILE_BLOCK_SIZE EQ_ fileSizeLiteral)?
      (ENCRYPTION EQ_ y_or_n=STRING_)?
      (ENGINE EQ_? STRING_)?
    ;

createTablespaceNdb
    : CREATE ( UNDO )? TABLESPACE identifier
      ADD DATAFILE STRING_
      USE LOGFILE GROUP identifier
      (EXTENT_SIZE EQ_? fileSizeLiteral)?
      (INITIAL_SIZE EQ_? fileSizeLiteral)?
      (AUTOEXTEND_SIZE EQ_? fileSizeLiteral)?
      (MAX_SIZE EQ_? fileSizeLiteral)?
      (NODEGROUP EQ_? identifier)?
      WAIT?
      (COMMENT EQ_? STRING_)?
      (ENGINE EQ_? identifier)?
    ;

alterTablespaceNdb
    : ALTER UNDO? TABLESPACE identifier
      (ADD | DROP) DATAFILE STRING_
      (INITIAL_SIZE EQ_ fileSizeLiteral)?
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
      (INITIAL_SIZE EQ_? fileSizeLiteral)?
      (UNDO_BUFFER_SIZE EQ_? fileSizeLiteral)?
      (REDO_BUFFER_SIZE EQ_? fileSizeLiteral)?
      (NODEGROUP EQ_? identifier)?
      WAIT?
      (COMMENT EQ_? STRING_)?
      (ENGINE EQ_? identifier)?
    ;

alterLogfileGroup
    : ALTER LOGFILE GROUP identifier
      ADD UNDOFILE STRING_
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
    : DROP TRIGGER existClause? (schemaName DOT_)? triggerName
    ;

renameTable
    : RENAME TABLE tableName TO tableName (tableName TO tableName)*
    ;

createDefinitionClause
    : LP_ tableElementList RP_
    ;

columnDefinition
    : column_name=identifier fieldDefinition referenceDefinition?
    ;

fieldDefinition
    : dataType (columnAttribute* | collateClause? generatedOption? AS LP_ expr RP_ storedAttribute=(VIRTUAL | STORED)? columnAttribute*)
    ;

columnAttribute
    : NOT? NULL
    | NOT SECONDARY
    | value = DEFAULT (signedLiteral | now | LP_ expr RP_)
    | value = ON UPDATE now
    | value = AUTO_INCREMENT
    | value = SERIAL DEFAULT VALUE
    | PRIMARY? value = KEY
    | value = UNIQUE KEY?
    | value = COMMENT STRING_
    | collateClause
    | value = COLUMN_FORMAT columnFormat
    | value = STORAGE storageMedia
    | value = SRID NUMBER_
    | constraintName? checkConstraint
    | constraintEnforcement
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

indexNameAndType
    : indexName indexTypeClause?
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
    | option = SECONDARY_ENGINE EQ_? (NULL | STRING_ | identifier)
    | option = MAX_ROWS EQ_? NUMBER_
    | option = MIN_ROWS EQ_? NUMBER_
    | option = AVG_ROW_LENGTH EQ_? NUMBER_
    | option = PASSWORD EQ_? STRING_
    | option = COMMENT EQ_? STRING_
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
    | option = ENGINE_ATTRIBUTE EQ_? jsonAttribute = STRING_
    | option = SECONDARY_ENGINE_ATTRIBUTE EQ_ jsonAttribute = STRING_
    ;

createSRSStatement
    : CREATE OR REPLACE SPATIAL REFERENCE SYSTEM NUMBER_ srsAttribute*
    | CREATE SPATIAL REFERENCE SYSTEM notExistClause? NUMBER_ srsAttribute*
    ;

dropSRSStatement
    : DROP SPATIAL REFERENCE SYSTEM notExistClause? NUMBER_
    ;

srsAttribute
    : NAME STRING_
    | DEFINITION STRING_
    | ORGANIZATION STRING_ IDENTIFIED BY NUMBER_
    | DESCRIPTION STRING_
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
    | COMMENT EQ_? STRING_
    | DATA DIRECTORY EQ_? STRING_
    | INDEX DIRECTORY EQ_? STRING_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? identifier
    ;

subpartitionDefinition
    : SUBPARTITION identifier partitionDefinitionOption*
    ;

ownerStatement
    : DEFINER EQ_ (userName | CURRENT_USER ( LP_ RP_)?)
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
    : HOST STRING_
    | DATABASE STRING_
    | USER STRING_
    | PASSWORD STRING_
    | SOCKET STRING_
    | OWNER STRING_
    | PORT numberLiterals 
    ;

routineOption
    : COMMENT STRING_                                       
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
