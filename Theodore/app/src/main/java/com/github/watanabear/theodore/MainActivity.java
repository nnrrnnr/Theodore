package com.github.watanabear.theodore;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.watanabear.theodore.helper.AccountUtils;
import com.github.watanabear.theodore.helper.AccountsHelper;
import com.meisolsson.githubsdk.core.TokenStore;
import com.meisolsson.githubsdk.model.User;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<User>>{

    private static final String EXTRA_KEY_REPOSITORY_NAME = "com.github.watanabear.theodore.EXTRA_KEY_REPOSITORY_NAME";

    private static final String EXTRA_KEY_SCREENSHOT = "com.github.watanabear.sampleapplication.EXTRA_KEY_SCREENSHOT";

    private List<User> orgs = Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        AccountManager m = AccountManager.get(this);
        try {
            Account a = AccountUtils.getAccount(m, this);
            if (a == null) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AccountsException e) {
            e.printStackTrace();
        }


        TokenStore tokenStore = TokenStore.getInstance(this);
        if (tokenStore.getToken() == null) {
            AccountManager manager = AccountManager.get(this);
            Account[] accounts = manager.getAccountsByType(getString(R.string.account_type));
            if (accounts.length > 0) {
                Account account = accounts[0];
                AccountsHelper.getUserToken(this, account);
                tokenStore.saveToken(AccountsHelper.getUserToken(this, account));
            }
        }

        byte[] sc = getIntent().getByteArrayExtra(EXTRA_KEY_SCREENSHOT);
        if (sc == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(sc, 0, sc.length);

        ImageView i = (ImageView) findViewById(R.id.my_image);
        i.setImageBitmap(bitmap);

        String repositoryName = getIntent().getStringExtra(EXTRA_KEY_REPOSITORY_NAME);
        if (repositoryName == null) {
            return;
        }
        TextView t = (TextView) findViewById(R.id.text_repository_name);
        t.setText(repositoryName);

    }

    @Override
    protected void onResume() {
        super.onResume();
        List<User> currentOrgs = orgs;
        if (currentOrgs != null && !currentOrgs.isEmpty()
                && !AccountUtils.isUser(this, currentOrgs.get(0))) {
            reloadOrgs();
        }
    }

    private void reloadOrgs() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {

    }
}
