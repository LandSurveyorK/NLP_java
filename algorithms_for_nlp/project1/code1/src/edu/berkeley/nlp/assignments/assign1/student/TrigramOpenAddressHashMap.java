


package edu.berkeley.nlp.assignments.assign1.student;
import java.util.Arrays;
/**
* @ author Wei Peng
*/

public class TrigramOpenAddressHashMap{
	
	private static final int INIT_CAPACITY = 4;
	private static final double MAX_LOAD_CAPACITY = 0.7;
	private static final long DEFAULT_KEY = -1;
	private int Size = 0; // the numnber of key-value pairs
	private long[] keys;
	private int[] vals;
	
	/**
	 * Initializes an empty table.
	 */
	public TrigramOpenAddressHashMap() {
		this(INIT_CAPACITY);
	}
	/**
	 * Initializes an empty table with the 
	 * specified initial capacity
	 */
	public TrigramOpenAddressHashMap(int capacity) {
		Size = 0;
		keys = new long[capacity];
		Arrays.fill(keys,DEFAULT_KEY);
		vals = new int[capacity];
	}
    /**
     * Returns the number of key-value pairs in this symbol table
     * @return
     */
	public int size() {
		return Size;
	}
    /**
     * Resizes the hashmap to the given capacity by rehashing all of the keys
     */
    private void resize() {
    	long[] tempkeys = new long[keys.length * 3 / 2];
    	int[] tempvals = new int[vals.length * 3 / 2];
    	Arrays.fill(tempkeys, DEFAULT_KEY);
    	Size  = 0;
    	for (int i = 0; i < keys.length; i++) {
    		long key = keys[i];
    		if (key != DEFAULT_KEY) {
    			int val = vals[i];
    			putAssist(key, val, tempkeys,tempvals);
    		}
    	}
    	keys = tempkeys;
    	vals = tempvals;
    }
    /**
     * To assist resize() by adding key-value pairs to the new table
     * @param key 
     * @param val
     * @param keyArray
     * @param valsArray
     */
    public void putAssist(long key, int val, long[] keysArray, int[] valsArray) {
    	int final_pos = findPosition(key,keysArray);
    	long curr = keysArray[final_pos];
    	
    	valsArray[final_pos] = val;
    	if (curr == DEFAULT_KEY) {
    		Size++;
    		keysArray[final_pos]  = key;
    	}
    }
    /**
     * Add the specified key-value pair into the table
     * @param key
     */
    public void add(long key) {
    	if (Size > MAX_LOAD_CAPACITY * keys.length) resize();
    	int pos = findPosition(key, keys);
    	
    	vals[pos] += 1;
    	if (keys[pos] == DEFAULT_KEY) {
    		Size++;
    		keys[pos] = key;
    	}
    }
  /**
   * Find position by key in keysArray
   * @param key
   * @param keysArray
   * @return
   */
     public int findPosition(long key, long[] keysArray) {
        int init_pos = initPosition(key, keysArray);
        long curr = keysArray[init_pos];
        int final_pos = init_pos;
        int step = 1;
        while(curr != DEFAULT_KEY && curr != key) {
        	final_pos = (init_pos + step) % keysArray.length;
        	step += 1;
        	curr = keysArray[final_pos];
        }
    	return final_pos;
    }
     /**
     * hash function for keys - returns value between
     * 0 and the capacity of the table
     * @param key
     * @return
     */
    public int hash6432shift(long key) // source: https://gist.github.com/badboy/6267743
	{
		key = (~key) + (key << 18); 
		key = key ^ (key >>> 31);
		key = key * 21; 
		key = key ^ (key >>> 11);
		key = key + (key << 6);
		key = key ^ (key >>> 22);
		return (int) key;
	}
    /**
     * find initial position in keysArray
     * @param key
     * @param keysArray
     * @return
     */
    private int initPosition(long key, long[] keysArray) {
   	     //int  hash = Long.hashCode(key);
         int hash = hash6432shift(key);
   	     int pos = hash % keysArray.length;
   	     if (pos < 0) pos += keysArray.length;
   	     return pos;
    }
    /**
     *  get position by key
     * @param key
     * @return
     */
    public int get(long key) {
    	int pos = findPosition(key,keys);
    	return vals[pos];
    }
  /**
   * get values
   * @return
   */
    public int[] values() {
    	return vals;
    }

}
