package in.connectree.mobile.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;

import in.connectree.mobile.popularmovies.DetailActivityFragment;
import in.connectree.mobile.popularmovies.R;

/**
 * Created by vidit on 18/06/16.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private Context mContext;

    public ReviewsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView author, content;

        public ViewHolder(View itemView) {
            super(itemView);
            author = (TextView) itemView.findViewById(R.id.text_view_author);
            content = (TextView) itemView.findViewById(R.id.text_view_content);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.review_layout, parent, false));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            holder.author.setText(DetailActivityFragment.reviews_result.getJSONObject(position).getString("author"));
            holder.content.setText(DetailActivityFragment.reviews_result.getJSONObject(position).getString("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return DetailActivityFragment.reviews_result.length();
    }
}
