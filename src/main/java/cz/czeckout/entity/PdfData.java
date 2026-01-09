package cz.czeckout.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfData {

    private List<Invoice> invoices;

    private Metadata metadata;
}
