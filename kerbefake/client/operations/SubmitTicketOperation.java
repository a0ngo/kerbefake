package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SubmitTicketRequest;
import kerbefake.msg_server.entities.SubmitTicketRequestFactory;

public final class SubmitTicketOperation extends ClientOperation<SubmitTicketRequest, EmptyResponse, Boolean> {

    private final Session session;

    private final String clientId;

    public SubmitTicketOperation(ClientConnection connection, Session session, String clientId) {
        super(connection, EmptyResponse.class);
        this.session = session;
        this.clientId = clientId;
    }

    @Override
    protected SubmitTicketRequest generateRequest() throws InvalidMessageException {
        return SubmitTicketRequestFactory.getInstance().setTicket(session.getTicket()).setAuthenticator(this.session.createAuthenticator(clientId)).encrypt(session.getSessionKey()).build();
    }

    @Override
    protected Boolean validateResponse(EmptyResponse response) {
        return true;
    }
}

