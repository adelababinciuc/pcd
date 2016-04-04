package pcd.twitter.pubsub2;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.pubsub.model.PubsubMessage;

import pcd.twitter.bigq.BQStorage;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TweetSubscriber extends HttpServlet
{
   private static final long serialVersionUID = -3925857995837617591L;

   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      ServletInputStream reader = req.getInputStream();
      // Parse the JSON message to the POJO model class.
      JsonParser parser = JacksonFactory.getDefaultInstance().createJsonParser(reader);
      parser.skipToKey("message");
      PubsubMessage message = parser.parseAndClose(PubsubMessage.class);
      // Base64-decode the data and work with it.
      String data = new String(message.decodeData(), "UTF-8");

      BQStorage.save(null, data);

      // Work with your message
      // Respond with a 20X to acknowledge receipt of the message.
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
      resp.getWriter().close();
   }

}