package com.kinggo.app.shiftworker.view.community;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.domain.Post;
import com.kinggo.app.shiftworker.view.community.service.CommunityApiService;

import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class CommunityBoardWrite extends Fragment {

    private final String TAG = "CommunityBoardWrite";

    private CommunityListFragment communityListFragment;
    private InputMethodManager imm;

    private String token;
    @BindView(R.id.etx_board_write_title)
    EditText etx_title;

    @BindView(R.id.etx_board_write_content)
    EditText etx_content;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_community_board_write, null);
        ButterKnife.bind(this, view);

        assert getFragmentManager() != null;
        communityListFragment = ((CommunityListFragment) getFragmentManager().findFragmentByTag("f_com"));

        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        assert getArguments() != null;
        token = getArguments().getString("token");
        Log.d(TAG, "token : " + token);

        return view;
    }

    @OnClick({R.id.layout_board_write_back, R.id.tx_board_write_okay, R.id.tx_board_write_cancel})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.layout_board_write_back:
                break;
            case R.id.tx_board_write_okay:
                String title = etx_title.getText().toString().trim();
                String content = etx_content.getText().toString().trim();
                if (title.length() > 0 && content.length() > 0) {

                    BoardWriteAsyncTask boardWriteAsyncTask = new BoardWriteAsyncTask();

                    Post post = new Post();
                    post.setTitle(title);
                    post.setContent(content);
                    boardWriteAsyncTask.execute(post);

                } else
                    Toast.makeText(getContext(), "제목 혹은 내용을 입력하지 않았습니다", LENGTH_SHORT).show();

                break;
            case R.id.tx_board_write_cancel:
                backFragment();
                break;
        }
    }

    class BoardWriteAsyncTask extends AsyncTask<Post, Void, Void> implements Callback<Post> {
        @Override
        protected Void doInBackground(Post... posts) {
            CommunityApiService
                    .getInstance()
                    .writePost(posts[0], token)
                    .enqueue(this);
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "onPostExecute");
        }

        @Override
        public void onResponse(Call<Post> call, Response<Post> response) {
            if (response.code() == HttpsURLConnection.HTTP_OK) {
                Toast.makeText(getContext(), "게시물 작성 성공", LENGTH_SHORT).show();
                hideKeyBoard();

                communityListFragment.resetSearch();
                communityListFragment.loadingPosts();
                communityListFragment.createContent(response.body());

                backFragment();
            } else {
                hideKeyBoard();
                Toast.makeText(getContext(), "게시물 작성 실패", LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<Post> call, Throwable t) {
            Toast.makeText(getContext(), "게시물 작성 실패", LENGTH_SHORT).show();

            try {
                hideKeyBoard();
                communityListFragment.loadingPosts();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                backFragment();
            }

        }
    }

    private void backFragment() {
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction()
                .remove(CommunityBoardWrite.this)
                .commit();
    }

    private void hideKeyBoard() {
        imm.hideSoftInputFromWindow(etx_content.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etx_title.getWindowToken(), 0);
    }
}
