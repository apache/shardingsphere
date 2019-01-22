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

package org.apache.shardingsphere.core.routing;

import org.apache.shardingsphere.core.routing.type.TableUnit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Route unit.
 * 
 * @author gaohongtao
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(exclude = "tableUnit")
@ToString
public final class RouteUnit {
    
    private final String dataSourceName;
    
    private final SQLUnit sqlUnit;
    
    private TableUnit tableUnit;
}
