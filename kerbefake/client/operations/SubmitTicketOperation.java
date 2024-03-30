package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.Authenticator;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SubmitTicketRequest;
import kerbefake.msg_server.entities.SubmitTicketRequestBody;

import static kerbefake.common.Constants.SERVER_VERSION;

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
        Authenticator authenticator = this.session.createAuthenticator(clientId);
        SubmitTicketRequestBody submitTicketRequestBody = new SubmitTicketRequestBody(authenticator, session.getTicket());
        ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.SUBMIT_TICKET, submitTicketRequestBody.toLEByteArray().length);
        SubmitTicketRequest submitTicketRequest = new SubmitTicketRequest(serverMessageHeader, submitTicketRequestBody);

        submitTicketRequest.encrypt(session.getSessionKey());

        return submitTicketRequest;
    }

    @Override
    protected Boolean validateResponse(EmptyResponse response) {
        return true;
    }
}

