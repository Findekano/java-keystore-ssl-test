import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.logging.*;

public class Test extends Formatter {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        Logger theLogger = Logger.getLogger(Test.class.getName());
        theLogger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Test());
        theLogger.addHandler(handler);

        try {
            if ((args.length != 1) && (args.length != 2) && (args.length != 4)) {
                theLogger.warning("Usage: java Test <https://address.server.edu> [timeout] [keystore keystore-pass]");
                return;
            }

            theLogger.info("Received host address " + args[0]);
            URL constructedUrl = new URL(args[0]);

            URLConnection conn = constructedUrl.openConnection();


            if (args.length >= 2) {
                conn.setConnectTimeout(Integer.valueOf(args[1]) * 1000);
            } else {
                conn.setConnectTimeout(5000);
            }

            if (args.length >= 4) {

                final InputStream in = new FileInputStream(new File(args[2]));
                final char[] password = args[3].toCharArray();
                final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

                ks.load(in, password);
                in.close();

                final TrustManagerFactory tmf =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                final X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new TrustManager[]{defaultTrustManager}, null);

                final SSLSocketFactory sslSocketFactory = context.getSocketFactory();
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            }

            theLogger.info("Setting connection timeout to " + conn.getConnectTimeout() / 1000 + " second(s).");

            theLogger.info("Trying to connect to " + args[0]);
            InputStreamReader reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
            BufferedReader in = new BufferedReader(reader);

            in.readLine();

            in.close();
            reader.close();

            theLogger.info("Great! It worked.");

        } catch (Exception e) {
            theLogger.info("Could not connect to the host address " + args[0]);
            theLogger.info("The error is: " + e.getMessage());
            theLogger.info("Here are the details:");
            theLogger.log(Level.SEVERE, e.getMessage(), e);

            throw new RuntimeException(e);
        }
    }

    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();

        sb.append("[");
        sb.append(record.getLevel().getName());
        sb.append("]\t");

        sb.append(formatMessage(record));
        sb.append("\n");

        return sb.toString();
    }
}
