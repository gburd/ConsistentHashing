Consistent Hashing
==================

Most hash tables require most if not all keys to be re-mapped into the buckets within the hash table when it is resized.  Consistent hashing is the name given to the set of hash algorithms that when a hash table is resized that on average K/n keys need to be remapped into new buckets, where K is the number of keys, and n is the number of buckets.  Hash table algorithms require coordination when changing size (up or down) generally in response to observing too many collisions (see [section 8.3.2](https://www.cs.auckland.ac.nz/~jmor159/PLDS210/hash_tables.html).

Consistent Hashing
 * [Wikipedia](https://en.wikipedia.org/wiki/Consistent_hashing)
 * [Consistent Hashing and Random Trees: Distributed Caching Protocols for Relieving Hot Spots on the World Wide Web](http://www.akamai.com/dl/technical_publications/ConsistenHashingandRandomTreesDistributedCachingprotocolsforrelievingHotSpotsontheworldwideweb.pdf)
 * http://www.cs.princeton.edu/courses/archive/fall09/cos518/papers/chash.pdf 
 * http://dl.acm.org/citation.cfm?id=258660
 * http://www.tom-e-white.com/2007/11/consistent-hashing.html

Rendezvous or Highest Random Weight (HRW) Hashing

An alternative to the ring based, consistent hashing.  This is a fast thread safe implementation of Rendezvous (Highest Random Weight, HRW) hashing.  An algorithm that allows clients to achieve distributed agreement on which node (or proxy) a given key is to be placed in. This implementation has the following properties.

* Non-blocking reads : Determining which node a key belongs to is always non-blocking.  Adding and removing nodes however blocks each other.
* Low overhead: providing using a hash function of low overhead.  Throughput can be computed as (hashes computable per sec)/node count
* Load balancing: Since the hash function is randomizing, each of the n nodes is equally likely to receive the key K. Loads are uniform across the sites.
* High hit rate: Since all clients agree on placing an key K into the same node N , each fetch or placement of K into N yields the maximum utility in terms of hit rate. The key K will always be found unless it is evicted by some replacement algorithm at N.
* Minimal disruption: When a node is removed, only the keys mapped to that node need to be remapped and they will be distributed evenly

 * https://en.wikipedia.org/wiki/Rendezvous_hashing
 * https://github.com/clohfink/RendezvousHash
 * http://www.eecs.umich.edu/techreports/cse/96/CSE-TR-316-96.pdf
 * https://escholarship.org/uc/item/3ks7q6mx.pdf

Jump Consistent Hashing
 * Google's "Jump" Consistent Hashing [A Fast, Minimal Memory, Consistent Hash Algorithm](http://arxiv.org/pdf/1406.2294.pdf)
 * https://github.com/anachronistic/jump-consistent-hash


In comparison ([source code](https://github.com/clohfink/RendezvousHash/blob/master/src/main/java/com/csforge/Compare.java)) of Consistent hashing and Rendezvous hashing, consider the following load distribution after removing a couple nodes in a 5 node ring:

![](https://raw.github.com/clohfink/RendezvousHash/master/images/chd.png)

Only node4 takes the load of the 2 that were removed.  However using HRW the distribution remains even

![](https://raw.github.com/clohfink/RendezvousHash/master/images/hrwd.png)

This example uses a [rather simple ring](https://github.com/clohfink/RendezvousHash/blob/master/src/main/java/com/csforge/ConsistentHash.java) for consistent hash implementation however and this extreme unbalance can be mitigated by adding the nodes many times (ie ~200) throughout the ring.  These virtual nodes (or vnodes) are used in databases like Riak and Cassandra.  Many libraries however do not implement vnodes.

Example:

```java
    private static final Funnel<CharSequence> strFunnel = Funnels.stringFunnel(Charset.defaultCharset());
    
    // prepare 5 initial nodes "node1", "node2" ... "node5"
    List<String> nodes = Lists.newArrayList();
    for(int i = 0 ; i < 5; i ++) {
        nodes.add("node"+i); 
    }
    
    // create HRW instance
    RendezvousHash<String, String> h = new RendezvousHash(Hashing.murmur3_128(), strFunnel, strFunnel, nodes);
    
    String node = h.get("key");  // returns "node1"
    // remove "node1" from pool
    h.remove(node);
    h.get("key"); // returns "node2"
    
    // add "node1" back into pool
    h.add(node);  
    h.get("key"); // returns "node1"
```
