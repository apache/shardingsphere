package io.shardingsphere.core.parsing.integrate.jaxb.table;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import io.shardingsphere.core.parsing.integrate.jaxb.meta.ExpectedTableMetaData;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnDefinition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedColumnPosition;
import io.shardingsphere.core.parsing.integrate.jaxb.token.ExpectedUpdateColumnDefinition;
import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ExpectedAlterTable {
    @XmlAttribute(name = "drop-columns")
    private String dropColumns = "";
    
    @XmlAttribute(name = "drop-primary-key")
    private boolean dropPrimaryKey;
    
    @XmlAttribute(name = "new-table-name")
    private String newTableName;
    
    @XmlElementWrapper(name = "add-columns")
    @XmlElement(name = "add-column")
    private List<ExpectedColumnDefinition> addColumns = new ArrayList<>();
    
    @XmlElementWrapper(name = "update-columns")
    @XmlElement(name = "update-column")
    private List<ExpectedUpdateColumnDefinition> updateColumns = new ArrayList<>();
    
    @XmlElementWrapper(name = "position-changed-columns")
    @XmlElement(name = "position-changed-column")
    private List<ExpectedColumnPosition> positionChangedColumns = new ArrayList<>();
    
    @XmlElement(name = "new-meta")
    private ExpectedTableMetaData newMeta;
    
}
