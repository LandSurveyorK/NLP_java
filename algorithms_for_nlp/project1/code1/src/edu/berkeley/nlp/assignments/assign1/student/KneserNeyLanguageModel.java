package edu.berkeley.nlp.assignments.assign1.student;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math; 
import edu.berkeley.nlp.langmodel.EnglishWordIndexer;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.util.CollectionUtils;


/**
 * Kneser-Ney Trigram Language Model
 * @author wei peng
 */
public class KneserNeyLanguageModel implements NgramLanguageModel{

	static final String STOP = NgramLanguageModel.STOP;
	static final String START = NgramLanguageModel.START;

	long[] uniword = new long[10];
    int[] word_dot = new int[10];
	int[] dot_word = new int[10];
	int[] dot_word_dot = new int[10];
	static BigramOpenAddressHashMap Bigram = new BigramOpenAddressHashMap(100);
	static TrigramOpenAddressHashMap Trigram = new TrigramOpenAddressHashMap(100);
	long total = 0;
	int bit_mask = 0xFFFFF;
	
	public KneserNeyLanguageModel(Iterable<List<String>> sentenceCollection) {
		System.out.println("Building TrigramLanguageModel ...");
		int sent = 0;
		for(List<String>sentence: sentenceCollection) {
			sent++;
			if (sent % 1000000 == 0) System.out.println("On sentence"  + sent);
			List<String>stoppedSentence = new ArrayList<String>(sentence);
			stoppedSentence.add(0,START);
			stoppedSentence.add(STOP);
			int size = stoppedSentence.size();
			String word;
			int index_0 = 0, index_1 = 0, index_2 = 0;
			// index_0: index of current word;index_1: index of previous 1 word; index_2: index of previous 2 word.
			for(int i = 0; i < size; i++) {
				total++;
				index_2 = index_1;
				index_1 = index_0;
				word = stoppedSentence.get(i);
				index_0 = EnglishWordIndexer.getIndexer().addAndGetIndex(word);
				if (index_0 >= uniword.length) {
					uniword = CollectionUtils.copyOf(uniword, uniword.length * 2);
					word_dot = CollectionUtils.copyOf(word_dot, word_dot.length * 2);
					dot_word = CollectionUtils.copyOf(dot_word, dot_word.length * 2);
					dot_word_dot = CollectionUtils.copyOf(dot_word_dot, dot_word_dot.length * 2);	
				}
                uniword[index_0]++ ;
				if(i > 0) {
					long encode10 = ((((long)index_1) & bit_mask) << 20) | (((long)index_0) & bit_mask);
					Bigram.add(encode10);
					if (Bigram.get(encode10) == 1) { // check whether ww is new or not
					    word_dot[index_1] += 1; //  update the fertility of wx
					    dot_word[index_0] += 1; // update the fertility of xw
					}
				    if(i > 1) {
					    long encode210 = ((((long)index_2)& bit_mask) << 40) 
					    		| ((((long)index_1) & bit_mask) << 20) 
					    		| (((long)index_0) & bit_mask);
					    Trigram.add(encode210);
					    if (Trigram.get(encode210) == 1) { // check whether www is new or not
					        dot_word_dot[index_1] += 1;
					        long encode21 = ((((long)index_2)& bit_mask) << 20) | (((long)index_1) & bit_mask);
					        Bigram.update(encode10,encode21); // update the fertilities of xww and wwx
					    }
				    }
				}			
			}
		}
		uniword = CollectionUtils.copyOf(uniword, EnglishWordIndexer.getIndexer().size());
		word_dot = CollectionUtils.copyOf(word_dot, EnglishWordIndexer.getIndexer().size());
		dot_word = CollectionUtils.copyOf(dot_word, EnglishWordIndexer.getIndexer().size());
		dot_word_dot = CollectionUtils.copyOf(dot_word_dot, EnglishWordIndexer.getIndexer().size());
		System.out.println(uniword.length);
        System.out.println(Bigram.size());
        System.out.println(Trigram.size());
		System.out.println("Done building EmpiricalTrigramLanguageModel.");
	}
	/**
	 * Maximum order of n-gram that will be scored by the model
	 * 
	 * @return
	 */
	public int getOrder() {
		return 3;
	}
	/**
	 * Returns the conditional probability of the n-gram specified by an array of 
	 * word indices, from (inclusive, 0-indexed) and to (exclusive, 0-indexed).
	 * For example, getNgramLogProbability([17,15,18],1,3) should return the (log of)
	 * P(w_2=18 | w_1 = 15) while getNgramLogProbability([17,15,18],0,3) should
	 * return the (log of) P(w_2=18 | w_0=17, w_1=15).
	 * Anything outside the bounds from and to is ignored.
	 * (This choice of interface allows for efficient reuse of arrays
	 * inside the decoder).
	 * 
	 * The integers represent words (Strings) via the mapping giving by
	 * EnglishWordIndexer.getIndexer().
	 * 
	 * Note that even a trigram language model must score bigrams (e.g. at the
	 * beginning of a sentence), and so you should not assume that to == from +
	 * getOrder().
	 * 
	 * @param ngram
	 * @param from
	 * @param to
	 * @return A log probability in the range (-infinity, 0]. Should never return
	 * Double.NEGATIVE_INFINITY or Double.NaN.
	 */
	

