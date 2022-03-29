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

grammar CommonDistSQLStatement;

import Symbol, RALStatement, RDLStatement, RQLStatement;

execute
    : (addResource
    | alterResource
    | dropResource
    | showResources
    | showUnusedResources
    | setVariable
    | showVariable
    | showAllVariables
    | clearHint
    | enableInstance
    | disableInstance
    | showInstance
    | showInstanceMode
    | labelInstance
    | unlabelInstance
    | countInstanceRules
    | alterInstance
    | countSchemaRules
    | prepareDistSQL
    | applyDistSQL
    | discardDistSQL
    | showSingleTable
    | showSingleTableRules
    | createDefaultSingleTableRule
    | alterDefaultSingleTableRule
    | dropDefaultSingleTableRule
    | refreshTableMetadata
    | showTableMetadata
    | showSQLParserRule
    | alterSQLParserRule
    | showAuthorityRule
    | showTransactionRule
    | alterTransactionRule
    | showTrafficRules
    | dropTrafficRule
    | createTrafficRule
    | alterTrafficRule
    | exportSchemaConfiguration
    | showRulesUsedResource
    | importSchemaConfiguration
    ) SEMI?
    ;
