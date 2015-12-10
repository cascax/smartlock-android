package xyz.codeme.smart321;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "userName";
    public static final int RESULT_OK = 1;

    private EditText mEditUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditUserName = (EditText) findViewById(R.id.edit_user);
    }

    public void clickOkButton(View v) {
        String userName = mEditUserName.getText().toString();
        if(userName.length() < 1) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_info)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
        } else {
            Intent i = new Intent();
            i.putExtra(EXTRA_USER_NAME, userName);
            setResult(RESULT_OK, i);
            this.finish();
        }
    }

}
