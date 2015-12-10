package jp.ad.iij.nakam;

import jp.boy.ie.n01.writer.CustomTwitterStatusWriter;
import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TwitterSearcher {

    public static void main(String[] args) throws TwitterException, Exception {

        StatusListener listener = new CustomTwitterStatusWriter();
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        String keywords = System.getProperty("twitter.keywords");
        if (keywords == null) {
            keywords = "iphone";
        }
        String[] keywordsArray = keywords.split(",");
        filter.track(keywordsArray);
        twitterStream.filter(filter);
    }
}
