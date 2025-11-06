package org.example.ds;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.TextAnalysisUtils;
import org.example.model.util.WordCount;

public class AirlineRBTreeImpl {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineRBTreeImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        // Build Red-Black Trees for good and bad words
        WordFrequencyRBTree goodRBTree = new WordFrequencyRBTree();
        WordFrequencyRBTree badRBTree = new WordFrequencyRBTree();

        int goodTotal = 0;
        int badTotal = 0;

        for (AirlineReview review : reviews) {
            if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                for (String word : review.getTokenizedReview()) {
                    goodRBTree.insert(word);
                    goodTotal++;
                }
            } else {
                for (String word : review.getTokenizedReview()) {
                    badRBTree.insert(word);
                    badTotal++;
                }
            }
        }

        // Convert Red-Black Tree to sorted lists
        List<WordCount> goodCounts = goodRBTree.toWordCountList(goodTotal);
        List<WordCount> badCounts = badRBTree.toWordCountList(badTotal);

        // Filter near-common dominant words
        ArrayList<WordCount> filteredGood = new ArrayList<>();
        ArrayList<WordCount> filteredBad = new ArrayList<>();
        TextAnalysisUtils.filterNearCommonDominant(goodCounts, badCounts, 0.30, filteredGood, filteredBad);

        // Get top 10
        List<WordCount> topGood = TextAnalysisUtils.takeTopK(filteredGood, 10);
        List<WordCount> topBad = TextAnalysisUtils.takeTopK(filteredBad, 10);

        return Pair.of(topGood, topBad);
    }

    // Inner class: Red-Black Tree Node
    private static class RBNode {
        String word;
        int count;
        RBNode left;
        RBNode right;
        RBNode parent;
        boolean isRed;  // true = RED, false = BLACK

        RBNode(String word) {
            this.word = word;
            this.count = 1;
            this.left = null;
            this.right = null;
            this.parent = null;
            this.isRed = true;  // New nodes are always RED
        }
    }

    // Inner class: Word Frequency Red-Black Tree
    private static class WordFrequencyRBTree {
        private RBNode root;

        public WordFrequencyRBTree() {
            this.root = null;
        }

        // Insert word or increment count if exists
        public void insert(String word) {
            RBNode newNode = insertBST(word);
            if (newNode != null) {
                fixInsert(newNode);
            }
        }

        // Standard BST insert, returns new node if created
        private RBNode insertBST(String word) {
            if (root == null) {
                root = new RBNode(word);
                root.isRed = false;  // Root is always BLACK
                return null;
            }

            RBNode current = root;
            RBNode parent = null;

            while (current != null) {
                parent = current;
                int cmp = word.compareTo(current.word);
                
                if (cmp < 0) {
                    current = current.left;
                } else if (cmp > 0) {
                    current = current.right;
                } else {
                    // Word already exists, increment count
                    current.count++;
                    return null;
                }
            }

            // Create new node
            RBNode newNode = new RBNode(word);
            newNode.parent = parent;

            int cmp = word.compareTo(parent.word);
            if (cmp < 0) {
                parent.left = newNode;
            } else {
                parent.right = newNode;
            }

            return newNode;
        }

        // Fix Red-Black Tree properties after insertion
        private void fixInsert(RBNode node) {
            while (node != root && node.parent.isRed) {
                RBNode parent = node.parent;
                RBNode grandparent = parent.parent;

                if (parent == grandparent.left) {
                    RBNode uncle = grandparent.right;

                    // Case 1: Uncle is RED - recolor
                    if (uncle != null && uncle.isRed) {
                        parent.isRed = false;
                        uncle.isRed = false;
                        grandparent.isRed = true;
                        node = grandparent;
                    } else {
                        // Case 2: Node is right child - left rotate
                        if (node == parent.right) {
                            node = parent;
                            rotateLeft(node);
                            parent = node.parent;
                        }
                        // Case 3: Node is left child - right rotate
                        parent.isRed = false;
                        grandparent.isRed = true;
                        rotateRight(grandparent);
                    }
                } else {
                    RBNode uncle = grandparent.left;

                    // Case 1: Uncle is RED - recolor
                    if (uncle != null && uncle.isRed) {
                        parent.isRed = false;
                        uncle.isRed = false;
                        grandparent.isRed = true;
                        node = grandparent;
                    } else {
                        // Case 2: Node is left child - right rotate
                        if (node == parent.left) {
                            node = parent;
                            rotateRight(node);
                            parent = node.parent;
                        }
                        // Case 3: Node is right child - left rotate
                        parent.isRed = false;
                        grandparent.isRed = true;
                        rotateLeft(grandparent);
                    }
                }
            }
            root.isRed = false;  // Root is always BLACK
        }

        // Left rotation
        private void rotateLeft(RBNode node) {
            RBNode rightChild = node.right;
            node.right = rightChild.left;

            if (rightChild.left != null) {
                rightChild.left.parent = node;
            }

            rightChild.parent = node.parent;

            if (node.parent == null) {
                root = rightChild;
            } else if (node == node.parent.left) {
                node.parent.left = rightChild;
            } else {
                node.parent.right = rightChild;
            }

            rightChild.left = node;
            node.parent = rightChild;
        }

        // Right rotation
        private void rotateRight(RBNode node) {
            RBNode leftChild = node.left;
            node.left = leftChild.right;

            if (leftChild.right != null) {
                leftChild.right.parent = node;
            }

            leftChild.parent = node.parent;

            if (node.parent == null) {
                root = leftChild;
            } else if (node == node.parent.right) {
                node.parent.right = leftChild;
            } else {
                node.parent.left = leftChild;
            }

            leftChild.right = node;
            node.parent = leftChild;
        }

        // In-order traversal to get sorted list of WordCount objects
        public List<WordCount> toWordCountList(int totalWords) {
            List<WordCount> result = new ArrayList<>();
            inOrderTraversal(root, result, totalWords);
            return result;
        }

        private void inOrderTraversal(RBNode node, List<WordCount> result, int totalWords) {
            if (node == null) {
                return;
            }

            inOrderTraversal(node.left, result, totalWords);
            result.add(new WordCount(node.word, node.count, totalWords));
            inOrderTraversal(node.right, result, totalWords);
        }
    }
}
