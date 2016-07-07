package in.connectree.mobile.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import in.connectree.mobile.popularmovies.ApplicationClass;
import in.connectree.mobile.popularmovies.DetailActivity;
import in.connectree.mobile.popularmovies.DetailActivityFragment;
import in.connectree.mobile.popularmovies.MainActivityFragment;
import in.connectree.mobile.popularmovies.R;

/**
 * Created by vidit on 14/02/16.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder>{

    private Context mContext;
    private FragmentActivity mFragmentActivity;
    private boolean mIsDualPane;

    public MoviesAdapter(Context context, FragmentActivity activity, boolean isDualPane) {
        mContext = context;
        mFragmentActivity = activity;
        mIsDualPane = isDualPane;
       }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_layout, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            Glide.with(mContext).load(ApplicationClass.IMAGE_BASE_URL + MainActivityFragment.movies_result.
                    getJSONObject(position).getString("poster_path")).placeholder(R.drawable.ic_error_picture).
                    into(holder.mImageView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.mImageView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return MainActivityFragment.movies_result.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mImageView;
        public ViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.image_view_main);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mIsDualPane){
                        mFragmentActivity.getSupportFragmentManager().beginTransaction().replace(R.id.movie_container,
                                DetailActivityFragment.newInstance((Integer)mImageView.getTag(),
                                        mIsDualPane))
                                .commit();
                    }
                    else {
                        Intent intent = new Intent(mContext, DetailActivity.class);
                        intent.putExtra("position", (Integer) mImageView.getTag());
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }
}
