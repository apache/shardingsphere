package com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TableToken {
    
    @XmlAttribute(name = "begin-position")
    private int beginPosition;
    
    @XmlAttribute(name = "original-literals")
    private String originalLiterals;
}
