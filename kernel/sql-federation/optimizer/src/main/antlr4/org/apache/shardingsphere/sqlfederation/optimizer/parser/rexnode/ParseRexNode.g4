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

grammar ParseRexNode;

import Symbol,Keyword,Literals;

expression
    : op LP_ parameter COMMA_ parameter RP_
    ;

parameter
    : input | expression
    ;

input
    : inputRef | searchArgs | constant | cast | paramWithType
    ;

inputRef
    : DOLLAR_ INTEGER_
    ;

searchArgs
    : SARG LBT_ (argRange | argList | argRangeList) RBT_
    ;

constant
    : INTEGER_ | STRING_
    ;

cast
    : CAST LP_ inputRef  RP_ COLON_ type
    ;

paramWithType
    : (STRING_|INTEGER_) COLON_ type
    ;

op
    : SEARCH | LIKE | OR | NOT | AND | EQ_ | NEQ_ | GT_ | GTE_ | LT_ | LTE_
    ;

argRange
    : LP_ (NEGETIVE_INFINITY_|INTEGER_) RANGE_ (INTEGER_|POSITIVE_INFINITY_) RP_ | LBT_ INTEGER_ RANGE_ INTEGER_ RBT_
    ;

argList
    : (LP_|LBT_)? INTEGER_ (COMMA_ INTEGER_)* (RP_|RBT_)?
    ;

argRangeList
    : argRange (COMMA_ argRange)*
    ;

type
    : INTEGER|VARCHAR
    ;

WS
    : [ \t]+ -> skip
    ;
