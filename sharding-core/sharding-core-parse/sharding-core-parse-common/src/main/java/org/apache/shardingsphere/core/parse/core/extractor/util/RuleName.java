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

package org.apache.shardingsphere.core.parse.core.extractor.util;

import lombok.RequiredArgsConstructor;

/**
 * Rule name.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public enum RuleName {
    
    OWNER("Owner"),
    
    NAME("Name"),
    
    SCHEMA_NAME("SchemaName"),
    
    TABLE_NAME("TableName"),
    
    COLUMN_DEFINITION("ColumnDefinition"),
    
    COLUMN_NAME("ColumnName"),
    
    DATA_TYPE("DataType"),
    
    DATA_TYPE_LENGTH("DataTypeLength"),
    
    FIRST_OR_AFTER_COLUMN("FirstOrAfterColumn"),
    
    PRIMARY_KEY("PrimaryKey"),
    
    COLUMN_NAMES("ColumnNames"),
    
    INDEX_NAME("IndexName"),
    
    ADD_COLUMN_SPECIFICATION("AddColumnSpecification"),
    
    CHANGE_COLUMN_SPECIFICATION("ChangeColumnSpecification"),
    
    DROP_COLUMN_SPECIFICATION("DropColumnSpecification"),
    
    MODIFY_COLUMN_SPECIFICATION("ModifyColumnSpecification"),
    
    RENAME_COLUMN_SPECIFICATION("RenameColumnSpecification"),
    
    DROP_PRIMARY_KEY_SPECIFICATION("DropPrimaryKeySpecification"),
    
    // TODO hongjun: parse AddIndex
    ADD_INDEX_SPECIFICATION("AddIndexSpecification"),
    
    RENAME_INDEX_SPECIFICATION("RenameIndexSpecification"),
    
    DROP_INDEX_SPECIFICATION("DropIndexSpecification"),
    
    // TODO hongjun: parse AddConstraint
    ADD_CONSTRAINT_SPECIFICATION("AddConstraintSpecification"),
    
    DROP_CONSTRAINT_CLAUSE("DropConstraintClause"),
    
    MODIFY_COL_PROPERTIES("ModifyColProperties"),
    
    INSERT_VALUES_CLAUSE("InsertValuesClause"),
    
    ON_DUPLICATE_KEY_CLAUSE("OnDuplicateKeyClause"),
    
    SET_ASSIGNMENTS_CLAUSE("SetAssignmentsClause"),
    
    ASSIGNMENT_VALUES("AssignmentValues"),
    
    ASSIGNMENT_VALUE("AssignmentValue"),
    
    ASSIGNMENT("Assignment"),
    
    DUPLICATE_SPECIFICATION("DuplicateSpecification"),
    
    SELECT_ITEMS("SelectItems"),
    
    SELECT_ITEM("SelectItem"),
    
    UNQUALIFIED_SHORTHAND("UnqualifiedShorthand"),
    
    QUALIFIED_SHORTHAND("QualifiedShorthand"),
    
    FUNCTION_CALL("FunctionCall"),
    
    AGGREGATION_FUNCTION("AggregationFunction"),
    
    DISTINCT("Distinct"),
    
    TABLE_CONSTRAINT("TableConstraint"),
    
    TABLE_REFERENCES("TableReferences"),
    
    ALIAS("Alias"),
    
    PARAMETER_MARKER("ParameterMarker"),
    
    LITERALS("Literals"),
    
    NUMBER_LITERALS("NumberLiterals"),
    
    STRING_LITERALS("StringLiterals"),
    
    EXPR("Expr"),
    
    SIMPLE_EXPR("SimpleExpr"),
    
    BIT_EXPR("BitExpr"),
    
    LOGICAL_OPERATOR("LogicalOperator"),
    
    FROM_CLAUSE("FromClause"),
    
    WHERE_CLAUSE("WhereClause"),
    
    GROUP_BY_CLAUSE("GroupByClause"),
    
    ORDER_BY_CLAUSE("OrderByClause"),
    
    ORDER_BY_ITEM("OrderByItem"),
    
    COMPARISON_OPERATOR("ComparisonOperator"),
    
    PREDICATE("Predicate"),
    
    LIMIT_CLAUSE("LimitClause"),
    
    LIMIT_ROW_COUNT("LimitRowCount"),
    
    LIMIT_OFFSET("LimitOffset"),
    
    SUBQUERY("Subquery"),
    
    AUTO_COMMIT_VALUE("AutoCommitValue"),
    
    IMPLICIT_TRANSACTIONS_VALUE("ImplicitTransactionsValue"),
    
    SHOW_LIKE("ShowLike"),
    
    FROM_SCHEMA("FromSchema"),
    
    TOP("Top");
    
    private final String name;
    
    /**
     * Get name.
     * 
     * @return name
     */
    public String getName() {
        return name + "Context";
    }
}
