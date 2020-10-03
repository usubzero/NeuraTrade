package io.ethanfine.neuratrade.networking.exceptions;

public class NetworkRequestException extends Exception {

    public NetworkRequestException() {
        super("Network Request Exception occurred");
    }

    /**
     * Create a NetworkRequestException with errorMessage as its corresponding message. Also prints errorMessage.
     * @param errorMessage Message associated with Exception and printed.
     */
    public NetworkRequestException(String errorMessage) {
        super(errorMessage);
        System.out.println(errorMessage); // TODO: change to log
    }

}
