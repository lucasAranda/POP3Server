/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import model.Mail;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Gerado
 */
public class XmlParcerExpert {

    private static XmlParcerExpert instance;
    private String agendaPath = "C:\\Users\\LUCAS\\Documents\\NetBeansProjects\\POP3Server\\src\\main\\remitentes.xml";
    private String mailPath = "C:\\Users\\LUCAS\\Documents\\NetBeansProjects\\POP3Server\\src\\main\\mails.xml";
    private String formatoFecha = "dd/MM/yyyy HH:mm:ss";

    public static XmlParcerExpert getInstance() {
        if (instance == null) {
            instance = new XmlParcerExpert();
        }
        return instance;
    }

    /**
     * Regresa una lista con las direcciones de mail de agenda.xml
     * @return 
     */
    public List<String> getAgenda() {
        List<String> agenda = new ArrayList<String>();
        try {
            File file = new File(agendaPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("contacto");
            System.out.println("Information of all directions");

            for (int s = 0; s < nodeLst.getLength(); s++) {
                String email;
                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element fstElmnt = (Element) fstNode;
                    NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("nombre");
                    Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
                    NodeList fstNm = fstNmElmnt.getChildNodes();
                    System.out.println("Nombre : " + ((Node) fstNm.item(0)).getNodeValue());
                    NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("mail");
                    Element lstNmElmnt = (Element) lstNmElmntLst.item(0);
                    NodeList lstNm = lstNmElmnt.getChildNodes();
                    email = ((Node) lstNm.item(0)).getNodeValue();
                    System.out.println("Mail : " + ((Node) lstNm.item(0)).getNodeValue());
                    agenda.add(email);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agenda;
    }

    /**
     * Procesa la trama recibida
     * @param from
     * @param content
     * @return 
     */
    public String processAndSaveMail(String from, String content) {
        try {
            content = content.substring(4);
            String timeStamp = content.substring(0, content.indexOf(";"));
            if (!seach("timestamp", timeStamp)) {
                content = content.substring(content.indexOf(";") + 1);
                String temperatura = content.substring(0, content.indexOf(";"));
                content = content.substring(content.indexOf(";") + 1);
                String tension = content.substring(0, content.indexOf(";"));
                content = content.substring(content.indexOf(";") + 1);
                String corriente = content.substring(0, content.indexOf(";"));
                content = content.substring(content.indexOf(";") + 1);
                String potencia = content.substring(0, content.indexOf(";"));
                content = content.substring(content.indexOf(";") + 1);
                String presion = content.substring(0, content.indexOf("&lt"));

                saveMailsInXML(from, timeStamp, temperatura, tension, corriente, potencia, presion);
            }
        } catch (Exception e) {

        }
        return null;
    }

    private boolean seach(String tag, String value) {
        try {
            File file = new File(mailPath);
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            NodeList l = d.getElementsByTagName(tag);
            for (int i = 0; i < l.getLength(); ++i) {
                if (value.equals(l.item(i).getNodeValue())) {
                    return true;
                }
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlParcerExpert.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XmlParcerExpert.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParcerExpert.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Guarda la trama recibida en mails.xml
     * @param fromValue
     * @param timeStampValue
     * @param temperaturaValue
     * @param tensionValue
     * @param corrienteValue
     * @param potenciaValue
     * @param presionValue 
     */
    private void saveMailsInXML(String fromValue, String timeStampValue, String temperaturaValue, String tensionValue, String corrienteValue, String potenciaValue, String presionValue) {
        try {

            File file = new File(mailPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc;
            Element rootElement;
            
            // si archivo mails.xml no existe lo crea
            if (file.exists()) {
                doc = db.parse(file);
                rootElement = doc.getDocumentElement();
            } else {
                doc = db.newDocument();
                rootElement = doc.createElement("mails");
                doc.appendChild(rootElement);
            }

            // mail elements
            Element mail = doc.createElement("mail");
            rootElement.appendChild(mail);

            // from
            Element from = doc.createElement("from");
            from.appendChild(doc.createTextNode(fromValue));
            mail.appendChild(from);
            
            // timestamp elements
            Element timestamp = doc.createElement("timstamp");
            timestamp.appendChild(doc.createTextNode(timeStampValue));
            mail.appendChild(timestamp);

            // temperatura elements
            Element temperatura = doc.createElement("temperatura");
            temperatura.appendChild(doc.createTextNode(temperaturaValue));
            mail.appendChild(temperatura);

            // potencia elements
            Element tension = doc.createElement("tension");
            tension.appendChild(doc.createTextNode(tensionValue));
            mail.appendChild(tension);

            // corriente elements
            Element corriente = doc.createElement("corriente");
            corriente.appendChild(doc.createTextNode(corrienteValue));
            mail.appendChild(corriente);

            // potencia elements
            Element potencia = doc.createElement("potencia");
            potencia.appendChild(doc.createTextNode(potenciaValue));
            mail.appendChild(potencia);

            // presion elements
            Element presion = doc.createElement("presion");
            presion.appendChild(doc.createTextNode(presionValue));
            mail.appendChild(presion);

            // escribe el contenido en el xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (SAXException ex) {
            Logger.getLogger(XmlParcerExpert.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlParcerExpert.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Retorna una lista con todos los mails en mails.xml
     * @return 
     */
    public List<Mail> getMails() {
        List<Mail> mails = new ArrayList<Mail>();
        try {
            File file = new File(mailPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("mail");

            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);

                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                    Mail mail = new Mail();

                    Element fstElmnt = (Element) fstNode;

                    //get from
                    NodeList elementLst = fstElmnt.getElementsByTagName("from");
                    Element element = (Element) elementLst.item(0);
                    NodeList node = element.getChildNodes();
                    mail.setFrom(((Node) node.item(0)).getNodeValue());

                    //get timstamp
                    elementLst = fstElmnt.getElementsByTagName("timstamp");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    Date timestamp=new SimpleDateFormat(formatoFecha).parse(((Node) node.item(0)).getNodeValue());
                    mail.setTimestamp(timestamp);

                    //get temperatura
                    elementLst = fstElmnt.getElementsByTagName("temperatura");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    mail.setTemperatura(Double.parseDouble(((Node) node.item(0)).getNodeValue()));

                    //get tension
                    elementLst = fstElmnt.getElementsByTagName("tension");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    mail.setTension(Double.parseDouble(((Node) node.item(0)).getNodeValue()));

                    //get corriente
                    elementLst = fstElmnt.getElementsByTagName("corriente");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    mail.setCorriente(Double.parseDouble(((Node) node.item(0)).getNodeValue()));

                    //get potencia
                    elementLst = fstElmnt.getElementsByTagName("potencia");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    mail.setPotencia(Double.parseDouble(((Node) node.item(0)).getNodeValue()));
                    
                    //get presion
                    elementLst = fstElmnt.getElementsByTagName("presion");
                    element = (Element) elementLst.item(0);
                    node = element.getChildNodes();
                    mail.setPresion(Double.parseDouble(((Node) node.item(0)).getNodeValue()));


                    mails.add(mail);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mails;
    }
}
