package com.example.android.popularmovies.view.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MoviesContract;
import com.example.android.popularmovies.data.MoviesContract.MoviesEntry;
import com.example.android.popularmovies.model.MovieUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;
    private ListItemClickListener mListener;

    public MovieAdapter (Context context, ListItemClickListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public interface ListItemClickListener {
        void onListItemClick(String id);
        void onListItemStar(String id);
        void onListItemUnstar(String id);
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.film_list_item, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        ImageView listItemImageView;
        LikeButton listItemFavoriteButton;

        public MovieViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            listItemImageView = (ImageView) itemView.findViewById(R.id.iv_item_poster);
            listItemImageView.setOnClickListener(this);

            listItemFavoriteButton = (LikeButton) itemView.findViewById(R.id.btn_item_star);
            listItemFavoriteButton.setOnLikeListener(
                    new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            String movieId = (String) likeButton.getTag();
                            mListener.onListItemStar(movieId);
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            String movieId = (String) likeButton.getTag();
                            mListener.onListItemUnstar(movieId);
                        }
                    });
        }

        void bind(int position) {
            /* Move cursor to my position */
            if (!mCursor.moveToPosition(position)) {
                Log.e(TAG, "Error moving cursor to position " + position);
                return;
            }

            /* Get needed info from database */
            String poster_path = mCursor.getString(mCursor.getColumnIndex(MoviesEntry.COLUMN_NAME_POSTER));
            String movieId = mCursor.getString(mCursor.getColumnIndex(MoviesEntry.COLUMN_NAME_MOVIE_ID));

            /* Update view */
            itemView.setTag(movieId);
            listItemFavoriteButton.setTag(movieId);

            Cursor cursor = mContext.getContentResolver().query(
                    MoviesContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon().appendPath(movieId).build(),
                    null, null, null, null);

            if (cursor.getCount() > 0) {
                listItemFavoriteButton.setLiked(true);
            } else {
                listItemFavoriteButton.setLiked(false);
            }

            Picasso.with(listItemImageView.getContext())
                    .load(MovieUtils.getPosterUrl(poster_path, MovieUtils.POSTER_SIZE_MOBILE))
                    .into(listItemImageView);
        }

        @Override
        public void onClick(View v) {
            String movieId = (String) itemView.getTag();

            switch (v.getId()) {
                case R.id.iv_item_poster:
                    mListener.onListItemClick(movieId);
                    break;
                default:
                    break;
            }

        }
    }
}