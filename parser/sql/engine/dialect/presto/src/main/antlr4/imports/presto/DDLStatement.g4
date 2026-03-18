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

import DMLStatement;

createTable
    : CREATE TABLE ifNotExists? tableName (createDefinitionClause? duplicateAsQueryExpression? | createLikeClause)
    ;

alterTable
    : ALTER TABLE tableName alterTableActions?
    ;

alterTableActions
    : alterCommandList
    ;

alterCommandList
    : alterList
    ;

alterList
    : alterListItem (COMMA_ alterListItem)*
    ;

alterListItem
    : ADD COLUMN? (columnDefinition | LP_ tableElementList RP_)  # addColumn
    | DROP (COLUMN? columnInternalRef=identifier)  # alterTableDrop
    | RENAME COLUMN oldColumn TO newColumn  # renameColumn
    | RENAME (TO | AS)? tableName # alterRenameTable
    ;

duplicateAsQueryExpression
    : AS LP_? select RP_?
    ;

createLikeClause
    : LIKE tableName (INCLUDING PROPERTIES)?
    | LP_ LIKE tableName (INCLUDING PROPERTIES)? RP_
    ;

tableElementList
    : tableElement (COMMA_ tableElement)*
    ;

tableElement
    : columnDefinition
    ;

dropTable
    : DROP tableOrTables ifExists? tableList
    ;

createView
    : CREATE (OR REPLACE)?
      VIEW viewName (LP_ columnNames RP_)?
      AS select
    ;

dropView
    : DROP VIEW ifExists? viewNames
    ;

createDefinitionClause
    : LP_ tableElementList RP_
    | WITH LP_ tableAttributeList RP_
    ;

tableAttributeList
    : tableAttribute (COMMA_ tableAttribute)*
    ;

tableAttribute
    : PARTITIONED_BY EQ_ ARRAY LBT_ string_ (COMMA_ string_)* RBT_
    | BUCKETED_BY EQ_ ARRAY LBT_ string_ (COMMA_ string_)* RBT_
    | BUCKET_COUNT EQ_ NUMBER_
    ;
