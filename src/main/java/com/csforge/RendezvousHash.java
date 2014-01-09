package com.csforge;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;

/**
 * <p>Rendezvous or Highest Random Weight (HRW) hashing is an algorithm that allows clients to achieve distributed agreement on which site (or proxy) a given
 * object is to be placed in. It accomplishes the same goal as consistent hashing, using an entirely different method.</p>
 * 
 * <p>
 * Rendezvous hashing has the following properties.
 * <ul>
 * <li>Low overhead: providing using a hash function of low overhead</li>
 * <li>Load balancing: Since the hash function is randomizing, each of the n sites is equally likely to receive the object O. Loads are uniform across the sites.</li>
 * <li>High hit rate: Since all clients agree on placing an object O into the same site SO , each fetch or placement of O into SO yields the maximum utility in terms of hit rate. The object O will always be found unless it is evicted by some replacement algorithm at SO .</li>
 * <li>Minimal disruption: When a site fails, only the objects mapped to that site need to be remapped. Disruption is at the minimal possible level</li>
 * </ul></p>
 * source: https://en.wikipedia.org/wiki/Rendezvous_hashing
 * 
 * @author Chris Lohfink
 * 
 * @param <K>
 *            type of key
 * @param <N>
 *            type of whatever want to be returned (ie IP address or String)
 */
public class RendezvousHash<K, N> {

	/**
	 * A hashing function from guava, ie Hashing.murmur3_128()
	 */
	private HashFunction hasher;

	/**
	 * A funnel to describe how to take the key and add it to a hash.
	 * 
	 * @see com.google.common.hash.Funnel
	 */
	private Funnel<K> keyFunnel;

	/**
	 * Funnel describing how to take the type of the node and add it to a hash
	 */
	private Funnel<N> nodeFunnel;

	/**
	 * All the current nodes in the pool
	 */
	private Set<N> nodes;

	/**
	 * Creates a new RendezvousHash with a starting set of nodes provided by init. The funnels will be used when generating the hash that combines the nodes and
	 * keys. The hasher specifies the hashing algorithm to use.
	 */
	public RendezvousHash(HashFunction hasher, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, Collection<N> init) {
		this.hasher = hasher;
		this.keyFunnel = keyFunnel;
		this.nodeFunnel = nodeFunnel;
		this.nodes = Sets.newCopyOnWriteArraySet();
		nodes.addAll(init);
	}

	/**
	 * Removes a node from the pool. Keys that referenced it should after this be evenly distributed amongst the other nodes
	 * 
	 * @return true if the node was in the pool
	 */
	public boolean remove(N node) {
		return nodes.remove(node);
	}

	/**
	 * Add a new node to pool and take an even distribution of the load off existing nodes
	 * 
	 * @return true if node did not previously exist in pool
	 */
	public boolean add(N node) {
		return nodes.add(node);
	}

	/**
	 * return a node for a given key
	 */
	public N get(K key) {
		long maxValue = Long.MIN_VALUE;
		N max = null;
		for (N node : nodes) {
			long nodesHash = hasher.newHasher().putObject(key, keyFunnel).putObject(node, nodeFunnel).hash().asLong();
			if (nodesHash > maxValue) {
				max = node;
				maxValue = nodesHash;
			}
		}
		return max;
	}
}