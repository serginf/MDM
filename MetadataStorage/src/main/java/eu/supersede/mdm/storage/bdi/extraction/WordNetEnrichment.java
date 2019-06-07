package eu.supersede.mdm.storage.bdi.extraction;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kashif Rabbani
 */
public class WordNetEnrichment {
    //private final static String MORPH_PHRASE = "running-away";
    private Dictionary dictionary;
    private List<String> hyponymsList = new ArrayList<>();

    public WordNetEnrichment() throws JWNLException {
        this.dictionary = Dictionary.getDefaultResourceInstance();
    }
//   hyponym is a word of more specific meaning than a general or superordinate term applicable to it.
//   For example, spoon is a hyponym of cutlery.
    public List<String> findHyponmys(String word) {
        try {
            if (dictionary.lookupIndexWord(POS.NOUN, word) != null) {
                demonstrateTreeOperation(dictionary.lookupIndexWord(POS.NOUN, word));
            } /*else if (dictionary.lookupIndexWord(POS.VERB, word) != null) {
                demonstrateTreeOperation(dictionary.lookupIndexWord(POS.VERB, word));
            } else if (dictionary.lookupIndexWord(POS.ADJECTIVE, word) != null) {
                demonstrateTreeOperation(dictionary.lookupIndexWord(POS.ADJECTIVE, word));
            } else if (dictionary.lookupIndexWord(POS.ADVERB, word) != null) {
                demonstrateTreeOperation(dictionary.lookupIndexWord(POS.ADVERB, word));
            }*/
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        return hyponymsList;
    }

    private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
        // Get all the hyponyms (children) of the first sense of <var>word</var>
        PointerTargetTree hyponyms = PointerUtils.getHyponymTree(word.getSenses().get(0));
        System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
        //System.out.println(hyponyms.getRootNode().getSynset().getWords().size());
        //hyponymsList = new ArrayList<>(hyponyms.getRootNode().getSynset().getWords().size());
        for (Word w : hyponyms.getRootNode().getSynset().getWords()) {
            //System.out.println(w.getLemma());
            hyponymsList.add(w.getLemma());
        }
    }

    public static void main(String[] args) throws Exception {
        List<String> x = new WordNetEnrichment().findHyponmys("Bike");
        System.out.println(x);
    }
}


