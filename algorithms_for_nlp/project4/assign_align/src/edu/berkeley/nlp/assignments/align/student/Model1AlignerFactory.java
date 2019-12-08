package edu.berkeley.nlp.assignments.align.student;


import edu.berkeley.nlp.mt.SentencePair;
import edu.berkeley.nlp.mt.WordAligner;
import edu.berkeley.nlp.mt.WordAlignerFactory;

public class Model1AlignerFactory implements WordAlignerFactory
{
	public WordAligner newAligner(Iterable<SentencePair> trainingData) {
        return new intersectedModel1Aligner(trainingData);
	}

}
