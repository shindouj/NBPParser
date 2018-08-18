package pl.parser.nbp;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.pmw.tinylog.Logger;
import pl.parser.nbp.exceptions.XStreamInitException;
import pl.parser.nbp.inet.CurrencyTableDownloader;
import pl.parser.nbp.xml.CurrencyTable;
import pl.parser.nbp.xml.CurrencyTablePosition;
import pl.parser.nbp.xml.LocaleAwareBigDecimalConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class MainClass {
    public static void main(String... args) {
        XStream xStream;
        try {
            xStream = xStreamInit();
        } catch (XStreamInitException e) {
            Logger.error(e);
            return;
        }

        if (args.length < 3) {
            // we expect user not to delete the default logger config from the JAR
            Logger.error("Too little arguments! " +
                    "Usage: java pl.parser.nbp.MainClass [CURRENCY_CODE] [START_DATE] [END_DATE]");
        }

        // no parameter flags specified in the project document, using hardcoded parameter positions
        String currencyCode = args[0];
        String startDateStr = args[1];
        String endDateStr   = args[2];

        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            Logger.error("Wrong date format!");
            return;
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        CurrencyTableDownloader downloader = new CurrencyTableDownloader.Builder(xStream).build();

        List<CurrencyTablePosition> currencyPrices = new ArrayList<>();

        for (long i = 0; i <= daysBetween; i++) {
            LocalDate publishingDate = startDate.plusDays(i);
            try {
                CurrencyTable table = downloader.downloadTable(publishingDate);
                Logger.info("Received table: " + table.toString());

                currencyPrices.add(table.getTablePosition(currencyCode));
            } catch (IOException | NoSuchElementException e) {
                Logger.error(e, "Error while downloading currency table");
            } catch (IllegalArgumentException e) {
                Logger.error(e, "Error while getting prices for " + currencyCode);
            }
        }

        BigDecimal meanBuyingPrice = currencyPrices
                .stream()
                .map(CurrencyTablePosition::getBuyingPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.DOWN)
                .divide(new BigDecimal(currencyPrices.size()), RoundingMode.DOWN);

        BigDecimal meanSellingPrice = currencyPrices
                .stream()
                .map(CurrencyTablePosition::getSellingPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.DOWN)
                .divide(new BigDecimal(currencyPrices.size()), RoundingMode.DOWN);

        // double is fine for statistical usage
        double standardDeviation = 0;

        for (CurrencyTablePosition currencyPrice: currencyPrices) {
            standardDeviation = standardDeviation + Math.pow(currencyPrice.getSellingPrice().doubleValue() - meanSellingPrice.doubleValue(), 2);
        }

        standardDeviation = standardDeviation / currencyPrices.size();
        standardDeviation = Math.sqrt(standardDeviation);

        System.out.println(meanBuyingPrice);
        DecimalFormat df = new DecimalFormat("#0.0000");
        System.out.println(df.format(standardDeviation));
    }

    private static XStream xStreamInit() throws XStreamInitException {
        try {
            XStream xStream = new XStream(new StaxDriver());
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[] {
                    CurrencyTable.class.getPackage().getName() + ".*"
            });
            xStream.processAnnotations(CurrencyTable.class);
            xStream.processAnnotations(CurrencyTablePosition.class);

            LocaleAwareBigDecimalConverter c = new LocaleAwareBigDecimalConverter();
            c.setLocale(Locale.GERMAN);

            xStream.registerConverter(c);

            Logger.debug("XStream initialized successfully!");
            return xStream;
        } catch (Exception e) {
            // rethrowing a non-runtime exception since multiple runtime exceptions can be thrown
            // during XStream initialization
            throw new XStreamInitException("Error while initializing XStream", e);
        }
    }
}
