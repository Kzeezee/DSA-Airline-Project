# Priority Queue Implementation - Airline Review Analysis

## Overview
This implementation uses a **Priority Queue (Min-Heap)** data structure to efficiently find the top K most frequent words in airline reviews, separated by good reviews (recommended) and bad reviews (not recommended).

---

## What is a Priority Queue?

A **Priority Queue** is like a special waiting line where items aren't served in the order they arrive, but based on their **priority**. 

## Why Use Priority Queue Instead of Just Sorting?

**The Problem:**
- We have 40,000+ reviews with thousands of unique words
- We only need the **top 10** most frequent words
- Sorting ALL words would be slow and wasteful

**The Solution - Priority Queue:**
- Only keeps track of the top K items (K=10 in our case)
- **Much faster:** O(N log K) instead of O(N log N)
- **Less memory:** Only stores K items instead of all N items

**Speed Comparison:**
For 100,000 words and K=10:
- Full sorting: 100,000 × log(100,000) ≈ **1,660,000 operations** 😰
- Priority Queue: 100,000 × log(10) ≈ **330,000 operations** 🚀
- **Result: About 5x faster!**

---

## How It Works (Step-by-Step)

### Step 1: Count Word Frequencies
First, we count how many times each word appears using a **HashMap**:

```
Input words: ["flight", "good", "flight", "bad", "good", "flight"]

HashMap result:
- "flight" → 3
- "good" → 2
- "bad" → 1
```

### Step 2: Use Min-Heap to Find Top K Words
We use a **Min-Heap** (smallest value stays on top) with a clever strategy:

**The Strategy:**
1. Create a heap that holds a maximum of K items (K=10)
2. For each word and its frequency:
   - Add it to the heap
   - If heap size exceeds K, **remove the smallest** frequency
3. What's left? **Only the K largest frequencies!**

**Visual Example (Using K=3 for simplicity):**

```
Processing words one by one:

Step 1: Add "flight" (3)
   Heap: [3]

Step 2: Add "good" (2)
   Heap: [2, 3]

Step 3: Add "bad" (1)
   Heap: [1, 2, 3]

Step 4: Add "seat" (5)
   Heap: [1, 2, 3, 5] → Too big! Remove smallest (1)
   Heap: [2, 3, 5] ✓

Step 5: Add "crew" (4)
   Heap: [2, 3, 5, 4] → Too big! Remove smallest (2)
   Heap: [3, 4, 5] ✓

Final Result: Top 3 words with frequencies [5, 4, 3]
```

### Step 3: Sort and Return
The heap contains our top K items, but not in perfect order. We sort them descending:
```
Heap: [3, 4, 5] → Sorted: [5, 4, 3] (highest to lowest)
```

---

## What Was Implemented

### 1. **WordFrequencyCounter.java** - The Main Tool
A new utility class that provides:

- **`getTopKWords()`** - The core Priority Queue algorithm
  - Uses a Min-Heap to find top K frequent words efficiently
  - Time Complexity: O(N log K) where N = total words, K = 10
  - Space Complexity: O(N) for the word frequency map
  
- **`getGoodReviews()`** - Filters reviews where recommended = "1"
- **`getBadReviews()`** - Filters reviews where recommended = "0"
- **`extractWordsFromReviews()`** - Extracts all tokenized words from reviews
- **`removeCommonWords()`** - Implements nullity check (removes common words)
- **`getWordSet()`** - Converts word frequency list to a set
- **`printWordFrequencies()`** - Pretty-prints results in formatted tables

### 2. **Updated Main.java** - Integration
Connected everything together to:
1. Separate good and bad reviews based on "recommended" field
2. Extract all words from each category
3. Use Priority Queue to find top 10 words for each
4. Perform nullity check (remove words appearing in both lists)
5. Display clean before/after results

---

## The Process in Our Code

Here's what happens when you run the program:

```java
// 1. Read and tokenize all reviews from CSV
tokenizedReviews = read airline.csv and process with Lucene analyzer

// 2. Separate by recommendation status
goodReviews = reviews where recommended = "1"  
badReviews = reviews where recommended = "0"

// 3. Extract all words from each category
goodWords = ["flight", "seat", "good", "very", "service", ...]  // thousands of words
badWords = ["flight", "delay", "bad", "hour", "had", ...]      // thousands of words

// 4. Use Priority Queue to find top 10
topGoodWords = getTopKWords(goodWords, 10)  // Uses Min-Heap magic! ✨
topBadWords = getTopKWords(badWords, 10)    // Uses Min-Heap magic! ✨

// 5. Find common words (nullity check)
commonWords = words appearing in BOTH top 10 lists
// Example: ["flight", "seat", "were", "i", "from", "we"]

// 6. Remove common words for cleaner results
filteredGoodWords = remove commonWords from topGoodWords
filteredBadWords = remove commonWords from topBadWords
```

---

## The "Nullity Check" Explained Simply

