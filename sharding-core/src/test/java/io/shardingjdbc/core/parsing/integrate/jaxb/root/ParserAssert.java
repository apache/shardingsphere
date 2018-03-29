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

import io.shardingjdbc.core.parsing.integrate.jaxb.condition.Condition;
import io.shardingjdbc.core.parsing.integrate.jaxb.groupby.GroupByColumn;
import io.shardingjdbc.core.parsing.integrate.jaxb.item.AggregationSelectItem;
import io.shardingjdbc.core.parsing.integrate.jaxb.limit.Limit;
import io.shardingjdbc.core.parsing.integrate.jaxb.orderby.OrderByColumn;
import io.shardingjdbc.core.parsing.integrate.jaxb.table.Table;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.IndexToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.ItemsToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OffsetToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.OrderByToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.RowCountToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.SQLToken;
import io.shardingjdbc.core.parsing.integrate.jaxb.token.TableToken;
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
    private List<Table> tables;
    
    @XmlElementWrapper
    @XmlElement(name = "condition") 
    private List<Condition> conditions;
    
    @XmlElementWrapper(name = "table-tokens")
    @XmlElement(name = "table-token")
    private List<TableToken> tableTokens;
    
    @XmlElement(name = "index-token")
    private IndexToken indexToken;
    
    @XmlElement(name = "items-token")
    private ItemsToken itemsToken;
    
    @XmlElement(name = "generated-key-token")
    private GeneratedKeyToken generatedKeyToken;
    
    @XmlElement(name = "multiple-insert-values-token")
    private MultipleInsertValuesToken multipleInsertValuesToken;
    
    @XmlElement(name = "order-by-token")
    private OrderByToken orderByToken;
    
    @XmlElement(name = "offset-token")
    private OffsetToken offsetToken;
    
    @XmlElement(name = "row-count-token")
    private RowCountToken rowCountToken;
    
    @XmlElementWrapper(name = "order-by-columns")
    @XmlElement(name = "order-by-column") 
    private List<OrderByColumn> orderByColumns;
    
    @XmlElementWrapper(name = "group-by-columns")
    @XmlElement(name = "group-by-column") 
    private List<GroupByColumn> groupByColumns;
    
    @XmlElementWrapper(name = "aggregation-select-items")
    @XmlElement(name = "aggregation-select-item") 
    private List<AggregationSelectItem> aggregationSelectItems;
    
    @XmlElement 
    private Limit limit;
    
    /**
     * Get SQL tokens.
     * 
     * @return SQL tokens
     */
    public List<SQLToken> getSqlTokens() {
        List<SQLToken> result = new ArrayList<>(7);
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
