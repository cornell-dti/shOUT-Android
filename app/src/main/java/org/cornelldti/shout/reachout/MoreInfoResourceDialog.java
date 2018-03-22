package org.cornelldti.shout.reachout;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

public class MoreInfoResourceDialog extends AppCompatDialogFragment {

    String name, description, url;

//    ArrayList<String> phoneNumbers, phoneDescriptions, phoneLabels;

    TextView resourceName, resourceDescription, resourceURL;

    ListView phoneNumberList;

    String mResId;

    public MoreInfoResourceDialog() {

    }

    public static MoreInfoResourceDialog newInstance(Resource resource, String resId) {
        MoreInfoResourceDialog dialog = new MoreInfoResourceDialog();

        Bundle args = new Bundle();
        args.putString("name", resource.getName());
        args.putString("description", resource.getDescription());
        args.putString("url", resource.getUrl());
        args.putString("resId", resId);

//        if (resource.getPhones() != null) {
//            Iterator<Phone> phonesIterator = resource.getPhones().iterator();
//            ArrayList<String> phoneNumbers = new ArrayList<String>();
//            ArrayList<String> phoneLabels = new ArrayList<String>();
//            ArrayList<String> phoneDescriptions = new ArrayList<String>();
//            while (phonesIterator.hasNext()) {
//                Phone phone = phonesIterator.next();
//
//                phoneNumbers.add(phone.getNumber());
//                phoneLabels.add(phone.getLabel());
//                phoneDescriptions.add(phone.getDescription());
//            }
//
//            args.putStringArrayList("phoneNumbers", phoneNumbers);
//            args.putStringArrayList("phoneLabels", phoneLabels);
//            args.putStringArrayList("phoneDescriptions", phoneDescriptions);
//        }

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View moreInfoResDialog = inflater.inflate(R.layout.more_info_resource_dialog, container, false);
        ImageButton closeButton = moreInfoResDialog.findViewById(R.id.goBack);
        resourceName = moreInfoResDialog.findViewById(R.id.name);
        resourceDescription = moreInfoResDialog.findViewById(R.id.description);
        resourceURL = moreInfoResDialog.findViewById(R.id.url);
        phoneNumberList = moreInfoResDialog.findViewById(R.id.phoneNumberList);

        closeButton.setOnClickListener(view -> {
            /* Manually hide the keyboard to ensure it doesn't stick around */
            dismiss();
        });

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
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Phone> phones = task.getResult().toObjects(Phone.class);
                phoneNumberList.setAdapter(new PhoneAdapter(getActivity(), phones));
            }
        });

//        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                for(DocumentSnapshot snapshot : documentSnapshots)
//                {
//                    Toast.makeText(getActivity(), snapshot.getString("name"), Toast.LENGTH_LONG ).show();
//                }
//            }
//        });

//        for(int i = 0; i < phoneNumbers.size(); i++)
//        {
//            Phone p = new Phone(phoneNumbers.get(i), phoneLabels.get(i));
//            if(phoneDescriptions.get(i))
//            phones.add();
//        }
//        phoneNumberList.setAdapter(inflater, );
//        if (phoneDescriptions.size() == phoneNumbers.size() && phoneDescriptions.size() == phoneLabels.size() && phoneLabels.size() == phoneNumbers.size()) {
//            Toast.makeText(getActivity(), "ALL SAME SIZE", Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            Toast.makeText(getActivity(), "NOT ALL SAME SIZE", Toast.LENGTH_SHORT).show();
//        }
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
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        /* This ensures the dialog fills the entire screen... */
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}

