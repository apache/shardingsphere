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

package org.apache.shardingsphere.core.parse.integrate.jaxb.token;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedTokens {
    
    @XmlElement(name = "table-token")
    private List<ExpectedTableToken> tableTokens = new LinkedList<>();
    
    @XmlElement(name = "index-token")
    private List<ExpectedIndexToken> indexTokens = new LinkedList<>();
    
    @XmlElement(name = "items-token")
    private ExpectedItemsToken itemsToken;
    
    @XmlElement(name = "generated-key-token")
    private ExpectedGeneratedKeyToken generatedKeyToken;
    
    @XmlElement(name = "insert-values-token")
    private ExpectedInsertValuesToken insertValuesToken;
    
    @XmlElement(name = "order-by-token")
    private ExpectedOrderByToken orderByToken;
    
    @XmlElement(name = "offset-token")
    private ExpectedOffsetToken offsetToken;
    
    @XmlElement(name = "row-count-token")
    private ExpectedRowCountToken rowCountToken;
    
    @XmlElement(name = "insert-column-token")
    private ExpectedInsertColumnToken insertColumnToken;
    
    @XmlElement(name = "schema-token")
    private List<ExpectedSchemaToken> schemaTokens = new LinkedList<>();
    
    @XmlElement(name = "aggregation-distinct-token")
    private List<ExpectedAggregationDistinctToken> aggregationDistinctTokens = new LinkedList<>();
    
    @XmlElement(name = "encrypt-column-token")
    private List<ExpectedEncryptColumnToken> encryptColumnTokens = new LinkedList<>();
}
