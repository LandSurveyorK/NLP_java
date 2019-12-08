package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Indexer;

import java.util.ArrayList;
import java.util.List;
import edu.berkeley.nlp.util.CollectionUtils;


public class HeuristicAligner implements WordAligner{
	
	double[][] translation;
	int[] countEnglish = new int[5];
	int[] countFrench = new int[5];
	int[][] countEnglishFrench = new int[10][10];
	Indexer<String> EnglishIndexer = new Indexer<String>();
	Indexer<String> FrenchIndexer = new Indexer<String>();
	
	
	public HeuristicAligner(Iterable<SentencePair> trainingData) {
		
		System.out.println("\nBulding Heuristic Aligner...");
		for(SentencePair sentencePair: trainingData) {
			List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
			List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
			
			boolean firstTime = true;
			for(String en: englishWords) {
				int enInd = EnglishIndexer.addAndGetIndex(en);
				// resize 
				if (enInd > countEnglish.length - 1) {
					countEnglish = CollectionUtils.copyOf(countEnglish,2 * countEnglish.length);
				    int[][] newCountEnglishFrench = new int[countEnglish.length][countFrench.length];
				    for(int i = 0; i < EnglishIndexer.size() - 1; i++) {
				    	for(int j = 0; j < FrenchIndexer.size(); j++) {
				    		newCountEnglishFrench[i][j] = countEnglishFrench[i][j];
				    	}
				    }
				    countEnglishFrench = newCountEnglishFrench;
				}
				
				countEnglish[enInd] += 1;
				
				for(String fr: frenchWords) {
					int frInd = FrenchIndexer.addAndGetIndex(fr);
					// resize
					if(firstTime) {
					    if(frInd > countFrench.length - 1) {
						    countFrench = CollectionUtils.copyOf(countFrench, 2 * countFrench.length);
						    int[][] newCountEnglishFrench = new int[countEnglish.length][countFrench.length];
					        for(int i = 0; i < EnglishIndexer.size(); i ++) {
					    	    for(int j = 0; j < FrenchIndexer.size() - 1; j ++) {
					    		    newCountEnglishFrench[i][j] = countEnglishFrench[i][j];
					    	    }
					        }
					        countEnglishFrench = newCountEnglishFrench;
					    }
					    countFrench[frInd] += 1;
					}
					countEnglishFrench[enInd][frInd] += 1;
				}
				firstTime = false;
			}
		}
		// calculate translation probability
		translation = new double[EnglishIndexer.size()][FrenchIndexer.size()];
	    for(int i = 0; i < EnglishIndexer.size(); i++) {
	    	for(int j = 0; j < FrenchIndexer.size(); j++) {
	    		translation[i][j] = (double)countEnglishFrench[i][j] / ( countEnglish[i] * countFrench[j]);
	    	}
	    }
	    System.out.println("done building heuristic model...\n");
	}
	


	public Alignment alignSentencePair(SentencePair sentencePair) {
		
		Alignment alignment = new Alignment();
		List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
		List<String> frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
		
		for(int frenchPosition = 0; frenchPosition < frenchWords.size(); frenchPosition++) {
			int frInd = FrenchIndexer.addAndGetIndex(frenchWords.get(frenchPosition));
			double likely = 0;
			int bestEnglishPosition = -1;
            for (int englishPosition = 0; englishPosition < englishWords.size(); englishPosition++) {
				int enInd = EnglishIndexer.addAndGetIndex(englishWords.get(englishPosition));	
				double newLikely = translation[enInd][frInd];
				if (newLikely > likely) {
						likely = newLikely;
						bestEnglishPosition = englishPosition;
				}
            }
		    alignment.addAlignment(bestEnglishPosition, frenchPosition, true);
		}
	
		return alignment;
	}

}