package com.kinggo.app.shiftworker.view.community.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.domain.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sangsik.kim
 */
public class PostAdapter extends BaseAdapter {

    private final List<Post> posts;
    private final LayoutInflater inflater;
    private final int layout;

    public PostAdapter(Context context, int layout, List<Post> posts) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
        this.posts = posts != null ? posts : new ArrayList<>();
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(layout, viewGroup, false);
        }
        TextView title = view.findViewById(R.id.tv_post_item_title);
        TextView author = view.findViewById(R.id.tv_post_item_author);
        TextView createdDate = view.findViewById(R.id.tv_post_item_createdDate);
        TextView commentsCount = view.findViewById(R.id.tv_post_item_comments_count);

        Post post = (Post) getItem(i);

        title.setText(post.getTitle());
        author.setText(post.getAuthor());
        createdDate.setText(post.getCreatedDate().substring(0,10));
        if(Integer.parseInt(post.getCommentsCount()) > 0)
        {
            commentsCount.setText(post.getCommentsCount());
        }else
            commentsCount.setText("");
        return view;
    }
}
