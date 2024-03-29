package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.msg_server.entities.SubmitTicketRequest;
import kerbefake.msg_server.entities.SubmitTicketRequestBody;

import java.io.IOException;

import static kerbefake.common.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.common.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Logger.error;

public final class SubmitTicketOperation extends ClientOperation<SubmitTicketRequest, Boolean> {

    private Session session;

    private final String clientId;

    public SubmitTicketOperation(ClientConnection connection, Session session, String clientId) {
        super(connection);
        this.session = session;
        this.clientId = clientId;
    }

    @Override
    public Boolean perform() {
        try {
            Authenticator authenticator = this.session.creatAuthenticator(clientId);
            SubmitTicketRequestBody submitTicketRequestBody = new SubmitTicketRequestBody(authenticator, session.getTicket());
            ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.SUBMIT_TICKET, submitTicketRequestBody.toLEByteArray().length);
            SubmitTicketRequest submitTicketRequest = new SubmitTicketRequest(serverMessageHeader, submitTicketRequestBody);

            submitTicketRequest.encrypt(session.getSessionKey());

            ServerMessage response = this.internalPerform(submitTicketRequest);

            if (response instanceof FailureResponse) {
                if (response.getHeader().getMessageCode().getCode() == UNKNOWN_FAILURE_CODE) {
                    error(REQUEST_FAILED);
                    return false;
                }
                error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                return false;
            }

            if (!(response instanceof EmptyResponse)) {
                error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return false;
            }

            return true;
        } catch (IOException | InvalidHexStringException e) {
            e.printStackTrace();
            if (e instanceof InvalidHexStringException) {
                // This can only happen if some hex string is invalid, but this is unlikely at this stage, we will print a message and fail regardless.
                error("Failed to encode or decode some part of the message sent or received from the server.");
                return false;
            }
            error("Failed to send a request to the auth server due to: %s.", e.getMessage());
        } catch (
                InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to decode message from server due to: %s\nPlease forward this to your server admin.", e.getMessage());
        }
        return false;
    }
}

