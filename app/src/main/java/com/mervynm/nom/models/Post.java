package com.mervynm.nom.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_HOMEMADE = "homemade";
    public static final String KEY_PRICE = "price";
    public static final String KEY_LIKED_BY_CURRENT_USER = "likedByCurrentUser";
    public static final String KEY_USERS_WHO_LIKED = "usersWhoLiked";
    public static final String KEY_LIKE_COUNT = "likeCount";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_RECIPE_URL = "recipeUrl";

    public Post() {}

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public void setAuthor(ParseUser author) {
        put(KEY_AUTHOR, author);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setKeyDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public List<String> getTags() {
        return getList(KEY_TAGS);
    }

    public String getTagsAsLowerCase() {
        StringBuilder tags = new StringBuilder();
        List<String> tagList = getTags();
        for (String s : tagList) {
            tags.append(s.toLowerCase()).append(" ");
        }
        return tags.toString();
    }

    public void setTags(List<String> tags) {
        put(KEY_TAGS, tags);
    }

    public Boolean getHomemade() {
        return getBoolean(KEY_HOMEMADE);
    }

    public void setHomemade(Boolean homemade) {
        put(KEY_HOMEMADE, homemade);
    }

    public double getPrice() {
        return getDouble(KEY_PRICE);
    }

    public void setPrice(double price) {
        put(KEY_PRICE, price);
    }

    public Boolean getLikedByCurrentUser() {
        return getBoolean(KEY_LIKED_BY_CURRENT_USER);
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        put(KEY_LIKED_BY_CURRENT_USER, likedByCurrentUser);
    }

    public int getLikeCount() {
        return getInt(KEY_LIKE_COUNT);
    }

    public void setLikeCount(int likeCount) {
        put(KEY_LIKE_COUNT, likeCount);
    }

    public ParseRelation<ParseUser> getUsersWhoLiked() {
        return getRelation(KEY_USERS_WHO_LIKED);
    }

    public void addLike(ParseUser user) {
        getUsersWhoLiked().add(user);
        saveInBackground();
    }

    public void removeLike(ParseUser user) {
        getUsersWhoLiked().remove(user);
        saveInBackground();
    }

    public Location getLocation() {
        return (Location) getParseObject(KEY_LOCATION);
    }

    public void setLocation(Location location) {
        put(KEY_LOCATION, location);
    }

    public String getRecipeUrl() {
        return getString(KEY_RECIPE_URL);
    }

    public void setRecipeUrl(String recipeUrl) {
        put(KEY_RECIPE_URL, recipeUrl);
    }
}
