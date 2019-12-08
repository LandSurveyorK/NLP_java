package edu.berkeley.nlp.assignments.parsing.student;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import edu.berkeley.nlp.assignments.parsing.Grammar;
import edu.berkeley.nlp.assignments.parsing.BinaryRule;
import edu.berkeley.nlp.assignments.parsing.UnaryRule;
import edu.berkeley.nlp.assignments.parsing.SimpleLexicon;
import edu.berkeley.nlp.assignments.parsing.UnaryClosure;
import edu.berkeley.nlp.assignments.parsing.Parser;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;
import edu.berkeley.nlp.assignments.parsing.student.annotateTrees;
//import edu.berkeley.nlp.assignments.parsing.TreeAnnotations;

public class GenerativeParser implements Parser{
	Grammar grammar;
	short num_grammar;
	Indexer<String> grammarIndexer;
	SimpleLexicon lexicon;
	UnaryClosure uc;
	// use for an alternative way by using two charts
	double[][][]  UnaryChart; //score
	double[][][] BinaryChart;
	int[][][] UnaryBack, BinaryMid, BinaryLeft, BinaryRight;
	
	public GenerativeParser(List<Tree<String>> trainTrees) {
		System.out.println("train parser size: " + trainTrees.size());
		
		System.out.println("Annotating / Binarizing training tress ...");
		int size = trainTrees.size();
		ArrayList<Tree<String>> annotatedTrainTrees = new ArrayList<Tree<String>>();
		for(int i = 0; i < size; i++) {
			annotatedTrainTrees.add(annotateTrees.annotate(trainTrees.get(i)));	
		}
		System.out.println("done.");
		
		System.out.println("Building grammer ... ");
		grammar = Grammar.generativeGrammarFromTrees(annotatedTrainTrees);
		grammarIndexer = grammar.getLabelIndexer();
		num_grammar = (short) grammarIndexer.size();
		System.out.println("done. (" + num_grammar + " states)");
		
		System.out.println("Getting lexicon ... ");
		lexicon = new SimpleLexicon(annotatedTrainTrees);
		uc = new UnaryClosure(grammarIndexer, grammar.getUnaryRules());
		System.out.println("done");
	}
	
	private Tree<String> unaryExtend(List<Integer> path, List<Tree<String>> tailTree){
		
		List<Tree<String>> trees = tailTree;
		for(int i = path.size() - 1; i >= 0; i--) {
			trees = Collections.singletonList(
					new Tree<String>(grammarIndexer.get(path.get(i)), trees));	
		}
		return trees.get(0);
	}
	
	// using one single chart
	
