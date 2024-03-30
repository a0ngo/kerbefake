package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SendMessageRequest;
import kerbefake.msg_server.entities.SendMessageRequestFactory;

import static kerbefake.client.Client.clientLogger;
import static kerbefake.client.UserInputOutputHandler.promptLongString;
import static kerbefake.client.UserInputOutputHandler.promptString;

public class SendMessageOperation extends ClientOperation<SendMessageRequest, EmptyResponse, Boolean> {

    private final Session session;

    public SendMessageOperation(ClientConnection connection, Session session, String clientId) {
        super(connection, EmptyResponse.class, clientId);
        this.session = session;
    }

    @Override
    protected SendMessageRequest generateRequest() throws InvalidMessageException {
        String message = promptLongString("Please provide the message to send to the server");

        return SendMessageRequestFactory.getInstance().setMessage(message).encrypt(session.getSessionKey()).setClientId(clientId).build();
    }

    @Override
    protected Boolean validateResponse(EmptyResponse response) {
        return response != null;
    }
}
