package pcd.twitter.bigq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.bigquery.samples.SyncQuerySample;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import pcd.twitter.endpoint.TweetData;
import twitter4j.Status;

public class BQStorage
{
   private static final ThreadLocal<List<String>> BUFFER = new ThreadLocal<List<String>>()
         {
            protected List<String> initialValue()
            {
               return new ArrayList<String>(100);
            };
         };
   private static final String projectId = "pcd-2015-master";
   private static final String datasetId = "pcd_twitter_data";
   private static final String tableId = "sample";
   private static final ThreadLocal<Bigquery> bigquery = new ThreadLocal<Bigquery>()
   {
      protected Bigquery initialValue()
      {
         return createAuthorizedClient();
      };
   };
   
   private static Map<String, Object> SCHEMA;

   static
   {
      BUFFER.set(new ArrayList<String>(100));
      bigquery.set(createAuthorizedClient());
      Gson gson = new Gson();
      try
      {
         final BufferedReader bf = new BufferedReader(new InputStreamReader(BQStorage.class.getResourceAsStream("sample_twet_twitter2.json")));
         String l = null;
         String schema = "";
         while ((l = bf.readLine()) != null)
         {
            schema += l + "\n";
         }
         bf.close();
         SCHEMA = gson.<Map<String, Object>> fromJson(schema, (new HashMap<String, Object>()).getClass());
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   public static void save(Status status, String json)
   {
      // BUFFER.get().add(json);
      
      // if (BUFFER.get().size() >= 1)
      {
         JsonReader reader = new JsonReader(new StringReader(new ArrayList<>(Arrays.asList(new String[]{json})).toString()));
         try
         {
            Iterator<TableDataInsertAllResponse> responses = run(projectId, datasetId, tableId,
                  reader);

            while (responses.hasNext())
            {
               System.err.println(responses.next());
            }

            reader.close();

         }
         catch (IOException e)
         {
            e.printStackTrace();
         }

         //BUFFER.get().clear();
      }
   }


   public static Iterator<TableDataInsertAllResponse> run(final String projectId,
         final String datasetId, final String tableId, final JsonReader rows) throws IOException
   {

      final Gson gson = new Gson();
      rows.beginArray();

      return new Iterator<TableDataInsertAllResponse>()
      {

         public boolean hasNext()
         {
            try
            {
               return rows.hasNext();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            return false;
         }

         public TableDataInsertAllResponse next()
         {
            try
            {
               Map<String, Object> rowData = gson.<Map<String, Object>> fromJson(rows,
                     (new HashMap<String, Object>()).getClass());
               String d = standardDate((String) rowData.get("created_at"));
               rowData.put("created_at", d);
               if (rowData.get("geo") != null)
               {
                  verifyGeo(rowData);
               }
               if (rowData.get("entities") != null)
               {
                  verifyHashTags(rowData);
               }
               verify(rowData, SCHEMA);
               return streamRow(bigquery.get(), projectId, datasetId, tableId,
                     new TableDataInsertAllRequest.Rows().setJson(rowData));
            }
            catch (JsonSyntaxException e)
            {
               e.printStackTrace();
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
            return null;
         }

         private void verifyGeo(Map<String, Object> rowData)
         {
            Map<String, Object> geo = (Map<String, Object>) rowData.get("geo");
            List<?> coord = (List<?>) geo.get("coordinates");
            Map mycoords = new HashMap();
            rowData.put("coordinates", mycoords);
            mycoords.put("lat", coord.get(0));
            mycoords.put("long", coord.get(1));
         }

         private void verifyHashTags(Map<String, Object> rowData)
         {
            Map<String, Object> entities = (Map<String, Object>) rowData.get("entities");
            
            List<?> ht = (List<?>) entities.get("hashtags");
            if (ht == null || ht.isEmpty())
               return;
            rowData.put("hashtags", ht);
         }

         private void verify(Map<String, Object> rowData, Map<String, Object> schema)
         {
            Iterator<Map.Entry<String, Object>> itr = rowData.entrySet().iterator();
            while (itr.hasNext())
            {
               Map.Entry<String, Object> e = itr.next();
               if (!schema.containsKey(e.getKey()) || e.getValue() == null)
                  itr.remove();
               else if (e.getValue() instanceof Map)
               {
                  verify((Map<String, Object>)e.getValue(), (Map<String, Object>)schema.get(e.getKey()));
               }
            }
         }

         public void remove()
         {
            this.next();
         }

      };

   }

   private static String standardDate(String date)
   {
      final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
      SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.US);
      sf.setLenient(true);
      Date d;
      try
      {
         d = sf.parse(date);
         sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
         return sf.format(d);
      }
      catch (ParseException e)
      {
         e.printStackTrace();
      }
      
      return date;
   }

   public static TableDataInsertAllResponse streamRow(final Bigquery bigquery,
         final String projectId, final String datasetId, final String tableId,
         final TableDataInsertAllRequest.Rows row) throws IOException
   {
      return bigquery.tabledata()
            .insertAll(projectId, datasetId, tableId,
                  new TableDataInsertAllRequest().setRows(Collections.singletonList(row)))
            .execute();
   }

   private static Bigquery createAuthorizedClient()
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
            Collection<String> bigqueryScopes = BigqueryScopes.all();
            credential = credential.createScoped(bigqueryScopes);
         }

         return new Bigquery.Builder(transport, jsonFactory, credential)
               .setApplicationName("BigQuery Samples").build();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      
      return null;
   }


   public static Set<TweetData> find(String content)
   {
      content = content.toUpperCase();
      String q = "SELECT created_at, hashtags.text, coordinates.lat, coordinates.long, text FROM [pcd_twitter_data.sample] "
            + "where (upper(text) like '%" + content + "%' or upper(hashtags.text) like '%" + content + "%') and "
            + "coordinates.long is not null and coordinates.lat is not null LIMIT 10000";
      Set<TweetData> sres = new HashSet<>(); 
      try
      {
         Iterator<GetQueryResultsResponse> pages = SyncQuerySample.run("pcd-2015-master", q, 10000);
         while (pages.hasNext())
         {
            GetQueryResultsResponse p = pages.next();
            List<TableRow> rows = p.getRows();
            if (rows == null)
               continue;

            for (TableRow tr : rows)
            {
               try
               {
                  long createdAt = (long) Double.parseDouble((String) tr.getF().get(0).getV());
                  String hashtag = "";//(String) tr.getF().get(1).getV();
                  double lat = Double.parseDouble((String) tr.getF().get(2).getV());
                  double lng = Double.parseDouble((String) tr.getF().get(3).getV());
                  String txt = (String) tr.getF().get(4).getV();
                  TweetData td = new TweetData();
                  td.setContent(txt);
                  td.setCreated_at(createdAt);
                  td.setHashtags(hashtag);
                  td.setLat(lat);
                  td.setLon(lng);
                  // String msg = "{\"created_at\":%d, \"content\":%s, \"hashtags\":%s, \"lon\":%.4f,  \"lat\":%.4f }";
                  // res.add(String.format(msg, createdAt, JSONObject.quote(txt), JSONObject.quote(hashtag), lng, lat));
                  sres.add(td);
               }
               catch (Throwable t)
               {
                  t.printStackTrace(System.err);
               }
            }
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return Collections.emptySet();
      }

      return sres;
   }
}
