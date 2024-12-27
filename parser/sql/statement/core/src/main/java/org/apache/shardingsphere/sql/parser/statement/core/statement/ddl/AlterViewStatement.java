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

package org.apache.shardingsphere.sql.parser.statement.core.statement.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Optional;

/**
 * Alter view statement.
 */
@Getter
@Setter
public abstract class AlterViewStatement extends AbstractSQLStatement implements DDLStatement {
    
    private SimpleTableSegment view;
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return Optional.empty();
    }
    
    /**
     * Set select statement.
     *
     * @param select select statement
     */
    public void setSelect(final SelectStatement select) {
    }
    
    /**
     * Get view definition.
     *
     * @return view definition
     */
    public Optional<String> getViewDefinition() {
        return Optional.empty();
    }
    
    /**
     * Get view definition.
     *
     * @param viewDefinition view definition
     */
    public void setViewDefinition(final String viewDefinition) {
    }
    
    /**
     * Get rename view.
     *
     * @return rename view
     */
    public Optional<SimpleTableSegment> getRenameView() {
        return Optional.empty();
    }
    
    /**
     * Get rename view.
     *
     * @param renameView rename view
     */
    public void setRenameView(final SimpleTableSegment renameView) {
    }
    
    /**
     * Get constraint definition.
     *
     * @return constraint definition
     */
    public Optional<ConstraintDefinitionSegment> getConstraintDefinition() {
        return Optional.empty();
    }
    
    /**
     * Get constraint definition.
     *
     * @param constraintDefinition constraint definition
     */
    public void setConstraintDefinition(final ConstraintDefinitionSegment constraintDefinition) {
    }
}
