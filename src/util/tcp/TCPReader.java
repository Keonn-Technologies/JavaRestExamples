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
 * ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
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
 * @date 24 Jan 2018
 * @copyright 2018 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */


package util.tcp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.xpath.XPathExpressionException;

import util.RESTUtil;
import util.Util;
import util.spec.Device;
import util.spec.Tuple;

public class TCPReader extends Thread{

	public static final char CHAR_TAB = 9;
	public static final char CHAR_NEW_LINE = 10;
	public static final char CHAR_CR = 13;
	public static final int BUFFER_LENGTH = 512;
	
	public static final int DEFAULT_MAX_QUEUE_SIZE = 200;
	private static int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
	
	private boolean shutdown=false;
	private volatile boolean isInError;
	private boolean firstRun=true;
	private BufferedInputStream in;

	private String IPAddress;
	private LinkedBlockingQueue<Tuple<String, String> > queue;
	private RESTUtil restUtil;
	private Device device;
	
	private final AtomicBoolean isAlive = new AtomicBoolean(true);
	
	public TCPReader(String IPAddress, LinkedBlockingQueue<Tuple<String, String> > queue, RESTUtil restUtil){
		super();
		this.IPAddress = IPAddress;
		this.queue = queue;
		this.restUtil = restUtil;
		try {
			this.device = this.restUtil.parseDevice(this.IPAddress);
		} catch (XPathExpressionException | IOException e) {
			System.out.println(Util.getCurrentTime() + " " + e);
		}
		
		System.out.println(Util.getCurrentTime() + " Max Queue size: " + maxQueueSize);
	}
	
	public void shutdown() {
		shutdown = true;
		
		synchronized (queue) {
			queue.notify();
		}
	}
	
	public void startDevice() throws XPathExpressionException, IOException{
		this.device = this.restUtil.parseDevice(this.IPAddress);
		System.out.println(Util.getCurrentTime() + " Detected device: " + this.device.getId());

		/**
		 * Stop the device, change the Device Mode, and 
		 * change the session
		 */
		this.restUtil.startStopDevice(this.device, false);
		/*this.restUtil.setDeviceMode(device, "Autonomous");
		this.restUtil.setGEN2_SESSION(device, "S0");*/
		this.restUtil.startStopDevice(this.device, true);
	}
	
	public void run(){
		
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		
		while(!shutdown){
			
			try (Socket socket = new Socket()) {
				
				while(!shutdown) {
					
					if(isInError || firstRun) {
							
						socket.setSoTimeout(8000);
						socket.connect(new InetSocketAddress(IPAddress, 3177), 8000);
						System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] connection established.");
						in = new BufferedInputStream(socket.getInputStream());
						TimerUtil.reset(getClass().getName());
						if(isInError){
							startDevice();
						}
						if (firstRun) {
							executor.scheduleAtFixedRate(() -> {
								if (!isAlive.compareAndSet(true, false) && !isInError){
									isInError = true;
									try {
										System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] connection not alive, closing...");
										socket.close();
									} catch (Exception e) {
										System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] IOException receiving message: " + e.getMessage());
									}
								}
							}, 10, 10, TimeUnit.SECONDS);
						}
						
						isInError=false;
						firstRun=false;
					}
						
					// read header
					String line = readLine(in,"UTF-8", true, true);
					if(line==null){
						isInError=true;
					}
						
					int errorCounter=0;
					boolean serveRequest=false;
					while(true && !shutdown){
						if(line == null){
							isInError=true;
							break;
						}
							
						if(line.startsWith("ADVANNET")){
							//String version = getLastPart(line, "/");
							serveRequest=true;
							break;
						}
							
						if(errorCounter++ > 100){
							System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] Receiving garbage data[" + line + "]. Resetting connection");
							isInError=true;
							break;
						}
						line = readLine(in,"UTF-8", true, true);
						if(line == null){
							isInError=true;
							break;
						}
					}
						
					if(isInError || !serveRequest){
						continue;
					}
						
					line = readLine(in,"UTF-8", true, true);
					if(line==null){
						isInError=true;
						continue;
					}
						
					int length=0;
					try {
						String ss = getLastPart(line, ":");
						length = isVoid(ss)?0:Integer.parseInt(ss);
					} catch (NumberFormatException e) {
						System.out.println(Util.getCurrentTime() + " Invalid header length: "+line);
						break;
					}
						
					
					line = readLine(in,"UTF-8", true, true);
					if(line == null){
						isInError = true;
						continue;
					}
						
					// Read Content-type
					line = readLine(in,"UTF-8", true, true);
					if(line == null){
						continue;
					}
						
