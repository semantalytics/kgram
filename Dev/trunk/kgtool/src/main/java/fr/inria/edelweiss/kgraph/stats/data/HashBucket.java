package fr.inria.edelweiss.kgraph.stats.data;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.stats.Options;
import java.util.HashMap;
import java.util.Map;

/**
 * Save map between nodes ( convert to hash code) and their count
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 4 juin 2014
 */
public class HashBucket extends BaseMap {

    private final Map<Integer, Integer> bucket;
    private final int bucketSize;

    public HashBucket(int triplesNo, double[] options) {
        super();
        bucket = new HashMap<Integer, Integer>();
        if (options == null || options.length != 2) {
            options = Options.DEF_PARA_HASH;
        }

        this.bucketSize = this.size(triplesNo, options[0], options[1]);
    }

    @Override
    public void add(Node n) {
        int v = hash(n.getLabel());
        if (this.bucket.containsKey(v)) {
            this.bucket.put(v, bucket.get(v) + 1);
        } else {
            this.bucket.put(v, 1);
        }
    }

    @Override
    public int get(String s) {
        Integer c = bucket.get(hash(s));
        return c == null ? 0 : c;
    }

    @Override
    public int get(Node n) {
        return this.get(n.getLabel());
    }

    //Generate a bucket size
    //the number of buckets is limited at highest BUCKET_NUMBER_MAX
    //the (average) size of each bucket is limited at least BUCKET_SIZE_MIN
    //namely, if the tripels are more than 10*10000, then the number of bucket 
    //is 10 0000, otherwise number = triples number/10
    private int size(int triples, double min, double max) {
        if (triples > max * min) {
            return (int) max;
        } else {
            return (int) (triples / min) + 1;
        }
    }

    private int hash(Object value) {
        long hash = Long.valueOf(Integer.MAX_VALUE) + value.hashCode();
        return (int) (hash % this.bucketSize);
    }
}
