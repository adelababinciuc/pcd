package pcd.twitter.endpoint;
import twitter4j.JSONObject;

public class TweetData
{
   private long created_at;
   private String content;
   private String hashtags;
   private double lon;
   private double lat;
   private double sent = 1;
   
   public long getCreated_at()
   {
      return created_at;
   }
   public void setCreated_at(long created_at)
   {
      this.created_at = created_at;
   }
   public String getContent()
   {
      return content;
   }
   public void setContent(String content)
   {
      this.content = content;
   }
   public String getHashtags()
   {
      return hashtags;
   }
   public void setHashtags(String hashtags)
   {
      this.hashtags = hashtags;
   }
   public double getLon()
   {
      return lon;
   }
   public void setLon(double lon)
   {
      this.lon = lon;
   }
   public double getLat()
   {
      return lat;
   }
   public void setLat(double lat)
   {
      this.lat = lat;
   }
   
   public double getSent()
   {
      return sent;
   }
   public void setSent(double sent)
   {
      this.sent = sent;
   }

   @Override
   public String toString()
   {
      String msg = "{\"created_at\":%d, \"sent\":%.2f, \"content\":%s, \"hashtags\":%s, \"lon\":%.4f,  \"lat\":%.4f }";
      return String.format(msg, created_at, sent, JSONObject.quote(content), JSONObject.quote(hashtags), lon, lat);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return obj != null && obj.toString().equals(this.toString());
   }
   
   @Override
   public int hashCode()
   {
      return toString().hashCode();
   }
}
