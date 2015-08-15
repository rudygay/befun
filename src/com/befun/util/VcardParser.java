package com.befun.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.befun.entity.Friend;

public class VcardParser {
	public Friend parse(String xmlStr) throws Exception{
		InputStream is = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
		Friend friend = new Friend();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList root = doc.getChildNodes();
	    Node iq = getNode("iq", root);
	    NodeList nodes = iq.getChildNodes();
	    Node vCard = getNode("vCard", nodes);
	    NodeList vnodes = vCard.getChildNodes();
	    String nickname = getNodeValue("NICKNAME", vnodes);
	    friend.setNickName(nickname);
	    String sex = getNodeValue("SEX", vnodes);
	    friend.setGender(sex);
		Log.v("ÐÔ±ð",friend.gender);
		Log.v("êÇ³Æ",friend.nickName);
		return friend;
	}
	protected Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }	 
	    return null;
	}
	protected String getNodeValue(String tagName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
	}
}
