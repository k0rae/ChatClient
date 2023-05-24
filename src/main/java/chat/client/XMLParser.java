package chat.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

public class XMLParser {
    public static ParsedResponse parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        Element root = doc.getDocumentElement();
        String type = root.getNodeName();

        ParsedResponse parsedResponse = new ParsedResponse();
        parsedResponse.setEventName(type);

        if (type.equals("event")) {
            parsedResponse.setEventName(root.getAttribute("name"));
            parsedResponse.setError(false);
            parsedResponse.setSuccess(true);
            HashMap<String, String> args = new HashMap<>();
            NodeList argNodes = root.getChildNodes();
            for (int i = 0; i < argNodes.getLength(); i++) {
                Element argElement = (Element) argNodes.item(i);
                String argName = argElement.getNodeName();
                String argValue = nodeToString(argElement);
                args.put(argName, argValue);
            }
            parsedResponse.setArgs(args);
        } else if (type.equals("error")) {
            parsedResponse.setError(true);
            parsedResponse.setSuccess(false);
            NodeList messageNodes = root.getElementsByTagName("message");
            if (messageNodes.getLength() > 0) {
                String errorMessage = nodeToString(messageNodes.item(0));
                parsedResponse.setErrorMessage(errorMessage);
            }
        } else if (type.equals("success")) {
            parsedResponse.setError(false);
            parsedResponse.setSuccess(true);
            HashMap<String, String> args = new HashMap<>();
            NodeList argNodes = root.getChildNodes();
            for (int i = 0; i < argNodes.getLength(); i++) {
                Element argElement = (Element) argNodes.item(i);
                String argName = argElement.getNodeName();
                String argValue = nodeToString(argElement);
                args.put(argName, argValue);
            }
            parsedResponse.setArgs(args);
        }

        return parsedResponse;
    }
    private static String nodeToString(Node node) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            transformer.transform(new DOMSource(childNode), new StreamResult(writer));
        }
        return writer.toString();
    }
}
