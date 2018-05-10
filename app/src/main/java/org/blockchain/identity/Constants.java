package org.blockchain.identity;

public interface Constants {

    public static final String REGISTERED_EMAIL_KEY = "org.blockchain.identity.REGISTERED_EMAIL";
    public static final String SERVER_PUBLIC_KEY = "org.blockchain.identity.SERVER_PUBLIC_KEY";
    public static final String REGISTERED_ID_KEY = "org.blockchain.identity.REGISTERED_ID_KEY";

    public static final String SERVER_BASE_URL = "http://10.134.117.38:8080";
    public static final String SERVER_REGISTER_URL = SERVER_BASE_URL + "/register";

    public static final String SERVER_VERIFY_TRANSACTION_URL = SERVER_BASE_URL + "/verify-transaction";
}