After finding the top 10 words for good and bad reviews, we notice something:

**Problem:** Some words appear in BOTH lists!

```
Good Reviews Top 10:  [flight, veri, good, seat, servic, were, i, ...]
Bad Reviews Top 10:   [flight, had, seat, my, hour, were, i, ...]
                       ^^^^^^       ^^^^        ^^^^  ^  
                       These appear in both! ⚠️
```

**Why is this a problem?**
- Words like "flight", "seat", "i", "were" appear everywhere
- They're generic travel words - they don't tell us if a review is **good or bad**
- They're noise! 📢

**Solution - Nullity Check:**
Remove words that appear in both top 10 lists:

```
Common words to remove: [flight, seat, were, i, from, we]

After filtering:
✅ Good: [veri, good, time, servic]     ← These indicate POSITIVE reviews
✅ Bad:  [had, my, have, hour]          ← These indicate NEGATIVE reviews
```

Now we have **meaningful indicator words** that actually distinguish good from bad experiences!

---

## Results from Our Airline Dataset

### Dataset Statistics:
- 📊 **Total Reviews Analyzed**: 41,397
- ✅ **Good Reviews** (recommended = "1"): 22,098
- ❌ **Bad Reviews** (recommended = "0"): 19,298

### Before Nullity Check:
**Good Reviews Top 10:**
1. flight - 40,878 times
2. i - 32,589 times
3. were - 23,424 times
4. seat - 20,707 times
5. veri (very) - 17,755 times
6. good - 16,447 times
7. time - 14,286 times
8. from - 14,226 times
9. servic (service) - 13,861 times
10. we - 13,599 times

**Bad Reviews Top 10:**
1. flight - 45,355 times
2. i - 43,808 times
3. we - 29,255 times
4. were - 24,862 times
5. seat - 19,701 times
6. had - 17,593 times
7. from - 17,049 times
8. my - 17,009 times
9. have - 15,405 times
10. hour - 14,269 times

**⚠️ Common words found:** flight, seat, were, i, from, we

---

### After Nullity Check (Final Results):

**✅ Good Review Indicator Words:**
1. **veri** (very) - 17,755 occurrences
   - People describe good experiences enthusiastically
2. **good** - 16,447 occurrences
   - Direct positive feedback
3. **time** - 14,286 occurrences
   - On-time flights matter to satisfied customers
4. **servic** (service) - 13,861 occurrences
   - Good service = happy customers

**❌ Bad Review Indicator Words:**
1. **had** - 17,593 occurrences
   - Past tense complaints: "we had to wait...", "I had a problem..."
2. **my** - 17,009 occurrences
   - Personal negative experiences: "my flight", "my bag"
3. **have** - 15,405 occurrences
   - Unmet expectations: "should have", "didn't have"
4. **hour** - 14,269 occurrences
   - Delays and waiting time complaints

### What This Tells Us:
- 😊 **Good reviews** focus on quality ("very good"), service, and timeliness
- 😠 **Bad reviews** focus on problems ("had"), personal issues, and time delays
- 🎯 Airlines can use this to understand what makes customers happy or upset!

---

## Technical Deep Dive - How Priority Queue Works

The Priority Queue implementation uses a **Min-Heap** approach. Here's why:

### Why Min-Heap? 🤔
- A **Min-Heap** keeps the **smallest** value at the top
- We use it "backwards" - keep the K largest by removing the smallest!
- When heap size exceeds K, we kick out the smallest frequency
- What remains? Only the top K frequencies! 🎯

### The Algorithm Breakdown:

**Step 1: Count Frequencies**
```
Input: All words from reviews
Output: HashMap of word → frequency

Example:
"flight" → 500
"good" → 300
"seat" → 250
"service" → 400
...
```

**Step 2: Build Min-Heap with K Elements**
```
For each word-frequency pair:
  1. Add to heap
  2. If heap.size > K (10):
     - Remove smallest (heap.poll())
  3. Continue

Example with K=3:
Add "flight"(500):   [500]
Add "good"(300):     [300, 500]
Add "seat"(250):     [250, 300, 500]
Add "service"(400):  [250, 300, 500, 400] → Remove 250 → [300, 400, 500]
Add "bad"(50):       [300, 400, 500, 50] → Remove 50 → [300, 400, 500]

Final heap contains top 3!
```

**Step 3: Extract and Sort**
```
Heap: [300, 400, 500]
Sorted descending: [500, 400, 300]
```

### Time Complexity Analysis:

| Operation | Complexity | Explanation |
|-----------|-----------|-------------|
| Count frequencies | O(N) | Visit each word once |
| Add to heap | O(log K) | Heap insertion |
| Remove from heap | O(log K) | Heap deletion |
| Process all N words | O(N log K) | N words × log K operations |
| Sort K items | O(K log K) | Sort final results |
| **Total** | **O(N log K)** | Dominated by processing |

