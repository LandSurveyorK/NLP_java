
package edu.berkeley.nlp.assignments.rerank.student;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import edu.berkeley.nlp.util.IntCounter;
import edu.berkeley.nlp.assignments.rerank.ParsingReranker;
import edu.berkeley.nlp.assignments.rerank.KbestList;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.parser.EnglishPennTreebankParseEvaluator;
import edu.berkeley.nlp.util.Pair;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.assignments.rerank.student.quicksort;
/**
 * basic reranker
 * 
 * @author wei
 *
 */
public class BasicParsingReranker implements ParsingReranker {
    
	IntCounter weights = new IntCounter();
	IntCounter AveWeights = new IntCounter();
    int numFeatures;
    int iter = 10;
	featureExtraction FE = new featureExtraction();
	Indexer<String> featureIndexer = new Indexer<String>();
	
	public BasicParsingReranker(Iterable<Pair<KbestList,Tree<String>>> kbestListsAndGoldTrees){
        List<List<int[]>> data = new ArrayList<List<int[]>>(); // tree -> feature vector
        List<Integer> bestIndexes = new ArrayList<Integer>();
		boolean addNew = true;
		// Features Extraction
		for(Pair<KbestList, Tree<String>> kbestListAndGoldTree : kbestListsAndGoldTrees) {
			KbestList kbestList = kbestListAndGoldTree.getFirst();
			Tree<String> goldTree = kbestListAndGoldTree.getSecond();
			EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String> eval = new EnglishPennTreebankParseEvaluator.LabeledConstituentEval<String>(Collections.singleton("ROOT"), new HashSet<String>(Arrays.asList(new String[] { "''", "``", ".", ":", "," })));
		    PrintWriter nullWriter = new PrintWriter(new OutputStream() {
		      public void write(int i) throws IOException {
		        // do nothing
		      }
		    });
		    
		    double bestF1 = 0;
		    int bestIdx = -1;
		    Tree<String> bestTree = new Tree<String>("junk");
			for (int idx = 0; idx < kbestList.getKbestTrees().size(); idx ++) {
				Tree<String> tree = kbestList.getKbestTrees().get(idx);
				eval.evaluate(tree, goldTree, nullWriter);
				double F1 = eval.getF1();
				if (F1 > bestF1) {
					bestF1 = F1;
					bestTree = tree;
					bestIdx = idx;
				}
			}
			bestIndexes.add(bestIdx);
		    List<int[]> featuresList = new ArrayList<int[]>();
		    for(int idx = -1; idx < kbestList.getKbestTrees().size(); idx ++) {
		    	// The first feature vector is gotten from goldTree.
		    	featuresList.add(FE.extractFeatures(kbestList, bestTree, idx, featureIndexer, addNew));
		    }
		   data.add(featuresList);
		}
		// Learning Weights
		numFeatures = featureIndexer.size();
		System.out.println("Feature Size: " + numFeatures);
		System.out.println("done feature extracton...");
		// initialization
		for(int k = 0; k < numFeatures; k ++) {
			weights.put(k, 0);
		}
		System.out.println("strat learning...\n");
		
		for(int epoch = 1; epoch <= iter; epoch ++) {
		    int sent = -1;
		    for(Pair<KbestList, Tree<String>> kbestListAndGoldTree : kbestListsAndGoldTrees){
			    sent ++;
			    KbestList kbestList = kbestListAndGoldTree.getFirst();
			    
			    List<int[]> featuresList = data.get(sent);
			    IntCounter gold_feat = new IntCounter();
			    gold_feat.incrementAll(featuresList.get(0));
			    int gold_idx = bestIndexes.get(sent);
			    
			    int best_idx = -1;
			    double score = Double.NEGATIVE_INFINITY;
			    IntCounter best_feat = new IntCounter(); 
				
			    for(int idx = 0; idx < kbestList.getKbestTrees().size(); idx ++) {
				    IntCounter curr_feat = new IntCounter();
				    curr_feat.incrementAll(featuresList.get(idx + 1));
				    double curr_score = 0;
				    for (Map.Entry<Integer,Double> entry : curr_feat.entries()) {
				          final int key = entry.getKey();
				          final float val = entry.getValue().floatValue();
				          curr_score += val * weights.getCount(key);
				    }
				    if (curr_score > score) {
					    score = curr_score;
					    best_feat = curr_feat;
					    best_idx = idx ;
				    }
			    }
			    if(best_idx != gold_idx ) {
			    	weights.incrementAll(gold_feat, 1.0);
			    	weights.incrementAll(best_feat, -1.0);
			    }
			    
		    }
            AveWeights.incrementAll(weights, 1.0 / iter);
		    
		    System.out.println( "done learning..." + "epcoh = " + epoch);
		}
		// top 10 features
		System.out.println("Top ten features...\n");
		double[] toArrayWeights = AveWeights.toArray(numFeatures);
		int[] idecies = new int[numFeatures];
		// export weights to .csv file 

	    
	    System.out.println("done save weights in .csv file.");
	    
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
		int best_idx  = 0;
		IntCounter best_feat = new IntCounter();
		best_feat.incrementAll(featuresList.get(0));
		double best_score = 0;
        for (Map.Entry<Integer,Double> entry : best_feat.entries()) {
          final int key = entry.getKey();
          final float val = entry.getValue().floatValue();
          best_score += val * AveWeights.getCount(key);
        }
		// find the tree with the max score.
		for(int idx = 1; idx < kbestList.getKbestTrees().size(); idx ++) {
			IntCounter curr_feat = new IntCounter();
			curr_feat.incrementAll(featuresList.get(idx));
			double curr_score = 0;
			for(Map.Entry<Integer,Double> entry : curr_feat.entries()) {
		          final int key = entry.getKey();
		          final float val = entry.getValue().floatValue();
		          curr_score += val * AveWeights.getCount(key);
		    }
			if(curr_score > best_score) {
				best_score = curr_score;
				best_idx = idx;
			}
		}

		return kbestList.getKbestTrees().get(best_idx);
	}
		  
}
