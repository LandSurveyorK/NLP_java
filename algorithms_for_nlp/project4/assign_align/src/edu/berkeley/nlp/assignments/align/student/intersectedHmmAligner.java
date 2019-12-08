
package edu.berkeley.nlp.assignments.align.student;


import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Pair;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.align.student.HmmAligner;


public class intersectedHmmAligner implements WordAligner{

	HmmAligner E2F;
	HmmAligner F2E;
	
   public intersectedHmmAligner(Iterable<SentencePair> trainingData) {
	   
	  E2F = new HmmAligner(trainingData);
	  System.out.println("done HMM E2F");
	  
	  List<SentencePair> trainingDataF2E = new ArrayList<SentencePair>();
	  for (SentencePair sentencePair: trainingData) {
		  List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
		  List<String>frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
		  SentencePair sentencePairF2E = new SentencePair(sentencePair.getSentenceID(),
		    		sentencePair.getSourceFile(),
		    		frenchWords,
		    		englishWords);
		  trainingDataF2E.add(sentencePairF2E);
	  }
	  
	  F2E = new HmmAligner(trainingDataF2E);
	  System.out.println("done HMM F2E");
	  
   }
	
	public Alignment alignSentencePair(SentencePair sentencePair) {
   
		
        Alignment alignment1 = E2F.alignSentencePair(sentencePair);
        
        List<String> englishWords = new ArrayList<String>(sentencePair.getEnglishWords());
		List<String>frenchWords = new ArrayList<String>(sentencePair.getFrenchWords());
		SentencePair sentencePairF2E = new SentencePair(sentencePair.getSentenceID(),
	    		sentencePair.getSourceFile(),
	    		frenchWords,
		    	englishWords);
        Alignment alignment2 = F2E.alignSentencePair(sentencePairF2E).getReverseCopy();
        
        Alignment alignment = new Alignment();
		for(Pair<Integer, Integer> align: alignment1.getSureAlignments()) {
			int englishPosition = align.getFirst();
			int frenchPosition = align.getSecond();
			if(alignment2.containsSureAlignment(englishPosition, frenchPosition)) {
				alignment.addAlignment(englishPosition, frenchPosition, true);
			}
		}
		return alignment; 
	}
}
	