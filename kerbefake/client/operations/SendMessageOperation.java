package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.EmptyResponse;
import kerbefake.models.MessageCode;
import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.msg_server.requests.SendMessageRequest;
import kerbefake.models.msg_server.requests.SendMessageRequestBody;

import java.io.IOException;

import static kerbefake.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.Constants.SERVER_VERSION;
import static kerbefake.Logger.error;
import static kerbefake.Utils.getIv;
import static kerbefake.client.UserInputOutputHandler.promptString;

public class SendMessageOperation extends ClientOperation<SendMessageRequest, Boolean> {

    private Session session;

    public SendMessageOperation(ClientConnection connection, Session session) {
        super(connection);
        this.session = session;
    }

    @Override
    public Boolean perform() {
        try {
            byte[] iv = getIv();
            String message = promptString("Please provide the message to send to the server", true);

            SendMessageRequestBody sendMessageRequestBody = new SendMessageRequestBody(iv, message);
            ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.SEND_MESSAGE, sendMessageRequestBody.toLEByteArray().length);
            SendMessageRequest sendMessageRequest = new SendMessageRequest(serverMessageHeader, sendMessageRequestBody);

            sendMessageRequest.encrypt(session.getSessionKey());

            ServerMessage response = this.internalPerform(sendMessageRequest);

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
