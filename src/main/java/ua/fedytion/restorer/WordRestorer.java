package main.java.ua.fedytion.restorer;

import java.util.*;

public class WordRestorer {
    private final Set<String> dictionary;
    private final Map<String, Long> unigramFreq;

    public WordRestorer(Set<String> dictionary, Map<String, Long> unigramFreq) {
        this.dictionary = dictionary;
        this.unigramFreq = unigramFreq;
    }

    public List<String> restoreCandidates(String damagedWord) {
        int length = damagedWord.length();
        List<String> candidates = new ArrayList<>();

        for (String word : dictionary) {
            if (word.length() != length) continue;

            if (word.length() < 3) continue;
            if (unigramFreq.getOrDefault(word, 0L) < 10) continue;

            if (matchesWithMissingAndShuffled(damagedWord, word)) {
                candidates.add(word);
            }
        }

        return candidates;
    }

    private boolean matchesWithMissingAndShuffled(String pattern, String candidate) {
        // Перевірка на однакову довжину (з урахуванням зірочок)
        if (pattern.length() != candidate.length()) return false;

        int[] counts = new int[26];
        for (char c : candidate.toCharArray()) {
            if (!Character.isLetter(c)) return false;
            int idx = Character.toLowerCase(c) - 'a';
            if (idx < 0 || idx >= 26) return false;
            counts[idx]++;
        }

        for (char c : pattern.toCharArray()) {
            if (c == '*') continue;
            if (!Character.isLetter(c)) return false;
            int idx = Character.toLowerCase(c) - 'a';
            if (idx < 0 || idx >= 26) return false;
            counts[idx]--;
        }

        for (int count : counts) {
            if (count < 0) return false;
        }

        return true;
    }
}
