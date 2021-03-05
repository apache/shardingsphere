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

package org.apache.shardingsphere.infra.optimize.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.shardingsphere.infra.optimize.schema.CalciteLogicSchema;

import java.util.Properties;

/**
 * Calcite context.
 */
@RequiredArgsConstructor
@Getter
public final class CalciteContext {
    
    private final Properties connectionProperties;
    
    private final CalciteLogicSchema calciteLogicSchema;
    
    private final SqlParser.Config parserConfig;
    
    private final SqlValidator validator;
    
    private final SqlToRelConverter relConverter;
}
