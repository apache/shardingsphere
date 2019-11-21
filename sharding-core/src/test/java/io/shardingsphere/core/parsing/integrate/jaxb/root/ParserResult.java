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

package io.shardingsphere.core.parsing.integrate.jaxb.root;

import com.google.common.base.Splitter;
import io.shardingsphere.core.parsing.integrate.jaxb.condition.ExpectedOrCondition;
import io.shardingsphere.core.parsing.integrate.jaxb.groupby.ExpectedGroupByColumn;
import io.shardingsphere.core.parsing.integrate.jaxb.item.ExpectedSelectItems;
import io.shardingsphere.core.parsing.integrate.jaxb.limit.ExpectedLimit;
import io.shardingsphere.core.parsing.integrate.jaxb.meta.ExpectedTableMetaData;
import io.shardingsphere.core.parsing.integrate.jaxb.orderby.ExpectedOrderByColumn;
import io.shardingsphere.core.parsing.integrate.jaxb.table.ExpectedAlterTable;
import io.shardingsphere.core.parsing.integrate.jaxb.table.ExpectedTable;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedTokens;
import lombok.Getter;
import lombok.Setter;

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
    
    @XmlAttribute
    private String parameters;
    
    @XmlElementWrapper
    @XmlElement(name = "table")
    private List<ExpectedTable> tables = new LinkedList<>();
    
    @XmlElementWrapper
    @XmlElement(name = "schema")
    private List<ExpectedTable> schemas = new LinkedList<>();
    
    @XmlElement(name = "or-condition")
    private ExpectedOrCondition orCondition = new ExpectedOrCondition();
    
    @XmlElement(name = "select-items")
    private ExpectedSelectItems selectItems = new ExpectedSelectItems();
    
    @XmlElement
    private ExpectedTokens tokens = new ExpectedTokens();
    
    @XmlElementWrapper(name = "order-by-columns")
    @XmlElement(name = "order-by-column") 
    private List<ExpectedOrderByColumn> orderByColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "group-by-columns")
    @XmlElement(name = "group-by-column") 
    private List<ExpectedGroupByColumn> groupByColumns = new LinkedList<>();
    
    @XmlElement 
    private ExpectedLimit limit;
    
    @XmlElement
    private ExpectedTableMetaData meta;
    
    @XmlElement(name = "alter-table")
    private ExpectedAlterTable alterTable;
    
    @XmlAttribute(name = "tcl-actual-statement-class-type")
    private String tclActualStatementClassType;
    
    /**
     * Get parameters.
     * 
     * @return parameters
     */
    public List<String> getParameters() {
        return null == parameters ? Collections.<String>emptyList() : Splitter.on(",").trimResults().splitToList(parameters);
    }
}
