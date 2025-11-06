# Binary Search Tree (BST) Implementation for Airline Review Analysis

## 📋 Table of Contents
1. [Overview](#overview)
2. [Why BST for Word Frequency?](#why-bst-for-word-frequency)
3. [How It Works](#how-it-works)
4. [Code Structure](#code-structure)
5. [Algorithm Explanation](#algorithm-explanation)
6. [Time & Space Complexity](#time--space-complexity)
7. [Comparison with ArrayList](#comparison-with-arraylist)
8. [Usage Examples](#usage-examples)
9. [Future Improvements](#future-improvements)

---

## Overview

The BST implementation (`AirlineBSTImpl.java`) provides an alternative approach to counting word frequencies in airline reviews. Instead of collecting all words in an ArrayList and then sorting/counting, we use a Binary Search Tree to:
- **Automatically maintain sorted order** during insertion
- **Count word occurrences** on-the-fly
- **Reduce memory usage** by storing only unique words

---

## Why BST for Word Frequency?

### Problem Statement
Given airline reviews, we need to:
1. Separate reviews into "good" (recommended) and "bad" (not recommended)
2. Count word frequencies in each category
3. Find top 10 most common words
4. Filter out words that appear similarly in both categories

### Why BST is a Good Fit
```
Traditional ArrayList Approach:
  Reviews → Collect ALL words → Sort → Count runs → Filter → Top 10
  Time: O(n) + O(n log n) + O(n) = O(n log n)
  Space: O(n) for all words including duplicates

BST Approach:
  Reviews → Insert into BST (auto-sort + count) → Traverse → Filter → Top 10
  Time: O(n log n) average for insertions + O(n) traversal = O(n log n)
  Space: O(unique words) - more memory efficient!
```

**Key Advantages:**
- ✅ No separate sorting step needed
- ✅ Counts duplicates during insertion
- ✅ Less memory (stores only unique words)
- ✅ Natural alphabetical ordering

---

## How It Works

### Step-by-Step Process

#### 1. Build Separate BSTs for Good/Bad Reviews
```java
WordFrequencyBST goodBST = new WordFrequencyBST();
WordFrequencyBST badBST = new WordFrequencyBST();

for (AirlineReview review : reviews) {
    if (isPositive(review)) {
        for (String word : review.getTokenizedReview()) {
            goodBST.insert(word);  // Inserts or increments count
        }
    } else {
        for (String word : review.getTokenizedReview()) {
            badBST.insert(word);
        }
    }
}
```

#### 2. BST Structure Example
```
Words inserted: ["flight", "delay", "good", "flight", "crew"]

Resulting BST:
           flight(2)
          /         \
      delay(1)     good(1)
                       \
                      crew(1)

In-order traversal: crew(1), delay(1), flight(2), good(1)
                    ↑ Alphabetically sorted!
```

#### 3. Convert BST to Sorted List
```java
List<WordCount> goodCounts = goodBST.toWordCountList(totalGoodWords);
// Returns: [WordCount("crew", 1), WordCount("delay", 1), 
//           WordCount("flight", 2), WordCount("good", 1)]
```

#### 4. Filter and Get Top 10
Uses existing `TextAnalysisUtils` methods:
- `filterNearCommonDominant()` - Removes words common to both good/bad reviews
- `takeTopK()` - Sorts by count and takes top 10

---

## Code Structure

### File: `AirlineBSTImpl.java`

```
AirlineBSTImpl (Main class)
│
├── Constructor
│   └── Stores airlineReviews HashMap and target airline
│
├── getTop10MostCommonWords()
│   ├── Build goodBST and badBST
│   ├── Convert BSTs to WordCount lists
│   ├── Filter common words
│   └── Return top 10 for each category
│
└── Inner Classes
    │
    ├── BSTNode
    │   ├── String word
    │   ├── int count
    │   ├── BSTNode left
    │   └── BSTNode right
    │
    └── WordFrequencyBST
        ├── BSTNode root
        │
        ├── insert(String word)
        │   └── insertRec(node, word) - Recursive insert
        │
        └── toWordCountList(int totalWords)
            └── inOrderTraversal() - Returns sorted list
```

---

## Algorithm Explanation

### 1. Insert Algorithm (Recursive)

**Purpose:** Add a word to the BST or increment its count if it exists

```java
private BSTNode insertRec(BSTNode node, String word) {
    // Base case: empty spot found, create new node
    if (node == null) {
        return new BSTNode(word);  // count = 1
    }
    
    // Compare word with current node
    int cmp = word.compareTo(node.word);
    
    if (cmp < 0) {
        // Word is "smaller" alphabetically, go left
        node.left = insertRec(node.left, word);
    } else if (cmp > 0) {
        // Word is "larger" alphabetically, go right
        node.right = insertRec(node.right, word);
    } else {
        // Word matches! Increment count
        node.count++;
    }
    
    return node;
}
```

**Example Trace:**
```
Inserting "delay" into tree with root "flight":

1. Compare "delay" vs "flight"
   "delay" < "flight" → go left
   
2. Left child is null
   Create new BSTNode("delay", count=1)
   
3. Return and attach to flight.left

Result:
    flight(1)
    /
delay(1)
```

---

### 2. In-Order Traversal (Recursive)

**Purpose:** Visit all nodes in alphabetical order and build WordCount list

```java
private void inOrderTraversal(BSTNode node, List<WordCount> result, int totalWords) {
    if (node == null) {
        return;  // Base case
    }
    
    // Recursive pattern: LEFT → ROOT → RIGHT
    inOrderTraversal(node.left, result, totalWords);    // 1. Visit left subtree
    result.add(new WordCount(node.word, node.count, totalWords));  // 2. Process current
    inOrderTraversal(node.right, result, totalWords);   // 3. Visit right subtree
}
```

**Example Trace:**
```
Tree:
       flight(2)
      /         \
  delay(1)     good(1)

Traversal order:
1. Visit flight → go left
2. Visit delay → go left (null) → process delay → go right (null)
3. Back to flight → process flight → go right
4. Visit good → go left (null) → process good → go right (null)

Result: [delay(1), flight(2), good(1)]  ← Sorted!
```

---

## Time & Space Complexity

### BST Operations

| Operation | Average Case | Worst Case | Best Case |
|-----------|-------------|------------|-----------|
| **Insert** | O(log n) | O(n) | O(log n) |
| **Search** | O(log n) | O(n) | O(1) |
| **Traversal** | O(n) | O(n) | O(n) |

**Note:** Worst case O(n) occurs when tree becomes unbalanced (like a linked list)

### Overall Algorithm Complexity

```
n = total number of words
u = unique words

1. Building BST: O(n log u) average
   - Insert n words
   - Each insert is O(log u) for u unique words

2. In-order traversal: O(u)
   - Visit each unique word once

3. Filter & Top-K: O(u log u)
   - Sorting by frequency

Total: O(n log u + u log u) = O(n log u) assuming u << n
```

### Space Complexity

```
ArrayList: O(n) - stores all words including duplicates
BST: O(u) - stores only unique words

Example:
- 10,000 words total
- 1,000 unique words
- ArrayList: 10,000 slots
- BST: 1,000 nodes ← 90% less memory!
```

---

## Comparison with ArrayList

| Aspect | ArrayList | BST |
|--------|-----------|-----|
| **Data Collection** | Store all words (duplicates) | Store unique words + counts |
| **Sorting** | Explicit `Collections.sort()` | Implicit via tree structure |
| **Counting** | After sorting, count runs | During insertion |
| **Memory** | O(n) all words | O(u) unique words |
| **Insert Time** | O(1) append | O(log n) average |
| **Overall Time** | O(n log n) | O(n log n) average |
| **Code Complexity** | Simpler | More complex (recursion) |
| **Best For** | Small datasets, simplicity | Large datasets, memory efficiency |

### Performance Example

```
Dataset: 100,000 review words, 5,000 unique

ArrayList:
- Memory: 100,000 strings
- Sort time: ~1,600,000 comparisons

BST:
- Memory: 5,000 nodes
- Insert time: ~850,000 comparisons (average)
- Memory savings: 95%
```

---

## Usage Examples

### Basic Usage

```java
// In Main.java
public static void bstImplementationTest(
    HashMap<String, List<AirlineReview>> airlineReviews, 
    String airline
) {
    // Create BST implementation
    AirlineBSTImpl bstImpl = new AirlineBSTImpl(airlineReviews, "\"" + airline + "\"");
    
    // Get top 10 words
    Pair<List<WordCount>, List<WordCount>> results = bstImpl.getTop10MostCommonWords();
    
    // Display good review words
    System.out.println("Top 10 words in GOOD reviews (BST):");
    for (WordCount wc : results.getLeft()) {
        System.out.println(wc);
    }
    
    // Display bad review words
    System.out.println("\nTop 10 words in BAD reviews (BST):");
    for (WordCount wc : results.getRight()) {
        System.out.println(wc);
    }
}

// Call in main()
bstImplementationTest(airlineReviews, "spirit-airlines");
```

### Expected Output

```
Top 10 words in GOOD reviews (BST):
Word: flight, Count: 245, Percent: 0.0523
Word: servic, Count: 198, Percent: 0.0423
Word: crew, Count: 156, Percent: 0.0333
...

Top 10 words in BAD reviews (BST):
Word: delay, Count: 312, Percent: 0.0687
Word: late, Count: 289, Percent: 0.0636
Word: cancel, Count: 234, Percent: 0.0515
...
```

---

## Future Improvements

### 1. Self-Balancing BST
**Problem:** Current BST can become unbalanced (O(n) worst case)

**Solution:** Implement AVL Tree or Red-Black Tree
```java
// Add balance factor to BSTNode
class BSTNode {
    String word;
    int count;
    int height;  // ← For AVL balancing
    BSTNode left, right;
}
```

### 2. Parallel Processing
**Idea:** Build good/bad BSTs concurrently
```java
ExecutorService executor = Executors.newFixedThreadPool(2);
Future<WordFrequencyBST> goodFuture = executor.submit(() -> buildBST(goodReviews));
Future<WordFrequencyBST> badFuture = executor.submit(() -> buildBST(badReviews));
```

### 3. Persistent Storage
**Idea:** Serialize BST to disk for reuse
```java
// Save BST structure
void saveBST(String filename) {
    // Serialize tree nodes
}

// Load pre-built BST
WordFrequencyBST loadBST(String filename) {
    // Deserialize and reconstruct
}
```

### 4. Additional Tree Operations
```java
// Find word frequency without full traversal
int getWordFrequency(String word);

// Get all words with frequency > threshold
List<WordCount> getWordsAboveThreshold(int minCount);

// Delete rare words (count < threshold)
void pruneRareWords(int threshold);
```

---

## Testing Recommendations

### Unit Tests
```java
@Test
public void testBSTInsertAndCount() {
    WordFrequencyBST bst = new WordFrequencyBST();
    bst.insert("flight");
    bst.insert("delay");
    bst.insert("flight");
    
    List<WordCount> result = bst.toWordCountList(3);
    
    assertEquals(2, result.size());
    assertEquals("delay", result.get(0).getWord());
    assertEquals(1, result.get(0).getCount());
    assertEquals("flight", result.get(1).getWord());
    assertEquals(2, result.get(1).getCount());
}
```

### Integration Tests
```java
@Test
public void testBSTvsArrayListConsistency() {
    // Both implementations should produce same top 10 words
    AirlineArrayListImpl arrayListImpl = new AirlineArrayListImpl(...);
    AirlineBSTImpl bstImpl = new AirlineBSTImpl(...);
    
    Pair<List<WordCount>, List<WordCount>> arrayResults = arrayListImpl.getTop10MostCommonWords();
    Pair<List<WordCount>, List<WordCount>> bstResults = bstImpl.getTop10MostCommonWords();
    
    // Compare results
    assertListEquals(arrayResults.getLeft(), bstResults.getLeft());
    assertListEquals(arrayResults.getRight(), bstResults.getRight());
}
```

---

## Contributors
- [Your Name] - Initial BST implementation
- [Team Members] - Testing and optimization

## References
- [Introduction to Algorithms (CLRS)](https://mitpress.mit.edu/books/introduction-algorithms)
- [BST Visualization](https://www.cs.usfca.edu/~galles/visualization/BST.html)
- Project repository: [Link to your repo]

---

**Last Updated:** [Date]
**Version:** 1.0