    public double getNgramLogProbability(int[] ngram, int from, int to) {
		
		double d = 0.8 ;
        
		if (to - from > 3) {
			  System.out.println("WARNING: to - from > 3 for EmpiricalTrigramLanguageModel");
			}
		// to - from = 1 Unigram 
		if (to - from == 1) {
			int index_0 = ngram[to - 1];
			if (index_0 < 0 || index_0 >= uniword.length) {
			    return  (double) Math.log(1e-40); //back off with a tiny probability
		    }
			return (double) Math.log(1.0 * uniword[index_0] / total); // Unigram probability
		}
		// to - from = 2 Bigram
		if (to - from == 2) {
					
					int index_0 = ngram[to - 1];
					if (index_0 < 0 || index_0 >= uniword.length) {
					    return  Math.log(1e-40);  // back of with a tiny probability
				    }
					double p2 = (double)dot_word[index_0] / (Bigram.size());
					int  index_1 = ngram[to - 2];
				    if (index_1 < 0 || index_1 >= uniword.length) {
					    	return Math.log(p2); // back off with Unigram probability
					 }
					double alphaWord = d * word_dot[index_1] / uniword[index_1] ;
					long encode10 = ((((long)index_1 & bit_mask) << 20)) 
							| (((long)index_0) & bit_mask);
					return (double) Math.log(Math.max(0, Bigram.get_ww()[Bigram.getPosFromKey(encode10)] - d) / uniword[index_1]  + 
							    alphaWord * p2); // Bigram probability
				}
		// to - from = 3 Trigram
		if (to - from == 3) {
					
					int index_0 = ngram[to - 1];
					if (index_0 < 0 || index_0 >= uniword.length) {
					    return  Math.log(1e-40); // back off with a tiny probability
				    }
					double p3 = (double)dot_word[index_0] / (Bigram.size());
					int  index_1 = ngram[to - 2];
				    if (index_1 < 0 || index_1 >= uniword.length || index_1 >= dot_word_dot.length) {
					    	return Math.log(p3);
					 }
				     if (dot_word_dot[index_1] <= 0) {
				    	 return Math.log(p3); // back off with Unigram probability
				    }
					double alphaWord = d * word_dot[index_1] / dot_word_dot[index_1] ;
					long encode10 = ((((long)index_1 & bit_mask) << 20)) 
							| (((long)index_0) & bit_mask);
					double p3_2 = (double) Math.max(0, Bigram.get_xww()[Bigram.getPosFromKey(encode10)] - d) / dot_word_dot[index_1]  + 
							    alphaWord * p3; 
					
					int index_2 = ngram[to - 3];
					if (index_2 < 0 || index_2 >= uniword.length) {
						return Math.log(p3_2); 
					}
					long encode21 = ((((long)index_2) & bit_mask) << 20) 
							| (((long)index_1) & bit_mask);
					int countWordWord = Bigram.get_ww()[Bigram.getPosFromKey(encode21)];
					if (countWordWord <= 0) { 
						return Math.log(p3_2);  // back off Bigram Language model
					}
					double alphaWordWord = d * Bigram.get_wwx()[Bigram.getPosFromKey(encode21)] / countWordWord;
					long encode210 = ((((long) index_2) & bit_mask) << 40) 
							| ((((long) index_1) & bit_mask) << 20) 
							| (((long) index_0 & bit_mask));
					return  (double) Math.log( Math.max(0,Trigram.get(encode210) - d)/ countWordWord 
							+ alphaWordWord * p3_2); // Trigram probability
				}
				return 0;
    }

	/**
	 * Returns the count of an n-gram. We will call this function when testing
	 * your code.
	 * 
	 * @param ngram
	 * @return
	 */
	public long getCount(int[] ngram) {
		if (ngram.length == 1) {
			int index_0 = ngram[0];
			if(index_0 < 0 || index_0 > uniword.length) {
				return 0;
			}
			return uniword[index_0];
	    }
		else if (ngram.length == 2) {
			long index_1 = ngram[0];
			long index_0 = ngram[1];
			long encode =  (index_1 & bit_mask) << 20 | (index_0 & bit_mask);
			return Bigram.get(encode);
		}
		else if(ngram.length == 3) {
			long index_2 = ngram[0];
			long index_1 = ngram[1];
			long index_0 = ngram[2];
			long encode = ((index_2  & bit_mask) << 40)
					| ((index_1 & bit_mask) << 20)
					| (index_0 & bit_mask);
			return Trigram.get(encode);
		}	
		return 0;
	}
}