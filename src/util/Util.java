package util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Copyright (c) 2015 Keonn technologies S.L.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY KEONN TECHNOLOGIES S.L.
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL KEONN TECHNOLOGIES S.L.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * 
 * @author salmendros, abages
 * @date 6 Mar 2017
 * @copyright 2015 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */

public class Util {
	
	private HashMap<String, String> hexCharacterToBinary = 
			new HashMap<String, String>();
	private HashMap<String, String> binaryStringToHexCharacter = 
			new HashMap<String, String>();
	
	public Util(){
		this.fillHexCharacterToBinaryHashMap();
		this.filBinaryStringToHexCharacter();
	}
	
	private void fillHexCharacterToBinaryHashMap(){
		this.hexCharacterToBinary.put("0", "0000");
		this.hexCharacterToBinary.put("1", "0001");
		this.hexCharacterToBinary.put("2", "0010");
		this.hexCharacterToBinary.put("3", "0011");
		this.hexCharacterToBinary.put("4", "0100");
		this.hexCharacterToBinary.put("5", "0101");
		this.hexCharacterToBinary.put("6", "0110");
		this.hexCharacterToBinary.put("7", "0111");
		this.hexCharacterToBinary.put("8", "1000");
		this.hexCharacterToBinary.put("9", "1001");
		this.hexCharacterToBinary.put("a", "1010");
		this.hexCharacterToBinary.put("b", "1011");
		this.hexCharacterToBinary.put("c", "1100");
		this.hexCharacterToBinary.put("d", "1101");
		this.hexCharacterToBinary.put("e", "1110");
		this.hexCharacterToBinary.put("f", "1111");
	}
	
	private void filBinaryStringToHexCharacter(){
		this.binaryStringToHexCharacter.put("0000", "0");
		this.binaryStringToHexCharacter.put("0001", "1");
		this.binaryStringToHexCharacter.put("0010", "2");
		this.binaryStringToHexCharacter.put("0011", "3");
		this.binaryStringToHexCharacter.put("0100", "4");
		this.binaryStringToHexCharacter.put("0101", "5");
		this.binaryStringToHexCharacter.put("0110", "6");
		this.binaryStringToHexCharacter.put("0111", "7");
		this.binaryStringToHexCharacter.put("1000", "8");
		this.binaryStringToHexCharacter.put("1001", "9");
		this.binaryStringToHexCharacter.put("1010", "a");
		this.binaryStringToHexCharacter.put("1011", "b");
		this.binaryStringToHexCharacter.put("1100", "c");
		this.binaryStringToHexCharacter.put("1101", "d");
		this.binaryStringToHexCharacter.put("1110", "e");
		this.binaryStringToHexCharacter.put("1111", "f");
	}
	
	public void printList(ArrayList<String> l){
		for (String string : l) {
			System.out.println(string);
		}
	}

    public String HexStringToBinary(String hex)
    {
        StringBuilder result = new StringBuilder();
        
        for(int i=0; i<hex.length(); i++){
        	String aux = hex.substring(i, i+1).toLowerCase();
        	result.append(this.hexCharacterToBinary.get(aux));
        }
        
        return result.toString();
    }
	
    public String BinaryStringToHex(String binary)
    {
        StringBuilder result = new StringBuilder();
        
        for(int i=0; i<binary.length(); i+=4){
        	result.append(this.binaryStringToHexCharacter.get(binary.substring(i, i+4)));
        }
        
        return result.toString();
    }
    
    public String DocToXML(Document doc){
    	TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = null;
		try {
			trans = transfac.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		trans.setOutputProperty(OutputKeys.METHOD, "xml");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc.getDocumentElement());

		try {
			trans.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		String xml = sw.toString();
		xml = xml.replaceAll("\t", "");
		xml = xml.replaceAll("\n", "");
		return xml;
    }
    
    public static Document stringToDom(String xmlSource) throws SAXException,
		ParserConfigurationException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xmlSource)));
	}
	
    //Get the current time in String format
  	public static String getCurrentTime() {
  		return new SimpleDateFormat("[MM/dd/yyyy HH:mm:ss]").format(new Date());
  	}
}
