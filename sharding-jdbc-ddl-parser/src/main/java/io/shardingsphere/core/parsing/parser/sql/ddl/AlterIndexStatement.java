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

package io.shardingsphere.core.parsing.parser.sql.ddl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;
@Getter
@ToString(callSuper = true)
public class AlterIndexStatement extends DDLStatement {
	protected final List<String> dropIndexs = new ArrayList<>();

	protected final List<String> updateIndexs = new ArrayList<>();

	protected final List<String> addIndexs = new ArrayList<>();
	
	protected final Map<String,String> renameIndexs = new LinkedHashMap<>();
}
