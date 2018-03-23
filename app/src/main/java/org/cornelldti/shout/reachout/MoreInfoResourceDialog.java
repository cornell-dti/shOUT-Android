package org.cornelldti.shout.reachout;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.cornelldti.shout.R;

import java.util.List;

/**
 * Created by kaushikr on 3/20/18.
 */

public class MoreInfoResourceDialog extends BottomSheetDialogFragment {

    String name, description, url;

    TextView resourceName, resourceDescription, resourceURL;

    ListView phoneNumberList;

    String mResId;

    List<Phone> mPhones;

    public MoreInfoResourceDialog() {

    }

    public static MoreInfoResourceDialog newInstance(Resource resource, String resId) {
        MoreInfoResourceDialog dialog = new MoreInfoResourceDialog();

        Bundle args = new Bundle();
        args.putString("name", resource.getName());
        args.putString("description", resource.getDescription());
        args.putString("url", resource.getUrl());
        args.putString("resId", resId);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View moreInfoResDialog = inflater.inflate(R.layout.more_info_resource_dialog, container, false);
        resourceName = moreInfoResDialog.findViewById(R.id.name);
        resourceDescription = moreInfoResDialog.findViewById(R.id.description);
        resourceURL = moreInfoResDialog.findViewById(R.id.url);
        phoneNumberList = moreInfoResDialog.findViewById(R.id.phoneNumberList);


        // SETUP DIALOG UI
        resourceName.setText(name);
        resourceDescription.setText(description);
        resourceURL.setText(url);
        resourceURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        Query query = FirebaseFirestore.getInstance().collection("resources").document(mResId).collection("phones");
        query.get().addOnCompleteListener(task -> {
            mPhones = task.getResult().toObjects(Phone.class);
            phoneNumberList.setAdapter(new PhoneAdapter(getActivity(), mPhones));
        });
        phoneNumberList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + mPhones.get(position).getNumber()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        return moreInfoResDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
            description = bundle.getString("description");
            url = bundle.getString("url");
            mResId = bundle.getString("resId");
        }

    }

}

