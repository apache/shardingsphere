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

grammar DMLStatement;

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule;

insert
    : withClause? INSERT INTO insert_target insert_rest opt_on_conflict? returning_clause?
    ;

insert_target
    : qualified_name | qualified_name AS colId
	;

insert_rest
    : select
	| OVERRIDING override_kind VALUE select
	| '(' insert_column_list ')' select
	| '(' insert_column_list ')' OVERRIDING override_kind VALUE select
	| DEFAULT VALUES
	;

override_kind
    : USER | SYSTEM
	;

insert_column_list
    : insert_column_item
	| insert_column_list ',' insert_column_item
	;

insert_column_item
    : colId opt_indirection
	;

opt_on_conflict
    : ON CONFLICT opt_conf_expr DO UPDATE SET set_clause_list whereClause?
	| ON CONFLICT opt_conf_expr DO NOTHING
	;

opt_conf_expr
    : '(' index_params ')' whereClause?
	| ON CONSTRAINT name
	|
	;

update
    : withClause? UPDATE relation_expr_opt_alias SET set_clause_list fromClause? where_or_current_clause? returning_clause?
    ;

set_clause_list
    : set_clause
	| set_clause_list ',' set_clause
	;

set_clause
    : set_target '=' a_expr
	| '(' set_target_list ')' '=' a_expr
	;

set_target
    : colId opt_indirection
	;

set_target_list
    : set_target
	| set_target_list ',' set_target
	;

returning_clause
    : RETURNING target_list
	;

delete
    : withClause? DELETE FROM relation_expr_opt_alias using_clause? where_or_current_clause? returning_clause?
    ;

relation_expr_opt_alias
    : relationExpr
	| relationExpr colId
	| relationExpr AS colId
	;

using_clause
    : USING fromList
	;

select
    : select_no_parens | select_with_parens
    ;

select_with_parens
    : '(' select_no_parens ')' | '(' select_with_parens ')'
	;

select_no_parens
    : selectClauseN
    | selectClauseN sort_clause
	| selectClauseN sort_clause? for_locking_clause select_limit?
	| selectClauseN sort_clause? select_limit for_locking_clause?
	| withClause selectClauseN
	| withClause selectClauseN sort_clause
	| withClause selectClauseN sort_clause? for_locking_clause select_limit?
	| withClause selectClauseN sort_clause? select_limit for_locking_clause?
	;

selectClauseN
    : simple_select
    | select_with_parens
    | selectClauseN UNION all_or_distinct selectClauseN
    | selectClauseN INTERSECT all_or_distinct selectClauseN
    | selectClauseN EXCEPT all_or_distinct selectClauseN
	;

simple_select
    : SELECT ALL? target_list? into_clause? fromClause? whereClause? group_clause? havingClause? window_clause?
	| SELECT distinct_clause target_list into_clause? fromClause? whereClause? group_clause? havingClause? window_clause?
	| values_clause
	| TABLE relationExpr
	;

withClause
    : WITH cte_list
	| WITH RECURSIVE cte_list
	;

into_clause
    : INTO optTempTableName
	;

optTempTableName
    : TEMPORARY TABLE? qualified_name
	| TEMP TABLE? qualified_name
	| LOCAL TEMPORARY TABLE? qualified_name
	| LOCAL TEMP TABLE? qualified_name
	| GLOBAL TEMPORARY TABLE? qualified_name
	| GLOBAL TEMP TABLE? qualified_name
	| UNLOGGED TABLE? qualified_name
	| TABLE? qualified_name
	| qualified_name
	;

cte_list
    : common_table_expr
	| cte_list ',' common_table_expr
	;

common_table_expr
    :  name optNameList AS opt_materialized '(' preparableStmt ')'
	;

opt_materialized
    : MATERIALIZED | NOT MATERIALIZED |
	;

optNameList
    :'(' nameList ')' |
	;

preparableStmt
    : select
	| insert
	| update
	| delete
	;

for_locking_clause
    : for_locking_items | FOR READ ONLY
	;

for_locking_items
    : for_locking_item
	| for_locking_items for_locking_item
	;

for_locking_item
    : for_locking_strength locked_rels_list? nowait_or_skip?
	;

nowait_or_skip
    : NOWAIT
	| 'skip' LOCKED
	;

for_locking_strength
    : FOR UPDATE
	| FOR NO KEY UPDATE
	| FOR SHARE
	| FOR KEY SHARE
	;

locked_rels_list
    : OF qualified_name_list
	;

