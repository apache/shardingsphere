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
import io.shardingjdbc.core.parsing.integrate.jaxb.token.GeneratedKeyTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.IndexTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ItemsTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.MultipleInsertValuesTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OffsetTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OrderByTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.RowCountTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.SQLTokenAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.TableTokenAssert;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import java.util.ArrayList;
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
    
    @XmlElementWrapper(name = "table-tokens")
    @XmlElement(name = "table-token")
    private List<TableTokenAssert> tableTokens;
    
    @XmlElement(name = "index-token")
    private IndexTokenAssert indexToken;
    
    @XmlElement(name = "items-token")
    private ItemsTokenAssert itemsToken;
    
    @XmlElement(name = "generated-key-token")
    private GeneratedKeyTokenAssert generatedKeyToken;
    
    @XmlElement(name = "multiple-insert-values-token")
    private MultipleInsertValuesTokenAssert multipleInsertValuesToken;
    
    @XmlElement(name = "order-by-token")
    private OrderByTokenAssert orderByToken;
    
    @XmlElement(name = "offset-token")
    private OffsetTokenAssert offsetToken;
    
    @XmlElement(name = "row-count-token")
    private RowCountTokenAssert rowCountToken;
    
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
    
    /**
     * Get SQL tokens.
     * 
     * @return SQL tokens
     */
    public List<SQLTokenAssert> getSqlTokens() {
        List<SQLTokenAssert> result = new ArrayList<>(7);
        if (null != tableTokens) {
            result.addAll(tableTokens);
        }
        if (null != indexToken) {
            result.add(indexToken);
        }
        if (null != offsetToken) {
            result.add(offsetToken);
        }
        if (null != rowCountToken) {
            result.add(rowCountToken);
        }
        if (null != itemsToken) {
            result.add(itemsToken);
        }
        if (null != generatedKeyToken) {
            result.add(generatedKeyToken);
        }
        if (null != multipleInsertValuesToken) {
            result.add(multipleInsertValuesToken);
        }
        if (null != orderByToken) {
            result.add(orderByToken);
        }
        return result;
    }
}
