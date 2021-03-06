package com.mervynm.nom.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brouding.doubletaplikeview.DoubleTapLikeView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.Target;
import com.hootsuite.nachos.NachoTextView;
import com.mervynm.nom.R;
import com.mervynm.nom.external.TimeFormatter;
import com.mervynm.nom.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> implements Filterable {

    public interface OnProfilePictureClickListener {
        void OnProfilePictureClicked(int position);
    }

    public interface OnLocationClickListener {
        void OnLocationClicked(int position);
    }

    public interface OnRecipeClickListener {
        void OnRecipeClicked(int position);
    }

    Context context;
    List<Post> posts;
    List<Post> postListFull;
    List<Post> postsCreatedAt;
    OnLocationClickListener locationClickListener;
    OnRecipeClickListener recipeClickListener;
    OnProfilePictureClickListener profilePictureClickListener;

    public PostAdapter(Context context, List<Post> posts, OnProfilePictureClickListener profilePictureClickListener,
                                                          OnLocationClickListener locationClickListener,
                                                          OnRecipeClickListener recipeClickListener) {
        this.context = context;
        this.posts = posts;
        postListFull = new ArrayList<>(posts);
        postsCreatedAt = new ArrayList<>(posts);
        this.profilePictureClickListener = profilePictureClickListener;
        this.locationClickListener = locationClickListener;
        this.recipeClickListener = recipeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View postView = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(postView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public Filter getFilter() {
        return postFilter;
    }

    private Filter postFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Post> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(postListFull);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Post post : postListFull)  {
                    if (!post.getTags().isEmpty() && post.getTagsAsLowerCase().contains(filterPattern)) {
                        filteredList.add(post);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            posts.clear();
            postsCreatedAt.clear();
            posts.addAll( (List<Post>) filterResults.values);
            postsCreatedAt.addAll( (List<Post>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public void sortByCreatedAt() {
        posts.clear();
        posts.addAll(postsCreatedAt);
        notifyDataSetChanged();
    }

    public void sortByDistance(double lat, double longi) {
        TreeMap<Double, List<Post>> map = new TreeMap<>();
        List<Post> noLocationOrLatLong = new ArrayList<>();
        for (Post post : posts) {
            if (post.getLocation() != null) {
                ParseGeoPoint latLong = null;
                try {
                    latLong = post.getLocation().fetchIfNeeded().getParseGeoPoint("latLong");
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.i("PostAdapter", "this is the post causing trouble " + post.getObjectId());
                    noLocationOrLatLong.add(post);
                }
                if (latLong != null) {
                    Double distance = distance(latLong.getLatitude(), latLong.getLongitude(), lat, longi);
                    if (map.containsKey(distance)) {
                        List<Post> otherPostsWithSamePrice = map.get(distance);
                        assert otherPostsWithSamePrice != null;
                        otherPostsWithSamePrice.add(post);
                        map.put(distance, otherPostsWithSamePrice);
                    }
                    else {
                        List<Post> onePostList = new ArrayList<>(1);
                        onePostList.add(post);
                        map.put(distance, onePostList);
                    }
                }
                else {
                    noLocationOrLatLong.add(post);
                }
            }
            else {
                noLocationOrLatLong.add(post);
            }
        }
        posts.clear();
        Collection<List<Post>> values = map.values();
        for (List<Post> postsWithDistanceSorted : values) {
            posts.addAll(postsWithDistanceSorted);
        }
        posts.addAll(noLocationOrLatLong);
        notifyDataSetChanged();
        Log.i("PostAdapter", "sorted");
    }

    public double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    public void sortByPrice() {
        TreeMap<Double, List<Post>> map = new TreeMap<>();
        List<Post> postsWithNoPrice = new ArrayList<>();
        for (Post post : posts) {
            double price = post.getPrice();
            if (price != 0) {
                if (map.containsKey(price)) {
                    List<Post> otherPostsWithSamePrice = map.get(price);
                    assert otherPostsWithSamePrice != null;
                    otherPostsWithSamePrice.add(post);
                    map.put(price, otherPostsWithSamePrice);
                }
                else {
                    List<Post> onePostList = new ArrayList<>();
                    onePostList.add(post);
                    map.put(post.getPrice(), onePostList);
                }
            }
            else {
                postsWithNoPrice.add(post);
            }
        }
        posts.clear();
        Collection<List<Post>> values = map.values();
        for (List<Post> postsWithPriceSorted : values) {
            posts.addAll(postsWithPriceSorted);
        }
        posts.addAll(postsWithNoPrice);
        notifyDataSetChanged();
    }

    public void clear() {
        posts.clear();
        postListFull.clear();
        postsCreatedAt.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> postList) {
        posts.addAll(postList);
        postListFull.addAll(postList);
        postsCreatedAt.addAll(postList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfilePicture;
        TextView textViewUsername;
        ImageView imageViewPostImage;
        ImageView imageViewLike;
        ImageView imageViewLocation;
        ImageView imageViewPrice;
        ImageView imageViewRecipe;
        TextView textViewLikeAmount;
        TextView textViewPriceAmount;
        DoubleTapLikeView doubleTapLike;
        TextView textViewDescription;
        TextView textViewCreatedAt;
        NachoTextView nachoTextViewTags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setupVariables(itemView);
            setupOnClickListeners();
        }

        private void setupVariables(View itemView) {
            imageViewProfilePicture = itemView.findViewById(R.id.imageViewProfilePicture);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            imageViewPostImage = itemView.findViewById(R.id.imageViewPostImage);
            doubleTapLike = itemView.findViewById(R.id.doubleTapLike);
            imageViewLike = itemView.findViewById(R.id.imageViewLike);
            imageViewLocation = itemView.findViewById(R.id.imageViewLocation);
            imageViewPrice = itemView.findViewById(R.id.imageViewPrice);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
            textViewLikeAmount = itemView.findViewById(R.id.textViewLikeAmount);
            textViewPriceAmount = itemView.findViewById(R.id.textViewPriceAmount);
            textViewPriceAmount.setVisibility(View.GONE);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewCreatedAt = itemView.findViewById(R.id.textViewCreatedAt);
            nachoTextViewTags = itemView.findViewById(R.id.nachoTextViewTags);
        }

        private void setupOnClickListeners() {
            imageViewLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLikeClick(false);
                }
            });
            imageViewPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPriceClick();
                }
            });
            imageViewRecipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRecipeClick();
                }
            });
        }

        private void onLikeClick(final boolean isDoubleTap) {
            imageViewLike.setClickable(false);
            doubleTapLike.setClickable(false);
            final Post clickedPost = posts.get(getAdapterPosition());
            final boolean[] noIssuesWithSaving = {true};
            ParseQuery<ParseUser> query = clickedPost.getUsersWhoLiked().getQuery();
            query.include("User");
            query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        if (isDoubleTap) {
                            imageViewLike.setImageResource(R.drawable.ic_baseline_favorite_24);
                            imageViewLike.setClickable(true);
                            doubleTapLike.setClickable(true);
                            return;
                        }
                        changeLike(clickedPost, -1);
                        clickedPost.removeLike(ParseUser.getCurrentUser());
                    }
                    else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        changeLike(clickedPost, 1);
                        clickedPost.addLike(ParseUser.getCurrentUser());
                    }
                    else {
                        Toast.makeText(context, "Error, cannot like " + e.getCode(), Toast.LENGTH_SHORT).show();
                        noIssuesWithSaving[0] = false;
                    }
                    if (noIssuesWithSaving[0]) {
                        clickedPost.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.i("PostAdapter", "Error in liking");
                                }
                            }
                        });
                    }
                    imageViewLike.setClickable(true);
                    doubleTapLike.setClickable(true);
                }
            });
        }

        @SuppressLint("DefaultLocale")
        private void changeLike(Post clickedPost, int change) {
            clickedPost.setLikeCount(clickedPost.getLikeCount()+change);
            if (change == 1) {
                imageViewLike.setImageResource(R.drawable.ic_baseline_favorite_24);
            }
            else {
                imageViewLike.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            }
            textViewLikeAmount.setText(String.format("%d Likes", clickedPost.getLikeCount()));
        }

        private void onPriceClick() {
            Post clickedPost = posts.get(getAdapterPosition());
            if (textViewPriceAmount.getVisibility() == View.GONE) {
                textViewPriceAmount.setVisibility(View.VISIBLE);
                imageViewPrice.setImageResource(R.drawable.ic_money_clicked);
                textViewPriceAmount.setText(String.format("$%s", clickedPost.getPrice()));
            }
            else {
                textViewPriceAmount.setVisibility(View.GONE);
                imageViewPrice.setImageResource(R.drawable.ic_baseline_attach_money_24);
                textViewPriceAmount.setText("");
            }
        }

        private void onRecipeClick() {
            Toast.makeText(context, "you have pressed recipe at position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }

        @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
        public void bind(Post post) {
            Glide.with(context).load(Objects.requireNonNull(post.getAuthor().getParseFile("profilePicture")).getUrl())
                               .transform(new CircleCrop())
                               .into(imageViewProfilePicture);
            imageViewProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    profilePictureClickListener.OnProfilePictureClicked(getAdapterPosition());
                }
            });
            String username = post.getAuthor().getUsername();
            textViewUsername.setText(username);
            String postImageUrl = post.getImage().getUrl();
            Glide.with(context).load(postImageUrl)
                               .override(Target.SIZE_ORIGINAL)
                               .into(imageViewPostImage);
            Glide.with(context).load(postImageUrl)
                    .override(Target.SIZE_ORIGINAL)
                    .into(doubleTapLike.imageView);
            doubleTapLike.getLayoutParams().height = imageViewPostImage.getLayoutParams().height;
            doubleTapLike.setOnTapListener(new DoubleTapLikeView.OnTapListener() {
                @Override
                public void onDoubleTap(View view) {
                    onLikeClick(true);
                }
                @Override
                public void onTap() {
                }
            });
            if (post.getLocation() != null) {
                imageViewLocation.setVisibility(View.VISIBLE);
                imageViewLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        locationClickListener.OnLocationClicked(getAdapterPosition());
                    }
                });
            }
            else {
                imageViewLocation.setVisibility(View.GONE);
            }
            ParseQuery<ParseUser> query = post.getUsersWhoLiked().getQuery();
            query.include("User");
            query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if (e == null) {
                        imageViewLike.setImageResource(R.drawable.ic_baseline_favorite_24);
                    }
                    else {
                        imageViewLike.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                }
            });
            textViewLikeAmount.setText(String.format("%d Likes", post.getLikeCount()));
            textViewPriceAmount.setVisibility(View.GONE);
            imageViewPrice.setImageResource(R.drawable.ic_baseline_attach_money_24);
            if (post.getPrice() == 0) {
                imageViewPrice.setVisibility(View.GONE);
            }
            else {
                imageViewPrice.setVisibility(View.VISIBLE);
            }
            if (!post.getHomemade() || post.getRecipeUrl() == null) {
                imageViewRecipe.setVisibility(View.GONE);
            }
            else if (post.getRecipeUrl() != null){
                imageViewRecipe.setVisibility(View.VISIBLE);
                imageViewRecipe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recipeClickListener.OnRecipeClicked(getAdapterPosition());
                    }
                });
            }
            SpannableString usernameAndDescription = new SpannableString(username + " " + post.getDescription());
            usernameAndDescription.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewDescription.setText(usernameAndDescription);
            textViewCreatedAt.setText(TimeFormatter.getTimeStamp(post.getCreatedAt().toString()));
            if (!post.getTags().isEmpty()) {
                nachoTextViewTags.setVisibility(View.VISIBLE);
                nachoTextViewTags.setText(post.getTags());
            }
            else {
                nachoTextViewTags.setVisibility(View.GONE);
            }
        }
    }
}