    public Tree<String> getBestParse(List<String> sentence){
	
	    short n = (short)sentence.size();
	    short m = (short)grammarIndexer.size();
	    double Chart[][][] = new double[n][n+1][m];
	    boolean[][][] toApplyUnaryRule = new boolean[n][n+1][m];
	    List<Integer>[][][] Back = (List<Integer>[][][]) new List[n][n+1][m];//trace info
	    // Initialize Chart, Term, back
	    for(int i = 0; i < n; i++) {
	        for(int j = i+1; j < n+1; j++){ 
	            for(int k = 0; k < m; k++){
	                toApplyUnaryRule[i][j][k] = false;
				    Chart[i][j][k] = Double.NEGATIVE_INFINITY;
			        Back[i][j][k] =  new ArrayList<Integer>();
			    }
		    }
	    }
	    // fill out diagonal cells
	    for (int i = 0; i < n; i++) {
	        for (String A_string: lexicon.getAllTags()) {
	            double score = lexicon.scoreTagging(sentence.get(i), A_string);
	            if (score != Double.NaN && score != Double.NEGATIVE_INFINITY) {
	                int A = grammarIndexer.addAndGetIndex(A_string);
	                Chart[i][i+1][A] = score;
	                toApplyUnaryRule[i][i+1][A] = true;
	                }
	            }
	        // handle unary rule
	        for(int A = 0; A < m ; A++) {
	            if(toApplyUnaryRule[i][i+1][A] == true) {
	                for (UnaryRule candidate: uc.getClosedUnaryRulesByChild(A)) {
	                    int B = candidate.getParent();
	                    double prob = candidate.getScore() + Chart[i][i+1][A];	
	                    if (prob > Chart[i][i+1][B]) { 
	                        Chart[i][i+1][B] = prob;
	                        List<Integer> info = new ArrayList<Integer>();
	                        info.add(A);
	                        Back[i][i+1][B] = info;
	                        }
	                    }
	                }
	            }
	        }
	    // fill out off-diagonal cells
	    for (int max = 2; max <= n; max++) {
	        for (int min = max - 2; min >= 0; min--) {
	            for (int mid = min + 1; mid <= max - 1; mid++) {
	                for(int B = 0; B < m; B++) {
	                    if(Chart[min][mid][B] != Double.NEGATIVE_INFINITY) {
	                        for (BinaryRule candidate: grammar.getBinaryRulesByLeftChild(B)) {
	                            int C = candidate.getRightChild();
	                            int A = candidate.getParent();
	                            if (Chart[mid][max][C] != Double.NEGATIVE_INFINITY) {
	                                double prob = Chart[min][mid][B] + 
	                                	    Chart[mid][max][C] + candidate.getScore();							        
	                                if (prob > Chart[min][max][A]) {
	                                    Chart[min][max][A] = prob;
	                                    List<Integer> info = new ArrayList<Integer>();
	                                    info.add(mid);
	                                    info.add(B);
	                                    info.add(C);
	                                    Back[min][max][A] = info;
	                                    toApplyUnaryRule[min][max][A] = true;
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	            //handle unary
	            for(int A = 0; A < m; A++) {
	                if(toApplyUnaryRule[min][max][A] == true) {
	                    for(UnaryRule candidate: uc.getClosedUnaryRulesByChild(A)) {
	                        double prob = candidate.getScore() + Chart[min][max][A];
	                        int B = candidate.getParent();
	                        if (prob > Chart[min][max][B]) {
	                            Chart[min][max][B] = prob;
	                            List<Integer> info = new ArrayList<Integer>();
	                            info.add(A);
	                            Back[min][max][B] = info;
	                        }
	                    }
	                }
	            }
	        }
	    }
        Tree<String> annotatedBestParse = buildTree(sentence, 0, n, "ROOT", Back);
        return annotateTrees.unAnnotateTree(annotatedBestParse);
    }

	private Tree<String> buildTree(List<String> sentence, int min, int max, 
		String label, List<Integer>[][][] back){
		
		int A = grammarIndexer.addAndGetIndex(label);
		List<Integer> info = back[min][max][A];
		// handle preterminal
		if (info.size() == 0 && min == max - 1) {
				return new Tree<String>(label, Collections.singletonList(
						new Tree<String>(sentence.get(min))));
		}
		// handle unary	
		if (info.size() == 1) {
			int B = info.get(0);
			UnaryRule unaryrule = new UnaryRule(A,B); 
		    String tailTreeLabel = grammarIndexer.get(B);
    		Tree<String> tailTree = buildTree(sentence,min, max, tailTreeLabel, back);
		    List<Integer> path = uc.getPath(unaryrule);			
		    Tree<String> Tree = unaryExtend(path.subList(0, path.size()-1), Collections.singletonList(tailTree));
		    return Tree;	
		}
		// handle binary
		if(info.size() == 3) {
		    String leftTreeLabel = grammarIndexer.get(info.get(1));
		    String rightTreeLabel = grammarIndexer.get(info.get(2));
		    Tree<String> leftTree = buildTree(sentence, min, info.get(0), leftTreeLabel, back);
		    Tree<String> rightTree = buildTree(sentence, info.get(0), max, rightTreeLabel,back);
		    List<Tree<String>> children = new ArrayList<Tree<String>>();
		    children.add(leftTree);
		    children.add(rightTree);
		    return new Tree<String>(label,children);
		}
		// No "ROOT" in [0][word.size()], return JUNK
	    return new Tree<String>("ROOT", Collections.singletonList(new Tree<String>("JUNK")));
	}
	
	
}
    

