


package edu.berkeley.nlp.assignments.assign1.student;
import java.util.Arrays;
/**
* @ author Wei Peng
*/

public class BigramOpenAddressHashMap{
	
	private static final int INIT_CAPACITY = 4;
	private static final double MAX_LOAD_CAPACITY = 0.7;
	private static final long DEFAULT_KEY = -1;
	private int Size = 0; // the number of key-value pairs in the table
	private long[] keys;
	private int[] ww;  // word word
	private int[] xww; // dot word word
	private int[] wwx; // word word dot
	
	/**
	 * Initializes an empty table.
	 */
	public BigramOpenAddressHashMap() {
		this(INIT_CAPACITY);
	}
	/**
	 * Initializes an empty table with the 
	 * specified initial capacity
	 */
	public BigramOpenAddressHashMap(int capacity) {
		Size = 0;
		keys = new long[capacity];
		Arrays.fill(keys,DEFAULT_KEY);  // default value = 1
		ww = new int[capacity]; // default value = 0
		xww = new int[capacity];
		wwx = new int[capacity];
	}
	/**
	 *  return the number of key-values pairs in the table
	 * @return
	 */
   public int size() {
	   return Size;  
   }

    /**
     * Resizes the hashmap to the given capacity by re-hashing all of the keys
     */
    private void resize() {
    	long[] temp_keys = new long[keys.length * 3 / 2];
    	int[] temp_ww = new int[ww.length * 3 / 2];
    	int[] temp_xww = new int[xww.length * 3 / 2];
    	int[] temp_wwx = new int[wwx.length * 3 / 2];
    	Arrays.fill(temp_keys, DEFAULT_KEY);
    	Size  = 0;
    	for (int i = 0; i < keys.length; i++) {
    		long key = keys[i];
    		if (key != DEFAULT_KEY) {
    			int ww_val = ww[i];
    			int xww_val = xww[i];
    			int wwx_val = wwx[i];
    			putAssist(key, ww_val,xww_val,wwx_val, temp_keys,temp_ww, temp_xww, temp_wwx);
    		}
    	}
    	keys = temp_keys;
    	ww = temp_ww;
    	xww = temp_xww;
    	wwx = temp_wwx;
    }
    /**
     * to assist resize()
     * @param key
     * @param ww_val
     * @param xww_val
     * @param wwx_val
     * @param keysArray
     * @param wwArray
     * @param xwwArray
     * @param wwxArray
     */
    public void putAssist(long key, int ww_val, int xww_val,
    		int wwx_val, long[] keysArray, int[]wwArray, 
    		int[] xwwArray, int[] wwxArray) {
    	int pos = findPosition(key, keysArray);
    	long curr = keysArray[pos];
 
        wwArray[pos] = ww_val;
        xwwArray[pos] = xww_val;
        wwxArray[pos] = wwx_val;
    	if (curr == DEFAULT_KEY) {
    		Size++;
    		keysArray[pos]  = key;
    	}
    	
    }
    
    /**
     * add the specified (key, ww) pair
     * @param key
     */
    public void add(long key) {
    	// double table size if 70% full
    	if (Size > MAX_LOAD_CAPACITY * keys.length) resize();
    	
    	int pos = findPosition(key,keys);
    	long curr = keys[pos];
    	if (curr == DEFAULT_KEY) {
    		Size++;
    		keys[pos] = key;
    		ww[pos] =  1;
    	}
    	else {
    		ww[pos] += 1;
    	}
    }
    /**
     * get positions of key1 and key2 to update xww and wwx
     * @param key1
     * @param key2
     */
    public void update(long key1, long key2) {
    	int xww_pos = findPosition(key1,keys);
    	int wwx_pos = findPosition(key2,keys);
    	xww[xww_pos] += 1;
    	wwx[wwx_pos] += 1;
    }
    /**
     * find the final position of a key in keysArray
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
    public int hash6432shift(long key) // key can be negative
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
     * find the initial position of key in keysArray
     * @param key
     * @param keysArray
     * @return
     */
    private int initPosition(long key, long[] keysArray) {
   	     //int  hash = Long.hashCode(key);
         int hash = hash6432shift(key);  // can be negative
   	     int pos = hash % keysArray.length;
   	     if (pos < 0) pos += keysArray.length;
   	     return pos;
    }
    /**
     * get the ww count by key
     * @param key
     * @return
     */
    public int get(long key) {
    	int pos = findPosition(key,keys);
    	return ww[pos];
    }
    /**
     * get position by key
     * @param key
     * @return
     */
    public int getPosFromKey(long key) {
    	return findPosition(key, keys);
    }
    /**
     * get the array of ww
     * @return
     */
    public int[] get_ww() {
    	return ww;
    }
    /**
     * get the array of xww
     * @return
     */
    public int[] get_xww() {
    	return xww;
    }
    /**
     * get the array of wwx
     * @return
     */
    public int[] get_wwx() {
    	return wwx;
    }
}



