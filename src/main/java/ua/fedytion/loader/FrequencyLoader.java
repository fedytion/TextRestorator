package main.java.ua.fedytion.loader;

import java.io.*;
import java.util.*;

public class FrequencyLoader {

    public static Map<String, Long> loadUnigramFrequencies(String filepath) throws IOException {
        Map<String, Long> frequencies = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    frequencies.put(parts[0].toLowerCase(), Long.parseLong(parts[1]));
                }
            }
        }
        return frequencies;
    }

    public static Map<String, Map<String, Long>> loadBigramFrequencies(String filepath) throws IOException {
        Map<String, Map<String, Long>> bigrams = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 3) {
                    String first = parts[0].toLowerCase();
                    String second = parts[1].toLowerCase();
                    long count = Long.parseLong(parts[2]);

                    bigrams.computeIfAbsent(first, k -> new HashMap<>()).put(second, count);
                }
            }
        }
        return bigrams;
    }
}