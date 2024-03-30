package kerbefake.common;

import kerbefake.common.entities.ServerMessageHeader;

public final class Constants {

    public static final int MODE_CLIENT = 1;
    public static final int MODE_AUTH = 2;
    public static final int MODE_SERVER = 3;

    public static final byte SERVER_VERSION = (byte) 24;

    public static final int NONCE_SIZE = 8;

    /**
     * Default port for authentication server
     */
    public static final int DEFAULT_PORT_AUTH_SERVER = 1256;

    /**
     * The request header's size is constant, see {@link ServerMessageHeader}
     */
    public static final int REQUEST_HEADER_SIZE = 23;

    /**
     * ID length in hex (16 bytes).
     */
    public static final int ID_HEX_LENGTH_CHARS = 32;

    /**
     * Size of any given IV in bytes.
     */
    public static final int IV_SIZE = 16;
    /**
     * The response header size.
     */
    public static final int RESPONSE_HEADER_SIZE = 9;
    public static final String CLIENTS_FILE_NAME = "./clients";
    public static final String CLIENT_CONFIG_FILE_NAME = "./me.info";
    public static final String SERVER_CONFIG_FILE_NAME = "./msg.info";

    public static final String DATE_FORMAT = "hh.mm.ss dd/MM/yyyy";

    public static final String FAILED_REQUEST_ID = "ZZZZZZZZZZZZZZZZ";


    public static final class RequestCodes {
        /**
         * Request code for registering clients.
         */
        public static final short REGISTER_CLIENT_CODE = 1024;

        public static final short REQ_ENC_SYM_KEY = 1027;

        public static final short SUBMIT_TICKET = 1028;

        public static final short SEND_MESSAGE = 1029;
    }


    public static final class ResponseCodes {
        public static final short REGISTER_CLIENT_SUCCESS_CODE = 1600;

        public static final short REGISTER_CLIENT_FAILURE_CODE = 1601;

        public static final short SEND_ENC_SYM_KEY = 1603;

        public static final short SUBMIT_TICKET_SUCCESS = 1604;

        public static final short SEND_MESSAGE_SUCCESS = 1605;

        public static final short UNKNOWN_FAILURE_CODE = 1609;
    }

    public static final class ClientConstants {
        public static final String REQUEST_FAILED = "server responded with an error";

        /**
         * A menu displayed to the user of the client before they registered.
         */
        public static final String MENU_PRE_REGISTER = "Please select an operation:\n1.\tRegister\n2.\tExit";

        public static final String MENU_POST_REGISTER = "Please select an operation:\n1. Open connection to the message server\n2.\tExit";

        public static final String MENU_POST_TICKET = "Please select an operation:\n1. Send a message to the message server\n2.\tExit";

        public static final String SEND_MESSAGE_PROMPT = "Please provide a message to send to the server.";

        public static final String DEFAULT_AUTH_SERVER_IP = "127.0.0.1";

        public static final int DEFAULT_AUTH_SERVER_PORT = 1256;
        public static final String DEFAULT_MESSAGE_SERVER_IP = "127.0.0.1";
        public static final int DEFAULT_MESSAGE_SERVER_PORT = 1235;

    }

}
