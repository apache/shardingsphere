/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.parser.sql.ddl.create.table;

import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedList;
import java.util.List;

/**
 * Create table statement.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class CreateTableStatement extends DDLStatement {
    
    private final List<String> columnNames = new LinkedList<>();
    
    private final List<String> columnTypes = new LinkedList<>();
    
    private final List<String> primaryKeyColumns = new LinkedList<>();
    
    private TableMetaData tableMetaData;
}
