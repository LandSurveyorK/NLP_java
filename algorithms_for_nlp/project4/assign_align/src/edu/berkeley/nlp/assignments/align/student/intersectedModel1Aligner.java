
package edu.berkeley.nlp.assignments.align.student;


import edu.berkeley.nlp.mt.Alignment;
import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.util.Pair;
import java.util.ArrayList;
import java.util.List;

import edu.berkeley.nlp.assignments.align.student.Model1Aligner;


public class intersectedModel1Aligner implements WordAligner{

	Model1Aligner E2F;
	Model1Aligner F2E;
	
   public intersectedModel1Aligner(Iterable<SentencePair> trainingData) {
	   
	  E2F = new Model1Aligner(trainingData);
	  System.out.println("done Model1 E2F");
	  
	  List<SentencePair> trainingDataF2E = new ArrayList<SentencePair>();
	  for (SentencePair sentencePair: trainingData) {
		  List<String> englishWords = new ArrayList<String>(sentencePair.englishWords);
		  List<String>frenchWords = new ArrayList<String>(sentencePair.frenchWords);
		  SentencePair sentencePairF2E = new SentencePair(sentencePair.getSentenceID(),
		    		sentencePair.getSourceFile(),
		    		frenchWords,
		    		englishWords);
		  trainingDataF2E.add(sentencePairF2E);
	  }
	  
	  F2E = new Model1Aligner(trainingDataF2E);
	  System.out.println("done Model1 F2E");
	  
   }
	
	public Alignment alignSentencePair(SentencePair sentencePair) {
   
        Alignment alignment1 = E2F.alignSentencePair(sentencePair);
        
        List<String> englishWords = new ArrayList<String>(sentencePair.englishWords);
		List<String>frenchWords = new ArrayList<String>(sentencePair.frenchWords);
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