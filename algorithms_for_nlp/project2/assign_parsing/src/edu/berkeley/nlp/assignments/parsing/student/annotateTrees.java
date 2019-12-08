package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import edu.berkeley.nlp.io.PennTreebankReader;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.ling.Trees.PennTreeRenderer;
import edu.berkeley.nlp.util.Filter;
//import edu.berkeley.nlp.io.PennTreebankReader;

/**
 * Class which contains code for annotating and binarizing trees for the
 * parser's use, and debinarizing and unannotating them for scoring.
 */
public class annotateTrees
{
	
	/**
	 * This performs lossless binarization. You'll need to define your own
	 * function to do more intelligent markovization.
	 * @param unAnnotatedTree
	 * @return
	 */
	
	// v= 2 h = 2
	
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

	return binarizeTree(unAnnotatedTree,"");
    }
	private static Tree<String> binarizeTree(Tree<String> tree, String parentLabel) {
		
	String label = tree.getLabel(); // get label 
	if (tree.isLeaf()) return new Tree<String>(label);
	String newLabel = label + parentLabel;
	if (tree.getChildren().size() == 1) {
		return new Tree<String>(newLabel, 
				Collections.singletonList(binarizeTree(tree.getChildren().get(0),"^"+label)));
	}
	String rootLabel = "@" + newLabel + "->";
	String passLabel = "@" + newLabel + "->";
	String passLabelAssist = "@" + newLabel + "->";
	Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, rootLabel, passLabel, passLabelAssist, "^"+label);
	
	return new Tree<String>(newLabel, intermediateTree.getChildren());
	}
	
	
	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
			String rootLabel,  String passLabel,String passLabelAssist, String parentLabel){
		
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree,parentLabel));
	
		String next_passLabelAssist = rootLabel;
		if(numChildrenGenerated < 1) { next_passLabelAssist = rootLabel + "_" + leftTree.getLabel() ;}
		if(numChildrenGenerated >= 1) { next_passLabelAssist = rootLabel + "..._" + leftTree.getLabel();}
        
		if (numChildrenGenerated < tree.getChildren().size() - 2) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, 
					rootLabel, passLabelAssist + "_" + leftTree.getLabel(), next_passLabelAssist, parentLabel);
			children.add(rightTree);	
		}
		if (numChildrenGenerated == tree.getChildren().size() - 2) {
		    Tree<String> rightTree = tree.getChildren().get(tree.getChildren().size()-1);
			children.add(binarizeTree(rightTree,parentLabel));	
		}
		
		return new Tree<String>(passLabel, children);
	}
	
	
	// v= 1 h = 0
	/*
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

		return binarizeTree(unAnnotatedTree);
	}
	  private static Tree<String> binarizeTree(Tree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf()) return new Tree<String>(label);
		if (tree.getChildren().size() == 1) { return new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0)))); }
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@" + label;
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelper(Tree<String> tree, 
			int numChildrenGenerated, String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel);
			children.add(rightTree);
		}
		return new Tree<String>(intermediateLabel, children);
	}

	
	*/
	 
	/*
    // v = 1, h = 1
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

		return binarizeTree(unAnnotatedTree);
	}
	private static Tree<String> binarizeTree(Tree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf()) return new Tree<String>(label);
		// after binarizaton, return a tree instead of list
		if (tree.getChildren().size() == 1) { return new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0)))); }
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@" + label + "->";
		String rootLabel = "@" + label + "->";
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel, rootLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated, String intermediateLabel, String rootLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, rootLabel + "_" + leftTree.getLabel(), rootLabel);
			children.add(rightTree);
		}
		return new Tree<String>(intermediateLabel, children);
	}
	*/
	
	// v = 1 h= 2
	/*
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

		return binarizeTree(unAnnotatedTree);
	    }
		private static Tree<String> binarizeTree(Tree<String> tree) {
			
		String label = tree.getLabel(); // get label 
		if (tree.isLeaf()) return new Tree<String>(label);
		String newLabel = label;
		if (tree.getChildren().size() == 1) {
			return new Tree<String>(newLabel, 
					Collections.singletonList(binarizeTree(tree.getChildren().get(0))));
		}
		String rootLabel = "@" + newLabel + "->";
		String passLabel = "@" + newLabel + "->";
		String passLabelAssist = "@" + newLabel + "->";
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, rootLabel, passLabel, passLabelAssist);
		
		return new Tree<String>(newLabel, intermediateTree.getChildren());
		}
		
		
		private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
				String rootLabel,  String passLabel,String passLabelAssist){
			
			Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			children.add(binarizeTree(leftTree));
		
			String next_passLabelAssist = rootLabel;
			if(numChildrenGenerated < 1) { next_passLabelAssist = rootLabel + "_" + leftTree.getLabel() ;}
			if(numChildrenGenerated >= 1) { next_passLabelAssist = rootLabel + "..._" + leftTree.getLabel();}
	        
			if (numChildrenGenerated < tree.getChildren().size() - 2) {
				Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, 
						rootLabel, passLabelAssist + "_" + leftTree.getLabel(), next_passLabelAssist);
				children.add(rightTree);	
			}
			if (numChildrenGenerated == tree.getChildren().size() - 2) {
			    Tree<String> rightTree = tree.getChildren().get(tree.getChildren().size()-1);
				children.add(binarizeTree(rightTree));	
			}
			
			return new Tree<String>(passLabel, children);
		}
	*/
	// v = 1 h = infinity
	/*
	 public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

		return binarizeTree(unAnnotatedTree);
	}

	private static Tree<String> binarizeTree(Tree<String> tree) {
		String label = tree.getLabel();
		if (tree.isLeaf()) return new Tree<String>(label);
		if (tree.getChildren().size() == 1) { return new Tree<String>(label, Collections.singletonList(binarizeTree(tree.getChildren().get(0)))); }
		// otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.
		String intermediateLabel = "@" + label + "->";
		Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel);
		return new Tree<String>(label, intermediateTree.getChildren());
	}

	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated, String intermediateLabel) {
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree));
		if (numChildrenGenerated < tree.getChildren().size() - 1) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel + "_" + leftTree.getLabel());
			children.add(rightTree);
		}
		return new Tree<String>(intermediateLabel, children);
	}
	 */
	
	
	// v= 2 h=0
	/* static Tree<String> annotate(Tree<String> unAnnotatedTree) {

	return binarizeTree(unAnnotatedTree,"");
    }
	private static Tree<String> binarizeTree(Tree<String> tree, String parentLabel) {
		
	String label = tree.getLabel(); // get label 
	if (tree.isLeaf()) return new Tree<String>(label);
	String newLabel = label + parentLabel;
	if (tree.getChildren().size() == 1) {
		return new Tree<String>(newLabel, Collections.singletonList(binarizeTree(tree.getChildren().get(0),"^"+label)));
	}
	String rootLabel = "@" + newLabel ;
	Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, rootLabel, "^"+label);
	return new Tree<String>(newLabel, intermediateTree.getChildren());
	}
	
	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
			String rootLabel,   String parentLabel){
		
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree,parentLabel));

		if (numChildrenGenerated < tree.getChildren().size() - 2) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, 
					rootLabel, parentLabel);
			children.add(rightTree);	
		}
		if (numChildrenGenerated == tree.getChildren().size() - 2) {
		    Tree<String> rightTree = tree.getChildren().get(tree.getChildren().size()-1);
			children.add(binarizeTree(rightTree,parentLabel));	
		}
		
		return new Tree<String>(rootLabel, children);
		
	}
	*/
	
	// v= 2, h = 1
	/*
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

	return binarizeTree(unAnnotatedTree,"");
    }
	private static Tree<String> binarizeTree(Tree<String> tree, String parentLabel) {
		
	String label = tree.getLabel(); // get label 
	if (tree.isLeaf()) return new Tree<String>(label);
	String newLabel = label + parentLabel;
	if (tree.getChildren().size() == 1) {
		return new Tree<String>(newLabel, Collections.singletonList(binarizeTree(tree.getChildren().get(0),"^"+label)));
	}
	String rootLabel = "@" + newLabel + "->";
	String passLabel = "@" + newLabel + "->";
	Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, rootLabel, passLabel, "^"+label);
	return new Tree<String>(newLabel, intermediateTree.getChildren());
	}
	
	private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
			String rootLabel,  String passLabel, String parentLabel){
		
		Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
		List<Tree<String>> children = new ArrayList<Tree<String>>();
		children.add(binarizeTree(leftTree,parentLabel));
		
		if (numChildrenGenerated < tree.getChildren().size() - 2) {
			Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, 
					rootLabel, rootLabel + "_" + leftTree.getLabel(), parentLabel);
			children.add(rightTree);	
		}
		if (numChildrenGenerated == tree.getChildren().size() - 2) {
		    Tree<String> rightTree = tree.getChildren().get(tree.getChildren().size()-1);
			children.add(binarizeTree(rightTree,parentLabel));	
		}
		
		return new Tree<String>(passLabel, children);
		
	}
	*/
	

	
