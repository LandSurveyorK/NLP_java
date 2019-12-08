
package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Indexer;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math; 


public class HmmAligner implements WordAligner{
	
	Indexer<String> EnglishIndexer = new Indexer<String>();
	Indexer<String> FrenchIndexer = new Indexer<String>();
	int n, m;
	double[][] translation; // in log space
	double[] transition; // in log space
	double[] pi; // in log space
	int numIter = 10; 
	double epsilon = 0.2;
	
	public HmmAligner(Iterable<SentencePair> trainingData) {
		
        System.out.println("\nStarting Training HMM...\n");
		int maxEngLength = 0; 
		int maxFreLength = 0;
		for(SentencePair sentencePair: trainingData) {
			List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
			englishWords.add(0, "NULL");
			List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
			maxEngLength = englishWords.size() > maxEngLength ? englishWords.size() : maxEngLength;
			maxFreLength = frenchWords.size() > maxFreLength ? frenchWords.size() : maxFreLength;
			for(String en: englishWords) {
				EnglishIndexer.addAndGetIndex(en);
			}
			for(String fr: frenchWords) {
				FrenchIndexer.addAndGetIndex(fr);
			}
		}
		// initialization 
		System.out.println("initialize...\n");
		n = EnglishIndexer.size();
		m = FrenchIndexer.size();
		pi = new double[maxEngLength];
        transition = new double[maxEngLength-1];
		translation = new double[n][m];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m ; j++) {
				translation[i][j] = Math.log(1.0 / m);
			}
		}
		
		for(int i = 0; i < pi.length; i++) {
			pi[i] = Math.log(1.0 / pi.length);
 		}
		
		for(int k = 0; k < transition.length; k++) {
			transition[k] = Math.log(1.0 / transition.length);
 		}
		// EM 
		System.out.println("start EM ...");
		
		for (int iter = 0 ; iter < numIter; iter++) {
			
			double[] new_pi = new double[maxEngLength];
	        double[] new_transition = new double[maxEngLength-1];
			double[][] new_translation = new double[n][m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m ; j++) {
					new_translation[i][j] = Double.NEGATIVE_INFINITY;
				}
			}
			//new_pi[0] = Math.log(epsilon);
			for(int i = 0; i < new_pi.length; i++) {
				new_pi[i] = Double.NEGATIVE_INFINITY;
	 		}
			for(int k = 0; k < new_transition.length; k++) {
				new_transition[k] = Double.NEGATIVE_INFINITY;
	 		}
			
			for(SentencePair sentencePair: trainingData) {
				
			    List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());			
			    englishWords.add(0, "NULL");
			    List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
			    int num_e = englishWords.size();
			    int num_f = frenchWords.size();
			    // get obs_prior
			    double[] obs_prior = new double[num_e];
		
			    double obs_prior_dom = Double.NEGATIVE_INFINITY;
			    for(int i = 0; i < num_e; i++) {
				    obs_prior_dom  = SloppyMath.logAdd(obs_prior_dom,
				    		pi[i]);
 			    }
			    for(int i = 0; i < num_e; i++) {
				    obs_prior[i] = pi[i] - obs_prior_dom;
 			    }
			    // get obs_transition.
			    double[] obs_transition  = new double[num_e - 1];
			    double obs_transition_dom = Double.NEGATIVE_INFINITY;
			    for(int k = 0; k < obs_transition.length; k++) {
			    	obs_transition_dom = SloppyMath.logAdd(obs_transition_dom, 
			    			transition[k]);
			    }
			    for(int k = 0; k < obs_transition.length; k++) {
			    	obs_transition[k] = transition[k] -  obs_transition_dom;
			    }
			    
			    double[][] alpha = getAlpha(englishWords, frenchWords, obs_prior,obs_transition);
			    double[][] beta = getBeta(englishWords, frenchWords, obs_transition);
			    double[][] gamma = getGamma(alpha, beta, englishWords, frenchWords);
			    double[][][] xi = getXi(alpha, beta, obs_transition, englishWords, frenchWords);
			    
		        // M step
		        // Update translation 
			    for(int frPos = 0; frPos < num_f; frPos ++) {
				    for (int enPos = 0; enPos < num_e; enPos ++) {
					    int frInd  = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
					    int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
					    new_translation[enInd][frInd] = SloppyMath.logAdd(new_translation[enInd][frInd],
					    		gamma[enPos][frPos]);
				    }  
			    }
			    // Update transition_expectedCount
			    for(int frPos = 0; frPos < num_f - 1; frPos ++) {
				    for (int enPos = 1; enPos < num_e; enPos ++) {
					    for(int next_enPos = 1; next_enPos < num_e; next_enPos ++) {
					        int  k = Math.abs(enPos - next_enPos);
					        new_transition[k] = SloppyMath.logAdd(new_transition[k], 
					        		xi[enPos][next_enPos][frPos]);
					    }
				    }
			    }
			    // update pi
			    for(int enPos = 0; enPos < num_e; enPos ++) {
				    new_pi[enPos] = SloppyMath.logAdd(new_pi[enPos],
				    		gamma[enPos][0]);
			    }
			}
			System.out.println("done training iter = " + iter);
			translation = normalizeTranslation(new_translation);
			transition = normalizeTransition(new_transition);
			pi = normalizePi(new_pi);
		}
		System.out.println("done EM ...");
		System.out.println("pi: " + java.util.Arrays.toString(pi));
		System.out.println(" ");
		System.out.println("transition: " + java.util.Arrays.toString(transition));
	}
	

	
	
	private double[][] normalizeTranslation(double [][] tran_slation) {
		// normalize translation 
		double[] translation_dom = new double[n];
		for(int i = 0; i < n; i++) {
			translation_dom[i] = Double.NEGATIVE_INFINITY;
		}
		for (int i = 0; i < n; i++) {
			for(int j = 0; j < m; j ++) {
				translation_dom[i] = SloppyMath.logAdd(translation_dom[i], 
						tran_slation[i][j]);
			}
		}
		for (int i = 0; i < n; i++) {
			for(int j = 0; j < m; j ++) {
				tran_slation[i][j] -= translation_dom[i];
			}
		}
		return tran_slation;
	}
	
	private double[] normalizeTransition(double[] tran_sition) {
		// normalize transition
		double transition_dom = Double.NEGATIVE_INFINITY;
		for(int k = 0; k < tran_sition.length; k++) {
		    transition_dom = SloppyMath.logAdd(transition_dom, 
		    		tran_sition[k]);	
		}
		for(int k = 0; k < tran_sition.length; k++) {
			tran_sition[k] -= transition_dom;
		}	
		return tran_sition;
	}
	
	private double[] normalizePi(double[] p_i) {
		// normalize obs_prior
		double pi_dom = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < p_i.length; i++) {
		    pi_dom = SloppyMath.logAdd(pi_dom,
		    		p_i[i]);
		}
		for(int i = 0; i < p_i.length ; i++) {
			p_i[i] -= pi_dom;
		}
		return p_i;
	}
	
 	private double[][] getAlpha(List<String> englishWords, List<String> frenchWords, 
 			double[] obs_prior,double[] obs_transition){
		
		int num_e = englishWords.size();
	    int num_f = frenchWords.size();
	    double[][] alpha = new double[num_e][num_f];
	    for(int i = 0; i < num_e; i++) {
	    	for(int j = 0; j < num_f; j++) {
	    		alpha[i][j] = Double.NEGATIVE_INFINITY;
	    	}
	    }
	    // Forward 
	      // j = 0 
	    int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(0));
	    for(int enPos = 0; enPos < num_e; enPos++) {
		    int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
		    alpha[enPos][0] =  obs_prior[enPos] + 
		    		translation[enInd][frInd];
	    }
        //  j = 1, 2 ... j-1 
	    for(int frPos = 1; frPos < num_f; frPos++) {
		    frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
		    int pre_frPos = frPos - 1;
		    // a(j) = null
		        // a(j-1) = null
		    alpha[0][frPos] = SloppyMath.logAdd(alpha[0][frPos], 
		    		alpha[0][pre_frPos] + 
		    		Math.log(epsilon) + 
		    		translation[0][frInd]);
		        // sum a(j-1) = i'
		    for(int pre_enPos = 1; pre_enPos < num_e; pre_enPos ++) {
			    alpha[0][frPos] = SloppyMath.logAdd(alpha[0][frPos],
			    		alpha[pre_enPos][pre_frPos] + 
			    		Math.log(epsilon) +  
			    		translation[0][frInd]);
		    }
		    // a(j) = i
		    for(int enPos = 1; enPos < num_e; enPos++) {
			    int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
			    // a(j-1) = null
			    alpha[enPos][frPos] = SloppyMath.logAdd(alpha[enPos][frPos],
			    		alpha[0][pre_frPos] +
			    		Math.log((1  - epsilon) / (num_e - 1)) + 
			    		translation[enPos][frInd]);
			    //  sum a(j+1) = i'
		        for(int pre_enPos = 1; pre_enPos < num_e; pre_enPos ++) {
			        alpha[enPos][frPos] = SloppyMath.logAdd(alpha[enPos][frPos],
			        		alpha[pre_enPos][pre_frPos] +
			        		Math.log( 1 - epsilon) + 
			    	     	obs_transition[Math.abs(enPos-pre_enPos)] +
			    	     	translation[enInd][frInd]);
		        }
		    }
	    }
	 return alpha;
	 
	}
	
	private double[][] getBeta(List<String> englishWords, List<String> frenchWords, 
			double[] obs_transition){
		
		int num_e = englishWords.size();
	    int num_f = frenchWords.size();
		double[][] beta = new double[num_e][num_f];
		for(int i = 0; i < num_e; i++) {
	    	for(int j = 0; j < num_f; j++) {
	    		beta[i][j] = Double.NEGATIVE_INFINITY;
	    	}
	    }
		// Backward
	    for(int enPos = 0; enPos < num_e; enPos ++) {
		    beta[enPos][num_f - 1] = Math.log(1.0);
	    }
	    for(int frPos = num_f - 2; frPos > -1; frPos-- ) {
		    int next_frPos = frPos + 1;
		    int next_frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(next_frPos));
		
		    // a(j) = null 
		        // a(j+1) = null
		    beta[0][frPos] = SloppyMath.logAdd(beta[0][frPos],
		    		Math.log(epsilon) +
		    		translation[0][next_frInd] +
		    		beta[0][next_frPos]);
		        // sum a(j+1) = i'
		    for(int next_enPos = 1; next_enPos < num_e; next_enPos ++) {
			    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(next_enPos));
			    beta[0][frPos] = SloppyMath.logAdd(beta[0][frPos],
			    		Math.log((1 - epsilon) / (num_e - 1)) +
				    	translation[next_enInd][next_frInd] +
					    beta[next_enPos][next_frPos]);
		    }
            // a(j) = i
		    for(int enPos = 1; enPos < num_e; enPos++) {
			    //  a(j+1) = null
			    beta[enPos][frPos] = SloppyMath.logAdd(beta[enPos][frPos], 
			    		Math.log(epsilon) +
			    		translation[0][next_frInd] +
			    		beta[0][next_frPos]);
			    //  sum a(j+1) = i'
			    for(int next_enPos = 1; next_enPos < num_e; next_enPos ++) {
				    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(next_enPos));
				    beta[enPos][frPos] = SloppyMath.logAdd(beta[enPos][frPos],
				    		Math.log(( 1- epsilon)) + 
				    		obs_transition[Math.abs(enPos-next_enPos)] + 
					    	translation[next_enInd][next_frInd] +
						    beta[next_enPos][next_frPos]);
			    }
		    }
	    }
	    return beta;
	}
	
	private double[][] getGamma(double[][] alpha, double[][] beta,List<String> englishWords, List<String> frenchWords){
		
		int num_e = englishWords.size();
	    int num_f = frenchWords.size();
	    double[][] gamma = new double[num_e][num_f];
	    for(int i = 0; i < num_e; i++) {
	    	for(int j = 0; j < num_f; j++) {
	    		gamma[i][j] = Double.NEGATIVE_INFINITY;
	    	}
	    }
		double[] gamma_dom = new double[num_f];
		for(int j = 0; j < num_f; j++) {
			gamma_dom[j] = Double.NEGATIVE_INFINITY;
		}
	    for(int frPos = 0; frPos < num_f; frPos ++) {
		    for(int enPos = 0; enPos < num_e; enPos ++) {
		    	gamma[enPos][frPos] = alpha[enPos][frPos] + 
			    		beta[enPos][frPos];
			    gamma_dom[frPos] =  SloppyMath.logAdd(gamma_dom[frPos], gamma[enPos][frPos]);
		    }
	    }
	    for(int frPos = 0; frPos < num_f; frPos ++) {
		    for(int enPos = 0; enPos < num_e; enPos ++) {
			    gamma[enPos][frPos] -= gamma_dom[frPos];
		    }
	    }
	    return gamma;
	}
	
	private double[][][] getXi(double[][] alpha, double[][] beta, double[] obs_transition, List<String> englishWords, 
			List<String> frenchWords){
		
		int num_e = englishWords.size();
	    int num_f = frenchWords.size();
		double[][][] xi = new double[num_e][num_e][num_f-1];
		for(int i = 0; i < num_e; i++) {
			for(int ii = 0; ii < num_e; ii++) {
				for(int j = 0; j < num_f - 1; j++) {
					xi[i][ii][j] = Double.NEGATIVE_INFINITY;
				}
			}
		}
		// xi
	    double[][] xi_dom = new double[num_e][num_e];
	    for(int i = 0; i < num_e; i++) {
			for(int ii = 0; ii < num_e; ii++) {
				xi_dom[i][ii] = Double.NEGATIVE_INFINITY;
			}
	    }
	    // a(j)=i, a(j+1) = i' , only update transition from i -> i'
        for(int frPos = 0; frPos < num_f - 1; frPos++) {
    	    int next_frPos = frPos + 1;
    	    int next_frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(next_frPos));
    	    
    	    for(int enPos = 1; enPos < num_e; enPos ++) {
    		    for(int next_enPos = 1; next_enPos < num_e; next_enPos++) {
    			    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(next_enPos));
    			    xi[enPos][next_enPos][frPos] = alpha[enPos][frPos] +
    			    		Math.log( 1 - epsilon) + 
    				    	obs_transition[Math.abs(enPos-next_enPos)] +
    					    translation[next_enInd][next_frInd] +
    					    beta[next_enPos][next_frPos];
    			    xi_dom[enPos][next_enPos] =  SloppyMath.logAdd(xi_dom[enPos][next_enPos], 
    			    		xi[enPos][next_enPos][frPos]);		
    		    }
    	    }
  	    }
        // since null - >i, null -> null, i -> null are set fixed, 
        // we don't need to learn
        
        // a(j) = null a(j+1) = i
       /* for(int frPos = 0; frPos < num_f - 1; frPos++) {
    	    int next_frPos = frPos + 1;
    	    int next_frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(next_frPos));
    		for(int next_enPos = 1; next_enPos < num_e; next_enPos++) {
        	    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(next_enPos));
        	    xi[0][next_enPos][frPos] = alpha[0][frPos] +
   				    	Math.log((1-epsilon) / (num_e-1)) +
   					    translation[next_enInd][next_frInd] +
   					    beta[next_enPos][next_frPos];
    		    xi_dom[0][next_enPos] =  SloppyMath.logAdd(xi_dom[0][next_enPos], xi[0][next_enPos][frPos]);    
    	    }
  	    }

        // a(j) = i, a(j+1) = null
         for(int frPos = 0; frPos < num_f - 1; frPos++) {
    	    int next_frPos = frPos + 1;
    	    int next_frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(next_frPos));
    	    
    	    for(int enPos = 1; enPos < num_e; enPos ++) { 
   			    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(0));
   			    xi[enPos][0][frPos] = alpha[enPos][frPos] +
   			    	    Math.log(epsilon) +
  					    translation[next_enInd][next_frInd] +
    					beta[0][next_frPos];
    			xi_dom[enPos][0] =  SloppyMath.logAdd(xi_dom[enPos][0], xi[enPos][0][frPos]);  
    	    }
        }
        // a(j) = null a(j+1) = null 
        for(int frPos = 0; frPos < num_f - 1; frPos++) {
    	    int next_frPos = frPos + 1;
    	    int next_frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(next_frPos));
    	    int next_enInd = EnglishIndexer.addAndGetIndex(englishWords.get(0));
   		    xi[0][0][frPos] = alpha[0][frPos] +
   			    	    Math.log(epsilon) +
  					    translation[next_enInd][next_frInd] +
    					beta[0][next_frPos];
    		xi_dom[0][0] =  SloppyMath.logAdd(xi_dom[0][0], xi[0][0][frPos]);  
        }
        */
       // normalize
        for(int frPos = 0; frPos < num_f - 1; frPos++) {
    	    //for(int enPos = 0; enPos < num_e; enPos ++) {
    		    //for(int next_enPos = 0; next_enPos < num_e; next_enPos++){
        	for(int enPos = 1; enPos < num_e; enPos ++) {
        		for(int next_enPos = 1; next_enPos < num_e; next_enPos++) {
    			    xi[enPos][next_enPos][frPos] -= xi_dom[enPos][next_enPos];
    		    }
    	    }
        }
		return xi;
	}
	

	public Alignment alignSentencePair(SentencePair sentencePair) {
        Alignment alignment = new Alignment();
		
        List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
        englishWords.add(0, "NULL");
		List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
		//int num_e  = englishWords.size();
		int num_f = frenchWords.size();
		
		List<Integer> path = getPath(englishWords, frenchWords);
	    for(int frPos = 0; frPos < num_f; frPos ++) {
	    	int englishPosition = path.get(frPos) - 1;
	    	if (englishPosition >= 0) {
	    		alignment.addAlignment(englishPosition , frPos, true);
	    	}
	    }
		return alignment;
	}
	
	public List<Integer> getPath(List<String> englishWords, List<String> frenchWords){
		
		int num_e  = englishWords.size();
		int num_f = frenchWords.size();
		int[][] Parent = new int[num_e][num_f];
		double[][] P  = new double[num_e][num_f];
	
	    double[] obs_prior = new double[num_e];
		double obs_prior_dom = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < num_e; i++) {
			obs_prior_dom = SloppyMath.logAdd(obs_prior_dom, pi[i]);
		}
		for(int i = 0; i < num_e; i++) {
			obs_prior[i] = pi[i] - obs_prior_dom;
		}
		
		
		double[] obs_transition  = new double[num_e - 1];
	    double obs_transition_dom = Double.NEGATIVE_INFINITY;
	    for(int k = 0; k < obs_transition.length; k++) {
	    	obs_transition_dom = SloppyMath.logAdd(obs_transition_dom, transition[k]);
	    }
	    for(int k = 0; k < obs_transition.length; k++) {
	    	obs_transition[k] = transition[k] - obs_transition_dom;
	    }
	    
		// frPos = 0
		int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(0));
		P[0][0] = obs_prior[0] + translation[0][frInd];
		for(int enPos = 1; enPos < num_e; enPos++) {
			int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
			
			P[enPos][0] = Math.log(1-epsilon) + obs_prior[enPos] + 
					translation[enInd][frInd]; 
		}
		// frPos = 1, 2, ... J-1
		for(int frPos = 1; frPos < num_f ; frPos ++) {
			int pre_frPos = frPos - 1;
			frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
			
			// a(j) = null
			Parent[0][frPos] = 0;  // parent = null
		    P[0][frPos] = P[0][pre_frPos] +
		    		Math.log(epsilon) + 
		    		translation[0][frInd];

			for(int pre_enPos = 1; pre_enPos < num_e; pre_enPos ++) {
				    int parent_candidate = pre_enPos;
					double p_candidate = P[pre_enPos][pre_frPos] +
							Math.log(epsilon) +
							+ translation[0][frInd];
					if (p_candidate >  P[0][frPos]) {
						Parent[0][frPos] = parent_candidate;
						P[0][frPos] = p_candidate;
					}		
			} // update parent 
            // a(j) = i
			for(int enPos = 1; enPos < num_e; enPos ++) {
				int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
				
				Parent[enPos][frPos] = 0; // parent = null
				P[enPos][frPos] = P[0][pre_frPos] +
						Math.log( (1 - epsilon) / (num_e -1) ) +
						 translation[enInd][frInd]; 
				
				for(int pre_enPos = 1; pre_enPos < num_e; pre_enPos ++) {
				    int parent_candidate = pre_enPos;
					double p_candidate = P[pre_enPos][pre_frPos]  + 
							Math.log(1 - epsilon) + 
							obs_transition[Math.abs(enPos - pre_enPos)] + 
							translation[enInd][frInd];
					if (p_candidate > P[enPos][frPos]) {
						Parent[enPos][frPos] = parent_candidate;
						P[enPos][frPos] = p_candidate;
					}	
				} // update parent
		    }
		}
	    // get best path 
		List<Integer> path = new ArrayList<Integer>();
		/*i nt curr = -1;
		double tail_p = Double.NEGATIVE_INFINITY;
		for (int enPos = 0; enPos < num_e; enPos++) {
	        if(P[enPos][num_f - 1] > tail_p) {
		        tail_p = P[enPos][num_f - 1];
		        curr = enPos;
			}
	    }
	    */ // here is the trick: always align . -> . and ? -> ?
		int curr = num_e - 1;
		path.add(0,curr);
		for(int frPos = num_f - 1; frPos > 0; frPos --) {
			int parent = Parent[curr][frPos];
			path.add(0,parent);
			curr = parent;
		}
		return path;
	}
}





