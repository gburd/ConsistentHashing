package com.csforge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;

@SuppressWarnings("serial")
public class RendezvousHashTests {
	private static final Random rand = new Random();
	private static final HashFunction hfunc = Hashing.murmur3_128();
	private static final Funnel<String> strFunnel = new Funnel<String>(){ 
		public void funnel(String from, PrimitiveSink into) {
			into.putBytes(from.getBytes());
		}};
	
	/**
	 * Ensure the same node returned for same key after a large change to the pool of nodes
	 */
	@Test
	public void testConsistentAfterRemove() {
		RendezvousHash<String, String> h = genEmpty();
		for(int i = 0 ; i < 1000; i++) {
			h.add("node"+i);
		}
		String node = h.get("key"); 
		Assert.assertEquals(node, h.get("key"));
		
		for(int i = 0; i < 250; i++) {
			String toRemove = "node" + rand.nextInt(1000);
			if(!toRemove.equals(node)) {
				h.remove(toRemove);
			}
		} 
		Assert.assertEquals(node, h.get("key")); 
	}
	
	/**
	 * Ensure that a new node returned after deleted
	 */
	@Test
	public void testPreviousDeleted() {
		RendezvousHash<String, String> h = genEmpty();
		h.add("node1");
		h.add("node2");
		String node = h.get("key");
		h.remove(node);
		Assert.assertTrue(Sets.newHashSet("node1", "node2").contains(h.get("key")));
		Assert.assertTrue(!node.equals(h.get("key")));
	}
	
	/**
	 * Ensure same node will still be returned if removed/readded
	 */
	@Test
	public void testReAdd() {
		RendezvousHash<String, String> h = genEmpty();
		h.add("node1");
		h.add("node2");
		String node = h.get("key");
		h.remove(node);
		h.add(node);
		Assert.assertEquals(node, h.get("key"));
	}

	private static RendezvousHash<String, String> genEmpty() {
		return new RendezvousHash<String, String>(hfunc, strFunnel, strFunnel, new ArrayList<String>());
	}
}