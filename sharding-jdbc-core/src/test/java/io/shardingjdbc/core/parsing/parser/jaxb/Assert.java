/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.jaxb;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class Assert {
    
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String parameters;
    
    @XmlElement(name = "tables") 
    private Tables tables;
    
    @XmlElement(name = "conditions") 
    private Conditions conditions;
    
    @XmlElementWrapper(name = "table-tokens")
    @XmlElement(name = "table-token")
    private List<TableToken> tableTokens;
    
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
    
    public List<SQLToken> getSqlTokens() {
        List<SQLToken> result = new ArrayList<SQLToken>(7);
        if (tableTokens != null) {
            result.addAll(tableTokens);
        }
        if (offsetToken != null) {
            result.add(offsetToken);
        }
        if (rowCountToken != null) {
            result.add(rowCountToken);
        }
        if (itemsToken != null) {
            result.add(itemsToken);
        }
        if (generatedKeyToken != null) {
            result.add(generatedKeyToken);
        }
        if (multipleInsertValuesToken != null) {
            result.add(multipleInsertValuesToken);
        }
        if (orderByToken != null) {
            result.add(orderByToken);
        }
        return result;
    }
}
