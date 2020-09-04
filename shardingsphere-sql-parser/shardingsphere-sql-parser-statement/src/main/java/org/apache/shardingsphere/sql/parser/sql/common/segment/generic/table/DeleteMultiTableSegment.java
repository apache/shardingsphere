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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class DeleteMultiTableSegment implements TableSegment {
    
    private int startIndex;
    
    private int stopIndex;
    
    private List<SimpleTableSegment> actualDeleteTables = new LinkedList<>();
    
    private TableSegment relationTable;
    
    @Override
    public Optional<String> getAlias() {
        return Optional.empty();
    }
    
    @Override
    public void setAlias(final AliasSegment alias) {
    
    }
}
