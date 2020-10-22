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

package org.apache.shardingsphere.sql.parser.spi;

import org.apache.shardingsphere.sql.parser.api.lexer.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.facade.SQLVisitorFacadeEngine;

/**
 * SQL parser configuration.
 */
public interface SQLParserConfiguration {
    
    /**
     * Get name of database type.
     *
     * @return name of database type
     */
    String getDatabaseTypeName();
    
    /**
     * Get SQL lexer class type.
     *
     * @return SQL lexer class type
     */
    Class<? extends SQLLexer> getLexerClass();
    
    /**
     * Get SQL parser class type.
     * 
     * @return SQL parser class type
     */
    Class<? extends SQLParser> getParserClass();
    
    /**
     * Get SQL visitor facade engine class.
     *
     * @return SQL visitor facade class
     */
    Class<? extends SQLVisitorFacadeEngine> getVisitorFacadeEngineClass();
}
