package kerbefake;

public final class Constants {

    public static final int MODE_CLIENT = 1;
    public static final int MODE_AUTH = 2;
    public static final int MODE_SERVER = 3;


    /**
     * Default port for authentication server
     */
    public static final int DEFAULT_PORT_AUTH_SERVER = 1256;

    /**
     * The request header's size is constant, see {@link kerbefake.models.auth_server.AuthServerRequestHeader}
     */
    public static final int REQUEST_HEADER_SIZE = 23;

    public static final String CLIENTS_FILE_NAME = "./clients";

    public static final String DATE_FORMAT = "hh.mm.ss dd/MM/yyyy";

    public static final class RequestCodes {
        /**
         * Request code for registering clients.
         */
        public static final short REGISTER_CLIENT_CODE = 1024;
    }


}
