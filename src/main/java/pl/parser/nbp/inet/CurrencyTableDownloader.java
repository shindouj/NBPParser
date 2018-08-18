package pl.parser.nbp.inet;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import pl.parser.nbp.xml.CurrencyTable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Downloads currency tables based on publishing date.
 * Uses Builder for pre-configuration.
 */
public class CurrencyTableDownloader {
    private final static String  DIR_FILE_NAME = "dir.txt";
    private final static String  DIR_PREVIOUS_YEARS_PREFIX = "dir";
    private final static String  DIR_PREVIOUS_YEARS_EXT = ".txt";

    private final static String  DEFAULT_TABLE_TYPE = "c";
    private final static String  DEFAULT_REMOTE_FOLDER = "http://www.nbp.pl/kursy/xml/";
    private final static Charset DEFAULT_DIR_FILE_CHARSET = Charset.forName("UTF-8");
    private final static int     DEFAULT_CONNECT_TIMEOUT = 1000;
    private final static int     DEFAULT_READ_TIMEOUT    = 1000;

    private final XStream xStreamInstance;

    private final String tableType;
    private final String remoteFolder;
    private final Charset dirFileCharset;

    private final int connectTimeout;
    private final int readTimeout;

    public static class Builder {
        private final XStream xStreamInstance;

        private Optional<String>  tableType = Optional.empty();
        private Optional<String>  remoteFolder = Optional.empty();
        private Optional<Charset> dirFileCharset = Optional.empty();
        private OptionalInt connectTimeout = OptionalInt.empty();
        private OptionalInt readTimeout = OptionalInt.empty();

        /**
         * Creates a new Builder instance.
         * @param xStreamInstance Initialized XStream instance for deserializing XML requests
         */
        public Builder(XStream xStreamInstance) {
            this.xStreamInstance = xStreamInstance;
        }

        /**
         * Mandatory field with a default value ({@link CurrencyTableDownloader#DEFAULT_TABLE_TYPE}).
         * @param tableType Currency table type
         * @return this Builder instance
         */
        public Builder tableType(String tableType) {
            this.tableType = Optional.of(tableType);
            return this;
        }

        /**
         * Mandatory field with a default value ({@link CurrencyTableDownloader#DEFAULT_REMOTE_FOLDER}).
         * @param remoteFolder The remote folder to download currency tables and directory files from (URL format)
         * @return this Builder instance
         */
        public Builder remoteFolder(String remoteFolder) {
            this.remoteFolder = Optional.of(remoteFolder);
            return this;
        }

        /**
         * Mandatory field with a default value ({@link CurrencyTableDownloader#DEFAULT_DIR_FILE_CHARSET}).
         * @param dirFileCharset Charsed used for reading the directory file
         * @return this Builder instance
         */
        public Builder dirFileCharset(Charset dirFileCharset) {
            this.dirFileCharset = Optional.of(dirFileCharset);
            return this;
        }

        /**
         * Mandatory field with a default value ({@link CurrencyTableDownloader#DEFAULT_CONNECT_TIMEOUT}).
         * @param connectTimeout Connection timeout value (in millis, 0 for none).
         * @return this Builder instance
         */
        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = OptionalInt.of(connectTimeout);
            return this;
        }

        /**
         * Mandatory field with a default value ({@link CurrencyTableDownloader#DEFAULT_READ_TIMEOUT}).
         * @param readTimeout Socket read timeout value (in millis, 0 for none).
         * @return this Builder instance
         */
        public Builder readTimeout(int readTimeout) {
            this.readTimeout = OptionalInt.of(readTimeout);
            return this;
        }

        public CurrencyTableDownloader build() {
            return new CurrencyTableDownloader(this);
        }
    }

    private CurrencyTableDownloader(Builder b) {
        this.tableType       = b.tableType.orElse(DEFAULT_TABLE_TYPE);
        this.remoteFolder    = b.remoteFolder.orElse(DEFAULT_REMOTE_FOLDER);
        this.dirFileCharset  = b.dirFileCharset.orElse(DEFAULT_DIR_FILE_CHARSET);
        this.xStreamInstance = b.xStreamInstance;

        this.connectTimeout = b.connectTimeout.orElse(DEFAULT_CONNECT_TIMEOUT);
        this.readTimeout = b.readTimeout.orElse(DEFAULT_READ_TIMEOUT);
    }

    /**
     * Downloads and returns a currency table.
     * @param publishingDate Publishing date of the currency file
     * @return Deserialized {@link CurrencyTable} instance
     * @throws IOException when remote file is not found and/or when a connection error occurs
     * @throws NoSuchElementException when currency table is not found in directory file
     */
    public CurrencyTable downloadTable(LocalDate publishingDate) throws IOException {
        LocalDate today = LocalDate.now();
        String dirFileName;

        // not using ternary operator for readability
        if (publishingDate.getYear() == today.getYear()) {
            dirFileName = DIR_FILE_NAME;
        } else {
            dirFileName = DIR_PREVIOUS_YEARS_PREFIX + publishingDate.getYear() + DIR_PREVIOUS_YEARS_EXT;
        }

        String formattedPublishingDate = publishingDate.format(DateTimeFormatter.ofPattern("yyMMdd"));
        Optional<String> tableFileName = Optional.empty();

        try (InputStream in = prepareConnection(new URL(remoteFolder + dirFileName))) {
            for (String line : IOUtils.readLines(in, dirFileCharset)) {
                if (line.startsWith(tableType) && line.contains(formattedPublishingDate)) {
                    tableFileName = Optional.of(line);
                    break;
                }
            }
        }

        URL tableXMLURL = new URL(remoteFolder
                + tableFileName.orElseThrow(() -> new NoSuchElementException("Currency table " + formattedPublishingDate + " not found!"))
                + ".xml");

        try (InputStream in = prepareConnection(tableXMLURL)) {
            return (CurrencyTable) xStreamInstance.fromXML(in);
        }
    }

    private BufferedInputStream prepareConnection(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return new BufferedInputStream(conn.getInputStream());
    }
}
