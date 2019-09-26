package util.tcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
 * @author salmendros
 * @date 17 Jul 2017
 * @copyright 2015 Keonn Technologies S.L. {@link http://www.keonn.com}
 *
 */

public class TimerUtil {
	
	public enum Type {DOUBLE,DOUBLE_TIL_MAX}
	
	private static Map<String,Long> timers = Collections.synchronizedMap(new HashMap<String, Long>());
	
	/**
	 * Returns a time value according to the settings
	 * @param id
	 * @return
	 */
	public static long getTime(String id, long initial,  long max, Type type){
		Long l = timers.get(id);
		if(l==null){
			timers.put(id, initial);
			return initial;
		} else {
			
			if(Type.DOUBLE.equals(type)){
				
				l *=2;
				timers.put(id, l);
				return l;
				
			} else if(Type.DOUBLE_TIL_MAX.equals(type)){
				
				long ll = l*2;
				if(ll<max){
					l=ll;
					timers.put(id, ll);
				}
				
				return l;
			}
		}
		return l;
	}
	
	public static void reset(String id){
		timers.remove(id);
	}
}