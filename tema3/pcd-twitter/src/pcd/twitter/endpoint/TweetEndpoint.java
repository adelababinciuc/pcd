package pcd.twitter.endpoint;

import java.util.Set;

import com.google.api.client.http.HttpMethods;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;

import pcd.twitter.bigq.BQStorage;
import pcd.twitter.prediction.PredictionHelper;

@Api(name = "pcd") 
public class TweetEndpoint
{
   @ApiMethod(name = "tweets", path = "tweets", httpMethod = HttpMethods.GET) 
   public Set<TweetData> getTweets(@Named("content") String content)
   {
      Set<TweetData> td = BQStorage.find(content);
      
      PredictionHelper.computeSentiment(td);
      
      return td;
   }
}
