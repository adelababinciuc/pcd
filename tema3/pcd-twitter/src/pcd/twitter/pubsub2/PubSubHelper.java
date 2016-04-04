package pcd.twitter.pubsub2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.common.collect.ImmutableList;

import twitter4j.Status;

public class PubSubHelper
{
   private static ThreadLocal<Pubsub> pubsub = new ThreadLocal<Pubsub>()
   {
      protected Pubsub initialValue()
      {
         try
         {
            return PortableConfiguration.createPubsubClient();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         return null;
      };
   };
   
   static
   {
      try
      {
         pubsub.set(PortableConfiguration.createPubsubClient());
         /*Topic newTopic = pubsub.projects().topics()
               .create("projects/pcd-2015-master/topics/mytopic", new Topic())
               .execute();
         System.out.println("Created: " + newTopic.getName());*/
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public static void publish(List<Object> stats)
   {
      try
      {
         List<PubsubMessage> messages = new ArrayList<>();
         for (Object o : stats)
         {
            Object[] oo = (Object[]) o;
            String json = (String) oo[1];
            PubsubMessage pubsubMessage = new PubsubMessage();
            pubsubMessage.encodeData(json.getBytes("UTF-8"));
            messages.add(pubsubMessage);
         }
         PublishRequest publishRequest = new PublishRequest().setMessages(messages);
         
         PublishResponse publishResponse = pubsub.get().projects().topics()
               .publish("projects/pcd-2015-master/topics/repository-changes.default", publishRequest)
               .execute();
         List<String> messageIds = publishResponse.getMessageIds();
//         if (messageIds != null) {
//             for (String messageId : messageIds) {
//                 System.out.println("messageId: " + messageId);
//             }
//         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   public static void publish(Status status, String json)
   {
      try
      {
         PubsubMessage pubsubMessage = new PubsubMessage();
         pubsubMessage.encodeData(json.getBytes("UTF-8"));
         List<PubsubMessage> messages = ImmutableList.of(pubsubMessage);
         PublishRequest publishRequest = new PublishRequest().setMessages(messages);
         
         PublishResponse publishResponse = pubsub.get().projects().topics()
               .publish("projects/pcd-2015-master/topics/repository-changes.default", publishRequest)
               .execute();
         List<String> messageIds = publishResponse.getMessageIds();
         if (messageIds != null) {
             for (String messageId : messageIds) {
                 System.out.println("messageId: " + messageId);
             }
         }

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
