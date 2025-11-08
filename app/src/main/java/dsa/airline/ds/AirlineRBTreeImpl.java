package dsa.airline.ds;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import dsa.airline.model.AirlineReview;
import dsa.airline.model.util.Pair;
import dsa.airline.model.util.TextAnalysisUtils;
import dsa.airline.model.util.WordCount;

public class AirlineRBTreeImpl implements WordFrequencyAnalyzer {
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

        // Get top 10 directly from RB trees via heap-based traversal (no ArrayList build)
        List<WordCount> topGood = goodRBTree.topK(goodTotal, 10);
        List<WordCount> topBad = badRBTree.topK(badTotal, 10);

        return Pair.of(topGood, topBad);
    }

    // Top-K moved into WordFrequencyRBTree to avoid materializing full list

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

        // Heap-based Top-K without building a full list
        public List<WordCount> topK(int totalWords, int k) {
            PriorityQueue<WordCount> minHeap = new PriorityQueue<>(k, (a, b) -> Integer.compare(a.getCount(), b.getCount()));
            inOrderCollectTopK(root, minHeap, totalWords, k);
            ArrayList<WordCount> result = new ArrayList<>(minHeap);
            result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
            return result;
        }

        private void inOrderCollectTopK(RBNode node, PriorityQueue<WordCount> heap, int totalWords, int k) {
            if (node == null) return;
            inOrderCollectTopK(node.left, heap, totalWords, k);
            WordCount wc = new WordCount(node.word, node.count, totalWords);
            if (heap.size() < k) {
                heap.offer(wc);
            } else if (!heap.isEmpty() && wc.getCount() > heap.peek().getCount()) {
                heap.poll();
                heap.offer(wc);
            }
            inOrderCollectTopK(node.right, heap, totalWords, k);
        }
    }
}
