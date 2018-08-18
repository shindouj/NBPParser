package pl.parser.nbp.inet;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import pl.parser.nbp.xml.CurrencyTable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CurrencyTableDownloader {
    private final static String DIR_FILE_NAME = "dir.txt";
    private final static String DIR_PREVIOUS_YEARS_PREFIX = "dir";
    private final static String DIR_PREVIOUS_YEARS_EXT = ".txt";

    private final static String  DEFAULT_TABLE_TYPE = "c";
    private final static String  DEFAULT_REMOTE_FOLDER = "http://www.nbp.pl/kursy/xml/";
    private final static Charset DEFAULT_DIR_FILE_CHARSET = Charset.forName("UTF-8");

    private final XStream xStreamInstance;

    private final String tableType;
    private final String remoteFolder;
    private final Charset dirFileCharset;

    public static class Builder {
        private final XStream xStreamInstance;

        private Optional<String>  tableType = Optional.empty();
        private Optional<String>  remoteFolder = Optional.empty();
        private Optional<Charset> dirFileCharset = Optional.empty();

        public Builder(XStream xStreamInstance) {
            this.xStreamInstance = xStreamInstance;
        }

        public Builder tableType(String tableType) {
            this.tableType = Optional.of(tableType);
            return this;
        }

        public Builder remoteFolder(String remoteFolder) {
            this.remoteFolder = Optional.of(remoteFolder);
            return this;
        }

        public Builder dirFileCharset(Charset dirFileCharset) {
            this.dirFileCharset = Optional.of(dirFileCharset);
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
    }

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

        InputStream in = new URL(remoteFolder + dirFileName).openStream();
        for (String line : IOUtils.readLines(in, dirFileCharset)) {
            if (line.startsWith(tableType) && line.contains(formattedPublishingDate)) {
                tableFileName = Optional.of(line);
                break;
            }
        }

        in.close();
        in = new URL(remoteFolder
                + tableFileName.orElseThrow(() -> new NoSuchElementException("Currency table " + formattedPublishingDate + " not found!"))
                + ".xml"
        ).openStream();

        return (CurrencyTable) xStreamInstance.fromXML(in);
    }
}
