package pcd.twitter;

import java.io.IOException;
import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.JSONObjectType;
import twitter4j.PublicObjectFactory;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterResponse;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterStreamClient extends Observable
{
   private final static Logger logger = LoggerFactory.getLogger(TwitterStreamClient.class);

   private String consumerKey = null;
   private String consumerSecret = null;
   private String token = null;
   private String secret = null;
   private StreamingEndpoint endpoint;
   private final PublicObjectFactory factory;

   private int sampleSize;

   public TwitterStreamClient()
   {
      this.factory = new PublicObjectFactory(new ConfigurationBuilder().build());
   }

   public void init(Properties props)
   {
      setConsumerKey(props.getProperty("consumerKey"));
      setConsumerSecret(props.getProperty("consumerSecret"));
      setSecret(props.getProperty("secret"));
      setToken(props.getProperty("token"));
   }
   
   public void setConsumerKey(String consumerKey)
   {
      this.consumerKey = consumerKey;
   }

   public void setConsumerSecret(String consumerSecret)
   {
      this.consumerSecret = consumerSecret;
   }

   public void setToken(String token)
   {
      this.token = token;
   }

   public void setSecret(String secret)
   {
      this.secret = secret;
   }

   public void setEndpoint(StreamingEndpoint endpoint)
   {
      this.endpoint = endpoint;
   }

   public void setSampleSize(int size)
   {
      this.sampleSize = size;
   }
   
   public void search(Query q)
   {
      if (consumerKey == null || consumerSecret == null || token == null || secret == null)
      {
         throw new IllegalStateException("Auth credentials were not set.");
      }

      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setOAuthConsumerKey(consumerKey);
      cb.setOAuthConsumerSecret(consumerSecret);
      TwitterFactory tf = new TwitterFactory(cb.build());
      Twitter tw = tf.getInstance(new AccessToken(token, secret));
      try
      {
         for (Status s : tw.search(q).getTweets())
         {
            System.out.println(s.getText() + " " + s.getHashtagEntities());

            setChanged();
            notifyObservers(s);
         }
      }
      catch (TwitterException e1)
      {
         e1.printStackTrace();
      }
   }
   
   public void sample()
   {
      if (consumerKey == null || consumerSecret == null || token == null || secret == null)
      {
         throw new IllegalStateException("Auth credentials were not set.");
      }
      
      // Create an appropriately sized blocking queue
      BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

      Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

      if (endpoint == null)
      {
         throw new NullPointerException("The endpoint was not specified!");
      }

      BasicClient client = new ClientBuilder().name("sampleExampleClient")
            .hosts(Constants.STREAM_HOST).endpoint(endpoint).authentication(auth)
            .processor(new StringDelimitedProcessor(queue)).build();
      
      client.connect();
      int size = sampleSize;
      
      while (size > 0 && !client.isDone())
      {
         try
         {
            String msg = queue.poll(100, TimeUnit.MILLISECONDS);

            if (msg == null)
            {
               continue;
            }

            TwitterResponse te = processMessage(msg);
            
            if (te == null)
            {
               continue;
            }
            
            size = size - 1;
            setChanged();
            notifyObservers(new Object[] {te, msg});
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         catch (JSONException e)
         {
            e.printStackTrace();
         }
         catch (TwitterException e)
         {
            e.printStackTrace();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      
      client.stop();
   }

   private TwitterResponse processMessage(String msg) throws JSONException, TwitterException, IOException
   {
      JSONObject json = new JSONObject(msg);
      JSONObjectType.Type type = JSONObjectType.determine(json);
      switch (type)
      {
         case STATUS:
            Status status = factory.createStatus(json);
            return status;
         default:
            return null;
      }
   }
}
