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

import io.shardingjdbc.core.parsing.integrate.jaxb.condition.ConditionAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.groupby.GroupByColumnAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.item.AggregationSelectItemAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.limit.LimitAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.orderby.OrderByColumnAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.table.TableAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.SQLTokenAsserts;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ParserAssert {
    
    @XmlAttribute(name = "sql-case-id")
    private String sqlCaseId;
    
    @XmlAttribute
    @XmlList
    private List<String> parameters;
    
    @XmlElementWrapper
    @XmlElement(name = "table")
    private List<TableAssert> tables;
    
    @XmlElementWrapper
    @XmlElement(name = "condition") 
    private List<ConditionAssert> conditions;
    
    @XmlElement
    private SQLTokenAsserts tokens = new SQLTokenAsserts();
    
    @XmlElementWrapper(name = "order-by-columns")
    @XmlElement(name = "order-by-column") 
    private List<OrderByColumnAssert> orderByColumns;
    
    @XmlElementWrapper(name = "group-by-columns")
    @XmlElement(name = "group-by-column") 
    private List<GroupByColumnAssert> groupByColumns;
    
    @XmlElementWrapper(name = "aggregation-select-items")
    @XmlElement(name = "aggregation-select-item") 
    private List<AggregationSelectItemAssert> aggregationSelectItems;
    
    @XmlElement 
    private LimitAssert limit;
}
