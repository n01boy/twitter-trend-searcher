package jp.boy.ie.n01.writer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Builder;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusDeletionNotice;

public class CustomTwitterStatusWriter extends StatusAdapter implements AutoCloseable {

    boolean initializedFlag = false;
    Tokenizer tokenizer = null;
    FileOutputStream fosInfluence = null;
    FileOutputStream fosKuromoji = null;
    FileOutputStream fosHttpurl = null;
    OutputStreamWriter oswInfluence = null;
    OutputStreamWriter oswKuromoji = null;
    OutputStreamWriter oswHttpurl = null;
    PrintWriter writerInfluence = null;
    PrintWriter writerKuromoji = null;
    PrintWriter writerHttpurl = null;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String urlPattern = "https?://\\S*";

    public void onStatus(Status status) {

        if (initializedFlag == false) {
            if (initializeSuccess()) {
                initializedFlag = true;
            } else {
                return; // notsuccess
            }
        }

        // influence : !black magic
        writerInfluence.write("[" + sdf.format(status.getCreatedAt()) + "]"
                + status.getUser().getName() + ";(userName=" + status.getUser().getScreenName()
                + "/userLocation=" + status.getUser().getLocation() + ") : "
                + status.getText().replaceAll("[\r\n]", " ") + "; followersCount="
                + status.getUser().getFollowersCount() + "\n");
        writerInfluence.flush();

        // kuromoji
        String kuromojiTarget = status.getText().replaceAll(urlPattern, ""); // exclude url
        for (Token token : tokenizer.tokenize(kuromojiTarget)) {
            String surfaceForm = token.getSurfaceForm();
            if (surfaceForm.length() > 2) { // over 3 chars
                writerKuromoji.write(surfaceForm + "\n");
            }
        }
        writerKuromoji.flush();

        // httpurl
        Pattern pat = Pattern.compile(urlPattern);
        Matcher mat = pat.matcher(status.getText());

        while (mat.find()) {
            String url = mat.group(0);
            if (!url.endsWith("...") && !url.endsWith("…")) { // exclude end with "..." or "…"
                writerHttpurl.write(url + "\n");
            }
        }
        writerHttpurl.flush();

    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

    public void onException(Exception ex) {
        ex.printStackTrace();
    }

    // initialize
    private boolean initializeSuccess() {
        boolean isSuccess = true;

        try {
            FileInputStream fis = new FileInputStream("userdict.txt");
            Builder builder = Tokenizer.builder();
            builder.userDictionary(fis);
            tokenizer = builder.build();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // influence
        try {
            fosInfluence = new FileOutputStream("./logs/influence.log", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isSuccess = false;
        }
        oswInfluence = new OutputStreamWriter(fosInfluence);
        writerInfluence = new PrintWriter(oswInfluence);

        // kuromoji
        try {
            fosKuromoji = new FileOutputStream("./logs/kuromoji.log", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isSuccess = false;
        }
        oswKuromoji = new OutputStreamWriter(fosKuromoji);
        writerKuromoji = new PrintWriter(oswKuromoji);

        // httpurl
        try {
            fosHttpurl = new FileOutputStream("./logs/httpurl.log", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isSuccess = false;
        }
        oswHttpurl = new OutputStreamWriter(fosHttpurl);
        writerHttpurl = new PrintWriter(oswHttpurl);

        return isSuccess;

    }

    @Deprecated
    @Override
    public void close() throws Exception { // fmmmm...
        writerInfluence.close();
        writerKuromoji.close();
        writerHttpurl.close();
        oswInfluence.close();
        oswKuromoji.close();
        oswHttpurl.close();
        fosInfluence.close();
        fosKuromoji.close();
        fosHttpurl.close();
    }
}
