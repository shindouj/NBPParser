package pl.parser.nbp.xml;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import org.pmw.tinylog.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A locale-aware converter that allows converting comma-separated floating point values
 * to BigDecimal objects. For use with the XStream (de)serializer,
 */
public class LocaleAwareBigDecimalConverter extends AbstractSingleValueConverter {
    private Locale locale = Locale.getDefault();

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return aClass.equals(BigDecimal.class);
    }

    public String toString(Object o) {
        NumberFormat nf = NumberFormat.getInstance(locale);
        return nf.format(o);
    }

    @Override
    public Object fromString(String s) {
        NumberFormat nf = NumberFormat.getInstance(locale);
        try {
            return new BigDecimal(nf.parse(s).toString());
        } catch (ParseException e) {
            Logger.error(e);
            throw new IllegalArgumentException(s, e);
        }
    }
}
