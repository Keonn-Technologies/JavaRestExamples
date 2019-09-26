package util.spec;

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
 * @date 25 Jan 2018
 * @copyright 2018 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */

import util.spec.Location;


public class TagData {
	private String hexEpc;
	private long ts;
	private Location loc;
	private String antenna;
	private String oldAnt;
	private String power;
	private String phase;
	
	public TagData() {
		
	}

	public TagData(String hexEpc, String ts) {
		this.hexEpc = hexEpc;
		try {
			this.ts = Long.parseLong(ts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addLocation(String loc) {
		this.loc = new Location(loc);
	}

	public String getAntenna() {
		return antenna;
	}
	
	public void setPower(String pow){
		String[] split=pow.split(":");
		split=split[1].split(",");
		this.power = split[0];
	}
	
	public void setPhase(String phase){
		String[] split=phase.split(":");
		split=split[1].split(",");
		this.phase = split[0];
	}
	
	public String getPhase(){
		return this.phase;
	}
	
	public String getPower(){
		return this.power;
	}
	
	public String getOldAntenna() {
		return oldAnt;
	}

	public void setAntenna(String antenna) {
		String[] split=antenna.split(":");
		split=split[1].split(",");
		this.antenna = split[0];
	}

	public String getHexEpc() {
		return hexEpc;
	}

	public void setHexEpc(String hexEpc) {
		this.hexEpc = hexEpc;
	}
	
	public void setTs(String ts){
		this.ts=Long.valueOf(ts).longValue();
	}
	
	public String getTs(){
		return String.valueOf(ts);
	}
	
	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	@Override
	public String toString() {
		return "TagData [hexEpc=" + hexEpc + ", ts=" + ts + ", loc=" + loc + ", antenna=" + antenna + " power=" + power+ "]";
	}
	
}
