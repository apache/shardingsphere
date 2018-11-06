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

package io.shardingsphere.core.parsing.antlr;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Rule name.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum RuleName {
    
    COLUMN_DEFINITION("ColumnDefinitionContext"),
    
    COLUMN_NAME("ColumnNameContext"),
    
    DATA_TYPE("DataTypeContext"),
    
    DATA_TYPE_LENGTH("DataTypeLengthContext"),
    
    FIRST_OR_AFTER_COLUMN("FirstOrAfterColumnContext"),
    
    PRIMARY_KEY("PrimaryKeyContext"),
    
    COLUMN_LIST("ColumnListContext"),
    
    ADD_COLUMN("AddColumnContext"),
    
    CHANGE_COLUMN("ChangeColumnContext"),
    
    DROP_COLUMN("DropColumnContext"),
    
    MODIFY_COLUMN("ModifyColumnContext"),
    
    MODIFY_COL_PROPERTIES("ModifyColPropertiesContext"),
    
    RENAME_COLUMN("RenameColumnContext"),
    
    DROP_PRIMARY_KEY("DropPrimaryKeyContext"),
    
    RENAME_TABLE("RenameTableContext"),
    
    TABLE_NAME("TableNameContext"),
    
    ADD_INDEX("AddIndexContext"),
    
    RENAME_INDEX("RenameIndexContext"),
    
    INDEX_NAME("IndexNameContext"),
    
    DROP_INDEX_REF("DropIndexDefContext"),
    
    ALTER_DROP_INDEX("AlterDropIndexContext"),
    
    ADD_CONSTRAINT("AddConstraintContext"),
    
    ADD_CONSTRAINT_CLAUSE("AddConstraintClauseContext"), 
    
    DROP_CONSTRAINT_CLAUSE("DropConstraintClauseContext"),
    
    ALTER_TABLE_ADD_CONSTRAINT("AlterTableAddConstraintContext"),
    
    TABLE_CONSTRAINT("TableConstraintContext");
    
    private final String name;
}
