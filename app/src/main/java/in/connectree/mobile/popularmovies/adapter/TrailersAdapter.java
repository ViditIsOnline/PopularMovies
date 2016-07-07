package in.connectree.mobile.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import in.connectree.mobile.popularmovies.ApplicationClass;
import in.connectree.mobile.popularmovies.DetailActivityFragment;
import in.connectree.mobile.popularmovies.R;

/**
 * Created by vidit on 11/06/16.
 */
public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.ViewHolder>{

private Context mContext;
public TrailersAdapter(Context context) {
        mContext = context;
        }

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private ImageView videoIcon;
    private TextView videoName;
    private TextView videoSite;
    private TextView videoQuality;
    private String key;

    public ViewHolder(View view) {
        super(view);
        videoIcon = (ImageView) view.findViewById(R.id.image_view_trailer);
        videoName = (TextView) view.findViewById(R.id.text_view_trailer_name);
        videoSite = (TextView) view.findViewById(R.id.text_view_trailer_site);
        videoQuality = (TextView) view.findViewById(R.id.text_view_trailer_quality);
        videoIcon.setOnClickListener(this);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public void onClick(View view) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v="+this.getKey())));
    }
}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder viewHolder = new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.trailer_layout, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        try {
            holder.videoName.setText(DetailActivityFragment.trailers_result.getJSONObject(position).getString("name"));
            holder.videoSite.setText(DetailActivityFragment.trailers_result.getJSONObject(position).getString("site"));
            holder.videoQuality.setText(DetailActivityFragment.trailers_result.getJSONObject(position).getString("size"));
            Glide.with(mContext).load(ApplicationClass.THUMBNAIL_URL + DetailActivityFragment.trailers_result.
                    getJSONObject(position).getString("key") + "/default.jpg").into(holder.videoIcon);
            holder.setKey(DetailActivityFragment.trailers_result.getJSONObject(position).getString("key"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        holder.mImageView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return DetailActivityFragment.trailers_result.length();
    }
}

