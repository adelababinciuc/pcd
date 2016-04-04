package pcd.twitter.prediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Output;
import com.google.api.services.prediction.model.Output.OutputMulti;

import pcd.twitter.endpoint.TweetData;

public class PredictionHelper
{
   private static final ThreadLocal<Prediction> PREDICTION = new ThreadLocal<Prediction>()
   {
      protected Prediction initialValue()
      {
         return createAuthorizedClient();
      };
   };

   private static final String PROJECT_ID = "414649711441";
   private static final String MODEL_ID = "sample.sentiment";

   static
   {
      PREDICTION.set(createAuthorizedClient());
   }
   
   public static void computeSentiment(Set<TweetData> tweets)
   {
      if (tweets.size() > 10)
         return;

      TweetData[] data = tweets.toArray(new TweetData[0]);

      Prediction prediction = PREDICTION.get();
      

      try
      {
         List<Object> t = new ArrayList<>();
         for (int i = 0; i < data.length; i++)
         {
            t.clear();
            t.add(data[i].getContent());
            
            Input input = new Input();
            InputInput inputInput = new InputInput();
            inputInput.setCsvInstance(t);
            input.setInput(inputInput);
            Output output = prediction.hostedmodels().predict(PROJECT_ID, MODEL_ID, input).execute();
            List<OutputMulti> om = output.getOutputMulti();
            for (int j = 0; j < om.size(); j++)
            {
               if (om.get(j).getLabel().equals("positive"))
               {
                  String score = om.get(j).getScore();
                  data[i].setSent(Double.parseDouble(score));
                  System.err.println(data[i].getContent() + " " + score);
                  break;
               }
               System.err.println(om.get(j).getScore() + " " + om.get(j).getLabel());
            }
         }
/*         for (int i = 0; i < tweets.size(); i++)
         {
            TweetData t = tweets.get(i);
            String score = om.get(i).getScore();
            t.setSent(Double.parseDouble(score));
            System.err.println(t.getContent() + " " + score);
         }*/
      }
      catch (IOException e)
      {
         e.printStackTrace(System.err);
      }
   }
   
   private static Prediction createAuthorizedClient()
   {
      // Create the credential
      HttpTransport transport = new NetHttpTransport();
      JsonFactory jsonFactory = new JacksonFactory();
      GoogleCredential credential;
      try
      {
         credential = GoogleCredential.getApplicationDefault(transport,
               jsonFactory);

         // Depending on the environment that provides the default credentials
         // (e.g. Compute Engine, App
         // Engine), the credentials may require us to specify the scopes we need
         // explicitly.
         // Check for this case, and inject the Bigquery scope if required.
         if (credential.createScopedRequired())
         {
            Collection<String> bigqueryScopes = PredictionScopes.all();
            credential = credential.createScoped(bigqueryScopes);
         }

         Prediction p = new Prediction.Builder(transport, jsonFactory, credential)
                                      .setApplicationName("tweet sent samples").build();
         return p;
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      
      return null;
   }

}
