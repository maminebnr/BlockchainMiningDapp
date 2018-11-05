package ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.tries;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.abstracciones.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.dataStructures.patterns.PatternCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clasp_AGP.idlists.IDList;

/**
 * Class that implement a trie structure. A trie is composed of a list of nodes
 * children that are also the beginning of other trie structure. Those nodes are
 * composed of both a ItemAstractionPair object and a Trie, where the children
 * appear.
 * 
 * The current trie is referring to a pattern that can be obtained from the root
 * until this one, passing by the different nodes in the way that are ancestors
 * of the current trie. We do not keep any trace of the parent nodes since the
 * whole trie will be run at the end of the algorithm, just before applying the
 * postprocessing step to remove the remaining non-closed frequent patterns.
 *
 * Besides, in a trie we keep some information relative to that pattern that is
 * referred, such as the sequences where the pattern appears, its support, and
 * some other information used in the key generation of the pruning methods.
 *
 * Copyright Antonio Gomariz Peñalver 2013
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public class Trie implements Comparable<Trie> {

	/**
	 * List of children of the current trie
	 */
	List<TrieNode> nodes;
	/*
	 * Tin insert:
	 */
	List<TrieNode> nodei;

	public void mergeWithTrie_i(TrieNode trie) {
		if (levelSize_i() == 0) {
			if (nodei == null) {
				nodei = new ArrayList<TrieNode>(1);
			}
		}
		nodei.add(trie);
	}

	public int levelSize_i() {
		if (nodei == null) {
			return 0;
		}
		return nodei.size();
	}

	/**
	 * IdList associated with the pattern to which the current trie is referring to
	 */
	private IDList idList;
	/**
	 * List of sequences IDs where the pattern, to which the current trie is
	 * referring to, appears
	 */
	private BitSet appearingIn = new BitSet();
	/**
	 * Support that the pattern, to which the current trie is referring to, has
	 */
	private int support = -1;
	/**
	 * Counter that keeps the sum of all the sequence IDs that are in appearingIn
	 * list
	 */
	private int sumSequencesIDs = -1;
	/**
	 * Static field in order to generate a different identifier for all the tries
	 * generated by the algorithm
	 */
	static int intId = 1;
	/**
	 * Trie identifier
	 */
	private int id;

	/**
	 * Constructor of a Trie by means of a list of NodeTrie and the IdList of the
	 * pattern associated to the trie.
	 * 
	 * @param nodes  List of nodes with which we want to initialize the Trie
	 * @param idList IdList of the pattern associated to the trie
	 */
	public Trie(List<TrieNode> nodes, IDList idList) {
		this.nodes = nodes;
		this.idList = idList;
		id = intId++;
	}

	/**
	 * Constructor of a Trie by means of a list of NodeTrie.
	 * 
	 * @param nodes List of nodes with which we want to initialize the Trie
	 */
	public Trie(List<TrieNode> nodes) {
		this.nodes = nodes;
		id = intId++;
	}

	/**
	 * Standard constructor of a Trie. It sets the list of nodes to empty.
	 */
	public Trie() {
		nodes = new ArrayList<TrieNode>();
//Tin insert:        
		nodei = new ArrayList<TrieNode>();
		id = intId++;
	}

	/**
	 * It obtain its ith trie child
	 * 
	 * @param index Child index in which we are interested
	 * @return the trie
	 */
	public Trie getChild(int index) {
		return nodes.get(index).getChild();
	}

	/**
	 * It set a child to the Trie given as parameter
	 * 
	 * @param index Child index in which we are interested
	 * @param child Trie that we want to insert
	 */
	public void setChild(int index, Trie child) {
		this.nodes.get(index).setChild(child);
	}

	/**
	 * It gets the IdList of the pattern associated to the trie
	 * 
	 * @return the idlist
	 */
	public IDList getIdList() {
		return idList;
	}

	/**
	 * It updates the value of the IdList of the pattern associated to the trie
	 * 
	 * @param idList
	 */
	public void setIdList(IDList idList) {
		this.idList = idList;
	}

	/**
	 * It gets the list of nodes associated with the Trie
	 * 
	 * @return the list of trie nodes
	 */
	public List<TrieNode> getNodes() {
		return nodes;
	}

	/**
	 * It updates the list of nodes associated with the Trie
	 * 
	 * @param nodes
	 */
	public void setNodes(List<TrieNode> nodes) {
		this.nodes = nodes;
	}

	/**
	 * It removes the ith child of the Trie.
	 * 
	 * @param index Child index in which we are interested
	 * @return true if the item was removed, otherwise it means that the index is
	 *         outside the bounds of the trie
	 */
	public boolean remove(int index) {
		// if there are some nodes and the index is within the range of nodes
		if (levelSize() == 0 || index >= levelSize()) {
			return false;
		}
		// We remove the child pointed out by index
		getChild(index).removeAll();
		return true;
	}

	/**
	 * It gets the pair of the ith child
	 * 
	 * @param index Child index in which we are interested
	 * @return the pair
	 */
	public ItemAbstractionPair getPair(int index) {
		return nodes.get(index).getPair();
	}

	/**
	 * It gets the whole TrieNode of the ith child
	 * 
	 * @param index Child index in which we are interested
	 * @return the trie node
	 */
	public TrieNode getNode(int index) {
		return nodes.get(index);
	}

	/**
	 * It updates the whole TrieNode of the ith child
	 * 
	 * @param index Child index in which we are interested
	 * @param node
	 */
	public void setNode(int index, TrieNode node) {
		nodes.set(index, node);
	}

	/**
	 * It returns the number of children that a Trie has
	 * 
	 * @return the number of children
	 */
	public int levelSize() {
		if (nodes == null) {
			return 0;
		}
		return nodes.size();
	}

	/**
	 * It removes all its descendands tries and then the Trie itself.
	 */
	public void removeAll() {
		// If there are no nodes
		/*
		 * Tin correct:
		 */
		if (levelSize() == 0 || levelSize_i() == 0) {
//        if (levelSize() == 0) {
			// We have already finish
			return;
		}
		// Otherwise, for each node of the Trie children
		for (TrieNode node : nodes) {
			Trie currentChild = node.getChild();
			// We remove all the descendants appearing from its child
			if (currentChild != null) {
				currentChild.removeAll();
			}
			// And we make null both its child and pair
			node.setChild(null);
			node.setPair(null);
		}
		/*
		 * Tin correct:
		 */
		for (TrieNode node : nodei) {
			Trie currentChild = node.getChild();
			// We remove all the descendants appearing from its child
			if (currentChild != null) {
				currentChild.removeAll();
			}
			// And we make null both its child and pair
			node.setChild(null);
			node.setPair(null);
		}

		setIdList(null);
		nodes.clear();
		nodei.clear();
		idList = null;
		appearingIn = null;
	}

	/**
	 * It merges a trie with another one, inserting the TrieNode given as parameter
	 * in the list of node associated with the current Trie.
	 * 
	 * @param trie
	 */
	public void mergeWithTrie(TrieNode trie) {
		if (levelSize() == 0) {
			if (nodes == null) {
				nodes = new ArrayList<TrieNode>(1);
			}
		}
		nodes.add(trie);
	}

	/**
	 * It sorts the children by lexicographic order (given by their pair values)
	 */
	public void sort() {
		Collections.sort(nodes);
		Collections.sort(nodei);
	}

	/**
	 * It returns the list of sequences Ids where the pattern referred by the Trie
	 * appears.
	 * 
	 * @return the list of sequence IDs as a bitset
	 */
	public BitSet getAppearingIn() {
		return this.appearingIn;
	}

	/**
	 * It updates the list of sequences Ids where the pattern referred by the Trie
	 * appears
	 * 
	 * @param appearingIn The list of sequence Ids to update
	 */
	public void setAppearingIn(BitSet appearingIn) {
		this.appearingIn = appearingIn;
	}

	/**
	 * Get a string representation of this Trie
	 * 
	 * @return the string representation
	 */
	@Override
	public String toString() {
		if (nodes == null) {
			return "";
		}
		StringBuilder result = new StringBuilder("ID=" + id + "[");
		if (!nodes.isEmpty()) {
			for (TrieNode node : nodes) {
				result.append(node.getPair()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		} else {
			result.append("NULL");
		}
		result.append(']');
		/*
		 * Tin Insert
		 */
		if (nodei == null) {
			return "";
		}
		result.append(", [");
		if (!nodei.isEmpty()) {
			for (TrieNode node : nodei) {
				result.append(node.getPair()).append(',');
			}
			result.deleteCharAt(result.length() - 1);
		} else {
			result.append("NULL");
		}
		result.append(']');

		return result.toString();
	}

	/**
	 * It gets the support of the pattern referred by the Trie.
	 * 
	 * @return the support
	 */
	public int getSupport() {
		if (this.support < 0) {
			this.support = appearingIn.cardinality();
		}
		return this.support;
	}

	/**
	 * It updates the support of the pattern referred by the Trie
	 * 
	 * @param support
	 */
	public void setSupport(int support) {
		this.support = support;
	}

	/**
	 * It gets the sum of the sequence identifiers of the sequences where the
	 * pattern, referred by the Trie, appears
	 * 
	 * @return the sum
	 */
	public int getSumIdSequences() {
		if (sumSequencesIDs < 0) {
			sumSequencesIDs = calculateSumIdSequences();
		}
		return sumSequencesIDs;
	}

	/**
	 * It updates the sum of the sequence identifiers of the sequences where the
	 * pattern, referred by the Trie, appears
	 * 
	 * @param sumIdSequences Value of the sum of sequence identifiers to update
	 */
	public void setSumIdSequences(int sumIdSequences) {
		this.sumSequencesIDs = sumIdSequences;
	}

	/**
	 * It calculates the sum of the sequence identifiers
	 * 
	 * @return
	 */
	private int calculateSumIdSequences() {
		int acum = 0;
		for (int i = appearingIn.nextSetBit(0); i >= 0; i = appearingIn.nextSetBit(i + 1)) {
			acum += i;
		}
		return acum;
	}

	/**
	 * It makes a pre-order traversal from the Trie. The result is concatenate to
	 * the prefix pattern given as parameter
	 * 
	 * @param p Prefix pattern
	 * @return a list of entries <Pattern, Trie>
	 */
	public List<Entry<Pattern, Trie>> preorderTraversal(Pattern p) {
		List<Entry<Pattern, Trie>> result = new ArrayList<Entry<Pattern, Trie>>();
		// If there is any node
		if (nodes != null) {
			for (TrieNode node : nodes) {
				/*
				 * We concatenate the pair component of this child with the previous prefix
				 * pattern, we set its appearances and we add it as a element in the result list
				 */
				Pattern newPattern = PatternCreator.getInstance().concatenate(p, node.getPair());
				Trie child = node.getChild();
				AbstractMap.SimpleEntry newEntry = new AbstractMap.SimpleEntry(newPattern, child);
				result.add(newEntry);
				if (child != null) {
					/*
					 * If the child is not null we make a recursive call with the new pattern
					 */
//                	System.out.println(newPattern);
					List<Entry<Pattern, Trie>> patternsFromChild = child.preorderTraversal(newPattern);
					if (patternsFromChild != null) {
						result.addAll(patternsFromChild);
					}
				}
			}
			/*
			 * Tin correct:
			 */
//            return result;
		}
		/*
		 * else { return null; }
		 */
		if (nodei != null) {
			for (TrieNode node : nodei) {
				/*
				 * We concatenate the pair component of this child with the previous prefix
				 * pattern, we set its appearances and we add it as a element in the result list
				 */
				Pattern newPattern = PatternCreator.getInstance().concatenate(p, node.getPair());
				Trie child = node.getChild();
				AbstractMap.SimpleEntry newEntry = new AbstractMap.SimpleEntry(newPattern, child);
				result.add(newEntry);
				if (child != null) {
					/*
					 * If the child is not null we make a recursive call with the new pattern
					 */
					List<Entry<Pattern, Trie>> patternsFromChild = child.preorderTraversal(newPattern);
					if (patternsFromChild != null) {
						result.addAll(patternsFromChild);
					}
				}
			}
		}
		if (nodes != null || nodei != null)
			return result;
		else
			return null;
	}

	/**
	 * It compares this trie with another
	 * 
	 * @param t the other trie
	 * @return 0 if equal, -1 if smaller, otherwise 1
	 */
	@Override
	public int compareTo(Trie t) {
		return (new Integer(this.id)).compareTo(t.id);
	}
}