// check if annotate correctly

	public static Tree<String> unAnnotateTree(Tree<String> annotatedTree) {
		// Remove intermediate nodes (labels beginning with "@"
		// Remove all material on node labels which follow their base symbol (cuts anything after <,>,^,=,_ or ->)
		// Examples: a node with label @NP->DT_JJ will be spliced out, and a node with label NP^S will be reduced to NP
		Tree<String> debinarizedTree = Trees.spliceNodes(annotatedTree, new Filter<String>()
		{
			public boolean accept(String s) {
				return s.startsWith("@");
			}
		});
		Tree<String> unAnnotatedTree = (new Trees.LabelNormalizer()).transformTree(debinarizedTree);
		return unAnnotatedTree;
	}
	
public static void main(String[] args) {
		int maxTrainLength = 1000;
		int trainTreesEnd = 10;
		String basePath = "/Users/pengwei/javaMaster/algorithms_for_nlp/project2/wsj";
		System.out.print("Loading training trees (sections 2-21) ... ");
		List<Tree<String>> trainTrees = readTrees(basePath, 10, trainTreesEnd, maxTrainLength);
		Tree<String> annotatedTrainTrees = annotate(trainTrees.get(6));
		System.out.println(PennTreeRenderer.render(annotatedTrainTrees));
		System.out.println(PennTreeRenderer.render(unAnnotateTree(annotatedTrainTrees)));
		System.out.println(PennTreeRenderer.render(trainTrees.get(6)));
		//System.out.println(1.0>Double.NEGATIVE_INFINITY);
		

		
    }
	private static List<Tree<String>> readTrees(String basePath, int low, int high, int maxLength) {
		Collection<Tree<String>> trees = PennTreebankReader.readTrees(basePath, low, high);
		// normalize trees
		Trees.TreeTransformer<String> treeTransformer = new Trees.StandardTreeNormalizer();
		List<Tree<String>> normalizedTreeList = new ArrayList<Tree<String>>();
		for (Tree<String> tree : trees) {
			Tree<String> normalizedTree = treeTransformer.transformTree(tree);
			if (normalizedTree.getYield().size() > maxLength) continue;
			//      System.out.println(Trees.PennTreeRenderer.render(normalizedTree));
			normalizedTreeList.add(normalizedTree);
		}
		return normalizedTreeList;
	}

	
}