package edu.berkeley.nlp.assignments.rerank.student;


import java.util.ArrayList;
//import java.util.Collection;
import java.util.List;
import edu.berkeley.nlp.assignments.rerank.KbestList;
//import edu.berkeley.nlp.assignments.rerank.SimpleFeatureExtractor;
//import edu.berkeley.nlp.assignments.rerank.SurfaceHeadFinder;
import edu.berkeley.nlp.ling.AnchoredTree;
//import edu.berkeley.nlp.ling.Constituent;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.util.Indexer;

/**
 * Baseline feature extractor for k-best lists of parses. Note that this does
 * not implement Featurizer, though you can adapt it to do so.
 * 
 * @author Wei Peng
 *
 */
public class featureExtraction {

  /**
   * 
   * @param kbestList
   * @param idx
   *          The index of the tree in the k-best list to extract features for
   * @param featureIndexer
   * @param addFeaturesToIndexer
   *          True if we should add new features to the indexer, false
   *          otherwise. When training, you want to make sure you include all
   *          possible features, but adding features at test time is pointless
   *          (since you won't have learned weights for those features anyway).
   * @return
   */
  public int[] extractFeatures(KbestList kbestList,Tree<String> targetTree, int idx, Indexer<String> featureIndexer, boolean addFeaturesToIndexer) {

    Tree<String> tree = idx >= 0 ? kbestList.getKbestTrees().get(idx) : targetTree;
    String punctuations = "!\"#$%&'()*+,-./:;?@[\\]^_`{|}~ ";
    // Converts the tree
    // (see below)
    AnchoredTree<String> anchoredTree = AnchoredTree.fromTree(tree);
    // If you just want to iterate over labeled spans, use the constituent list
   // Collection<Constituent<String>> constituents = tree.toConstituentList();
    // You can fire features on parts of speech or words
    List<String> poss = tree.getPreTerminalYield();
    List<String> words = tree.getYield();
    int sentenceSize = words.size();
    // Allows you to find heads of spans of preterminals. Use this to fire
    // dependency-based features
    // like those discussed in Charniak and Johnson
    //SurfaceHeadFinder shf = new SurfaceHeadFinder();

    // FEATURE COMPUTATION
    List<Integer> feats = new ArrayList<Integer>();
    
    // (1)Fires a feature based on the position in the k-best list. This should
    // allow the model to learn that
    // high-up trees
    addFeature("Posn=" + idx, feats, featureIndexer, addFeaturesToIndexer);
    
    for (AnchoredTree<String> subtree : anchoredTree.toSubTreeList()) {
    	if (!subtree.isPreTerminal() && !subtree.isLeaf()) {
        // Fires a feature based on the identity of a nonterminal rule
        // production. This allows the model to learn features
        // roughly equivalent to those in an unbinarized coarse grammar.
    		
    		// (2) Fires RULE
           String rule = "RULE=" + subtree.getLabel() + " ->";
            for (AnchoredTree<String> child : subtree.getChildren()) {
            rule += " " + child.getLabel();
            }
            addFeature(rule, feats, featureIndexer, addFeaturesToIndexer);
            
            // (3) PARENT, FIRSTWORD, LASTWORD,LENGTH
            String parent = "PARENT=" + subtree.getLabel();
   	        String firstword = "FIRSTWORD=" + words.get(subtree.getStartIdx())+ "@" + rule;
   	        String lastword = "LASTWORD=" + words.get(subtree.getEndIdx()-1)+ "@" + rule;
   	        // String length = "LENGTH=" + subtree.getSpanLength();
   	        String spanInfo = "SPANIFO=" + subtree.getLabel() + " " + subtree.getSpanLength() +
   	        		" " + words.get(subtree.getStartIdx()) + " " + words.get(subtree.getEndIdx()-1);
            addFeature(parent, feats, featureIndexer, addFeaturesToIndexer);
            addFeature(firstword, feats, featureIndexer, addFeaturesToIndexer);
            addFeature(lastword, feats, featureIndexer, addFeaturesToIndexer);
            //addFeature(length, feats, featureIndexer, addFeaturesToIndexer);
            addFeature(spanInfo, feats, featureIndexer, addFeaturesToIndexer);
            
           // span shape
           String span_shape = "SPAN_SHAPE=" + subtree.getLabel() + " ->";
           for(int i = subtree.getStartIdx(); i < subtree.getEndIdx(); i ++) {
        	   if (punctuations.contains(words.get(i))){
        		   span_shape += words.get(i);
        	   }
        	   else if(Character.isUpperCase(words.get(i).charAt(0))){
        		   span_shape += "X";  
        	   }
        	   else {
        		   span_shape += "x";
        	   }
           }
           addFeature(span_shape, feats, featureIndexer,addFeaturesToIndexer);
           
           // (5) span context
           String span_context = "SPAN_CONTEXT=" + subtree.getLabel() + " -> ";
           if (subtree.getStartIdx() > 0 ) {
        	   span_context += words.get(subtree.getStartIdx()-1);
           }
           else {
        	   span_context += "BOS";
           }
           for (AnchoredTree<String> child : subtree.getChildren()) {
               span_context += " " + child.getLabel();
           }
           addFeature(span_context, feats, featureIndexer, addFeaturesToIndexer);
           
           
          // (6) split point
          String split_point = "SPLIT_POINT=" + subtree.getLabel() + " ->";
           List<AnchoredTree<String>> children = subtree.getChildren();
           for (int i = 0; i < children.size() - 1; i++) {
               split_point += " " + children.get(i).getLabel() + "..." + words.get(children.get(i).getEndIdx()-1);
           }
           split_point += children.get(children.size()-1).getLabel();
           addFeature(split_point, feats, featureIndexer, addFeaturesToIndexer);
          
           
           // (7) heavyness
           String heavyness = "HEAVYNESS=" + subtree.getLabel() + " " + subtree.getSpanLength();
           heavyness += " " + (-subtree.getEndIdx() +  words.size());
           addFeature(heavyness, feats, featureIndexer, addFeaturesToIndexer);
           
           //(8) word n word + ancestor n = 3
           List<AnchoredTree<String>> children1 = subtree.getChildren();
           if(children1.size() > 0) {
           	for(AnchoredTree<String> child1: children1) {
           		List<AnchoredTree<String>> children2 = child1.getChildren();
           		if(children2.size() > 0) {
           			for(AnchoredTree<String> child2 : children2) {
           				if(child2.isPreTerminal()) {
           					String ancestor = "ANCESTOR=" + child2.getChildren().get(0).getLabel() + " " +
           							child2.getLabel() + " " +
           							child1.getLabel() + " " + subtree.getLabel();
           					addFeature(ancestor, feats, featureIndexer, addFeaturesToIndexer);
           				}
           			}
           		}	
           	}
          }
           
           // (9) neighbors
           String neighbor = "NEIGHBOR=" + subtree.getLabel() + " " + subtree.getSpanLength();
           if(subtree.getStartIdx() != 0) {
        	   neighbor += " " + poss.get(subtree.getStartIdx()-1);
           }
           else { neighbor += " " + "BOS"; }
           if(subtree.getEndIdx() < sentenceSize) {
        	   neighbor +=  " " + poss.get(subtree.getEndIdx());
           }
           else { neighbor += " " +  "EOS"; }
           addFeature(neighbor, feats, featureIndexer, addFeaturesToIndexer); 
        }
     }
    
    // (10) RightBranch: number of nodes in the right most branch
    int num = findNumberOfNodesInRightMostBranch(anchoredTree) - 2;
    String rightBranch = "NUM_NODES_RIGHTMOST=" + num ;
    addFeature(rightBranch, feats, featureIndexer, addFeaturesToIndexer);

    // convert to array
    int[] featsArr = new int[feats.size()];
    for (int i = 0; i < feats.size(); i++) {
      featsArr[i] = feats.get(i).intValue();
    }
    return featsArr;
  }

