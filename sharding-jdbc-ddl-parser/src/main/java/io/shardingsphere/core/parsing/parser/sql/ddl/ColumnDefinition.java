package io.shardingsphere.core.parsing.parser.sql.ddl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ColumnDefinition {
	private String name;
	
	private String type;
	
	private Integer length;
	
	private boolean primaryKey;
}
