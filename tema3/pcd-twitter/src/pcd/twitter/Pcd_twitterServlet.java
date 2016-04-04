package pcd.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pcd.twitter.bigq.BQStorage;
import pcd.twitter.endpoint.TweetData;

@SuppressWarnings("serial")
public class Pcd_twitterServlet extends HttpServlet
{
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
   {
      String content = req.getParameter("content");
      resp.setContentType("text/plain");
      PrintWriter pw = resp.getWriter();
      Set<TweetData> results = BQStorage.find(content);
      pw.write(results.toString());
      pw.close();
   }
   /*
   public static void main(String[] args) throws IOException
   {
      String content = "bru";
      List results = BQStorage.find(content);
      String q = "SELECT created_at, hashtags.text, coordinates.lat, coordinates.long, text FROM [pcd_twitter_data.sample] where upper(hashtags.text) like '%" + (content.toUpperCase()) + "%' and coordinates.long is not null and coordinates.lat is not null LIMIT 10000";
      Iterator<GetQueryResultsResponse> pages =SyncQuerySample.run("pcd-2015-master", q, 10000);
        while (pages.hasNext()) {
           List<TableRow> rows = pages.next().getRows();
           if (rows == null)
              continue;
          BigqueryUtils.printRows(rows, System.out);
        }
   }*/
}