**For our data:**
- N ≈ 100,000 words
- K = 10
- O(N log K) ≈ 100,000 × 3.3 ≈ 330,000 operations

**Compare to full sorting:**
- O(N log N) ≈ 100,000 × 16.6 ≈ 1,660,000 operations
- **Priority Queue is 5x faster!** 🚀

---

## Advantages of Priority Queue Approach

### 1. **Speed - Blazing Fast! 🚀**
- **O(N log K)** vs **O(N log N)** for full sorting
- When K is small (like 10), this is significantly faster
- For 100,000 words: ~5x speedup!

### 2. **Memory Efficient 💾**
- Heap only maintains K elements (10 items)
- HashMap stores unique words (necessary for counting)
- Doesn't need to sort the entire dataset

### 3. **Scalable 📈**
- Performance scales with K, not N
- Adding more reviews (increasing N) has minimal impact
- Perfect for big data scenarios

### 4. **Clean & Maintainable Code 🧹**
- Clear separation of concerns
- Each method has a single responsibility
- Easy to test and debug

### 5. **Flexible 🔧**
- Easy to change K (top 5, top 20, etc.)
- Can reuse for different analyses
- Works for any frequency counting problem

---

## Real-World Applications

This same Priority Queue approach can be used for:
- 🔍 **Search engines**: Finding most relevant documents
- 📱 **Social media**: Trending hashtags
- 🛒 **E-commerce**: Best-selling products
- 📊 **Analytics**: Top performing content
- 🎵 **Music apps**: Most played songs

---

## Next Steps for Your Team

Other team members can implement the same word frequency analysis using different data structures and compare results!

### Alternative Data Structures to Try:

#### 1. **HashMap + ArrayList** (Simple Approach)
- Count with HashMap (like we do)
- Add all entries to ArrayList
- Sort the entire ArrayList
- Take first K elements
- **Pros:** Simple to understand and implement
- **Cons:** O(N log N) - slower for large datasets
- **Good for:** Learning basics, small datasets

#### 2. **TreeMap** (Auto-Sorted)
- TreeMap keeps keys sorted automatically
- Can iterate in sorted order
- **Pros:** Always sorted, clean iteration
- **Cons:** O(N log N) insertions, overkill for top-K
- **Good for:** When you need full sorted data

#### 3. **Binary Search Tree (BST)** (Custom Implementation)
- Build your own BST with frequency as key
- Practice tree operations (insert, traverse)
- **Pros:** Great learning experience
- **Cons:** More code, can become unbalanced
- **Good for:** Understanding tree structures

#### 4. **Trie** (Prefix Tree)
- Store words as paths in tree
- Each node has frequency count
- **Pros:** Good for prefix searches
- **Cons:** Complex for this use case
- **Good for:** Autocomplete, prefix matching

#### 5. **Heap Sort with Max-Heap**
- Build max-heap with all frequencies
- Extract top K elements
- **Pros:** Similar to priority queue
- **Cons:** Similar complexity
- **Good for:** Understanding heap operations

### Comparison Criteria:

When implementing, compare:
- ⏱️ **Time Complexity**: How does it scale with data size?
- 💾 **Space Complexity**: How much memory does it use?
- 📝 **Code Simplicity**: How easy is it to write and understand?
- 🐛 **Debugging**: How easy is it to find and fix bugs?
- 🚀 **Performance**: Actual runtime on our 40K+ review dataset

### Running Your Implementation:

1. Create your own class (e.g., `WordFrequencyArrayList.java`)
2. Implement the same methods as `WordFrequencyCounter`
3. Update `Main.java` to call your implementation
4. Compare results and runtime!

---

## Key Takeaways 🎓

### What You Learned:
1. **Priority Queue** is a smart tool for finding top-K items efficiently
2. **Min-Heap strategy** keeps the K largest by removing smallest
3. **Time matters**: O(N log K) beats O(N log N) for top-K problems
4. **Nullity check** removes noise to find meaningful patterns
5. **Real results**: Words that distinguish good from bad airline experiences

### Why This Matters:
- 💼 **Industry use**: Same algorithms power Google, Amazon, Netflix recommendations
- 🧠 **Interview prep**: Top-K problems are common in coding interviews
- 📊 **Data science**: Foundation for text mining and sentiment analysis
- 🎯 **Problem solving**: Learn to choose the right data structure for the job

### The Bottom Line:
You now have a **working, efficient implementation** that:
- ✅ Processes 40,000+ reviews in seconds
- ✅ Finds meaningful patterns in text data
- ✅ Uses optimal algorithms (O(N log K))
- ✅ Produces actionable insights for airlines

**Great job on completing the Priority Queue implementation! 🎉**

---

## Questions to Think About 🤔

1. What happens if K = N (all words)? Would Priority Queue still be faster?
2. Could we parallelize this to make it even faster?
3. What if we wanted top words per airline instead of overall?
4. How would this change for real-time streaming data?
5. Could machine learning improve on this word-based approach?

Discuss these with your team! 💬
