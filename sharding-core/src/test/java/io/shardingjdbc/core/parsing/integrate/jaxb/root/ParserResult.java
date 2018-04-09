/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.integrate.jaxb.root;

import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ExpectedOrConditions;
import io.shardingjdbc.core.parsing.integrate.jaxb.groupby.ExpectedGroupByColumn;
import io.shardingjdbc.core.parsing.integrate.jaxb.item.ExpectedAggregationSelectItem;
import io.shardingjdbc.core.parsing.integrate.jaxb.limit.ExpectedLimit;
import io.shardingjdbc.core.parsing.integrate.jaxb.orderby.ExpectedOrderByColumn;
import io.shardingjdbc.core.parsing.integrate.jaxb.table.ExpectedTable;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ExpectedTokens;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ParserResult {
    
    @XmlAttribute(name = "sql-case-id")
    private String sqlCaseId;
    
    @XmlAttribute
    @XmlList
    private List<String> parameters = new LinkedList<>();
    
    @XmlElementWrapper
    @XmlElement(name = "table")
    private List<ExpectedTable> tables = new LinkedList<>();
    
    @XmlElement(name = "or-conditions")
    private ExpectedOrConditions orConditions = new ExpectedOrConditions();
    
    @XmlElement
    private ExpectedTokens tokens = new ExpectedTokens();
    
    @XmlElementWrapper(name = "order-by-columns")
    @XmlElement(name = "order-by-column") 
    private List<ExpectedOrderByColumn> orderByColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "group-by-columns")
    @XmlElement(name = "group-by-column") 
    private List<ExpectedGroupByColumn> groupByColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "aggregation-select-items")
    @XmlElement(name = "aggregation-select-item") 
    private List<ExpectedAggregationSelectItem> aggregationSelectItems = new LinkedList<>();
    
    @XmlElement 
    private ExpectedLimit limit;
}
