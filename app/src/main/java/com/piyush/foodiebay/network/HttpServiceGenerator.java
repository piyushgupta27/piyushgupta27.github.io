package com.piyush.foodiebay.network;

import android.content.Context;
import android.os.Build;

import com.piyush.foodiebay.BuildConfig;
import com.piyush.foodiebay.utils.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by piyush on 13/05/16.
 */

public class HttpServiceGenerator {

    //Timeout, specify in SECONDS
    public static final long TIMEOUT_RESPONSE = 30;
    public static final long TIMEOUT_CONNECTION = 10;

    //URL's to be used
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BuildConfig.URL_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create());

    /**
     * Method to generate the instance for provided service class
     * @param context in which context service will be fired
     * @param serviceClass which service is required
     * @return instance of generated service class
     */
    public static <S> S generate(final Context context, Class<S> serviceClass){
        return generate(context, serviceClass, TIMEOUT_RESPONSE);
    }

    /**
     * Method to generate the instance for provided service class
     * @param context in which context service will be fired
     * @param serviceClass which service is required
     * @param responseTimeout if any custom timeout is required
     * @return
     */
    public static <S> S generate(final Context context, Class<S> serviceClass, long responseTimeout) {

        if (BuildConfig.ENV.equalsIgnoreCase(Constants.ENV_RELEASE)) {
            // For PROD environments
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        } else {

            // For basic information logging
            //logging.setLevel(Level.BASIC);

            // For basic + headers information logging
            //logging.setLevel(Level.HEADERS);

            // For detailed information logging
            // [IMPORTANT] Use this level only if necessary
            // because logs will clutter our Android monitor if we’re receiving large data sets
            logging.setLevel(Level.BODY);
        }

        /**
         * Use this interceptor to add request level headers
         *
         * AppVersion, UserID and all mandatory information
         * should be added through this interceptor
         * and avoid adding these into each api call as params
         */
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(responseTimeout, TimeUnit.SECONDS);
        httpClient.connectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("X-App-Token", BuildConfig.APP_TOKEN)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        });

        /**
         * [IMPORTANT]
         * Should be the last interceptor because this will also log the information
         * which was added with previous interceptors to our request
         */
        httpClient.interceptors().add(logging);

        Retrofit retrofit = builder.client(httpClient.build()).build();

        return retrofit.create(serviceClass);
    }
}