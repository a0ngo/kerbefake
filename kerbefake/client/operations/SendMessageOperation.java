package kerbefake.client.operations;

import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SendMessageRequest;
import kerbefake.msg_server.entities.SendMessageRequestBody;

import java.io.IOException;

import static kerbefake.client.UserInputOutputHandler.promptString;
import static kerbefake.common.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.common.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.CryptoUtils.getIv;
import static kerbefake.common.Logger.error;

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
