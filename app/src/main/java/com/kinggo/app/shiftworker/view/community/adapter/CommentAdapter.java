package com.kinggo.app.shiftworker.view.community.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.CommunityContentFragment;
import com.kinggo.app.shiftworker.view.community.CommunityListFragment;
import com.kinggo.app.shiftworker.view.community.domain.Comment;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;
import com.kinggo.app.shiftworker.view.community.service.CommunityApiService;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private InputMethodManager imm;

    private ArrayList<Comment> comments;

    private CommunityContentFragment communityContentFragment;

    private UserInfo userInfo;
    private Context context;
    private Long postId;
    private EditText etx_write;

    public CommentAdapter(ArrayList<Comment> comments, Long postId, UserInfo userInfo) {
        this.comments = comments;
        this.postId = postId;
        this.userInfo = userInfo;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();

        communityContentFragment = (CommunityContentFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentByTag("f_community_content");

        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = LayoutInflater.from(context).inflate(R.layout.item_comment, viewGroup, false);
        CommentHolder commentHolder = new CommentHolder(v);

        return commentHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder commentHolder, int position) {
        if (comments.size() > position) {
            Comment comment = comments.get(position);

            commentHolder.tx_author.setText(comment.getAuthor());
            commentHolder.tx_content.setText(comment.getContent());
            commentHolder.tx_createDate.setText(comment.getCreatedDate().substring(0, 10));
        } else {
            // 가장 마지막에 댓글 등록 용 Layout 추가
            commentHolder.layout_comment_back.removeAllViews();
            commentHolder.layout_comment_back.addView(commentHolder.layout_comment_write);

            etx_write = commentHolder.etx_write;

            commentHolder.tx_comment_okay.setOnClickListener(view -> {
                String content = commentHolder.etx_write.getText().toString();
                if (content.length() > 0) {
                    imm.hideSoftInputFromWindow(etx_write.getWindowToken(), 0);

                    Comment comment = new Comment();
                    comment.setContent(content);
                    comment.setPostId(postId);

                    CommentAsyncTask commentAsyncTask = new CommentAsyncTask();
                    commentAsyncTask.execute(comment);
                } else
                    Toast.makeText(context, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();

            });
        }
    }

    @Override
    public int getItemCount() {
        if (userInfo != null && userInfo.getToken() != null && userInfo.getToken().length() > 0)
            return comments.size() + 1;
        else
            return comments.size();
    }

    class CommentAsyncTask extends AsyncTask<Comment, Void, Void> implements Callback<Comment> {
        @Override
        protected Void doInBackground(Comment... comments) {
            CommunityApiService
                    .getInstance()
                    .addComment(comments[0], userInfo.getToken())
                    .enqueue(this);
            return null;
        }

        @Override
        public void onResponse(Call<Comment> call, Response<Comment> response) {
            if (response.code() == HttpURLConnection.HTTP_OK) {
                etx_write.setText("");
                Toast.makeText(context, "댓글 등록 성공", Toast.LENGTH_SHORT).show();
                etx_write.setFocusable(false);

                // 댓글 갱신
                CommunityApiService
                        .getInstance()
                        .getComments(postId)
                        .enqueue(new Callback<List<Comment>>() {
                            @Override
                            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                                if (response.code() == HttpURLConnection.HTTP_OK) {
                                    communityContentFragment.resetComment(response.body());
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Comment>> call, Throwable t) {

                            }
                        });


            } else
                Toast.makeText(context, "댓글 등록 실패", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Call<Comment> call, Throwable t) {
            Toast.makeText(context, "댓글 등록 실패", Toast.LENGTH_SHORT).show();
        }
    }


    class CommentHolder extends RecyclerView.ViewHolder {

        private TextView tx_author;
        private TextView tx_content;
        private TextView tx_createDate;
        private EditText etx_write;
        private TextView tx_comment_okay;
        private RelativeLayout layout_comment_back;
        private LinearLayout layout_comment_write;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            tx_author = itemView.findViewById(R.id.item_comment_author);
            tx_content = itemView.findViewById(R.id.item_comment_content);
            tx_createDate = itemView.findViewById(R.id.item_comment_createDate);
            layout_comment_back = itemView.findViewById(R.id.layout_comment_back);

            // 댓글 등록 용 layout
            layout_comment_write = new LinearLayout(itemView.getContext());
            layout_comment_write.setOrientation(LinearLayout.HORIZONTAL);
            layout_comment_write.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout_comment_write.setWeightSum(1);

            etx_write = new EditText(itemView.getContext());
            LinearLayout.LayoutParams etx_write_param = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            etx_write_param.weight = 0.70f;
            etx_write.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
            etx_write.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            etx_write.setHint("댓글을 입력하세요");
            etx_write.setLayoutParams(etx_write_param);

            tx_comment_okay = new TextView(itemView.getContext());
            LinearLayout.LayoutParams tx_okay_param = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            tx_okay_param.weight = 0.30f;
            tx_okay_param.leftMargin = 20;
            tx_okay_param.rightMargin = 20;
            tx_comment_okay.setText("등록");
            tx_comment_okay.setGravity(Gravity.CENTER);
            tx_comment_okay.setTextColor(Color.parseColor("#FFFFFF"));
            tx_comment_okay.setBackgroundColor(Color.parseColor("#52b3d9"));
            tx_comment_okay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tx_comment_okay.setLayoutParams(tx_okay_param);

            layout_comment_write.addView(etx_write);
            layout_comment_write.addView(tx_comment_okay);
        }
    }

    public EditText getEtx_write() {
        return etx_write;
    }
}