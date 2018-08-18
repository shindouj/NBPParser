package pl.parser.nbp.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.time.LocalDate;
import java.util.List;

@XStreamAlias("tabela_kursow")
public class CurrencyTable {
    @XStreamAlias("numer_tabeli")
    private String tableID;

    @XStreamAsAttribute
    @XStreamAlias("typ")
    private String tableType;

    @XStreamAlias("data_notowania")
    private LocalDate listingDate;

    @XStreamAlias("data_publikacji")
    private LocalDate publishingDate;

    @XStreamImplicit(itemFieldName = "pozycja")
    private List<CurrencyTablePosition> tablePositions;

    public String getTableID() {
        return tableID;
    }

    public String getTableType() {
        return tableType;
    }

    public LocalDate getListingDate() {
        return listingDate;
    }

    public LocalDate getPublishingDate() {
        return publishingDate;
    }

    public CurrencyTablePosition getTablePosition(String currencyCode) {
        return tablePositions.stream()
                .filter(x -> x.getCurrencyCode().equals(currencyCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Currency code not found: " + currencyCode));
    }

    @Override
    public String toString() {
        return "CurrencyTable{" +
                "tableID='" + tableID + '\'' +
                ", tableType='" + tableType + '\'' +
                ", listingDate=" + listingDate +
                ", publishingDate=" + publishingDate +
                ", tablePositions=" + tablePositions +
                '}';
    }
}
