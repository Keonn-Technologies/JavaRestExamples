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
 * @date 25 Jan 2018
 * @copyright 2018 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */

public class Antenna {
	private int readerPort;
	private int mux1;
	private int mux2;
	private int orientation;
	private String location;
	private int x, y, z;
	private int power, sensitivity;

	public Antenna(int readerPort, int mux1, int mux2) {
		this.readerPort = readerPort;
		this.mux1 = mux1;
		this.mux2 = mux2;

		this.orientation = 0;
		this.x = this.y = this.z = 0;
		this.power = this.sensitivity = 0;
		this.location = "loc_id";
	}

	public Antenna(int readerPort, int mux1, int mux2, int orientation,
			String location, int x, int y, int z) {
		super();
		this.readerPort = readerPort;
		this.mux1 = mux1;
		this.mux2 = mux2;
		this.orientation = orientation;
		this.location = location;
		this.x = x;
		this.y = y;
		this.z = z;
		this.power = this.sensitivity = 0;
	}

	public Antenna(String readerPort, String mux1, String mux2,
			String orientation, String location, String x, String y,
			String z) {
		super();
		this.readerPort = Integer.parseInt(readerPort);
		this.mux1 = Integer.parseInt(mux1);
		this.mux2 = Integer.parseInt(mux2);
		this.orientation = Integer.parseInt(orientation);
		this.location = location;
		this.x = Integer.parseInt(x);
		this.y = Integer.parseInt(y);
		this.z = Integer.parseInt(z);
		this.power = this.sensitivity = 0;
	}

	public Antenna(String readerPort, String mux1, String mux2) {
		this(Integer.parseInt(readerPort), Integer.parseInt(mux1), Integer
				.parseInt(mux2));
	}

	public int getReaderPort() {	return readerPort;	}
	public int getMux1() {			return mux1;	}
	public int getMux2() {			return mux2;	}
	public int getOrientation() {	return orientation;	}
	public String getLocation() {	return location;	}
	public int getX() {				return x;	}
	public int getY() {				return y;	}
	public int getZ() {				return z;	}
	public int getPower() {			return power;	}
	public int getSensitivity() {	return sensitivity;	}

	public void setReaderPort(int readerPort) {		this.readerPort = readerPort;	}
	public void setMux1(int mux1) {					this.mux1 = mux1;	}
	public void setMux2(int mux2) {					this.mux2 = mux2;	}
	public void setOrientation(int orientation) {	this.orientation = orientation;	}
	public void setLocation(String location) {		this.location = location; }
	public void setX(int x) {						this.x = x;	}
	public void setY(int y) {						this.y = y; }
	public void setZ(int z) {						this.z = z; }
	public void setPower(int power) {				this.power = power; }
	public void setPower(String power) {			this.power = Integer.parseInt(power); }
	public void setSensitivity(int sensitivity) {	this.sensitivity = sensitivity; }
	public void setSensitivity(String sensitivity) {this.sensitivity = Integer.parseInt(sensitivity); }

	@Override
	public String toString() {
		return "Antenna [readerPort=" + readerPort + ", mux1=" + mux1
				+ ", mux2=" + mux2 + ", orientation=" + orientation
				+ ", location=" + location + ", x=" + x + ", y=" + y
				+ ", z=" + z + ", power=" + power + ", sensitivity="
				+ sensitivity + "]";
	}
}
