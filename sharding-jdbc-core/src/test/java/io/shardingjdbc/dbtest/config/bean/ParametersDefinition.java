package io.shardingjdbc.dbtest.config.bean;


import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class ParametersDefinition {

    @XmlElement(name = "parameter")
    private List<ParameterDefinition> parameter;



}
