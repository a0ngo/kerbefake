package kerbefake.msg_server.entities;

import kerbefake.common.CryptoUtils;
import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidMessageException;

import java.nio.charset.StandardCharsets;

import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.CryptoUtils.getIv;
import static kerbefake.common.Utils.assertNonZeroedByteArrayOfLengthN;

/**
 * A class for generating all messsage server related messages.
 */
public final class MessageServerMessageFactory {

    private static MessageServerMessageFactory instance;

    public static MessageServerMessageFactory getInstance() {
        return instance == null ? new MessageServerMessageFactory() : instance;
    }

    private MessageServerMessageFactory() {
        instance = this;
    }

    public SubmitTicketRequestFactory getSubmitTicketRequestFactory() {
        return new SubmitTicketRequestFactory();
    }

    public SendMessageRequestFactory getSendMessageRequestFactory() {
        return new SendMessageRequestFactory();
    }


    private static final class SubmitTicketRequestFactory extends MessageFactory<SubmitTicketRequest> {

        private Authenticator authenticator;

        private Ticket ticket;

        private boolean encrypted;

        public SubmitTicketRequestFactory setAuthenticator(Authenticator authenticator) {
            if (this.authenticator != null)
                if (this.authenticator.isEncrypted())
                    payloadSize -= this.authenticator.toLEByteArray().length;

            this.authenticator = authenticator;
            if (this.authenticator != null)
                if (this.authenticator.isEncrypted())
                    payloadSize += this.authenticator.toLEByteArray().length;
            return this;
        }

        public SubmitTicketRequestFactory setTicket(Ticket ticket) throws InvalidMessageException {
            if (this.ticket != null)
                if (this.ticket.isEncrypted())
                    payloadSize -= this.ticket.toLEByteArray().length;

            this.ticket = ticket;
            if (this.ticket != null)
                if (this.ticket.isEncrypted())
                    payloadSize += this.ticket.toLEByteArray().length;
            return this;
        }

        public SubmitTicketRequestFactory encrypt(byte[] key) throws InvalidMessageException {
            if (this.authenticator == null)
                throw new InvalidMessageException("Missing authenticator for request.");
            if (this.ticket == null)
                throw new InvalidMessageException("Missing ticket for request.");

            if (!this.authenticator.isEncrypted())
                this.authenticator.encrypt(key);
            if (!this.ticket.isEncrypted())
                this.ticket.encrypt(key);

            encrypted = true;

            if (payloadSize == 0) {
                payloadSize += this.authenticator.toLEByteArray().length;
                payloadSize += this.ticket.toLEByteArray().length;
            }

            return this;
        }

        @Override
        public SubmitTicketRequest build() throws InvalidMessageException {
            if (this.authenticator == null)
                throw new InvalidMessageException("Missing authenticator for request.");
            if (this.ticket == null)
                throw new InvalidMessageException("Missing ticket for request.");
            if (!encrypted)
                throw new InvalidMessageException("Must encrypt before building.");

            ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.SUBMIT_TICKET, payloadSize);
            return new SubmitTicketRequest(header, new SubmitTicketRequestBody(authenticator, ticket));
        }
    }

    private static final class SendMessageRequestFactory extends MessageFactory<SendMessageRequest> {

        private int messageSize;
        private byte[] iv;
        private byte[] encryptedMessage;
        private String message;
        private boolean encrypted;

        public SendMessageRequestFactory() {
        }

        public SendMessageRequestFactory setMessage(String message) {
            this.message = message;
            return this;
        }

        public SendMessageRequestFactory encrypt(byte[] key) throws InvalidMessageException {
            if (message == null || message.isEmpty()) {
                throw new InvalidMessageException("Missing message for request.");
            }

            this.iv = getIv();
            encryptedMessage = CryptoUtils.encrypt(key, iv, message.getBytes(StandardCharsets.UTF_8));
            this.messageSize = encryptedMessage.length;

            payloadSize += 4; // messageSize
            payloadSize += 16; // IV
            payloadSize += encryptedMessage.length;

            encrypted = true;

            return this;
        }

        @Override
        public SendMessageRequest build() throws InvalidMessageException {
            if (iv == null || !encrypted)
                throw new InvalidMessageException("Missing IV or not encrypted before building.");
            if (encryptedMessage == null || encryptedMessage.length == 0 || !assertNonZeroedByteArrayOfLengthN(encryptedMessage, encryptedMessage.length))
                throw new InvalidMessageException("Must encrypt before building.");
            ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.SEND_MESSAGE, payloadSize);
            return new SendMessageRequest(header, new SendMessageRequestBody(messageSize, iv, encryptedMessage));
        }
    }
}
