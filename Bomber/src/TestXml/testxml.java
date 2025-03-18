package TestXml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Console;
import java.io.File;

public class testxml {
    public static void main(String[] args) throws  Exception
    {

        System.out.println("Test XML");

        File fXmlFile = new File("Bomber\\res\\GameBoard.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();


        System.out.println("ROOT: " + doc.getDocumentElement().getNodeName());

        NodeList nList = doc.getElementsByTagName("WallH");
        for (int i=0; i < nList.getLength(); i++) {
            Node nWH = nList.item(i);
            Element lmWH = (Element) nWH;
            System.out.println(nWH.getNodeName()+" StartX="+lmWH.getAttribute("StartX"));
        }


        NodeList nListZigZag = doc.getElementsByTagName("WallZigZag");
        for (int i=0; i < nListZigZag.getLength(); i++) {
            Node nWZ = nListZigZag.item(i);
            Element lmWZ = (Element) nWZ;
            System.out.println(nWZ.getNodeName());
            NodeList nlPoints = lmWZ.getElementsByTagName("Point");
            for(int j=0; j < nlPoints.getLength(); j++) {
                Element lmPoint = (Element) nlPoints.item(j);
                System.out.println(lmPoint.getNodeName()+" X="+lmPoint.getAttribute("X"));
            }
        }

    }
}