  private int findNumberOfNodesInRightMostBranch(AnchoredTree<String> tree) {
	  String punctuations = "!\"#$%&'()*+,-./:;?@[\\]^_`{|}~ ";
	  if (tree.isLeaf()){ return 0;}
  	  List<AnchoredTree<String>> children = tree.getChildren();
  	  int maxsubTree = 0;
  	  int size = children.size();
  	  int idx = size - 1 ;
  	  AnchoredTree<String> child = children.get(idx);
  	  while(punctuations.contains(child.getLabel()) && idx > 0 ){
  		  idx = idx - 1;
  		  child = children.get(idx);
  	  }
  	  if(punctuations.contains(child.getLabel())) { maxsubTree = 0;}
  	  else{
  		  maxsubTree = findNumberOfNodesInRightMostBranch(child);
  	  }
  	 return 1 + maxsubTree;
  	 }
	     

  /**
   * Shortcut method for indexing a feature and adding it to the list of
   * features.
   * 
   * @param feat
   * @param feats
   * @param featureIndexer
   * @param addNew
   */
  private void addFeature(String feat, List<Integer> feats, Indexer<String> featureIndexer, boolean addNew) {
    if (addNew || featureIndexer.contains(feat)) {
      feats.add(featureIndexer.addAndGetIndex(feat));
    }
  }
  
}