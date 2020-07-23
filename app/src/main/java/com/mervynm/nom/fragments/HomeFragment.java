package com.mervynm.nom.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mervynm.nom.R;
import com.mervynm.nom.adapters.PostAdapter;
import com.mervynm.nom.models.Location;
import com.mervynm.nom.models.Post;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    SwipeRefreshLayout swipeContainer;
    RecyclerView recyclerViewPosts;
    List<Post> posts;
    PostAdapter adapter;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSwipeRefreshLayout(view);
        setupRecyclerView(view);
        queryPosts();
    }

    private void setupSwipeRefreshLayout(View view) {
        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts();
                swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setupRecyclerView(View view) {
        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);
        posts = new ArrayList<>();
        PostAdapter.OnLocationClickListener onLocationClickListener = new PostAdapter.OnLocationClickListener() {
            @Override
            public void OnLocationClicked(int position) {
                try {
                    createLocationDialog(posts.get(position));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        PostAdapter.OnRecipieClickListener onRecipieClickListener = new PostAdapter.OnRecipieClickListener() {
            @Override
            public void OnRecipeClicked(int position) {
                createRecipeDialog(posts.get(position));
            }
        };
        adapter = new PostAdapter(getContext(), posts, onLocationClickListener, onRecipieClickListener);
        recyclerViewPosts.setAdapter(adapter);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void createLocationDialog(Post post) throws ParseException {
        Location postLocation = post.getLocation();
        String name = postLocation.fetchIfNeeded().getString("name");
        double rating = postLocation.fetchIfNeeded().getDouble("rating");
        String address = postLocation.fetchIfNeeded().getString("address");
        int priceLevel = postLocation.fetchIfNeeded().getInt("priceLevel");
        LocationDialogFragment locationDialogFragment = LocationDialogFragment.newInstance(name,
                                                                                           rating,
                                                                                           address,
                                                                                           priceLevel);
        assert getFragmentManager() != null;
        locationDialogFragment.show(getFragmentManager(), "fragment_location_dialog");
    }

    private void createRecipeDialog(Post post) {
        String recipeUrl = post.getRecipeUrl();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipeUrl));
        if (browserIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            startActivity(browserIntent);
        }
    }

    protected void queryPosts() {
        final List<ParseQuery<Post>> followingUsersPosts = new ArrayList<>();
        final List<ParseUser> usernames = new ArrayList<>();
        ParseQuery<ParseObject> following = ParseUser.getCurrentUser().getRelation("following").getQuery();
        following.include("User");
        following.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject o : objects) {
                    Log.i("HomeFragment", "the following user is " + o.getString("username"));
                    usernames.add((ParseUser) o);
                }
                for (ParseUser user : usernames) {
                    followingUsersPosts.add(ParseQuery.getQuery(Post.class).whereEqualTo(Post.KEY_AUTHOR, user));
                }
                followingUsersPosts.add(ParseQuery.getQuery(Post.class).whereEqualTo(Post.KEY_AUTHOR, ParseUser.getCurrentUser()));
                ParseQuery<Post> query = ParseQuery.or(followingUsersPosts);
                query.include(Post.KEY_AUTHOR);
                query.setLimit(20);
                query.addDescendingOrder(Post.KEY_CREATED_AT);
                query.findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> postList, ParseException e) {
                        if (e != null) {
                            Log.e("Homefragment", "Issue with getting posts", e);
                            return;
                        }
                        for (Post post : postList) {
                            Log.i("Homefragment", "Post " + post.getDescription() +  ", username " + post.getAuthor().getUsername() + " ," + post.getPrice());
                        }
                        adapter.clear();
                        adapter.addAll(postList);
                    }
                });
            }
        });
    }
}