package cz.czeckout.entity;

import java.util.Map;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public final class Metadata {

    private final Map<String, Party> parties;
    private final Map<String, Address> addresses;
    private final Map<String, Account> accounts;
    private final Map<String, Method> methods;
    private final Map<String, String> variables;

}
