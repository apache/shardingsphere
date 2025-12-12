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

package org.apache.shardingsphere.test.e2e.operation.pipeline.command;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
@Setter
public final class ExtraSQLCommand {
    
    @XmlElement(name = "create-table-order")
    private String createTableOrder;
    
    @XmlElement(name = "create-table-order-item")
    @Getter
    private String createTableOrderItem;
    
    @XmlElement(name = "full-insert-order")
    private String fullInsertOrder;
    
    @XmlElement(name = "full-insert-order-item")
    @Getter
    private String fullInsertOrderItem;
    
    /**
     * Get full insert order sql.
     *
     * @param orderTableName order table name
     * @return migration single table DistSQL
     */
    public String getFullInsertOrder(final String orderTableName) {
        return String.format(fullInsertOrder, orderTableName);
    }
    
    /**
     * Get create table order.
     *
     * @param orderTableName order table name
     * @return Get create table order sql
     */
    public String getCreateTableOrder(final String orderTableName) {
        return String.format(createTableOrder, orderTableName);
    }
}
