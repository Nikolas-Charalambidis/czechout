package cz.czeckout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


@Data
public class Party {

    private String id;

    private String name;

    private String identifierType;

    private String identifier;

    private String vatPrefix;

    private String vat;

    private Address address;

    @JsonIgnore
    private String addressReference;
}
