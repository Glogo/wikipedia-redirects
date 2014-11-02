package com.glogo.wikiparser;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashTest {
	
	private String title;

	public HashTest(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		HashFunction hf = Hashing.md5();
		HashCode hc = hf.newHasher().putString(title, Charsets.UTF_8).hash();
		return hc.asInt();
		
	}

	public static void main(String[] args) {
		Map<HashTest, Integer> map = new HashMap<HashTest, Integer>();
		
		SecureRandom random = new SecureRandom();
		
		long start = System.currentTimeMillis();
		for(int i=0; i<1000000; i++){
			map.put(new HashTest(new BigInteger(130, random).toString(32)), new Integer(1));
		}
		long end = System.currentTimeMillis();
		System.out.printf("%d\n", (end - start) / 1000);
	}

}
