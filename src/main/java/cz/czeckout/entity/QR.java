package cz.czeckout.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;


@With
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QR {

    private String code;
    private String svg;
}
