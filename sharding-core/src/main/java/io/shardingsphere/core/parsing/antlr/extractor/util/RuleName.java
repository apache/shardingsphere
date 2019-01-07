/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor.util;

import lombok.RequiredArgsConstructor;

/**
 * Rule name.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public enum RuleName {
    
    COLUMN_DEFINITION("ColumnDefinition"),
    
    COLUMN_NAME("ColumnName"),
    
    DATA_TYPE("DataType"),
    
    DATA_TYPE_LENGTH("DataTypeLength"),
    
    FIRST_OR_AFTER_COLUMN("FirstOrAfterColumn"),
    
    PRIMARY_KEY("PrimaryKey"),
    
    COLUMN_LIST("ColumnList"),
    
    ADD_COLUMN("AddColumn"),
    
    CHANGE_COLUMN("ChangeColumn"),
    
    DROP_COLUMN("DropColumn"),
    
    MODIFY_COLUMN("ModifyColumn"),
    
    MODIFY_COL_PROPERTIES("ModifyColProperties"),
    
    RENAME_COLUMN("RenameColumn"),
    
    DROP_PRIMARY_KEY("DropPrimaryKey"),
    
    RENAME_TABLE("RenameTable"),
    
    TABLE_NAME("TableName"),
    
    ADD_INDEX("AddIndex"),
    
    RENAME_INDEX("RenameIndex"),
    
    INDEX_NAME("IndexName"),
    
    DROP_INDEX_DEF("DropIndexDef"),
    
    ADD_CONSTRAINT("AddConstraint"),
    
    DROP_CONSTRAINT_CLAUSE("DropConstraintClause"),
    
    SELECT_CLAUSE("SelectClause"),
    
    SELECT_EXPR("SelectExpr"),
    
    SELECT_EXPRS("SelectExprs"),
    
    FUNCTION_CALL("FunctionCall"),
    
    DISTINCT("Distinct"),
    
    TABLE_CONSTRAINT("TableConstraint"),
    
    TABLE_REFERENCES("TableReferences"),
    
    TABLE_REFERENCE("TableReference"),
    
    TABLE_FACTOR("TableFactor"),
    
    JOIN_TABLE("JoinTable"),
    
    JOIN_CONDITION("JoinCondition"),
    
    ALIAS("Alias"),
    
    QUESTION("Question"),
    
    NUMBER("Number"),
    
    STRING("String"),
    
    EXPR("Expr"),
    
    SIMPLE_EXPR("SimpleExpr"),
    
    BIT_EXPR("BitExpr"),
    
    FROM_CLAUSE("FromClause"),
    
    WHERE_CLAUSE("WhereClause"),
    
    GROUP_BY_CLAUSE("GroupByClause"),
    
    ORDER_BY_CLAUSE("OrderByClause"),
    
    ORDER_BY_ITEM("OrderByItem"),
    
    COMPARISON_OPERATOR("ComparisonOperator"),
    
    PREDICATE("Predicate"),
    
    LIMIT_CLAUSE("LimitClause"),
    
    RANGE_CLAUSE("RangeClause"),
    
    SUBQUERY("Subquery"),
    
    SHOW_PARAM("ShowParam"),
    
    AUTO_COMMIT_VALUE("AutoCommitValue");
    
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
