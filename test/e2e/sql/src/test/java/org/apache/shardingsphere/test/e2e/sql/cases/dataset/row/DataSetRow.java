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

package org.apache.shardingsphere.test.e2e.sql.cases.dataset.row;

import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data set row.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@EqualsAndHashCode
public final class DataSetRow {
    
    private static final String E2E_DATA_DELIMITER = "{E2E_DATA_DELIMITER}";
    
    @XmlAttribute(name = "data-node")
    private String dataNode;
    
    @XmlAttribute(required = true)
    private String values;
    
    @XmlAttribute
    private boolean updated;
    
    /**
     * Split values with vertical bar.
     *
     * @param delimiter delimiter of splitter
     * @return split values
     */
    public List<String> splitValues(final String delimiter) {
        return Splitter.on(delimiter).trimResults().splitToList(values).stream().map(each -> each.replace(E2E_DATA_DELIMITER, delimiter)).collect(Collectors.toList());
    }
}
