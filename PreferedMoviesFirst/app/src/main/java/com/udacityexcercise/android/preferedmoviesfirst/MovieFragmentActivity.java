package com.udacityexcercise.android.preferedmoviesfirst;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.udacityexcercise.android.preferedmoviesfirst.Adapters.MoviesAdapter;
import com.udacityexcercise.android.preferedmoviesfirst.models.MovieData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class MovieFragmentActivity extends Fragment {

    public static final String TAG = MovieFragmentActivity.class.getSimpleName();

    ArrayList<MovieData> MovieList = new ArrayList<>();
    private MoviesAdapter adapter;

    public static final String EXTRA_TITLE = "title";
    String sortBy;

    public MovieFragmentActivity() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_movie_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.grid_view);
        adapter = new MoviesAdapter(MovieList, getActivity());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, MovieList.get(position));
                startActivity(intent);
            }
        }));

        return v;
    }

    private void fetchJSON() {

        Uri uri;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        sortBy = prefs.getString(getString(R.string.sort), getString(R.string.popularity));

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        if (sortBy.equals(getString(R.string.popularity))) {

            uri = Uri.parse("http://api.themoviedb.org/3/movie/popular?")
                    .buildUpon()
                    .appendQueryParameter(getString(R.string.API_KEY_ATTR), getString(R.string.MOVIEDB_API_KEY))
                    .build();

        } else{
            uri = Uri.parse("http://api.themoviedb.org/3/movie/top_rated?")
                    .buildUpon()
                    .appendQueryParameter(getString(R.string.API_KEY_ATTR), getString(R.string.MOVIEDB_API_KEY))
                    .build();
        }

        String url = uri.toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            for (int i = 0; i < 20; i++) {
                                JSONObject obj = response.getJSONArray("results").getJSONObject(i);
                                String title = obj.getString("original_title");
                                String poster = obj.getString("poster_path");
                                String popularity = obj.getString("popularity");
                                String overview = obj.getString("overview");
                                String release_date = obj.getString("release_date");
                                String vote_average = obj.getString("vote_average");
                                MovieList.add(new MovieData(title, poster, popularity, overview, release_date, vote_average));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                });

        queue.add(request);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetworkConnected())
            fetchJSON();
        else
            Toast.makeText(getContext(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!MovieList.isEmpty() && isNetworkConnected())
            MovieList.clear();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sort_by:
                startActivity(new Intent(getContext(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    public interface ClickListener {
        void onClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}

