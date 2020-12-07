package com.leadinka.android.coldcall;


import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A login screen that offers login via email/password.
 */
public class AuthorizationActivity extends AppCompatActivity {


    // UI references.
    private EditText mFioOperatom;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mPin;
    private SQLiteDatabase mDB;
    private static final String TAG = "AuthorizationActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        mDB = CC_controller.getDB();
        // Set up the login form.

        mFioOperatom = (EditText) findViewById(R.id.fio_op);
        mEmail = (EditText) findViewById(R.id.email);
        mPhone = (EditText) findViewById(R.id.phone);
        mPin = (EditText) findViewById(R.id.pin);


        try {
            JSONArray ids = DB_schema.getAllParams(mDB, DB_schema.Params.Types.AUTHORIZATION);
            for (int i = 0; i < ids.length(); i++) {
                JSONObject ob = ids.getJSONObject(i);
                String name = ob.getString("name");
                String value = ob.getString("value");
                if (name.equals("_FIO")) {
                    mFioOperatom.setText(value);
                }
                if (name.equals("_EMAIL")) {
                    mEmail.setText(value);
                }
                if (name.equals("_PHONE")) {
                    mPhone.setText(value);
                }
                if (name.equals("_PIN")) {
                    mPin.setText(value);
                }
            }
        } catch (JSONException e) {
            CC_controller.SYS_LogError(TAG, "Чтение приватных параметров " + e);
        } catch (SQLException e) {
            CC_controller.SYS_LogError(TAG, "Чтение приватных параметров " + e);
        }


        Button mButtonLead = (Button) findViewById(R.id.PrivateParams);
        mButtonLead.requestFocus();
        mButtonLead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAuth();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAuth() {


        // Reset errors.
        mFioOperatom.setError(null);
        mEmail.setError(null);
        mPhone.setError(null);
        mPin.setError(null);


        // Store values at the time of the login attempt.
        String fio = mFioOperatom.getText().toString();
        String email = mEmail.getText().toString();
        String phone = mPhone.getText().toString();
        String pin = mPin.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(fio)) {
            mFioOperatom.setError(getString(R.string.error_invalid_fiooperator));
            focusView = mFioOperatom;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhone.setError(getString(R.string.error_field_required));
            focusView = mPhone;
            cancel = true;
        }
        if (phone.length()<11) {
            mPhone.setError("Не менее 10 цифр");
            focusView = mPhone;
            cancel = true;
        }
        if (!TextUtils.isDigitsOnly(phone)) {
            mPhone.setError(getString(R.string.error_digits_required));
            focusView = mPhone;
            cancel = true;
        }
        if (!TextUtils.isDigitsOnly(pin)) {
            mPin.setError(getString(R.string.error_digits_required));
            focusView = mPin;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // плюемся интентом
            JSONArray arr = new JSONArray();

            try {
                JSONObject ob = new JSONObject();
                ob.put("name", "_FIO");
                ob.put("value", fio);
                ob.put("type", DB_schema.Params.Types.AUTHORIZATION);
                arr.put(ob);

                ob = new JSONObject();
                ob.put("name", "_EMAIL");
                ob.put("value", email);
                ob.put("type", DB_schema.Params.Types.AUTHORIZATION);
                arr.put(ob);

                ob = new JSONObject();
                ob.put("name", "_PHONE");
                ob.put("value", phone);
                ob.put("type", DB_schema.Params.Types.AUTHORIZATION);
                arr.put(ob);

                ob = new JSONObject();
                ob.put("name", "_PIN");
                ob.put("value", pin);
                ob.put("type", DB_schema.Params.Types.AUTHORIZATION);
                arr.put(ob);

                DB_schema.setParams(mDB, arr);
            } catch (JSONException e) {
                CC_controller.SYS_LogError(TAG, "Установка приватных параметров " + e);
            } catch (SQLException e) {
                CC_controller.SYS_LogError(TAG, "Установка приватных параметров " + e);
            }
            CC_controller.that.GUI_startAuthorization();
            this.finish();
        }
    }

}


