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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.root;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.insert.ExpectedInsertColumnsAndValues;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedOrderBy;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.pagination.ExpectedLimit;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedWhere;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.projection.ExpectedProjections;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.set.ExpectedSetAssignment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.table.ExpectedAlterTable;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.table.ExpectedTable;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.token.ExpectedTokens;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ParserResult {
    
    @XmlAttribute(name = "sql-case-id")
    private String sqlCaseId;
    
    @XmlAttribute(name = "long-sql")
    private boolean longSQL;
    
    @XmlAttribute
    private String parameters;
    
    @XmlElementWrapper
    @XmlElement(name = "table")
    private List<ExpectedTable> tables = new LinkedList<>();
    
    @XmlElement(name = "projections")
    private ExpectedProjections projections = new ExpectedProjections();
    
    @XmlElementWrapper
    @XmlElement(name = "schema")
    private List<ExpectedTable> schemas = new LinkedList<>();
    
    @XmlElement
    private ExpectedTokens tokens = new ExpectedTokens();
    
    @XmlElement(name = "where")
    private ExpectedWhere where;
    
    @XmlElement(name = "group-by")
    private ExpectedOrderBy groupBy;
    
    @XmlElement(name = "order-by")
    private ExpectedOrderBy orderBy;
    
    @XmlElement
    private ExpectedLimit limit;
    
    @XmlElement(name = "set-assignment")
    private ExpectedSetAssignment setAssignment;
    
    @XmlElement(name = "alter-table")
    private ExpectedAlterTable alterTable;
    
    @XmlElement(name = "insert-columns-and-values")
    private ExpectedInsertColumnsAndValues insertColumnsAndValues = new ExpectedInsertColumnsAndValues();
    
    @XmlAttribute(name = "tcl-actual-statement-class-type")
    private String tclActualStatementClassType;
    
    @XmlAttribute(name = "auto-commit")
    private boolean autoCommit;
    
    @XmlAttribute(name = "lock-clause")
    private boolean lockClause;
    
    /**
     * Get parameters.
     * 
     * @return parameters
     */
    public List<String> getParameters() {
        return null == parameters ? Collections.<String>emptyList() : Splitter.on(",").trimResults().splitToList(parameters);
    }
}
