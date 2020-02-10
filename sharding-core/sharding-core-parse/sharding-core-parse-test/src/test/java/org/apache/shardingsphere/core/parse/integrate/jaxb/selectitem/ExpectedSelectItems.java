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

package org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collection;
import java.util.LinkedList;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedSelectItems {
    
    @XmlAttribute(name = "start-index")
    private Integer startIndex;
    
    @XmlAttribute(name = "stop-index")
    private Integer stopIndex;
    
    @XmlAttribute
    private boolean distinctRow;
    
    @XmlElementWrapper(name = "shorthand-select-items")
    @XmlElement(name = "shorthand-select-item")
    private Collection<ExpectedShorthandSelectItem> expectedShorthandSelectItems = new LinkedList<>();
    
    @XmlElementWrapper(name = "aggregation-select-items")
    @XmlElement(name = "aggregation-select-item")
    private Collection<ExpectedAggregationItem> expectedAggregationItems = new LinkedList<>();
    
    @XmlElementWrapper(name = "aggregation-distinct-select-items")
    @XmlElement(name = "aggregation-distinct-select-item")
    private Collection<ExpectedAggregationDistinctItem> expectedAggregationDistinctItems = new LinkedList<>();
    
    @XmlElementWrapper(name = "column-select-items")
    @XmlElement(name = "column-select-item")
    private Collection<ExpectedColumnSelectItem> expectedColumnSelectItems = new LinkedList<>();
    
    @XmlElementWrapper(name = "expression-items")
    @XmlElement(name = "expression-item")
    private Collection<ExpectedExpressionItem> expectedExpressionItems = new LinkedList<>();
    
    @XmlElementWrapper(name = "top-select-items")
    @XmlElement(name = "top-select-item")
    private Collection<ExpectedTopSelectItem> expectedTopSelectItems = new LinkedList<>();
    
    /**
     * Get size.
     * 
     * @return size
     */
    public int getSize() {
        return expectedAggregationItems.size() + expectedShorthandSelectItems.size()
                + expectedAggregationDistinctItems.size() + expectedColumnSelectItems.size()
                + expectedExpressionItems.size() + expectedTopSelectItems.size();
    }
    
    /**
     * Find expected select items.
     * 
     * @param expectedSelectItemType expected select item type
     * @param <T> type of select item type
     * @return expected select items
     */
    public <T extends ExpectedSelectItem> Collection<ExpectedSelectItem> findExpectedSelectItems(final Class<T> expectedSelectItemType) {
        Collection<ExpectedSelectItem> result = new LinkedList<>();
        if (expectedSelectItemType.equals(ExpectedShorthandSelectItem.class)) {
            result.addAll(expectedShorthandSelectItems);
        }
        if (expectedSelectItemType.equals(ExpectedAggregationItem.class)) {
            result.addAll(expectedAggregationItems);
        }
        if (expectedSelectItemType.equals(ExpectedColumnSelectItem.class)) {
            result.addAll(expectedColumnSelectItems);
        }
        if (expectedSelectItemType.equals(ExpectedAggregationDistinctItem.class)) {
            result.addAll(expectedAggregationDistinctItems);
        }
        if (expectedSelectItemType.equals(ExpectedExpressionItem.class)) {
            result.addAll(expectedExpressionItems);
        }
        if (expectedSelectItemType.equals(ExpectedTopSelectItem.class)) {
            result.addAll(expectedExpressionItems);
        }
        return result;
    }
}
