package id.netzme.aryandi.getcontactname;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String EMAIL_SEPARATOR = "@";
    private static final String GOOGLE_ACCOUNT_IDENTIFIER = "com.google";
    private static final int REQUEST_GET_ACCOUNT = 112;

    private AccountManager accountManager;

    private ContentResolver contentResolver;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = AccountManager.get(getApplicationContext());
        contentResolver = this.getContentResolver();

        textView = (TextView) findViewById(R.id.textNama);

        if(android.os.Build.VERSION.SDK_INT > 22){
            if(isGETACCOUNTSAllowed()){
                // do your task

//                getMailAddress();

                String nama = getFullNameFromGoogle();

                if (nama != null){
                    textView.setText(nama);
                }

                return;
            }else{
                requestGET_ACCOUNTSPermission();
            }

        }


    }

    private Account[] getGoogleAccounts() {
        return accountManager.getAccountsByType(GOOGLE_ACCOUNT_IDENTIFIER);
    }


    public String getFullNameFromGoogle() {
        Account[] accounts = getGoogleAccounts();

        if (accounts.length > 0) {
            Cursor cursor = queryEmailAccount(accounts);

            if (cursor != null) {
                try {
                    return findFullName(cursor, ContactsContract.Contacts.DISPLAY_NAME);
                } finally {
                    cursor.close();
                }
            }
        }

        return null;
    }

    private String findFullName(Cursor cursor, String fieldName) {
        int fullNameColumnIndex = cursor.getColumnIndex(fieldName);

            while (cursor.moveToNext()) {
                String fullName = cursor.getString(fullNameColumnIndex);
                if (fullName != null && !fullName.isEmpty()) {
                    return fullName;
                }
            }

        return null;
    }

    private Cursor queryEmailAccount(Account[] accounts) {
        StringBuilder filterQuery = new StringBuilder();
        int length = accounts.length;
        int lastIndex = length - 1;
        List<String> accountNameList = new ArrayList<>();

        for (int index = 0; index < length; index++) {
            filterQuery.append(ContactsContract.CommonDataKinds.Email.DATA);
            filterQuery.append(" = ?");

            if (index < lastIndex) {
                filterQuery.append(" OR ");
            }

            accountNameList.add(accounts[index].name);
        }

//        return contentResolver.query(
//                ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Email.CONTACT_ID,
//                        ContactsContract.Data.DISPLAY_NAME },
//                filterQuery.toString(),
//                accountNameList.toArray(new String[length]), null);

        return contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                filterQuery.toString(),
                accountNameList.toArray(new String[length]), null);

    }

    public String getGoogleUsername() {
        Account[] accounts = getGoogleAccounts();

        for (Account account : accounts) {
            String email = account.name;

            if (email != null) {
                int separatorIndex = email.indexOf(EMAIL_SEPARATOR);

                if (separatorIndex > 0) {
                    return email.substring(0, separatorIndex);
                }
            }
        }

        return null;
    }

    private boolean isGETACCOUNTSAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }


    //if you don't have the permission then Requesting for permission
    private void requestGET_ACCOUNTSPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.GET_ACCOUNTS)){


        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.GET_ACCOUNTS},REQUEST_GET_ACCOUNT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == REQUEST_GET_ACCOUNT){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this,"Thanks You For Permission Granted ", Toast.LENGTH_LONG).show();

//                getMailAddress();

                String nama = getFullNameFromGoogle();

                if (nama != null){
                    textView.setText(nama);
                }

            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }

    }

    public void getMailAddress(){

        String possibleEmail = null;

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(MainActivity.this).getAccountsByType(
                GOOGLE_ACCOUNT_IDENTIFIER);
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                Log.i("MY_EMAIL_count", "" + possibleEmail);
            }
        }
    }

}
