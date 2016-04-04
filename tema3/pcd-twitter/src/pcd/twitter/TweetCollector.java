package pcd.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;

import pcd.twitter.pubsub2.PubSubHelper;
import twitter4j.Status;

public class TweetCollector
{
   public static final String PHRASE = "indian wells,tennis finals,tennis,federer,murray,djokovic,ausopen,williams,serena,kerber,paris,belgium,bruxelles,attack,airport,metro,london";
   
   public static final String LOCATIONS = "-180,-90,180,90";
   
   public static final String LANGUAGES = "en,fr,es";
   
   public static final List<Object> CACHE = new ArrayList<>();
   
   private static final Observer collector = new Observer()
   {
      public void update(Observable o, Object arg)
      {
         Object[] a = (Object[]) arg;
         if (!(a[0] instanceof Status))
         {
            return;
         }
         
         Status status = (Status) a[0];
         String json = (String) a[1];
         System.out.println(status.getText());
         if (status.getHashtagEntities() != null && status.getHashtagEntities().length > 0)
         {
         //   System.out.println(status.getHashtagEntities()[0].getText());
         }
         CACHE.add(new Object[] {status, json});
         if (CACHE.size() >= 1000)
         {
            PubSubHelper.publish(CACHE);
            CACHE.clear();
         }
         //BQStorage.save(status, json);
      }
   };
   
   private final String phrase;
   
   private final String location;
   
   private final String language;
   
   public TweetCollector(String phrase, String location, String language)
   {
      this.phrase = phrase;
      this.location = location;
      this.language = language;
   }

   public void collect(Properties props)
   {
      TwitterStreamClient client = new TwitterStreamClient();
      client.init(props);
      StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
      endpoint.addQueryParameter("track", phrase);
      endpoint.addQueryParameter("locations", location);
      endpoint.addQueryParameter("language", language);
      
      client.setEndpoint(endpoint);
      
      client.setSampleSize(Integer.MAX_VALUE);

      client.addObserver(collector);

      client.sample();
   }
   
   public static void main(String[] args) throws Exception
   {
      Properties props = new Properties();
      props.load(TwitterStreamClient.class.getResourceAsStream("credentials.properties"));

      TweetCollector tc = new TweetCollector(PHRASE, LOCATIONS, LANGUAGES);
      tc.collect(props); 
   }

   /*
   public static void main2(String[] args) throws Exception
   {
      storage.initialize(true);
      
      TwitterStreamClient client = new TwitterStreamClient();

      client.addObserver(collector);

      Query q = new Query();
      q.setQuery(phrase.replaceAll(" ", " or ").replaceAll(",", " or "));
      q.setQuery("open");
      q.setGeoCode(new GeoLocation(0, 0), 4000, Unit.mi);
      
      client.search(q);
   }
   */
}
