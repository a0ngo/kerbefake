package kerbefake.common.entities;

import kerbefake.common.errors.InvalidMessageException;

/**
 * A message factory, can be provided by the various servers in order to streamline the creation of messages
 */
public abstract class MessageFactory<MSG> {

    protected int payloadSize;

    protected String clientId;

    @SuppressWarnings("unchecked")
    public <T extends MessageFactory<MSG>> T setClientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }

    /**
     * Builds the message required.
     *
     * @return the message created by the factory
     * @throws InvalidMessageException - in case the message data provided to the factory is missing something.
     */
    protected abstract MSG internalBuild() throws InvalidMessageException;

    public MSG build() throws InvalidMessageException {
         try {
            if (this.clientId == null) {
                throw new InvalidMessageException("Missing Client ID for request.");
            }
            return internalBuild();
        } finally {
            payloadSize = 0;
        }
    }

}
