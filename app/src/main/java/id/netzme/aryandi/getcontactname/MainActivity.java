package id.netzme.aryandi.getcontactname;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String EMAIL_SEPARATOR = "@";
    private static final String GOOGLE_ACCOUNT_IDENTIFIER = "com.google";

    private AccountManager accountManager;

    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountManager = AccountManager.get(getApplicationContext());
        contentResolver = this.getContentResolver();

        getFullNameFromGoogle();
    }

    private Account[] getGoogleAccounts() {
        return accountManager.getAccountsByType(GOOGLE_ACCOUNT_IDENTIFIER);
    }


    public String getFullNameFromGoogle() {
        Account[] accounts = getGoogleAccounts();

        if (accounts.length > 0) {
            Cursor cursor = queryEmailAccount(accounts);

            try {
                return findFullName(cursor, ContactsContract.Contacts.DISPLAY_NAME);
            } finally {
                cursor.close();
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

        return contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[] { ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER },
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

}
