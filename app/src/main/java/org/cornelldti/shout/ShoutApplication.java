package org.cornelldti.shout;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by kaushikr on 2/1/18.
 */

public class ShoutApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
