package io.shardingsphere.core.parsing.parser.sql.ddl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AlterTableStatement extends AlterIndexStatement {
	private final List<String> dropColumns = new ArrayList<>();

	private final Map<String, ColumnDefinition> updateColumns = new LinkedHashMap<>();

	private final List<ColumnDefinition> addColumns = new ArrayList<>();

	private boolean dropPrimaryKey;

	private String newTableName;

}
