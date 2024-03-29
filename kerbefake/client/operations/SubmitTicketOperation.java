package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.client.SessionManager;
import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.*;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.models.msg_server.requests.SubmitTicketRequest;
import kerbefake.models.msg_server.requests.SubmitTicketRequestBody;

import java.io.IOException;

import static kerbefake.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.Constants.SERVER_VERSION;
import static kerbefake.Logger.error;

public final class SubmitTicketOperation extends ClientOperation<SubmitTicketRequest, Boolean> {

    private Session session;

    public SubmitTicketOperation(ClientConnection connection, Session session) {
        super(connection);
        this.session = session;
    }

    @Override
    public Boolean perform() {
        try {
            Authenticator authenticator = this.session.creatAuthenticator(null, null);
            SubmitTicketRequestBody submitTicketRequestBody = new SubmitTicketRequestBody(authenticator, session.getTicket());
            ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.SUBMIT_TICKET, submitTicketRequestBody.toLEByteArray().length);
            SubmitTicketRequest submitTicketRequest = new SubmitTicketRequest(serverMessageHeader, submitTicketRequestBody);

            ServerMessage response = this.internalPerform(submitTicketRequest);

            if (response instanceof FailureResponse) {
                if (response.getHeader().getMessageCode().getCode() == UNKNOWN_FAILURE_CODE) {
                    error(REQUEST_FAILED);
                    return null;
                }
                error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                return null;
            }

            if (!(response instanceof EmptyResponse)) {
                error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return null;
            }

            return true;
        } catch (IOException | InvalidHexStringException e) {
            e.printStackTrace();
            if (e instanceof InvalidHexStringException) {
                // This can only happen if some hex string is invalid, but this is unlikely at this stage, we will print a message and fail regardless.
                error("Failed to encode or decode some part of the message sent or received from the server.");
                return null;
            }
            error("Failed to send a request to the auth server due to: %s.", e.getMessage());
        } catch (
                InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to decode message from server due to: %s\nPlease forward this to your server admin.", e.getMessage());
        }
        return null;
    }
}

