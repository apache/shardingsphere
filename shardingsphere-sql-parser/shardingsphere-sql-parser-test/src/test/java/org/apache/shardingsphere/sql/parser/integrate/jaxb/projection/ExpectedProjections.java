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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.projection;

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
public final class ExpectedProjections {
    
    @XmlAttribute(name = "start-index")
    private Integer startIndex;
    
    @XmlAttribute(name = "stop-index")
    private Integer stopIndex;
    
    @XmlAttribute
    private boolean distinctRow;
    
    @XmlElementWrapper(name = "shorthand-select-items")
    @XmlElement(name = "shorthand-select-item")
    private Collection<ExpectedShorthandProjection> expectedShorthandProjections = new LinkedList<>();
    
    @XmlElementWrapper(name = "aggregation-select-items")
    @XmlElement(name = "aggregation-select-item")
    private Collection<ExpectedAggregationProjection> expectedAggregationProjections = new LinkedList<>();
    
    @XmlElementWrapper(name = "aggregation-distinct-select-items")
    @XmlElement(name = "aggregation-distinct-select-item")
    private Collection<ExpectedAggregationDistinctProjection> expectedAggregationDistinctProjections = new LinkedList<>();
    
    @XmlElementWrapper(name = "column-select-items")
    @XmlElement(name = "column-select-item")
    private Collection<ExpectedColumnProjection> expectedColumnProjections = new LinkedList<>();
    
    @XmlElementWrapper(name = "expression-items")
    @XmlElement(name = "expression-item")
    private Collection<ExpectedExpressionProjection> expectedExpressionProjections = new LinkedList<>();
    
    @XmlElementWrapper(name = "top-select-items")
    @XmlElement(name = "top-select-item")
    private Collection<ExpectedTopProjection> expectedTopProjections = new LinkedList<>();
    
    /**
     * Get size.
     * 
     * @return size
     */
    public int getSize() {
        return expectedAggregationProjections.size() + expectedShorthandProjections.size()
                + expectedAggregationDistinctProjections.size() + expectedColumnProjections.size()
                + expectedExpressionProjections.size() + expectedTopProjections.size();
    }
    
    /**
     * Find expected projections.
     * 
     * @param expectedProjectionType expected projections type
     * @param <T> type of projection type
     * @return expected projections
     */
    public <T extends ExpectedProjection> Collection<ExpectedProjection> findExpectedProjections(final Class<T> expectedProjectionType) {
        Collection<ExpectedProjection> result = new LinkedList<>();
        if (expectedProjectionType.equals(ExpectedShorthandProjection.class)) {
            result.addAll(expectedShorthandProjections);
        }
        if (expectedProjectionType.equals(ExpectedAggregationProjection.class)) {
            result.addAll(expectedAggregationProjections);
        }
        if (expectedProjectionType.equals(ExpectedColumnProjection.class)) {
            result.addAll(expectedColumnProjections);
        }
        if (expectedProjectionType.equals(ExpectedAggregationDistinctProjection.class)) {
            result.addAll(expectedAggregationDistinctProjections);
        }
        if (expectedProjectionType.equals(ExpectedExpressionProjection.class)) {
            result.addAll(expectedExpressionProjections);
        }
        if (expectedProjectionType.equals(ExpectedTopProjection.class)) {
            result.addAll(expectedExpressionProjections);
        }
        return result;
    }
}
