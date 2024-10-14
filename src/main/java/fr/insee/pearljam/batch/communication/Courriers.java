package fr.insee.pearljam.batch.communication;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XmlRootElement(name = "Courriers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Courriers {

    @XmlElement(name = "IdOperation")
    private String idOperation;
    @XmlElement(name = "TypeGenerateur")
    private String typeGenerateur;
    @XmlElement(name = "PartieNomFichierLibreZip")
    private String partieNomFichierLibreZip;
    @XmlElement(name = "Courrier")
    private List<Courrier> courriers;
    @XmlTransient
    private String editionId;
    @XmlTransient
    private String communicationModel;

}
