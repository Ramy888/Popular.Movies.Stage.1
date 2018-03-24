package com.udacityexcercise.android.preferedmoviesfirst;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacityexcercise.android.preferedmoviesfirst.models.MovieData;

public class DetailActivity extends AppCompatActivity {

    private final String RATED = "Rated: ";
    private final String RELEASE_DATE = "Released: ";
    private ImageView mImageView;
    private TextView mVote_average, mRelease_date, mOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        mImageView = (ImageView) findViewById(R.id.detail_image_view);
        mVote_average = (TextView) findViewById(R.id.detail_vote_average);
        mRelease_date = (TextView) findViewById(R.id.detail_release_date);
        mOverview = (TextView) findViewById(R.id.detail_overview);

        MovieData data = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);

        setTitle(data.getOriginal_title());

        mVote_average.setText(RATED + data.getVote_average());
        mRelease_date.setText(RELEASE_DATE + data.getRelease_date());
        mOverview.setText(data.getOverview());
        Uri builder = Uri.parse("http://image.tmdb.org/t/p/w185/").buildUpon()
                .appendEncodedPath(data.getPoster_path())
                .build();
        Picasso.with(this).load(builder.toString()).fit().into(mImageView);


    }
}
