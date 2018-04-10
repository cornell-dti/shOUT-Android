package org.cornelldti.shout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Evan Welsh on 4/3/18.
 */
public class UnauthenticatedFragment extends AppCompatDialogFragment {
    private AuthenticateCallback authenticateCallback;

    interface AuthenticateCallback {
        void authenticate(AuthenticationResult result);

        interface AuthenticationResult {
            void failure();

            void success();
        }
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof AuthenticateCallback) {
            authenticateCallback = (AuthenticateCallback) context;
        }

        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.unauthenticated_layout, container, false);

        Button button = view.findViewById(R.id.unauthenticated_refresh_button);

        button.setOnClickListener(l -> {
            if (authenticateCallback != null) {
                authenticateCallback.authenticate(new AuthenticateCallback.AuthenticationResult() {
                    @Override
                    public void failure() {
                        // TODO
                    }

                    @Override
                    public void success() {
                        dismiss();
                    }
                });
            }

            // todo error out
        });

        return view;
    }

}
