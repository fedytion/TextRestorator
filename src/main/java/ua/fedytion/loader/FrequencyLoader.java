package main.java.ua.fedytion.loader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class FrequencyLoader {
    private final Map<String, Long> freqMap = new HashMap<>();

    public void load(String fileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName))))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String word = parts[0].toLowerCase();
                    long freq = Long.parseLong(parts[1]);
                    freqMap.put(word, freq);
                }
            }

            System.out.println("Frequencies loaded: " + freqMap.size() + " entries");

        } catch (Exception e) {
            System.err.println("Failed to load frequencies: " + e.getMessage());
        }
    }

    public long getFrequency(String word) {
        return freqMap.getOrDefault(word.toLowerCase(), 1L);
    }
}
