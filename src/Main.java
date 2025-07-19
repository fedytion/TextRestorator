import main.java.ua.fedytion.loader.DictionaryLoader;
import main.java.ua.fedytion.restorer.WordRestorer;

import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Set<String> dictionary = DictionaryLoader.loadDictionary("resources/words_alpha.txt");

        WordRestorer restorer = new WordRestorer(dictionary);

        List<String> restored = restorer.restoreCandidates("pic*u*e");
        System.out.println("Candidates for 'pic*u*e': " + restored);

        List<String> mixed = restorer.restoreCandidates("be*ninnig");
        System.out.println("Candidates for 'be*ninnig': " + mixed);
    }
}