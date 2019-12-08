package edu.berkeley.nlp.assignments.assign1.student;

import java.util.List;


import edu.berkeley.nlp.langmodel.LanguageModelFactory;
import edu.berkeley.nlp.langmodel.NgramLanguageModel;
import edu.berkeley.nlp.assignments.assign1.student.KneserNeyLanguageModel;

public class LmFactory implements LanguageModelFactory
{

  /**
   * Returns a new NgramLanguageModel; this should be an instance of a class that you implement.
   * Please see edu.berkeley.nlp.langmodel.NgramLanguageModel for the interface specification.
   * 
   * @param trainingData
   */
	public NgramLanguageModel newLanguageModel(Iterable<List<String>> trainingData) {

		 return new KneserNeyLanguageModel(trainingData); // TODO Construct an exact LM implementation here.
	    /* return KneserNeyLanguageModel(trainingData); */

	}

}
