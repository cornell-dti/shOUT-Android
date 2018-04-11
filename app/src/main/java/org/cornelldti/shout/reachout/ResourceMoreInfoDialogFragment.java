package org.cornelldti.shout.reachout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.cornelldti.shout.R;

import java.util.List;

/**
 * Created by kaushikr on 3/20/18.
 */

public class ResourceMoreInfoDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ResourceMoreInfoDialogFragment";
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
        TextView emailTextView = moreInfoResDialog.findViewById(R.id.resource_item_email_label_text_view);

        ConstraintLayout emailButton = moreInfoResDialog.findViewById(R.id.resource_info_email_button);
        ConstraintLayout addressButton = moreInfoResDialog.findViewById(R.id.resource_info_directions_button);
        ConstraintLayout websiteButton = moreInfoResDialog.findViewById(R.id.resource_info_url_button);

        ListView mPhoneNumberList = moreInfoResDialog.findViewById(R.id.resource_info_phone_number_list_view);

        // SETUP DIALOG UI
        nameTextView.setText(mResource.getName());
        descriptionTextView.setText(mResource.getDescription());

        if(mResource.getEmail() == null)
        {
            emailButton.setVisibility(View.GONE);
        }
        else
        {
            emailTextView.setText(mResource.getEmail());
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",mResource.getEmail(), null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            });
        }

        if (mResource.getUrl() == null) {
            websiteButton.setVisibility(View.GONE);
            moreInfoResDialog.findViewById(R.id.resource_info_separator_c).setVisibility(View.INVISIBLE);
        } else {
            String url = mResource.getUrl();

            final Uri uri = Uri.parse(url);

            TextView website = moreInfoResDialog.findViewById(R.id.resource_item_website_subtitle_text_view);
            website.setText(uri.getHost());

            websiteButton.setOnClickListener(v -> {


                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            });
        }

        if (mResource.getAddress() == null) {
            addressButton.setVisibility(View.GONE);
            moreInfoResDialog.findViewById(R.id.resource_info_separator).setVisibility(View.INVISIBLE);
        } else {
            TextView address = moreInfoResDialog.findViewById(R.id.resource_item_directions_subtitle_text_view);
            address.setText(mResource.getAddress());

            addressButton.setOnClickListener(v -> {
                String uri = "google.navigation:q=" + mResource.getAddress();

                Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(navigationIntent);
            });
        }

        // TODO cleanup this hacky fix

        mPhoneNumberList.setAdapter(new PhoneAdapter(getContext(), mResource));

        // TODO add emergency confirmation popup w/ mDescription

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

    public static ResourceMoreInfoDialogFragment newInstance(Resource resource) {
        ResourceMoreInfoDialogFragment dialog = new ResourceMoreInfoDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("resource", resource);

        dialog.setArguments(args);

        return dialog;
    }
}

