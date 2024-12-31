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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Drop table statement.
 */
@Getter
public abstract class DropTableStatement extends AbstractSQLStatement implements DDLStatement {
    
    private final Collection<SimpleTableSegment> tables = new LinkedList<>();
    
    /**
     * Set if exists.
     *
     * @param ifExists if exists or not
     */
    public void setIfExists(final boolean ifExists) {
    }
    
    /**
     * Judge whether contains if exists.
     *
     * @return contains contains if exists or not
     */
    public boolean isIfExists() {
        return false;
    }
    
    /**
     * Set contains cascade.
     *
     * @param containsCascade contains cascade or not
     */
    public void setContainsCascade(final boolean containsCascade) {
    }
    
    /**
     * Judge whether contains cascade.
     *
     * @return contains cascade or not
     */
    public boolean isContainsCascade() {
        return false;
    }
}
