package pcd.twitter.pubsub2;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Utility class to interact with the Pub/Sub API.
 */
public final class PubsubUtils {

    /**
     * The application name will be attached to the API requests.
     */
    private static final String APPLICATION_NAME =
            "pcd-2015-master";

    /**
     * Prevents instantiation.
     */
    private PubsubUtils() {
    }

    /**
     * Builds a new Pubsub client with default HttpTransport and
     * JsonFactory and returns it.
     *
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient() throws IOException {
        return getClient(Utils.getDefaultTransport(),
                         Utils.getDefaultJsonFactory());
    }

    /**
     * Builds a new Pubsub client and returns it.
     *
     * @param httpTransport HttpTransport for Pubsub client.
     * @param jsonFactory JsonFactory for Pubsub client.
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient(final HttpTransport httpTransport,
                                   final JsonFactory jsonFactory)
            throws IOException {
        Preconditions.checkNotNull(httpTransport);
        Preconditions.checkNotNull(jsonFactory);
        GoogleCredential credential = GoogleCredential.getApplicationDefault();
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(PubsubScopes.all());
        }
        // Please use custom HttpRequestInitializer for automatic
        // retry upon failures.
        HttpRequestInitializer initializer =
                new RetryHttpInitializerWrapper(credential);
        return new Pubsub.Builder(httpTransport, jsonFactory, initializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Returns a topic name for this application.
     *
     * @return a topic name.
     */
    public static String getAppTopicName() {
        return "mytopic";
    }

    /**
     * Returns a subscription name for this application.
     *
     * @return a subscription name.
     */
    public static String getAppSubscriptionName() {
        return "subscription-" + getProjectId();
    }

    /**
     * Returns the push endpoint URL.
     *
     * @return the push endpoint URL.
     */
    public static String getAppEndpointUrl() {
        //String subscriptionUniqueToken = System.getProperty(Constants.BASE_PACKAGE + ".subscriptionUniqueToken");

        return "https://" + APPLICATION_NAME + ".appspot.com/publish";
            //+ "?token=" + subscriptionUniqueToken;
    }

    /**
     * Returns the project ID.
     *
     * @return the project ID.
     */
    public static String getProjectId() {
       return APPLICATION_NAME;
        /*AppIdentityService identityService =
                AppIdentityServiceFactory.getAppIdentityService();

        // The project ID associated to an app engine application is the same
        // as the app ID.
        return identityService.parseFullAppId(ApiProxy.getCurrentEnvironment()
                .getAppId()).getId();*/
    }
}