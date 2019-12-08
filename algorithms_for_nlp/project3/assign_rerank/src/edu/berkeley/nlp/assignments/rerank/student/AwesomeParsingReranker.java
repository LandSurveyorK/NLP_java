
package edu.berkeley.nlp.assignments.rerank.student;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import edu.berkeley.nlp.assignments.rerank.KbestList;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.assignments.rerank.PrimalSubgradientSVMLearner;
import edu.berkeley.nlp.assignments.rerank.student.SVMLossAugmentedLinearModel;
import edu.berkeley.nlp.assignments.rerank.student.featureExtraction;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import edu.berkeley.nlp.assignments.rerank.student.quicksort;

public class AwesomeParsingReranker implements ParsingReranker {
    
	IntCounter weights;
    int numFeatures;
    int batchSize = 700;
    final double stepSize = 1e-2;
    final double regConstant = 0.5; // 0.5 
    int iters = 30; // 30 
    
    
	featureExtraction FE = new featureExtraction();
	Indexer<String> featureIndexer = new Indexer<String>();
	SVMLossAugmentedLinearModel model = new 
			SVMLossAugmentedLinearModel();
	
	
	public AwesomeParsingReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees) {
        List<List<int[]>> data = new ArrayList<List<int[]>>(); // tree -> feature vector
		
		boolean addNew = true;
		// Features Extraction
		for(Pair<KbestList, Tree<String>> kbestListAndGoldTree : kbestListsAndGoldTrees) {
			KbestList kbestList = kbestListAndGoldTree.getFirst();
			Tree<String> goldTree = kbestListAndGoldTree.getSecond();
			// find best tree (with largest F1 score)
			EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(Collections.singleton("ROOT"), new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));
		    PrintWriter nullWriter = new PrintWriter(new OutputStream() {
		      public void write(int i) throws IOException {
		        // do nothing
		      }
		    });
		    double bestF1 = 0;
		    Tree<String> bestTree = new Tree<String>("junk");
			for (Tree<String> tree: kbestList.getKbestTrees()) {
				eval.evaluate(tree, goldTree, nullWriter);
				double F1 = eval.getF1();
				if ( F1 > bestF1 ) {
					bestF1 = F1;
					bestTree = tree;
				}
			}
			
		    List<int[]> featuresList = new ArrayList<int[]>();
		    // idx = -1 extract features of bestTree
		    // idx >= 0 extract features of kbestTree
		    for(int idx = -1; idx < kbestList.getKbestTrees().size(); idx ++) {
		    	// The first feature vector is gotten from goldTree.
		    	featuresList.add(FE.extractFeatures(kbestList, bestTree,idx, featureIndexer, addNew));
		    }
		   data.add(featuresList);
		}
		numFeatures = featureIndexer.size();
		System.out.println("Feature Size: " + numFeatures);
		System.out.println("done feature extracton...");
		
		// Learning Weights
		IntCounter initWeights = new IntCounter();
		for(int k = 0; k < numFeatures; k ++) {
			initWeights.put(k, 0.0);
		}		
		//SVMPrimalSubgradientSVMLearner learner = new 
		//		SVMPrimalSubgradientSVMLearner(stepSize,
		//		regConstant,numFeatures,batchSize);
		PrimalSubgradientSVMLearner<List<int[]>> learner = new 
				PrimalSubgradientSVMLearner<List<int[]>>(stepSize,
				regConstant, numFeatures, batchSize);
		System.out.println("start learning...\n");
		
		weights = learner.train(initWeights, model,
				data, iters);
		System.out.println("done learning...\n");
		
		System.out.println("Top ten features...\n");
		double[] toArrayWeights = weights.toArray(numFeatures);
		int[] idecies = new int[numFeatures];
		// export weights to .csv file
		/*FileWriter writer = new FileWriter("/Users/pengwei/javaMaster/algorithms_for_nlp/project3/assign_rerank/Perceptron.csv");
		for(int k = 0; k < numFeatures; k++) {
		  writer.append(String.valueOf(toArrayWeights[k]));
		  writer.append(",");
		  writer.append(featureIndexer.get(k));
		  writer.append("\n");
		}
	    writer.close();
	    System.out.println("done save weights in .csv file.");
		*/
		for(int i = 0; i < numFeatures; i++) {
			idecies[i] = i;
		}
		quicksort qs = new quicksort(toArrayWeights, idecies);
		qs.sort();
	    for(int i = 0; i < 10; i++) {
			System.out.println("top" + (i+1) + " " +  featureIndexer.get(qs.index[i]));
		}
		System.out.println('\n');
		
	}
	
	 /**
	   * @param sentence
	   *          The input sentence. This equals the terminal yield of each tree in
	   *          the k-best list; it's just provided in this form so that you have
	   *          more convenient accessors.
	   * @param kbestList
	   *          The list of input trees and their associated scores.
	   * @return The tree from the k-best list selected by the reranker.
	   */
	public Tree<String> getBestParse(List<String> sentence, KbestList kbestList){
		
		List<int[]> featuresList = new ArrayList<int[]>();
		Tree<String> goldTree = new Tree<String>("junk");
		
		boolean addNew = false;
	    for(int idx = 0; idx < kbestList.getKbestTrees().size(); idx ++) {
	        featuresList.add(FE.extractFeatures(kbestList, goldTree, idx, featureIndexer, addNew));
		}
	    // initial best tree as the first one
		double best_score = 0;
		int best_idx  = 0;
		IntCounter best_feat = new IntCounter();
		best_feat.incrementAll(featuresList.get(0));
		
        for (Map.Entry<Integer,Double> entry : best_feat.entries()) {
          final int key = entry.getKey();
          final float val = entry.getValue().floatValue();
          best_score += val * weights.getCount(key);
        }
        kbestList.getScores()[0] = best_score;
		// find the tree with the max score.
		for(int idx = 1; idx < kbestList.getKbestTrees().size(); idx ++) {
			IntCounter curr_feat = new IntCounter();
			curr_feat.incrementAll(featuresList.get(idx));
			double score = 0;
			for(Map.Entry<Integer,Double> entry : curr_feat.entries()) {
		          final int key = entry.getKey();
		          final float val = entry.getValue().floatValue();
		          score += val * weights.getCount(key);
		    }
			kbestList.getScores()[idx] = score;
			if(score > best_score) {
				best_score = score;
				best_idx = idx;
			}
		}

		return kbestList.getKbestTrees().get(best_idx);
	}
		  
}
