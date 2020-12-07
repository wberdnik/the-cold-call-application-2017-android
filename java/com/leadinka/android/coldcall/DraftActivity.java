package com.leadinka.android.coldcall;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A login screen that offers login via email/password.
 */
public class DraftActivity extends AppCompatActivity {


    // UI references.
    private EditText mFio;
    private EditText mFabule;
    private EditText mDateTime_min;
    private EditText mDateTime_h;
    private EditText mDateTime_d;
    private EditText mDateTime_m;
    private EditText mDateTime_y;
    private EditText mCity;
    private TextView mStatus;
    private int mPin;
    private String mPhone;
    private int current_year;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        // Set up the login form.
        mFio = (EditText) findViewById(R.id.fio);
        mFabule = (EditText) findViewById(R.id.fabule);
        mCity = (EditText) findViewById(R.id.city);

        String city_default;
        try {
            city_default = DB_schema.getParamByName(CC_controller.getDB(), "default_city");
        } catch (java.lang.NullPointerException mm) {
            CC_controller.SYS_LogError("DRAFTACTIVITY", "ошибка получения дескриптора БД Draft Activity " + mm);
            throw new java.lang.NullPointerException();
        }
        if (city_default.length() > 1) {
            mCity.setText(city_default);
        }

        mDateTime_min = (EditText) findViewById(R.id.datetime_min);
        mDateTime_h = (EditText) findViewById(R.id.datetime_h);
        mDateTime_d = (EditText) findViewById(R.id.datetime_d);
        mDateTime_m = (EditText) findViewById(R.id.datetime_m);
        mDateTime_y = (EditText) findViewById(R.id.datetime_y);

        Date df = new java.util.Date(System.currentTimeMillis());
        // String vv = new SimpleDateFormat("MM dd, yyyy hh:mma").format(df);
        String tmp = new SimpleDateFormat("yyyy").format(df);
        mDateTime_y.setText(tmp);
        current_year = Integer.parseInt(tmp);

        tmp = new SimpleDateFormat("MM").format(df);
        mDateTime_m.setText(tmp);
        tmp = new SimpleDateFormat("dd").format(df);
        mDateTime_d.setText(tmp);
        mDateTime_min.setText("00");


        mStatus = (TextView) findViewById(R.id.status);
        mPin = getIntent().getIntExtra("pin", 0);
        mPhone = getIntent().getStringExtra("lastPhone");

        mStatus.setText("ПИН " + mPin + " Телефон " + mPhone + " " + getIntent().getStringExtra("type"));


        Button mButtonDraft = (Button) findViewById(R.id.draft);
        mButtonDraft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDraft(1);
            }
        });

        Button mButtonLead = (Button) findViewById(R.id.lead);
        mButtonLead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDraft(2);
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptDraft(int type) {


        // Reset errors.
        mFio.setError(null);
        mFabule.setError(null);
        mDateTime_y.setError(null);
        mDateTime_m.setError(null);
        mDateTime_d.setError(null);
        mDateTime_h.setError(null);
        mDateTime_min.setError(null);
        mCity.setError(null);

        // Store values at the time of the login attempt.
        String fio = mFio.getText().toString();
        String fabule = mFabule.getText().toString();
        String dateTime_y = mDateTime_y.getText().toString();
        String dateTime_m = mDateTime_m.getText().toString();
        String dateTime_d = mDateTime_d.getText().toString();
        String dateTime_h = mDateTime_h.getText().toString();
        String dateTime_min = mDateTime_min.getText().toString();
        String city = mCity.getText().toString();
        if (city.isEmpty()) {
            city = "Москва";
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(fio)) {
            mFio.setError(getString(R.string.error_invalid_fio));
            focusView = mFio;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(fabule)) {
            mFabule.setError(getString(R.string.error_field_required));
            focusView = mFabule;
            cancel = true;
        }

        // Check for a valid email address.
        if (!TextUtils.isEmpty(dateTime_h)) {
            if (!TextUtils.isDigitsOnly(dateTime_h)) {
                mDateTime_h.setError(getString(R.string.error_digits_required));
                focusView = mDateTime_h;
                cancel = true;
            } else if (!TextUtils.isDigitsOnly(dateTime_min)) {
                mDateTime_min.setError(getString(R.string.error_digits_required));
                focusView = mDateTime_min;
                cancel = true;
            } else if (!TextUtils.isDigitsOnly(dateTime_d)) {
                mDateTime_d.setError(getString(R.string.error_digits_required));
                focusView = mDateTime_d;
                cancel = true;
            } else if (!TextUtils.isDigitsOnly(dateTime_m)) {
                mDateTime_m.setError(getString(R.string.error_digits_required));
                focusView = mDateTime_m;
                cancel = true;
            } else if (!TextUtils.isDigitsOnly(dateTime_y)) {
                mDateTime_y.setError(getString(R.string.error_digits_required));
                focusView = mDateTime_y;
                cancel = true;
            }
            if (!cancel) {
                int tmp = Integer.parseInt(dateTime_y);
                if (tmp != current_year && tmp != (current_year + 1)) {
                    mDateTime_y.setError("Год может быть текущим или следующим");
                    focusView = mDateTime_y;
                    cancel = true;
                }
                tmp = Integer.parseInt(dateTime_m);
                if (tmp < 1 || tmp > 12) {
                    mDateTime_m.setError("В году 12 месяцев от 1 до 12");
                    focusView = mDateTime_m;
                    cancel = true;
                }
                int dayOfMonth;
                if (tmp == 1 || tmp == 3 || tmp == 5 || tmp == 7 || tmp == 8 || tmp == 10 || tmp == 12) {
                    dayOfMonth = 31;
                } else if (tmp == 2) {
                    dayOfMonth = 29;
                } else {
                    dayOfMonth = 30;
                }

                tmp = Integer.parseInt(dateTime_d);
                if (tmp < 1 || tmp > dayOfMonth) {
                    mDateTime_d.setError("В этом месяце от 1 до " + dayOfMonth + " дней");
                    focusView = mDateTime_d;
                    cancel = true;
                }
                tmp = Integer.parseInt(dateTime_h);
                if (tmp < 0 || tmp > 23) {
                    mDateTime_h.setError("В сутках 24 часа от 0 до 23");
                    focusView = mDateTime_h;
                    cancel = true;
                }
                tmp = Integer.parseInt(dateTime_min);
                if (tmp < 0 || tmp > 59) {
                    mDateTime_min.setError("В часе 60 минут от 0 до 59");
                    focusView = mDateTime_min;
                    cancel = true;
                }

            }


        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // плюемся интентом
            Intent data = new Intent();
            String dateTime = "";
            if (!TextUtils.isEmpty(dateTime_h)) {
                dateTime = "" + dateTime_y + "-" + dateTime_m + "-" + dateTime_d + " " + dateTime_h + ":" + dateTime_min + ":00";
            }

            data.putExtra("lastPhone", mPhone);
            data.putExtra("fio", fio);
            data.putExtra("fabule", fabule);
            data.putExtra("datetime", dateTime);
            data.putExtra("city", city);
            data.putExtra("type", "" + type);
            setResult(RESULT_OK, data);
            this.finish();
        }
    }

}






