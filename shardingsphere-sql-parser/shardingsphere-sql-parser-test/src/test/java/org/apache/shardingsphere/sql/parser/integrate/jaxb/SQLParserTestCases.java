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

package org.apache.shardingsphere.sql.parser.integrate.jaxb;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.AlterTableStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.CommonStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.DeleteStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.InsertStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.SelectStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.TCLStatementTestCase;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.UpdateStatementTestCase;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL parser test cases.
 * 
 * @author zhangliang 
 */
@XmlRootElement(name = "sql-parser-test-cases")
@Getter
public final class SQLParserTestCases {
    
    @XmlElement(name = "select")
    private final List<SelectStatementTestCase> selectTestCases = new LinkedList<>();
    
    @XmlElement(name = "update")
    private final List<UpdateStatementTestCase> updateTestCases = new LinkedList<>();
    
    @XmlElement(name = "delete")
    private final List<DeleteStatementTestCase> deleteTestCases = new LinkedList<>();
    
    @XmlElement(name = "insert")
    private final List<InsertStatementTestCase> insertTestCases = new LinkedList<>();
    
    @XmlElement(name = "alter-table")
    private final List<AlterTableStatementTestCase> alterTableTestCases = new LinkedList<>();
    
    @XmlElement(name = "tcl")
    private final List<TCLStatementTestCase> tclTestCases = new LinkedList<>();
    
    @XmlElement(name = "common")
    private final List<CommonStatementTestCase> commonTestCases = new LinkedList<>();
}
