package com.talipov;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Марсель on 10.02.2017.
 */
public class XMLSerializer {

    public static void serialize(FileOutputStream stream, Object object) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        DOMImplementation impl = builder.getDOMImplementation();
        Document doc = impl.createDocument(null, null, null); // doctype

        Element objectElement = doc.createElement("object");
        objectElement.setAttribute("type", object.getClass().getName());
        doc.appendChild(objectElement);

        for (Field field: object.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            String type = field.getType().getName();
            String typeName;
            if (type.equals("java.lang.String")) {
                typeName = "String";
            } else if (type.equals("java.lang.Integer") || type.equals("int")) {
                typeName = "Integer";
            } else if (type.equals("java.lang.Double") || type.equals("double")) {
                typeName = "Double";
            } else {
                continue; // другие типы пока не обрабатываем
            }

            try {
                Element fieldElement = doc.createElement("field");
                fieldElement.setAttribute("type", typeName);
                fieldElement.setAttribute("id", field.getName());
                fieldElement.setAttribute("value", field.get(object).toString());
                objectElement.appendChild(fieldElement);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.transform(new DOMSource(doc), new StreamResult(stream));
        } catch (TransformerException te) {
            System.out.println(te.getMessage());
        }
    };

    public static Object deserialize(FileInputStream stream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = null; // стооитель построил документ
        try {
            doc = db.parse(stream);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NodeList list = doc.getChildNodes();
        Node objectElement = list.item(0); // объект

        String className = objectElement.getAttributes().getNamedItem("type").getNodeValue();

        Object object = null;
        try {
            object = Class.forName(className).getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        NodeList fieldElements = objectElement.getChildNodes();
        for (int i = 0; i < fieldElements.getLength(); i++) {
            Node fieldElement = fieldElements.item(i);
            if (!fieldElement.getNodeName().equals("field")) {
                continue;
            }
            String id = fieldElement.getAttributes().getNamedItem("id").getNodeValue();
            String type = fieldElement.getAttributes().getNamedItem("type").getNodeValue();
            String value = fieldElement.getAttributes().getNamedItem("value").getNodeValue();

            Field field = null;
            try {
                field = Class.forName(className).getDeclaredField(id);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Object v = null;

            if (type.equals("Integer")) {
                v = Integer.parseInt(value);
            } else if (type.equals("Double")) {
                v = Double.parseDouble(value);
            } else if (type.equals("String")){
                v = value;
            } else {
                continue;
            }

            field.setAccessible(true);
            try {
                field.set(object, v);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return object;
    }
}
