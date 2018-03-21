package org.cornelldti.shout.reachout;

///**
// * Created by Evan Welsh on 3/1/18.
// */
//
//public class ReachOutAdapter extends FirestoreRecyclerAdapter<Resource, ReachOutAdapter.ViewHolder> {
//
////    private final Consumer<Uri> callback;
//
//    private Query query;
//
//    static class ViewHolder extends RecyclerView.ViewHolder {
//        private TextView title, description;
//        private Button website, directions;
//
//        ViewHolder(View v) {
//            super(v);
//
//            /* Cache views */
//            title = v.findViewById(R.id.resource_item_title);
//            description = v.findViewById(R.id.resource_item_description);
////            website = v.findViewById(R.id.websiteButton);
////            directions = v.findViewById(R.id.directionsButton);
//        }
//    }
//
////    public ReachOutAdapter(Query query)
////    {
////        this.query = query;
////    }
//
////    private ReachOutAdapter(FirestoreRecyclerOptions<Resource> options, Consumer<Uri> callback) {
////        super(options);
////        this.callback = callback;
////    }
//
////    static ReachOutAdapter construct(Fragment fragment, Consumer<Uri> callback) {
////        FirebaseFirestore database = FirebaseFirestore.getInstance();
////        Query query = database.collection("resources"); // ORDER BY ORDERING TODO
////
////        FirestoreRecyclerOptions<Resource> options = new FirestoreRecyclerOptions.Builder<Resource>()
////                .setQuery(query, Resource.class)
////                .setLifecycleOwner(fragment)
////                .build();
////
////        return new ReachOutAdapter(options, callback);
////    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // create a new view
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resource_item, parent, false);
//        // set the view's size, margins, paddings and layout parameters
//        return new ViewHolder(v);
//    }
//
//
//    @Override
//    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Resource model) {
//        holder.title.setText(model.getName());
//        holder.description.setText(model.getDescription());
//
////        final String website = model.getWebsite();
//
////        holder.website.setOnClickListener(listener -> {
////            Uri uri = Uri.parse(website);
////            this.callback.apply(uri);
////        });
//
//    }
//}
