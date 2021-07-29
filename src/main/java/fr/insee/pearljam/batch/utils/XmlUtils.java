package fr.insee.pearljam.batch.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.ValidateException;

/**
 * Operation on XML Content 
 * - getXmlNodeFile 
 * - validateXMLSchema 
 * - xmlToObject 
 * - objectToXML 
 * - removeSurveyUnitNode
 * - updateSampleFileErrorList
 * 
 * @author Claudel Benjamin
 * 
 */
public class XmlUtils {
	private static final Logger logger = LogManager.getLogger(XmlUtils.class);

	private XmlUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * get an XML node in an XML File
	 * 
	 * @param filename filename reference
	 * @param nodeName nodeName to search
	 * @return the XML node find
	 */
	public static NodeList getXmlNodeFile(String filename, String nodeName) {
		try {
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(filename));
			doc.getDocumentElement().normalize();
			return doc.getElementsByTagName(nodeName);
		} catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Validate an XML file by XSD validator
	 * 
	 * @param xsdPath xsd path
	 * @param xmlPath xml path
	 * @return true if XML is valid
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws BatchException
	 */
	public static void validateXMLSchema(URL model, String xmlPath) throws ValidateException, IOException, XMLStreamException {
		ValidateException ve = null;
		
		XMLStreamReader xmlEncoding= null;
		try (FileInputStream fis = new FileInputStream(new File(xmlPath));
			FileReader fr = new FileReader(xmlPath);
			) {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(model);
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			Validator validator = schema.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			xmlEncoding = XMLInputFactory.newInstance().createXMLStreamReader(fr);
			if(xmlEncoding.getCharacterEncodingScheme().equals("UTF8") || xmlEncoding.getCharacterEncodingScheme().equals(StandardCharsets.UTF_8.toString())) {
				validator.validate(new StreamSource(fis));
			}
		} catch (Exception e) {
			ve = new ValidateException("Error during validation : " + e.getMessage());
		} finally {
			if(xmlEncoding!=null)xmlEncoding.close();
		}
		if(ve!=null)throw ve;
		logger.log(Level.INFO, "{} validate with {}", xmlPath, model.getFile());
	}
	
	
	public static <T> T xmlToObject(String filename, Class<T> clazz) throws ValidateException{
		try{
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			DocumentBuilder builder = df.newDocumentBuilder();
			Document document = builder.parse(new InputSource(filename));
			document.getDocumentElement().normalize();
			DOMSource domSource = new DOMSource(document);
	
	        StringWriter strWriter = new StringWriter();
	        StreamResult streamResult = new StreamResult(strWriter);
	        TransformerFactory.newInstance().newTransformer().transform(domSource, streamResult);
			StreamSource xmlStream = new StreamSource(new StringReader(strWriter.getBuffer().toString()));
			
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return clazz.cast(unmarshaller.unmarshal(xmlStream));
		} catch (ParserConfigurationException | SAXException | IOException | TransformerException | JAXBException e) {
			throw new ValidateException("Error during transfo xml to object : " + e.getMessage());
		}
	}
	
	
	public static File objectToXML(String filename, Object object) throws BatchException{
		try {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
           //Store XML to File
            File file = new File(filename);
            //Writes XML file to file-system
            jaxbMarshaller.marshal(object, file); 
            return file;
		}catch (JAXBException e) {
			throw new BatchException("Error during transfo object to xml : " + e.getMessage());
		}
    }

	/**
	 * This method takes a document and an id in entry, it removes the reportingUnit node
	 * identified by its id in the document
	 * @param doc
	 * @param xmlId
	 * @return StreamResult
	 * @throws XPathExpressionException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public static StreamResult removeSurveyUnitNode(Document doc, String xmlId) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("//SurveyUnit/Id" + xmlId);
		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		Node parent = node.getParentNode();
		parent.removeChild(node);
		DOMSource domSource = new DOMSource(doc);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		transformer.transform(domSource, sr);
		return sr;
	}
	
	/**
	 * This method update an error file for the sample steps. It writes all the objects
	 * in the sample.xml that created errors
	 * @param sr
	 * @param fileName
	 */
	public static void updateSampleFileErrorList(StreamResult sr, String fileName) {
		// writing to file
		File fileNew = new File(fileName);
		try (FileOutputStream fop = new FileOutputStream(fileNew);){
			if (!fileNew.exists() && !fileNew.createNewFile()) {
				logger.log(Level.ERROR, "Failed to create file %s", fileName);
			}
			// get the content in bytes
			String xmlString = sr.getWriter().toString();
			byte[] contentInBytes = xmlString.getBytes();
			fop.write(contentInBytes);
			fop.flush();
		} catch (IOException e) {
			logger.log(Level.ERROR, e.getMessage());
		}
	}
	

}
