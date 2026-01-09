package cz.czeckout.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("invoices")
public class Invoices {

    @JacksonXmlElementWrapper(useWrapping = false) // Removes the extra outer wrapper if you don't want triple nesting
    @JacksonXmlProperty(localName = "invoice")    // Renames each list item to <invoice>
    private List<Invoice> invoices = new ArrayList<>();
}
