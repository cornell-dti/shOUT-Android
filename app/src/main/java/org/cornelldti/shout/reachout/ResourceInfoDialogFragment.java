package org.cornelldti.shout.reachout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.cornelldti.shout.R;

import java.util.List;

/**
 * Created by kaushikr on 3/20/18.
 */

public class ResourceInfoDialogFragment extends BottomSheetDialogFragment {

    private Resource mResource;

    /* UI Elements */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            mResource = (Resource) bundle.getSerializable("resource");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View moreInfoResDialog = inflater.inflate(R.layout.more_info_resource_dialog, container, false);

        TextView nameTextView = moreInfoResDialog.findViewById(R.id.resource_info_name_text_view);
        TextView descriptionTextView = moreInfoResDialog.findViewById(R.id.resource_info_description_text_view);
        Button addressButton = moreInfoResDialog.findViewById(R.id.resource_info_directions_button);
        Button websiteButton = moreInfoResDialog.findViewById(R.id.resource_info_url_button);
        ListView mPhoneNumberList = moreInfoResDialog.findViewById(R.id.resource_info_phone_number_list_view);

        // SETUP DIALOG UI
        nameTextView.setText(mResource.getName());
        descriptionTextView.setText(mResource.getDescription());

        if (mResource.getUrl() == null) {
            websiteButton.setVisibility(View.GONE);
        } else {
            websiteButton.setOnClickListener(v -> {
                String uri = mResource.getUrl();

                if (!uri.startsWith("http://") && !uri.startsWith("https://")) // TODO is this safe?
                    uri = "http://" + uri;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(browserIntent);
            });
        }

        if (mResource.getAddress() == null) {
            addressButton.setVisibility(View.GONE);
        } else {
            addressButton.setOnClickListener(v -> {
                String uri = "google.navigation:q=" + mResource.getAddress();

                Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(navigationIntent);
            });
        }

        // TODO cleanup this hacky fix

        if (mResource.getAddress() == null && mResource.getUrl() == null) {
            moreInfoResDialog.findViewById(R.id.resource_info_actions).setVisibility(View.GONE);
            moreInfoResDialog.findViewById(R.id.resource_info_separator).setVisibility(View.GONE);
        }

        mPhoneNumberList.setAdapter(new PhoneAdapter(getContext(), mResource));

        // TODO add emergency confirmation popup w/ description

        mPhoneNumberList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);

            List<Phone> phones = mResource.getPhoneNumbers();
            Phone phone = phones.get(position);

            if (phone != null) {
                String number = phone.getNumber();

                intent.setData(Uri.parse("tel:" + number));

                Activity activity = getActivity();

                if (activity != null) {
                    if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        // todo Turn into resource
                        Toast.makeText(activity, "No application found to open phone numbers.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return moreInfoResDialog;
    }

    public static ResourceInfoDialogFragment newInstance(Resource resource) {
        ResourceInfoDialogFragment dialog = new ResourceInfoDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("resource", resource);

        dialog.setArguments(args);

        return dialog;
    }
}

