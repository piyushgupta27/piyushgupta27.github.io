package com.piyush.foodiebay.utils;

import android.content.Context;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit2.Response;

/**
 * Created by piyush on 14/05/16.
 */
public class NetworkUtils {
    /**
     * check's network call status
     * success - if statusCode is between 200 && 400
     * failure - else cases are failure
     * also if status is 406 the revalidates user if enabled
     * @param context
     * @param response
     * @return true or false
     */
    public static boolean isCallSuccess(final Context context, Response response) {

        int code = response.code();
        if (code >= 200 && code < 400) {
            return true;
        }
        return false;
    }

    public static void handleCallFailure(final Context context, Throwable t) {

        if (t instanceof SocketTimeoutException) {
            // Network Call Timed-Out Error Message
            Notify.error(context, Notify.TYPE_DIALOG_NO_TITLE, ErrorMessages.NETWORK_ISSUE);

        } else if (t instanceof IOException) {
            // No Internet Available Error Message
            Notify.error(context, Notify.TYPE_DIALOG_NO_TITLE, ErrorMessages.NO_INTERNET_CONNECTION);

        } else {
            // Generic Error Message
            Notify.error(context, Notify.TYPE_DIALOG_NO_TITLE, ErrorMessages.GENERIC_ERROR_MSG);
        }
    }
}
