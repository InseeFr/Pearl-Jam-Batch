package fr.insee.pearljam.batch.communication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Courrier {

    // generate required empty tags
    @XmlElement(name = "VariablesLibres")
    private String variablesLibres = "";

    @XmlElement(name = "Variables")
    private Variables variables;

    @XmlElement(name = "Images")
    private String images = "";
}


