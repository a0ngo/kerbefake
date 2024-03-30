package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SubmitTicketRequest;
import kerbefake.msg_server.entities.SubmitTicketRequestFactory;

public final class SubmitTicketOperation extends ClientOperation<SubmitTicketRequest, EmptyResponse, Boolean> {

    private final Session session;

    public SubmitTicketOperation(ClientConnection connection, Session session, String clientId) {
        super(connection, EmptyResponse.class, clientId);
        this.session = session;
    }

    @Override
    protected SubmitTicketRequest generateRequest() throws InvalidMessageException {
        return SubmitTicketRequestFactory.getInstance().setTicket(session.getTicket()).setAuthenticator(this.session.createAuthenticator(clientId)).encrypt(session.getSessionKey()).setClientId(clientId).build();
    }

    @Override
    protected Boolean validateResponse(EmptyResponse response) {
        return true;
    }
}

