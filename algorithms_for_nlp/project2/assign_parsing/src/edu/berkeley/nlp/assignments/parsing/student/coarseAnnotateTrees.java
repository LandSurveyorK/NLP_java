package edu.berkeley.nlp.assignments.parsing.student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.berkeley.nlp.io.PennTreebankReader;
import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.ling.Trees.PennTreeRenderer;
import edu.berkeley.nlp.util.Filter;

/**
 * Class which contains code for annotating and binarizing trees for the
 * parser's use, and debinarizing and unannotating them for scoring.
 */
public class coarseAnnotateTrees
{
	/**
	 * This performs lossless binarization. You'll need to define your own
	 * function to do more intelligent markovization.
	 * 
	 * @param unAnnotatedTree
	 * @return
	 */
	public static Tree<String> annotate(Tree<String> unAnnotatedTree) {

		return binarizeTree(unAnnotatedTree);
	}
   // v = 1. h = 0
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
		Tree<String> annotatedTrainTrees = annotate(trainTrees.get(3));
		System.out.println(PennTreeRenderer.render(annotatedTrainTrees));
		System.out.println(PennTreeRenderer.render(unAnnotateTree(annotatedTrainTrees)));
		System.out.println(PennTreeRenderer.render(trainTrees.get(3)));
		//System.out.println(1.0>Double.NEGATIVE_INFINITY);
		//System.out.println(SloppyMath.logAdd(1,1));
		String a = "123";
		System.out.println(a.indexOf("@"));
		List<Integer> b = new ArrayList<Integer>();
		System.out.println(b.indexOf(2));
		
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