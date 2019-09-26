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
 * 
 * @author salmendros, abages
 * @date 6 Mar 2018
 * @copyright 2018 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */


package adrd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import util.RESTUtil;
import util.Util;
import util.spec.Device;
import util.spec.Device.ReadMode;
import util.spec.Tuple;
import util.tcp.TCPReader;

public class ADRDAsynch{

	private static String address;
	private static boolean debug = false;
	private boolean keepRunning = true;
	private static Device device;
	private ArrayList<String> deviceModeNames;
	private static String AUTONOMOUS_MODE = "autonomous"; 
	private static RESTUtil util;
	private TCPReader tcpReader;
	private LinkedBlockingQueue<Tuple<String, String> > queue = new LinkedBlockingQueue<Tuple<String, String> >();
	private List<String> tagDataList = new ArrayList<String>(); 
	private static ADRDAsynch app = null;
	private static int dailyTagsCounter = 0;
	private static int totalTagsCounter = 0;
	private static int keepRunningTime = 10;	//minutes
	
	
	public ADRDAsynch(String address) {
		util = new RESTUtil(debug);
	}
	
	public static ADRDAsynch getInstance(){
		return app;
	}

	/**
	 * Entry method.
	 * The application accepts two parameters:
	 * -h: hostname or IP address where AdvanNet is running
	 * -d: to enable debug messages
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		if (args.length < 1) {
			System.out.println("Please specify the IP of the reader");
			return;
		}
		
		//Get the address from the arguments.
		address = args[0];
		
		//Demo class initialization
		app = new ADRDAsynch(address);
		
		/**
		 * On application shutdown make sure to stop devices.
		 * This is not required, it's not a problem if a devices stays 'running'
		 */
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				app.shutdown();
			}
		});
		
		//Log the number of tags read every day
		ScheduledExecutorService tagCountlogger = Executors.newScheduledThreadPool(1);
		tagCountlogger.scheduleAtFixedRate(() -> {
			System.out.println(Util.getCurrentTime() + "Tags read: " + dailyTagsCounter + " today, " + totalTagsCounter + " in total.");
			dailyTagsCounter = 0;
		}, 1, 1, TimeUnit.DAYS);
		
		//Make sure the device is not left stopped
		ScheduledExecutorService keepAlwaysRunning = Executors.newScheduledThreadPool(1);
		keepAlwaysRunning.scheduleAtFixedRate( () -> {
			try {
				util.startStopDevice(device, true);
			} catch (IOException e) {
				System.out.println(Util.getCurrentTime() + " " + e);
			}
		}, 1, keepRunningTime, TimeUnit.MINUTES);
		
		//Run the demo application
		app.run();
		keepAlwaysRunning.shutdown();
		tagCountlogger.shutdown();
	}
	
	/**
	 * Shutdown method that runs when the application terminates or the msdos window is closed by the user.<br>
	 * The method just stops any running AdvanReader device
	 */
	protected void shutdown() {
		try {
			keepRunning = false;
			util.startStopDevice(device,false);
			tcpReader.shutdown();
		} catch (IOException e) {
			System.out.println(Util.getCurrentTime() + " " + e);
		}
	}

	/**
	 * Main method of the demo application.<br>
	 */
	private void run() {
		
		try{

			/**
			 * Get the device connected to Advannet. 
			 * @parseDevice checks if there is any and throws an Exception otherwise. 
			 * 
			 */
			device = util.parseDevice(address);

			
			/**
			 * Execute an inventory operation on the Advanreader
			 * 
			 * 	1) Check if the unit has Autonomous mode. If so, force the read mode to be Autonomous
			 *	2) Start an inventory. In this example, it runs until the program ends
			 * 	3) Create a new thread to listen in the 3177 port
			 *  	3.1) Send the information received to a queue that will read the main thread
			 * 	4) The main thread will parse the information in the queue, and will extract the epc's.
			 *  
			 */

			// start dispatcher
			tcpReader = new TCPReader(address, this.queue, util);
			
			deviceModeNames = util.getDeviceModesName(device);


			/** Check if the device has Autonomous mode (or Test mode) **/
			if (deviceModeNames.toString().toLowerCase().contains(AUTONOMOUS_MODE)) {
				
				//Get the current Mode 
				String readMode = util.getReadMode(device);
				
				//Change to Autonomous ReadMode
				if(!ReadMode.AUTONOMOUS.toString().equals(readMode)){
					util.setDeviceMode(device, "Autonomous");
				}
				
				//Make sure device is started
				System.out.println(Util.getCurrentTime() + " Device[" + device.getId() + "] Making sure device is started... ");
				util.startStopDevice(device,true);
				Thread.sleep(200);
				System.out.println(Util.getCurrentTime() + " Done.");
				
				System.out.println(Util.getCurrentTime() + " Device[" + device.getId() + "] Reading the 3177 port... ");
				tcpReader.start();					
				System.out.println(Util.getCurrentTime() + " Done.");
				
				while (keepRunning) {
				
					synchronized (queue) {
						while(queue.isEmpty()){
							try {
								queue.wait();
							} catch (InterruptedException e) {
								return;
							}
						}
					}
					
					if(!queue.isEmpty()){
					
						Tuple<String, String>  t;
						while((t = queue.poll()) != null){
							
							util.processTCPdata(t.getB(), this.tagDataList);
							
							for(String tagData : this.tagDataList) {
								
								System.out.println(Util.getCurrentTime() + " TAG READ: " + "epc[" + tagData + "]");
								dailyTagsCounter++;
								totalTagsCounter++;
							}
							this.tagDataList.clear();
						}
					}
				}
				
			} else {
				System.out.println(Util.getCurrentTime() + " Device [" + device.getId() + "] does not have Autonomous mode. Exiting");
				System.exit(1);
			}

		} catch (Exception e) {
			System.out.println(Util.getCurrentTime() + " " + e);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				System.out.println(Util.getCurrentTime() + " " + e1);
			}
			run();
		}
	}
}