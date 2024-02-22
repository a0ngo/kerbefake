package kerbefake;

import kerbefake.models.auth_server.AuthServerMessageHeader;

public final class Constants {

    public static final int MODE_CLIENT = 1;
    public static final int MODE_AUTH = 2;
    public static final int MODE_SERVER = 3;


    /**
     * Default port for authentication server
     */
    public static final int DEFAULT_PORT_AUTH_SERVER = 1256;

    /**
     * The request header's size is constant, see {@link AuthServerMessageHeader}
     */
    public static final int REQUEST_HEADER_SIZE = 23;

    /**
     * The response header size.
     */
    public static final int RESPONSE_HEADER_SIZE = 9;
    public static final String CLIENTS_FILE_NAME = "./clients";

    public static final String DATE_FORMAT = "hh.mm.ss dd/MM/yyyy";

    public static final String REQUEST_FAILED = "server responded with an error";

    public static final String FAILED_REQUEST_ID = "ZZZZZZZZZZZZZZZZ";

    public static final class RequestCodes {
        /**
         * Request code for registering clients.
         */
        public static final short REGISTER_CLIENT_CODE = 1024;
    }


    public static final class ResponseCodes {
        public static final short REGISTER_CLIENT_SUCCESS_CODE = 1600;

        public static final short REGISTER_CLIENT_FAILURE_CODE = 1601;

        public static final short UNKNOWN_FAILURE_CODE = 1609;
    }

}
