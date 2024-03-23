package kerbefake.models;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.org.apache.bcel.internal.Const;
import kerbefake.Constants;
import kerbefake.auth_server.AuthServerConnectionHandler;
import kerbefake.errors.InvalidMessageCodeException;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponseBody;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;
import kerbefake.models.msg_server.requests.SubmitTicketRequest;
import kerbefake.models.msg_server.requests.SubmitTicketRequestBody;
import kerbefake.models.msg_server.responses.SubmitTicketResponse;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

/**
 * An enum of all available messages to use or to parse.
 */
public enum MessageCode {

    /**
     * A request to register a client
     */
    REGISTER_CLIENT(Constants.RequestCodes.REGISTER_CLIENT_CODE, RegisterClientRequest.class, RegisterClientRequestBody.class, true),

    /**
     * Successful response for client registration.
     */
    REGISTER_CLIENT_SUCCESS(Constants.ResponseCodes.REGISTER_CLIENT_SUCCESS_CODE, RegisterClientResponse.class, RegisterClientResponseBody.class, true),
    /**
     * Failure response for client registration.
     */
    REGISTER_CLIENT_FAILED(Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE, FailureResponse.class, null, true),

    /**
     * A user requests a symmetric key to communicate with a message server.
     */
    REQUEST_SYMMETRIC_KEY(Constants.RequestCodes.REQ_ENC_SYM_KEY, GetSymmetricKeyRequest.class, GetSymmetricKeyRequestBody.class, true),

    /**
     * Success response for getting a symmetric key for communication.
     */
    REQUEST_SYMMETRIC_KEY_SUCCESS(Constants.ResponseCodes.SEND_ENC_SYM_KEY, GetSymmetricKeyResponse.class, GetSymmetricKeyResponseBody.class, true),

    SUBMIT_TICKET(Constants.RequestCodes.SUBMIT_TICKET, SubmitTicketRequest.class, SubmitTicketRequestBody.class, false),

    SUBMIT_TICKET_SUCCESS(Constants.ResponseCodes.SUBMIT_TICKET_SUCCESS, SubmitTicketResponse.class, null, false),

    UNKNOWN_FAILURE(Constants.ResponseCodes.UNKNOWN_FAILURE_CODE, FailureResponse.class, null, true);
    /**
     * The code for a given request
     */
    private final short code;

    /**
     * The class of the message that is used for the given message.
     * This is used when parsing the message from the input stream in {@link AuthServerConnectionHandler}
     * or as part of a message parsing in {@link ServerMessage#parse(InputStream)}.
     * The class must extend {@link ServerMessage} and thus have a constructor matching the signature:
     * `{@link ServerMessageHeader}, {@link ServerMessageBody}`
     */
    private final Class<? extends ServerMessage> messageClazz;

    /**
     * The class of the message's body. This class can be null, for example a failure response has no body, just the header.
     * If this exists it must extend {@link ServerMessageBody} and implement an empty constructor with the {@link ServerMessageBody#parse(byte[])}
     * method.
     */
    private final Class<? extends ServerMessageBody> bodyClazz;

    /**
     * Indicates whether this message class should be handled by the auth server or message server.
     */
    private final boolean forAuthServer;

    MessageCode(short code, Class<? extends ServerMessage> messageClazz, Class<? extends ServerMessageBody> bodyClazz, boolean forAuthServer) {
        this.code = code;
        this.messageClazz = messageClazz;
        this.bodyClazz = bodyClazz;
        this.forAuthServer = forAuthServer;
    }

    /**
     * Converts a given request code to a little-endian byte array
     *
     * @return a 2 item byte array for the code in little endian order.
     */
    public byte[] toLEByteArray() {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this.code).array();
    }


    public short getCode() {
        return this.code;
    }

    public Class<? extends ServerMessage> getMessageClass() {
        return messageClazz;
    }

    public Class<? extends ServerMessageBody> getBodyClass() {
        return bodyClazz;
    }

    /**
     * Parses two bytes as a request code enum
     *
     * @param requestCodeBytes - the request code bytes
     * @return the corresponding {@link MessageCode} for the provided values
     * @throws InvalidMessageCodeException - in case there are more than 2 bytes or it is no a known request code.
     */
    public static MessageCode parse(byte[] requestCodeBytes) throws InvalidMessageCodeException {
        if (requestCodeBytes.length != 2) { // Generally shouldn't happen
            throw new InvalidMessageCodeException("Request Code");
        }
        short reqCode = byteArrayToLEByteBuffer(requestCodeBytes).getShort();

        List<MessageCode> matchingCodes = Arrays.stream(values()).filter(v -> v.code == reqCode).collect(Collectors.toList());
        if (matchingCodes.size() != 1) {
            throw new InvalidMessageCodeException(String.format("Request Code - %d", reqCode));
        }
        return matchingCodes.get(0);
    }

    public boolean isForAuthServer() {
        return this.forAuthServer;
    }
}
