
package edu.berkeley.nlp.assignments.align.student;

import java.util.ArrayList;
import java.util.List;
import edu.berkeley.nlp.math.SloppyMath;
import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Indexer;


public class Model1Aligner implements WordAligner{
	
	int n,m;
	double translation[][];
	Indexer<String> EnglishIndexer = new Indexer<String>();
	Indexer<String> FrenchIndexer = new Indexer<String>();
	int numIter = 10;
	double epsilon = 0.2;
	
	public Model1Aligner(Iterable<SentencePair> trainingData) {
		System.out.println("\nStarting Training IBM model 1...");
		
		for(SentencePair sentencePair: trainingData) {

			List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
			englishWords.add(0, "NULL");
			List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
			for(String en: englishWords) {
				EnglishIndexer.addAndGetIndex(en);
			}
			for(String fr: frenchWords) {
				FrenchIndexer.addAndGetIndex(fr);
			}
		}
		
		// initialization 
		System.out.println("EnglishIndexer Size: " + EnglishIndexer.size() + 
				"  FrenchIndexer Size: " + FrenchIndexer.size());
		System.out.println("initialize...\n");
		n = EnglishIndexer.size();
		m = FrenchIndexer.size();
		translation = new double[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				translation[i][j] = Math.log(1.0/m);
		    }
		}	
		// EM 
		System.out.println("start EM ...");
		
		for (int iter = 0; iter < numIter; iter++) {
			
			double[][] new_translation = new double[n][m];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					new_translation[i][j] = Double.NEGATIVE_INFINITY;
			    }
			}
			
			for(SentencePair sentencePair: trainingData) {
				
				List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
				englishWords.add(0, "NULL");
				List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
				int num_e = englishWords.size();
				int num_f = frenchWords.size();
				
				double[] obs_prior = new double[num_e];
				obs_prior[0] = Math.log(epsilon);
				for(int i = 1; i < obs_prior.length; i++) {
					obs_prior[i] = Math.log((1 - epsilon) / (obs_prior.length - 1) );
				}
				
                // add to translation
				double[][] gamma = getGamma(obs_prior, englishWords, frenchWords);
				
				for(int frPos = 0; frPos < num_f; frPos++) {
					for(int enPos = 0; enPos < num_e; enPos++) {
						int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
						int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
						new_translation[enInd][frInd] = SloppyMath.logAdd(new_translation[enInd][frInd], 
								gamma[enPos][frPos]);
					}
				}
			}
			translation = normalizeTranslation(new_translation);
			System.out.println("done training iter = " + iter);
		}

		System.out.println("done EM ..."); 	
	}
	
	private double[][] getGamma(double[] obs_prior, 
			List<String> englishWords, List<String>frenchWords){
		
		int num_e = englishWords.size();
		int num_f = frenchWords.size();
		double[][] gamma = new double[num_e][num_f]; 
		double[] gamma_dom = new double[num_f];
		for(int j = 0; j < num_f; j++) {
			gamma_dom[j] = Double.NEGATIVE_INFINITY;
		}
		// get gamma
		for(int frPos = 0; frPos < num_f; frPos++) {
			for(int enPos = 0; enPos < num_e; enPos++) {
				int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
				int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
				gamma[enPos][frPos]  = translation[enInd][frInd]  +  obs_prior[enPos];
				gamma_dom[frPos] = SloppyMath.logAdd(gamma_dom[frPos], gamma[enPos][frPos]);
			}
		}
		// normalization
		for(int frPos = 0; frPos < num_f; frPos++) {
			for(int enPos = 0; enPos < num_e; enPos++) {
				gamma[enPos][frPos] -= gamma_dom[frPos];	
			}
		}
		
		return gamma;
	}
	
	
	private double[][] normalizeTranslation(double[][] tran_slation){
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
			for(int j = 0; j < m; j++) {
				tran_slation[i][j] -= translation_dom[i];
			}
		}
	    return tran_slation;
	}

	public Alignment alignSentencePair(SentencePair sentencePair) {
	
		Alignment alignment = new Alignment();
		List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
		englishWords.add(0,"NULL");
		List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
		//System.out.println(englishWords);
		for(int frPos = 0; frPos < frenchWords.size(); frPos ++) {
			int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frPos));
			int bestEnglishPosition = - 1;
			double likely = Double.NEGATIVE_INFINITY;
            for(int enPos = 0; enPos < englishWords.size(); enPos++) {
				int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(enPos));
			    double newLikely = translation[enInd][frInd];
				if(newLikely > likely) {
					bestEnglishPosition = enPos;
					likely = newLikely;
				}
		    }
            if(bestEnglishPosition > 0) {
			    alignment.addAlignment(bestEnglishPosition - 1, frPos, true);
            }
		}
		return alignment;
	}
	
	
}