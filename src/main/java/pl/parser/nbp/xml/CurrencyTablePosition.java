package pl.parser.nbp.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.math.BigDecimal;

@XStreamAlias("pozycja")
public class CurrencyTablePosition {
    @XStreamAlias("nazwa_waluty")
    private String currencyName;

    @XStreamAlias("kod_waluty")
    private String currencyCode;

    @XStreamAlias("przelicznik")
    private int relationToPLN;

    @XStreamAlias("kurs_kupna")
    private BigDecimal buyingPrice; // BigDecimal used for precision (due to calculations involving money)

    @XStreamAlias("kurs_sprzedazy")
    private BigDecimal sellingPrice;

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public int getRelationToPLN() {
        return relationToPLN;
    }

    public BigDecimal getBuyingPrice() {
        return buyingPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    @Override
    public String toString() {
        return "CurrencyTablePosition{" +
                "currencyName='" + currencyName + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", relationToPLN=" + relationToPLN +
                ", buyingPrice=" + buyingPrice +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
