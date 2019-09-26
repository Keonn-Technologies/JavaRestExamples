package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import util.spec.Antenna;
import util.spec.Device;
import util.spec.TagData;




/**
 * Copyright (c) 2018 Keonn technologies S.L.
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
 * This class is not Thread safe, if different threads call this class, it is better to create different instances
 * 
 * @author salmendros, abages
 * @date 6 Mar 2018
 * @copyright 2018 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */
public class RESTUtil {

	// XPath library initialization
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private boolean debug;
	private Util utilities;
	
	/**
	 * The different versions of AdvanNet can make the Rest Calls different,
	 * in this case we are working only with 2, AdvanNet-2.1.x (a21), and
	 * AdvanNet-2.3.x (a23)
	 */
	public static enum AdvanNetVersion {a21, a23}

	public static enum EventTypes {
		TAG_ALARM
	}

	public static enum ReadModesClassName {
		READMODE_EPC_EAS_ALARM, READMODE_SQL_EAS_ALARM
	}

	public RESTUtil(boolean debug) {
		this.debug = debug;
		this.utilities = new Util();
	}


	/**
	 * Starts or stops a device.<br>
	 * Starting a device means to connect to it, and whenever configured start
	 * RF operations.<br>
	 * - AdvanPanel does not start any RF operation at start time. -
	 * AdvanFitting starts RF operations at start time.
	 * 
	 * @param d
	 *            the device to be started
	 * @param start
	 *            whether to start or stop the device
	 * @throws IOException
	 */
	public void startStopDevice(Device d, boolean start) throws IOException, RuntimeException {

		/**
		 * Build the start/stop URL The URL depends on the id of the device
		 */
		URL startURL = new URL("http", d.getAddress(), 3161, "/devices/" + d.getId() + "/"
				+ (start ? "start" : "stop"));

		/**
		 * Access the URL
		 */
		String xmlFile = getFileFromURL(startURL);

		/**
		 * Print start device response
		 */
		if (debug) {
			System.out
					.println((start ? "Start" : "Stop") + " device response:");
			System.out.println("==============================");
			System.out.println(xmlFile);
			System.out.println("==============================");
		}

		if(xmlFile.contains("ERROR"))
			throw new RuntimeException("[ERROR] Failed to start/stop the device " + d.getId());
	}
	
