package fr.insee.pearljam.batch.service;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Component
public class PilotageFolderService {
	
	@Autowired
	@Qualifier("filename")
	String filename;
	
	@Autowired
	String getCampaignName;
	
	public String getFilename() {
		return filename;	
	}

	public void setFilename(String fileName) {
		this.filename = fileName;
	}
	
	public String getCampaignName() {
		return getCampaignName;
	}
	
	public void setCampaignName(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File( xmlPath ));
		StringBuilder campaignId = new StringBuilder(document.getElementsByTagName("Campagne").item(0).getAttributes().getNamedItem("idSource").getNodeValue());
		campaignId.append(document.getElementsByTagName("Campagne").item(0).getAttributes().getNamedItem("millesime").getNodeValue());
		campaignId.append(document.getElementsByTagName("Campagne").item(0).getAttributes().getNamedItem("idPeriode").getNodeValue());
		this.getCampaignName = campaignId.toString();
	}
}
