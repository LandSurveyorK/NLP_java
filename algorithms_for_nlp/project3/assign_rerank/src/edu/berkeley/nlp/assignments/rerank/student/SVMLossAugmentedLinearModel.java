
package edu.berkeley.nlp.assignments.rerank.student;

import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.assignments.rerank.LossAugmentedLinearModel;
import java.util.List;

import java.util.Map;


// an interface for hooking up a model to an SVM trainer (see comment below)
public class SVMLossAugmentedLinearModel implements LossAugmentedLinearModel <List<int[]>>{

/*
    public class UpdateBundle {
		    public UpdateBundle(IntCounter goldFeatures, IntCounter lossAugGuessFeatures, double lossOfGuess) {
		      this.goldFeatures = goldFeatures;
		      this.lossAugGuessFeatures = lossAugGuessFeatures;
		      this.lossOfGuess = lossOfGuess;
		    }

		    public final IntCounter goldFeatures;
		    public final IntCounter lossAugGuessFeatures;
		    public final double lossOfGuess;
    }
    */
public UpdateBundle getLossAugmentedUpdateBundle(List<int[]> datum, IntCounter weights) {
	  
	  
	  IntCounter goldFeatures = new IntCounter();
	  goldFeatures.incrementAll(datum.get(0)); //which is the tree with the best F1 score
	  IntCounter lossAugGuessFeatures = new IntCounter();
	  double lossOfGuess = 1; // initial value
	  
	  double score = Float.NEGATIVE_INFINITY; // w'f(y) + l(y,y^*)
	  
	  for(int i = 1; i < datum.size(); i++) {
		  
		  IntCounter currFeatures = new IntCounter();
		  currFeatures.incrementAll(datum.get(i));
		  double dotProd = 0;
          for(Map.Entry<Integer,Double> entry : currFeatures.entries()){
            final int key = entry.getKey();
            final float val = entry.getValue().floatValue();
            dotProd += val * weights.getCount(key);
          }
          IntCounter different = new IntCounter();
          different.incrementAll(goldFeatures, - 1.0);
          different.incrementAll(currFeatures, 1.0);
          double curr_lossOfGuess = different.normSquared() > 2? 1 : 0;
          
          double curr_score = curr_lossOfGuess + dotProd; 
          if (curr_score > score) {
        	  score = curr_score;
        	  lossAugGuessFeatures = currFeatures;
        	  lossOfGuess = curr_lossOfGuess;
          }
	  }
	  UpdateBundle ub = new UpdateBundle(goldFeatures, lossAugGuessFeatures, lossOfGuess);
	return ub;  
  }
}