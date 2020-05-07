package io.ethanfine.neuratrade.networking.exceptions;

import java.util.logging.Logger;

public class NetworkRequestException extends Exception {

    public NetworkRequestException() {
        super("Network Request Exception occurred");
    }

    public NetworkRequestException(String errorMessage) {
        super(errorMessage);
        System.out.println(errorMessage); // TODO: change to log
    }

}
