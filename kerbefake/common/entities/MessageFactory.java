package kerbefake.common.entities;

import kerbefake.common.errors.InvalidMessageException;

/**
 * A message factory, can be provided by the various servers in order to streamline the creation of messages
 */
public abstract class MessageFactory<MSG> {

    protected int payloadSize;

    /**
     * Builds the message required.
     * @return the message created by the factory
     * @throws InvalidMessageException - in case the message data provided to the factory is missing something.
     */
    public abstract MSG build() throws InvalidMessageException;

}
