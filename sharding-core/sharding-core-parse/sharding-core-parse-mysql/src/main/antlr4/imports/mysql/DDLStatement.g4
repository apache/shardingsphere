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

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

createTable
    : CREATE createTableSpecification_? TABLE tableNotExistClause_ tableName (createDefinitionClause_ | createLikeClause_)
    ;

createIndex
    : CREATE createIndexSpecification_ INDEX indexName indexType_? ON tableName
    ;

alterTable
    : ALTER TABLE tableName alterDefinitionClause_?
    ;

dropTable
    : DROP dropTableSpecification_ TABLE tableExistClause_ tableNames
    ;

dropIndex
    : DROP INDEX dropIndexSpecification_? indexName (ON tableName)?
    ;

truncateTable
    : TRUNCATE TABLE? tableName
    ;

createTableSpecification_
    : TEMPORARY
    ;

tableNotExistClause_
    : (IF NOT EXISTS)?
    ;

createDefinitionClause_
    : LP_ createDefinitions_ RP_
    ;

createDefinitions_
    : createDefinition_ (COMMA_ createDefinition_)*
    ;

createDefinition_
    : columnDefinition | indexDefinition_ | constraintDefinition_ | checkConstraintDefinition_
    ;

columnDefinition
    : columnName dataType (inlineDataType_* | generatedDataType_*)
    ;

inlineDataType_
    : commonDataTypeOption_
    | AUTO_INCREMENT
    | DEFAULT (literals | expr)
    | COLUMN_FORMAT (FIXED | DYNAMIC | DEFAULT)
    | STORAGE (DISK | MEMORY | DEFAULT)
    ;

commonDataTypeOption_
    : primaryKey | UNIQUE KEY? | NOT? NULL | collateClause_ | checkConstraintDefinition_ | referenceDefinition_ | COMMENT STRING_
    ;

checkConstraintDefinition_
    : (CONSTRAINT ignoredIdentifier_?)? CHECK expr (NOT? ENFORCED)?
    ;

referenceDefinition_
    : REFERENCES tableName keyParts_ (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)? (ON (UPDATE | DELETE) referenceOption_)*
    ;

referenceOption_
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

generatedDataType_
    : commonDataTypeOption_
    | (GENERATED ALWAYS)? AS expr
    | (VIRTUAL | STORED)
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
    : KEY_BLOCK_SIZE EQ_? NUMBER_ | indexType_ | WITH PARSER identifier_ | COMMENT STRING_ | VISIBLE | INVISIBLE
    ;

constraintDefinition_
    : (CONSTRAINT ignoredIdentifier_?)? (primaryKeyOption_ | uniqueOption_ | foreignKeyOption_)
    ;

primaryKeyOption_
    : primaryKey indexType_? columnNames indexOption_*
    ;

primaryKey
    : PRIMARY? KEY
    ;

uniqueOption_
    : UNIQUE (INDEX | KEY)? indexName? indexType_? keyParts_ indexOption_*
    ;

foreignKeyOption_
    : FOREIGN KEY indexName? columnNames referenceDefinition_
    ;

createLikeClause_
    : LP_? LIKE tableName RP_?
    ;

createIndexSpecification_
    : (UNIQUE | FULLTEXT | SPATIAL)?
    ;

alterDefinitionClause_
    : alterSpecification_ (COMMA_ alterSpecification_)*
    ;

alterSpecification_
    : tableOptions_
    | addColumnSpecification
    | addIndexSpecification
    | addConstraintSpecification
    | ADD checkConstraintDefinition_
    | DROP CHECK ignoredIdentifier_
    | ALTER CHECK ignoredIdentifier_ NOT? ENFORCED
    | ALGORITHM EQ_? (DEFAULT | INSTANT | INPLACE | COPY)
    | ALTER COLUMN? columnName (SET DEFAULT literals | DROP DEFAULT)
    | ALTER INDEX indexName (VISIBLE | INVISIBLE)
    | changeColumnSpecification
    | DEFAULT? characterSet_ collateClause_?
    | CONVERT TO characterSet_ collateClause_?
    | (DISABLE | ENABLE) KEYS
    | (DISCARD | IMPORT_) TABLESPACE
    | dropColumnSpecification
    | dropIndexSpecification
    | dropPrimaryKeySpecification
    | DROP FOREIGN KEY ignoredIdentifier_
    | FORCE
    | LOCK EQ_? (DEFAULT | NONE | SHARED | EXCLUSIVE)
    | modifyColumnSpecification
    // TODO hongjun investigate ORDER BY col_name [, col_name] ...
    | ORDER BY columnNames
    | renameColumnSpecification
    | renameIndexSpecification
    | renameTableSpecification_
    | (WITHOUT | WITH) VALIDATION
    | ADD PARTITION LP_ partitionDefinition_ RP_
    | DROP PARTITION ignoredIdentifiers_
    | DISCARD PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
    | IMPORT_ PARTITION (ignoredIdentifiers_ | ALL) TABLESPACE
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
    | STATS_AUTO_RECALC EQ_? (DEFAULT | NUMBER_)
    | STATS_PERSISTENT EQ_? (DEFAULT | NUMBER_)
    | STATS_SAMPLE_PAGES EQ_? NUMBER_
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
    : ADD constraintDefinition_
    ;

changeColumnSpecification
    : CHANGE COLUMN? columnName columnDefinition firstOrAfterColumn?
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

modifyColumnSpecification
    : MODIFY COLUMN? columnDefinition firstOrAfterColumn?
    ;

renameColumnSpecification
    : RENAME COLUMN columnName TO columnName
    ;

// TODO hongjun: should support renameIndexSpecification on mysql
renameIndexSpecification
    : RENAME (INDEX | KEY) indexName TO indexName
    ;

renameTableSpecification_
    : RENAME (TO | AS)? identifier_
    ;

partitionDefinitions_
    : LP_ partitionDefinition_ (COMMA_ partitionDefinition_)* RP_
    ;

partitionDefinition_
    : PARTITION identifier_ 
    (VALUES (LESS THAN partitionLessThanValue_ | IN LP_ partitionValueList_ RP_))?
    partitionDefinitionOption_* 
    (LP_ subpartitionDefinition_ (COMMA_ subpartitionDefinition_)* RP_)?
    ;

partitionLessThanValue_
    : LP_ (expr | partitionValueList_) RP_ | MAXVALUE
    ;

partitionValueList_
    : literals (COMMA_ literals)*
    ;

partitionDefinitionOption_
    : STORAGE? ENGINE EQ_? identifier_
    | COMMENT EQ_? STRING_
    | DATA DIRECTORY EQ_? STRING_
    | INDEX DIRECTORY EQ_? STRING_
    | MAX_ROWS EQ_? NUMBER_
    | MIN_ROWS EQ_? NUMBER_
    | TABLESPACE EQ_? identifier_
    ;

subpartitionDefinition_
    : SUBPARTITION identifier_ partitionDefinitionOption_*
    ;

dropTableSpecification_
    : TEMPORARY?
    ;

tableExistClause_
    : (IF EXISTS)?
    ;

dropIndexSpecification_
    : ONLINE | OFFLINE
    ;