					if(length>0){
						isAlive.set(true);
						
						byte[] buf = new byte[length];
						int bufferLength = length>BUFFER_LENGTH?BUFFER_LENGTH:length;
						int l=-1;
						int offset=0;
							
						while(true && !shutdown){
							
							l = in.read(buf, offset, bufferLength);
							offset+=l;
							
							if(offset==length) break;
							
							if(length-offset>BUFFER_LENGTH){
								bufferLength=BUFFER_LENGTH;
							} else {
								bufferLength=length-offset;
							}
						}
							
						String rspns = new String(buf, "UTF-8");
	
						if (queue.size() >= maxQueueSize) {
							System.out.println(Util.getCurrentTime() + " Queue full, deleting the oldest message");
							queue.take();
						}
						queue.put(new Tuple<String, String>(this.device.getId(), rspns));
						
						synchronized (queue) {
							queue.notify();
						}
						
						if(!serveRequest){
							continue;
						}
					} else {
						isAlive.set(true);
					}
					
				}						
			} catch (IOException e) {
					
				if("Socket closed".equals(e.getMessage()) && shutdown){
					System.out.println(Util.getCurrentTime() + " ["+IPAddress+"] connection closed");
				} else {
					System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] IOException receiving message: " + e.getMessage());
					
					isInError = true;
					try {
						long l = TimerUtil.getTime(getClass().getName(), 2000, 65000, TimerUtil.Type.DOUBLE_TIL_MAX);
						System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] Waiting " + l + " ms. to reconnect.");
						if(shutdown)
							break;
						Thread.sleep(l);
					} catch (InterruptedException e1) {
						System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] InterruptedException caught. Terminating thread.");
						break;
					}
				}
				
			} catch (Exception e) {
				System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] Exception receiving message: " + e.getMessage());
				isInError  = true;
				try {
					long l = TimerUtil.getTime(getClass().getName(), 2000, 65000, TimerUtil.Type.DOUBLE_TIL_MAX);
					System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] Waiting " + l + " ms. to reconnect.");
					if(shutdown)
						break;
					Thread.sleep(l);
				} catch (InterruptedException e1) {
					System.out.println(Util.getCurrentTime() + " [" + IPAddress + "] InterruptedException caught. Terminating thread.");
					break;
				}
			}
		} 
		System.out.println(Util.getCurrentTime() + " End of TCPReader.");
		if (executor != null)
			executor.shutdown();
	}
	
	public String readLine(InputStream in, String charset, boolean onlyReadable, boolean trimLine) throws IOException {
		
		BufferedInputStream bis = null;
		if(in instanceof BufferedInputStream){
			bis = (BufferedInputStream) in;
		} else {
			bis = new BufferedInputStream(in);
		}
		
		int bufferSize = 128;
		byte[] buffer = new byte[bufferSize];
		int index = 0;
		
		while(true){
			
			int b = bis.read();
			if(onlyReadable && !isReadable(b)){
				return null;
			}
			
			/*
			 * abc Jan 18
			 * 
			 * The method read() returns a byte of data, so \r\n will be read separately.
			 * That is, b = "\r" and b = "\n" afterwards. 
			 *  
			 */
			
			if(b == '\r'){
				if(bis.available() > 0){
					bis.mark(4);
					int ii = bis.read();
					if(ii != '\n'){
						bis.reset();
					}
					if(trimLine){
						return new String(buffer,0,index,charset).trim();
					} else {
						return new String(buffer,0,index,charset);
					}
					
				}
			} else if (b == '\n') {
				if(trimLine){
					return new String(buffer,0,index,charset).trim();
				} else {
					return new String(buffer,0,index,charset);
				}
				
			}
						
			if(index == bufferSize){
				byte[] tmp = new byte[bufferSize*2];
				System.arraycopy(buffer, 0, tmp, 0, index);
				buffer=tmp;
				bufferSize*=2;
			}
			
			buffer[index++]=(byte) b;
		}
	}
	
	public String getLastPart(String s, String separator){
		if(s != null && separator != null){
			int index = s.lastIndexOf(separator);
			if(index != -1)
				return s.substring(index+separator.length());
			else
				return s;
		}
		
		return null;
	}
	
	public boolean isVoid(String str)
	{
		if (str == null || str.length() == 0)
			return true;

		return str.trim().length() == 0;
	}
	
	private boolean isReadable(int c) {
		return (c >= 32 && c <= 126) || c == CHAR_TAB || c == CHAR_NEW_LINE
				|| c == CHAR_CR;
	}
	
	public static int getMaxQueueSize() {
		return maxQueueSize;
	}
}