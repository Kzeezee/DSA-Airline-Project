package dsa.airline.ds;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import dsa.airline.model.AirlineReview;
import dsa.airline.model.util.Pair;
import dsa.airline.model.util.TextAnalysisUtils;
import dsa.airline.model.util.WordCount;

public class AirlineAVLImpl implements WordFrequencyAnalyzer {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineAVLImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        // Build AVL trees for good and bad words
        WordFrequencyAVL goodAVL = new WordFrequencyAVL();
        WordFrequencyAVL badAVL = new WordFrequencyAVL();

        int goodTotal = 0;
        int badTotal = 0;

        for (AirlineReview review : reviews) {
            if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                for (String word : review.getTokenizedReview()) {
                    goodAVL.insert(word);
                    goodTotal++;
                }
            } else {
                for (String word : review.getTokenizedReview()) {
                    badAVL.insert(word);
                    badTotal++;
                }
            }
        }

        // Get top 10 from AVL
        List<WordCount> topGood = goodAVL.topK(goodTotal, 10);
        List<WordCount> topBad = badAVL.topK(badTotal, 10);

        return Pair.of(topGood, topBad);
    }

    // Includes height for balancing AVL
    private static class AVLNode {
        String word;
        int count;
        int height; 
        AVLNode left;
        AVLNode right;

        AVLNode(String word) {
            this.word = word;
            this.count = 1;
            this.height = 1; 
            this.left = null;
            this.right = null;
        }
    }


    private static class WordFrequencyAVL {
        private AVLNode root;

        public WordFrequencyAVL() {
            this.root = null;
        }

        // Get height of a node
        private int height(AVLNode node) {
            return node == null ? 0 : node.height;
        }

        // Get balance factor of a node
        private int getBalance(AVLNode node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        // Update height of a node
        private void updateHeight(AVLNode node) {
            if (node != null) {
                node.height = 1 + Math.max(height(node.left), height(node.right));
            }
        }

        // Right rotation (LL case)
        private AVLNode rotateRight(AVLNode y) {
            AVLNode x = y.left;
            AVLNode T2 = x.right;

            // Perform rotation
            x.right = y;
            y.left = T2;

            // Update heights
            updateHeight(y);
            updateHeight(x);

            return x; // New root
        }

        // Left rotation (RR case)
        private AVLNode rotateLeft(AVLNode x) {
            AVLNode y = x.right;
            AVLNode T2 = y.left;

            // Perform rotation
            y.left = x;
            x.right = T2;

            // Update heights
            updateHeight(x);
            updateHeight(y);

            return y; // New root
        }

        // Insert word or increment count if exists, then rebalance
        public void insert(String word) {
            root = insertRec(root, word);
        }

        private AVLNode insertRec(AVLNode node, String word) {
            // Standard BST insertion
            if (node == null) {
                return new AVLNode(word);
            }

            int cmp = word.compareTo(node.word);
            if (cmp < 0) {
                node.left = insertRec(node.left, word);
            } else if (cmp > 0) {
                node.right = insertRec(node.right, word);
            } else {
                // Word already exists, increment count
                node.count++;
                return node;
            }

            // Update height of current node
            updateHeight(node);

            // Get balance factor to check if node became unbalanced
            int balance = getBalance(node);

            // Left-Left Case (LL)
            if (balance > 1 && word.compareTo(node.left.word) < 0) {
                return rotateRight(node);
            }

            // Right-Right Case (RR)
            if (balance < -1 && word.compareTo(node.right.word) > 0) {
                return rotateLeft(node);
            }

            // Left-Right Case (LR)
            if (balance > 1 && word.compareTo(node.left.word) > 0) {
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }

            // Right-Left Case (RL)
            if (balance < -1 && word.compareTo(node.right.word) < 0) {
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }

            return node;
        }

        // Get top K words by count using in-order traversal + min-heap
        public List<WordCount> topK(int totalWords, int k) {
            // Min-heap to keep top K elements
            PriorityQueue<WordCount> minHeap = new PriorityQueue<>(
                    (a, b) -> Integer.compare(a.getCount(), b.getCount()));

            inOrderTraversal(root, totalWords, minHeap, k);

            // Convert heap to list (will be in ascending order, so reverse it)
            List<WordCount> result = new ArrayList<>(minHeap);
            result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));

            return result;
        }

        private void inOrderTraversal(AVLNode node, int totalWords, PriorityQueue<WordCount> minHeap, int k) {
            if (node == null)
                return;

            inOrderTraversal(node.left, totalWords, minHeap, k);

            // Process current node
            WordCount wc = new WordCount(node.word, node.count, totalWords);
            if (minHeap.size() < k) {
                minHeap.offer(wc);
            } else if (node.count > minHeap.peek().getCount()) {
                minHeap.poll();
                minHeap.offer(wc);
            }

            inOrderTraversal(node.right, totalWords, minHeap, k);
        }
    }
}
