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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.command;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class CursorSQLCommand {
    
    @XmlElement(name = "single-table-cursor")
    private CursorSQL singleTableCursor;
    
    @XmlElement(name = "single-table-cursor-order-by")
    private CursorSQL singleTableCursorOrderBy;
    
    @XmlElement(name = "broadcast-tables-cursor")
    private CursorSQL broadcastTablesCursor;
    
    @XmlElement(name = "broadcast-tables-cursor2")
    private CursorSQL broadcastTablesCursor2;
    
    @XmlElement(name = "broadcast-and-sharding-tables-cursor")
    private CursorSQL broadcastAndShardingTablesCursor;
    
    @XmlElement(name = "broadcast-and-single-tables-cursor")
    private CursorSQL broadcastAndSingleTablesCursor;
    
    @XmlElement(name = "broadcast-and-single-tables-cursor2")
    private String broadcastAndSingleTablesCursor2;
    
    @XmlElement(name = "view-cursor")
    private CursorSQL viewCursor;
}
