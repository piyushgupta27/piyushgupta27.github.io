package com.piyush.foodiebay.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by piyush on 14/05/16.
 */
public class Notify {

    public static final int TYPE_TOAST = 101;
    public static final int TYPE_DIALOG = 201;
    public static final int TYPE_DIALOG_NO_TITLE = 202;

    /**
     * Displays success message with default type : Snackbar
     *
     * @param context
     * @param message
     */
    public static void success(Context context, @NonNull String message) {
        create(context, 0, false, null, message, null);
    }

    /**
     * Displays success message depending upon the type specified
     *
     * @param context
     * @param type
     * @param message
     */
    public static void success(Context context, int type, @NonNull String message) {
        create(context, type, false, null, message, null);
    }

    /**
     * Displays success message depending upon the type specified
     *
     * @param context
     * @param type
     * @param message
     */
    public static void success(Context context, int type, String title, @NonNull String message) {
        create(context, type, false, title, message, null);
    }


    /**
     * Displays error message with default type : Snackbar
     *
     * @param context
     * @param message
     */
    public static void error(Context context, @NonNull String message) {
        create(context, 0, true, null, message, null);
    }

    /**
     * Displays error message depending upon the type specified
     *
     * @param context
     * @param type
     * @param message
     */
    public static void error(Context context, int type, @NonNull String message) {
        create(context, type, true, null, message, null);
    }

    /**
     * Displays error message depending upon the type specified
     *
     * @param context
     * @param type
     * @param message
     */
    public static void error(Context context, int type, String title, @NonNull String message) {
        create(context, type, true, title, message, null);
    }

    private static void create(Context context, int type, boolean isError, String title,
                               String message, DialogInterface.OnDismissListener dismissListener) {

        //simple check whether context is valid or not
        if(!((Activity) context).isFinishing()){

            switch (type) {
                case TYPE_DIALOG: {

                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(isError ? "Error" : "Success");
                        builder.setMessage(message);
                        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(dismissListener);
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TYPE_DIALOG_NO_TITLE: {

                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage(message);
                        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(dismissListener);
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TYPE_TOAST:
                default: {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }
}
