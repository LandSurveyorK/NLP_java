package edu.berkeley.nlp.assignments.align.student;

import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.mt.WordAlignerFactory;
import edu.berkeley.nlp.assignments.align.student.HeuristicAligner;


public class HeuristicAlignerFactory implements WordAlignerFactory{

	public WordAligner newAligner(Iterable<SentencePair> trainingData) {

		 return new HeuristicAligner(trainingData);
	}

}