qualified_name_list
    : qualified_name
	| qualified_name_list ',' qualified_name
	;

qualified_name
    : colId | colId indirection
	;



select_limit
    : limit_clause offset_clause
	| offset_clause limit_clause
	| limit_clause
	| offset_clause
	;

values_clause
    : VALUES '(' exprList ')'
	| values_clause ',' '(' exprList ')'
	;

limit_clause
    : LIMIT select_limit_value
	| LIMIT select_limit_value ',' select_offset_value
	| FETCH first_or_next select_fetch_first_value row_or_rows ONLY
	| FETCH first_or_next select_fetch_first_value row_or_rows WITH TIES
	| FETCH first_or_next row_or_rows ONLY
	| FETCH first_or_next row_or_rows WITH TIES
	;

offset_clause
    : OFFSET select_offset_value
	| OFFSET select_fetch_first_value row_or_rows
	;

select_limit_value
    : a_expr
	| ALL
	;

select_offset_value
    : a_expr
	;

select_fetch_first_value
    : c_expr
	| '+' NUMBER_
	| '-' NUMBER_
	;

row_or_rows
    : ROW | ROWS
	;

first_or_next
    : FIRST | NEXT
	;

target_list
    : target_el
	| target_list ',' target_el
	;

target_el
    : colId DOT_ASTERISK_
    | a_expr AS identifier
	| a_expr identifier
	| a_expr
	| '*'
	;

group_clause
    : GROUP BY group_by_list
	;

group_by_list
    : group_by_item (',' group_by_item)*
	;

group_by_item
    : a_expr
	| empty_grouping_set
	| cube_clause
	| rollup_clause
	| grouping_sets_clause
	;

empty_grouping_set
    : '(' ')'
	;

rollup_clause
    : ROLLUP '(' exprList ')'
	;

cube_clause
    : CUBE '(' exprList ')'
	;

grouping_sets_clause
    : GROUPING SETS '(' group_by_list ')'
	;

window_clause
    : WINDOW window_definition_list
	;

window_definition_list
    : window_definition
	| window_definition_list ',' window_definition
	;

window_definition
    : colId AS window_specification
	;

window_specification
    : '(' existing_window_name? partition_clause? sort_clause? frame_clause? ')'
	;

existing_window_name
    : colId
	;

partition_clause
    : PARTITION BY exprList
	;

frame_clause
    : RANGE frame_extent opt_window_exclusion_clause
	| ROWS frame_extent opt_window_exclusion_clause
	| GROUPS frame_extent opt_window_exclusion_clause
	;

frame_extent
    : frame_bound
	| BETWEEN frame_bound AND frame_bound
	;

frame_bound
    : UNBOUNDED PRECEDING
	| UNBOUNDED FOLLOWING
	| CURRENT ROW
	| a_expr PRECEDING
	| a_expr FOLLOWING
	;

opt_window_exclusion_clause
    : EXCLUDE CURRENT ROW
	| EXCLUDE GROUP
	| EXCLUDE TIES
	| EXCLUDE NO OTHERS
	|
	;

alias
    : identifier | STRING_
    ;

fromClause
    : FROM fromList
    ;

fromList
    : tableReference | fromList ',' tableReference
    ;

tableReference
    : relationExpr aliasClause?
	| relationExpr aliasClause? tablesampleClause
	| functionTable funcAliasClause?
	| LATERAL functionTable funcAliasClause?
	| xmlTable aliasClause?
	| LATERAL xmlTable aliasClause?
	| select_with_parens aliasClause?
	| LATERAL select_with_parens aliasClause?
	| tableReference joined_table
	| tableReference '(' joined_table ')' aliasClause
	;

joined_table
    : CROSS JOIN tableReference
	| join_type JOIN tableReference join_qual
	| JOIN tableReference join_qual
	| NATURAL join_type JOIN tableReference
	| NATURAL JOIN tableReference
	;

join_type
    : FULL join_outer?
	| LEFT join_outer?
	| RIGHT join_outer?
	| INNER
	;

join_outer
    : OUTER
	;

join_qual
    : USING '(' nameList ')'
	| ON a_expr
	;


relationExpr
    : qualifiedName
    | qualifiedName ASTERISK_
    | ONLY qualifiedName
    | ONLY LP_ qualifiedName RP_
    ;

whereClause
    : WHERE a_expr
    ;

where_or_current_clause
    : whereClause
	| WHERE CURRENT OF cursor_name
	;


havingClause
    : HAVING a_expr
    ;
