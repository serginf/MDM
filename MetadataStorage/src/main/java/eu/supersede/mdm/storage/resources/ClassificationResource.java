package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

/**
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class ClassificationResource {

    /** System Metadata **/
    @POST @Path("classification/feedback")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_classify_feedback(String JSON_feedback) {
        System.out.println("[POST /classification/feedback/");

        JSONObject objBody = (JSONObject) JSONValue.parse(JSON_feedback);
        String feedback = objBody.getAsString("feedback");

        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = ConfigManager.getProperty("resources_path");// Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        //String pathToSentimentAnalysisModel = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        //String pathToFeatureExtractor = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");

        JSONObject out = new JSONObject();

        if (!feedback.trim().isEmpty()) {
            ClassificationResult classification = null;
            try {
                classification = feedbackClassifier.classify(pathToClassificationModel, new UserFeedback(feedback));
                out.put("classification",classification.getLabel());
                out.put("accuracy",classification.getAccuracy());
                    /*if (!feedbackClassified.containsKey(classification.getLabel())) {
                        feedbackClassified.put(classification.getLabel(), Sets.newHashSet());
                    }
                    feedbackClassified.get(classification.getLabel()).add(new UserFeedback(feedback));*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Response.ok(out.toJSONString()).build();
    }

}