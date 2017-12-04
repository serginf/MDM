package eu.supersede.mdm.storage.model;

/**
 * Created by snadal on 18/01/17.
 */
public enum StatisticalAnalysisModelTypes {

    FEEDBACK_CLASSIFIER_BAG_OF_WORDS("N/A Feedback Classifier (Bag of Words)"),
    FEEDBACK_CLASSIFIER_SPEECH_ACT("Feedback Classifier (Speech Act)"),
    SENTIMENT_ANALYZER_ML("Sentiment Analyzer (ML)"),
    SENTIMENT_ANALYZER_SENTI_WORD("N/A Sentiment Analyzer (Senti Word)"),
    SENTIMENT_ANALYZER_SENTI_STRENGTH("N/A Sentiment Analyzer (Senti Strength)");

    private String element;

    StatisticalAnalysisModelTypes(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}
