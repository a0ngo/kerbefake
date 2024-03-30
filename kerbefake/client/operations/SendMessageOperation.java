package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.client.Session;
import kerbefake.common.entities.EmptyResponse;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.msg_server.entities.SendMessageRequest;
import kerbefake.msg_server.entities.SendMessageRequestBody;

import static kerbefake.client.UserInputOutputHandler.promptString;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.CryptoUtils.getIv;

public class SendMessageOperation extends ClientOperation<SendMessageRequest, EmptyResponse, Boolean> {

    private final Session session;

    public SendMessageOperation(ClientConnection connection, Session session) {
        super(connection, EmptyResponse.class);
        this.session = session;
    }

    @Override
    protected SendMessageRequest generateRequest() {
        byte[] iv = getIv();
        String message = promptString("Please provide the message to send to the server", true);

        SendMessageRequestBody sendMessageRequestBody = new SendMessageRequestBody(iv, message);
        ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.SEND_MESSAGE, sendMessageRequestBody.toLEByteArray().length);
        SendMessageRequest sendMessageRequest = new SendMessageRequest(serverMessageHeader, sendMessageRequestBody);

        sendMessageRequest.encrypt(session.getSessionKey());

        return sendMessageRequest;
    }

    @Override
    protected Boolean validateResponse(EmptyResponse response) {
        return response == null;
    }
}
