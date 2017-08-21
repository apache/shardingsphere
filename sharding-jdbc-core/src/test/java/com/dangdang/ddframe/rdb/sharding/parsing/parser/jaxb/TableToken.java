package com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TableToken {
    
    @XmlElement
    private int beginPosition;
    
    @XmlElement
    private String originalLiterals;
    
}
