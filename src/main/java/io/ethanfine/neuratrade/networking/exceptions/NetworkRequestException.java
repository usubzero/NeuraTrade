package io.ethanfine.neuratrade.networking.exceptions;

public class NetworkRequestException extends Exception {

    public NetworkRequestException() {
        super("Network Request Exception occurred");
    }

    public NetworkRequestException(String errorMessage) {
        super(errorMessage);
        System.out.println(errorMessage); // TODO: change to log
    }

}
