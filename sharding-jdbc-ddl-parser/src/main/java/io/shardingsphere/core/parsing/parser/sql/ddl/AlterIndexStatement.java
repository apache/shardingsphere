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
