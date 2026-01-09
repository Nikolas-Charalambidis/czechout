package cz.czeckout.entity;

import lombok.Data;


@Data
public class Address {

    private String id;

    private String street;

    private String houseNumber;

    private String city;

    private String district;

    private String zipCode;

    private String country;
}