	/**
	 * Helper method that executes a call to a given URL and parses the response
	 * as a string.
	 * 
	 * @param devicesURL
	 *            The URL to be accessed
	 * @return The response as a string
	 * @throws IOException
	 */
	public String getFileFromURL(URL devicesURL) throws IOException {

		InputStream is = devicesURL.openStream();
		InputStreamReader fr = null;

		try {
			fr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(fr);

			char buff[] = new char[65536];
			StringBuffer sb = new StringBuffer();
			int count;

			do {
				count = br.read(buff);
				if (count != -1)
					sb.append(buff, 0, count);
			} while (count != -1);

			return sb.toString();

		} catch (Exception exx) {
			exx.printStackTrace();
			return null;
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception exx2) {
				exx2.printStackTrace();
			}

			try {
				if (is != null)
					is.close();
			} catch (Exception exx2) {
				exx2.printStackTrace();
			}
		}	
	}
	
	/**
	 * Helper method that executes a call to a given URL and parses the response
	 * as a string.
	 * 
	 * @param devicesURL
	 *            The URL to be accessed
	 * @return The response as a string
	 * @throws IOException
	 */
	public String getFileFromURL(URL devicesURL, String postData)
			throws IOException {

		HttpURLConnection con = (HttpURLConnection) devicesURL.openConnection();

		con.setRequestMethod("PUT");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postData);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}	
	
	
	/**
	 * Parse the devices of an AdvanNet instance
	 * 
	 * @return A set of device objects
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public Set<Device> parseDevices(String address) throws IOException,
			XPathExpressionException {
		
		/**
		 * Build the inventory URL The URL does not depend on any variable
		 */
		URL devicesURL = new URL("http", address, 3161, "/devices");

		/**
		 * Access the URL to retrieve devices data
		 */

		String xmlFile = getFileFromURL(devicesURL);

		/**
		 * Print device file
		 */
		if (debug) {
			System.out.println("Raw devices data:");
			System.out.println("==============================");
			System.out.println(xmlFile);
			System.out.println("==============================");
		}
		
		String expression2 = "/response/msg-version/text()";

		// XPath expression evaluation
		InputSource inputSource2 = new InputSource(new StringReader(xmlFile));
		NodeList nodes2 = (NodeList) xpath.evaluate(expression2, inputSource2,
				XPathConstants.NODESET);
		String advanNetVersion = "";
		// Iteration over the results
		for (int i = 0; i < nodes2.getLength(); i++) {
			advanNetVersion = nodes2.item(i).getNodeValue();
		}

		/**
		 * Parse devices file Devices file is an XML file (a sample can be found
		 * at the end of this file)
		 * 
		 * The parsing of an XML file can be done using several different
		 * approaches, we have used the XML Path Language (XPath) but any
		 * approach is OK.
		 * 
		 * The goal is to retrieve the list of found devices
		 * 
		 */
		Set<Device> devices = new HashSet<Device>();

		// XPath expression to retrieve the id and family of all found devices
		String expression = "/response/data/devices/device/*[self::id or self::family]/text()";

		// XPath expression evaluation
		InputSource inputSource = new InputSource(new StringReader(xmlFile));
		NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource,
				XPathConstants.NODESET);

		// Iteration over the results
		for (int i = 0; i < nodes.getLength(); i += 2) {
			String id = nodes.item(i).getNodeValue();
			String family = nodes.item(i + 1).getNodeValue();

			devices.add(new Device(id, family, advanNetVersion, address));
		}

		return devices;
	}
	
	/**
	 * Parse the unique device in an AdvanNet instance
	 * 
	 * @return A device object defining the reader
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public Device parseDevice(String address) throws IOException, XPathExpressionException {
		Set<Device> devices = parseDevices(address);
		if (devices.isEmpty()) {
			throw new IOException("No devices found");
		}		
		else if (devices.size() == 1) {
			return devices.iterator().next();
		}

		throw new IOException("Wrong device number: " + devices.size());
	}
	
	
	/**
	 * Configure a set of antennas deleting the old ones
	 * @param d The device to configure the antennas
	 * @param lantennas A list of Antenna objects to configure
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public void setAntennaConfiguration(Device d,
			ArrayList<Antenna> lantennas) throws ParserConfigurationException,
			IOException, RuntimeException {

		URL antennasURL = new URL("http", d.getAddress(), 3161, "/devices/"
				+ d.getId() + "/antennas");

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element request = doc.createElement("request");
		doc.appendChild(request);

		Element entries = doc.createElement("entries");
		request.appendChild(entries);

		for (Antenna antenna : lantennas) {
			Document ant = this.getAntennaXML(antenna, d);
			entries.appendChild(doc.importNode(ant.getDocumentElement(), true));
		}

		String antennaConfiguration = this.utilities.DocToXML(doc);
		String xmlFileResponse = getFileFromURL(antennasURL,
				antennaConfiguration);

		if (xmlFileResponse.contains("ERROR"))
			throw new RuntimeException("[ERROR] The antenna could not be configured");

	}
	
	/**
	 * From an Antenna object it will create an XML document
	 * @param a Antenna object to use
	 * @param d The device for the antenna
	 * @return A document containing the XML representation of the Antenna
	 * @throws ParserConfigurationException
	 */
	private Document getAntennaXML(Antenna a, Device d)
			throws ParserConfigurationException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element entry = doc.createElement("entry");
		doc.appendChild(entry);

		Element antennaClass = doc.createElement("class");
		antennaClass.appendChild(doc.createTextNode("ANTENNA_DEFINITION"));
		entry.appendChild(antennaClass);

		Element cid = doc.createElement("cid");
		cid.appendChild(doc.createTextNode(d.getId()));
		entry.appendChild(cid);

		Element port = doc.createElement("port");
		port.appendChild(doc.createTextNode("" + a.getReaderPort()));
		entry.appendChild(port);

		Element mux1 = doc.createElement("mux1");
		mux1.appendChild(doc.createTextNode("" + a.getMux1()));
		entry.appendChild(mux1);

		Element mux2 = doc.createElement("mux2");
		mux2.appendChild(doc.createTextNode("" + a.getMux2()));
		entry.appendChild(mux2);

		Element orientation = doc.createElement("orientation");
		orientation.appendChild(doc.createTextNode("" + a.getOrientation()));
		entry.appendChild(orientation);

		Element location = doc.createElement("location");
		;
		entry.appendChild(location);

		Element locationClass = doc.createElement("class");
		locationClass.appendChild(doc.createTextNode("LOCATION"));
		location.appendChild(locationClass);

		Element locID = doc.createElement("locID");
		if (!a.getLocation().equals(""))
			locID.appendChild(doc.createTextNode(a.getLocation()));
		location.appendChild(locID);

		Element x = doc.createElement("x");
		x.appendChild(doc.createTextNode("" + a.getX()));
		location.appendChild(x);

		Element y = doc.createElement("y");
		y.appendChild(doc.createTextNode("" + a.getY()));
		location.appendChild(y);

		Element z = doc.createElement("z");
		z.appendChild(doc.createTextNode("" + a.getZ()));
		location.appendChild(z);
		
		Element conf = doc.createElement("conf");
		entry.appendChild(conf);

		Element confClass = doc.createElement("class");
		confClass.appendChild(doc.createTextNode("ANTENNA_CONF"));
		conf.appendChild(confClass);

		Element power = doc.createElement("power");
		if(a.getPower() != 0){
			power.appendChild(doc.createTextNode("" + a.getPower()));
		}
		conf.appendChild(power);

		Element sensitivity = doc.createElement("sensitivity");
		if(a.getSensitivity() != 0){
			sensitivity.appendChild(doc.createTextNode("" + a.getSensitivity()));
		}
		conf.appendChild(sensitivity);

		Element readTime = doc.createElement("readTime");
		conf.appendChild(readTime);

		return doc;
	}

	
	/**
	 * Function to retrieve the read mode of the device
	 * @param d The device to get the read mode
	 * @return String with the read mode of the device
	 * @throws XPathExpressionException
	 * @throws IOException
	 */
	public String getReadMode(Device d) throws XPathExpressionException,
			IOException, RuntimeException {
		/**
		 * Build the URL The URL depends on the id of the device
		 */
		URL uRL = new URL("http", d.getAddress(), 3161, "/devices/" + d.getId()
				+ "/activeReadMode");

		/**
		 * Access the URL to retrieve the content
		 */
		String xmlFile = getFileFromURL(uRL);

		/**
		 * Print file
		 */
		if (debug) {
			System.out.println("Raw data:");
			System.out.println("==============================");
			System.out.println(xmlFile);
			System.out.println("==============================");
		}

		/**
		 * Parse file File is an xml file (a sample can be found at the end of
		 * this file)
		 * 
		 * The parsing of an xml file can be done using several different
		 * approaches, we have used the XML Path Language (XPath) but any
		 * approach is ok.
		 * 
		 * The goal is to retrieve the value of a readMode
		 * 
		 */

		// XPath expression to retrieve the id and family of all found devices
		String expression = "/response/data/result/text()";

		// XPath expression evaluation
		InputSource inputSource = new InputSource(new StringReader(xmlFile));
		NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource,
				XPathConstants.NODESET);

		if (nodes.getLength() == 0)
			return "";
		if (nodes.getLength() > 1)
			throw new RuntimeException("[WRONG_RESPONSE]");

		return nodes.item(0).getNodeValue();
	}
	
	/**
	 * Sets the device mode for the device
	 * @param d The device to change the device mode
	 * @param deviceMode String with the device mode
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public void setDeviceMode(Device d, String deviceMode) throws IOException, RuntimeException {

		/**
		 * Build the URL The URL depends on the id of the device
		 */
		URL inventoryURL = new URL("http", d.getAddress(), 3161, "/devices/" + d.getId()
				+ "/activeDeviceMode");

		/**
		 * Access the URL to retrieve response data
		 */
		String xmlFileResponse = getFileFromURL(inventoryURL, deviceMode);

		if (xmlFileResponse.contains("ERROR"))
			throw new RuntimeException("[ERROR] The device mode " + deviceMode + " was not able to be set");
	}
	
	/**
	 * Parse the device modes of the reader
	 * 
	 * @param d
	 *            The device to retrieve the device modes
	 * @return The name of all the deviceModes (an arrayList)
	 * @throws XPathExpressionException
	 * @throws IOException
	 */
	public ArrayList<String> getDeviceModesName(Device d)
			throws XPathExpressionException, IOException {

		/**
		 * Build the read modes URL (it depends on the id of the device)
		 */
		URL readModesURL = new URL("http", d.getAddress(), 3161, "/devices/" + d.getId()
				+ "/deviceModes");

		/**
		 * Access the URL to retrieve read modes
		 */
		String xmlFile = getFileFromURL(readModesURL);

		/**
		 * Print read mode file
		 */
		if (debug) {
			System.out.println("Raw read modes data:");
			System.out.println("==============================");
			System.out.println(xmlFile);
			System.out.println("==============================");
		}

		/**
		 * Parse read modes file Read mode file is an XML file (a sample can be
		 * found at the end of this file)
		 * 
		 * The parsing of an XML file can be done using several different
		 * approaches, we have use the XML Path Language (XPath) but any
		 * approach is OK.
		 * 
		 * The goal is to retrieve the list of parameter names
		 * 
		 */
		ArrayList<String> names = new ArrayList<String>();

		// XPath expression to retrieve all the device mode's name
		String expression = "/response/data/entries/entry/readModes/readMode/name/text()";

		// XPath expression evaluation
		InputSource inputSource = new InputSource(new StringReader(xmlFile));
		NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource,
				XPathConstants.NODESET);

		// Iteration over the results
		for (int i = 0; i < nodes.getLength(); i++) {
			String name = nodes.item(i).getNodeValue();
			names.add(name);
		}

		return names;
	}
	
	/**
	 * Adds an EPC to a list
	 * @param data
	 * @param tagDataList
	 */
	public void processTCPdata(String data, List<String> tagDataList) {

		Pattern TAG_REGEX = Pattern.compile("<epc>(.+?)</epc>");
		Matcher matcher = TAG_REGEX.matcher(data);
		while (matcher.find()) {
			tagDataList.add(matcher.group(1));
		}
	}

	
	/**
	 * Function to get the inventory information
	 * @param d The device to get the inventory information
	 * @return A Set of TagDatas with all the inventory information
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public Set<TagData> getInventory(Device d) throws IOException,
			XPathExpressionException {

		/**
		 * Build the inventory URL The URL depends on the id of the device
		 */
		URL inventoryURL = new URL("http", d.getAddress(), 3161, "/devices/" + d.getId()
				+ "/inventory");

		/**
		 * Access the URL to retrieve inventory data
		 */
		String xmlFile = getFileFromURL(inventoryURL);

		/**
		 * Print raw inventory file
		 */
		if (debug) {
			System.out.println("Raw inventory data:");
			System.out.println("==============================");
			System.out.println(xmlFile);
			System.out.println("==============================");
		}

		/**
		 * Parse inventory data Inventory data is an xml file (a sample can be
		 * found at the end of this file)
		 * 
		 * The parsing of an xml file can be done using several different
		 * approaches, we have use the XML Path Language (XPath) but any
		 * approach is ok.
		 * 
		 * The goal is to retrieve the list of found tags
		 * 
		 */
		Set<TagData> tags = new HashSet<TagData>();

		// XPath expression to retrieve the epc and ts of all found tags
		String expression = "/inventory/data/inventory/items/item/*[self::epc or self::ts]/text()";

		// XPath expression evaluation
		InputSource inputSource = new InputSource(new StringReader(xmlFile));
		NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource,
				XPathConstants.NODESET);

		// Iteration over the results
		for (int i = 0; i < nodes.getLength(); i += 2) {
			String hexEpc = nodes.item(i).getNodeValue();
			String ts = nodes.item(i + 1).getNodeValue();

			tags.add(new TagData(hexEpc, ts));
		}

		return tags;
	}

}
