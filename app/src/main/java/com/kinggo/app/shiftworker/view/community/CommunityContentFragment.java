package com.kinggo.app.shiftworker.view.community;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.adapter.CommentAdapter;
import com.kinggo.app.shiftworker.view.community.domain.Comment;
import com.kinggo.app.shiftworker.view.community.domain.Post;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommunityContentFragment extends Fragment {

    private InputMethodManager imm;
    private Fragment parentFragment;

    @BindView(R.id.tx_board_title)
    TextView title_tx;

    @BindView(R.id.tx_board_content)
    TextView content_tx;

    @BindView(R.id.tx_board_author)
    TextView writer_tx;

    @BindView(R.id.tx_board_viewCount)
    TextView viewCount;

    @BindView(R.id.rv_board_comment)
    RecyclerView rv_comments;

    @BindView(R.id.tx_board_createDate)
    TextView createDate_tx;

    @BindView(R.id.community_content_toolbar)
    Toolbar toolbar;

    private CommentAdapter commentAdapter;
    private Post post;
    private UserInfo userInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_community_content, null);
        ButterKnife.bind(this, view);
        resetObject();
        return view;
    }

    private void resetObject() {
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.arrow_calendar_icon_blue);
        toolbar.setNavigationOnClickListener(clickNavigation);

        imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);

        assert getArguments() != null;
        post = (Post) getArguments().getSerializable("post");
        userInfo = (UserInfo) getArguments().getSerializable("userInfo");

        assert post != null;
        title_tx.setText(post.getTitle());
        content_tx.setText(post.getContent());
        writer_tx.setText(post.getAuthor());
        viewCount.setText(post.getViewCount());
        createDate_tx.setText(post.getCreatedDate());


        if (post.getComments() != null) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            commentAdapter = new CommentAdapter((ArrayList<Comment>) post.getComments(), post.getId(), userInfo);

            rv_comments.setNestedScrollingEnabled(false);
            rv_comments.setHasFixedSize(false);
            rv_comments.setLayoutManager(layoutManager);
            rv_comments.setAdapter(commentAdapter);
        }
    }

    // 백버튼 눌렀을 시
    private View.OnClickListener clickNavigation = view -> {
        backFragment();
    };

    // 이전 Fragment 화면 클릭 방지
    @OnClick({R.id.a_community_back})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.a_community_back:
                break;
        }
    }

    public void backFragment() {
        // 키보드 숨기기
        if (commentAdapter.getEtx_write() != null) {
            imm.hideSoftInputFromWindow(commentAdapter.getEtx_write().getWindowToken(), 0);
        }

        // 해당 Fragment 삭제 애니메이션
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.right_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 해당 Fragment 삭제
                assert getFragmentManager() != null;
                getFragmentManager().beginTransaction()
                        .remove(CommunityContentFragment.this)
                        .commit();

//                if (parentFragment instanceof CommunityListFragment)
//                    ((CommunityListFragment) parentFragment).loginUserInfo(userInfo);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // 애니메이션 시작
        Objects.requireNonNull(getView()).startAnimation(animation);
    }

    // 댓글 갱신
    public void resetComment(List<Comment> comments) {
        commentAdapter = new CommentAdapter(((ArrayList<Comment>) comments), post.getId(), userInfo);
        rv_comments.setAdapter(commentAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.community_content_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.community_content_modify:
                checkUserLogin(ContentOption.Modify);
                break;
            case R.id.community_content_delete:
                checkUserLogin(ContentOption.Delete);
                break;
            case R.id.community_content_report:
                checkUserLogin(ContentOption.Report);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserLogin(ContentOption contentOption) {
        if (userInfo != null && userInfo.getToken().length() > 0) {
            switch (contentOption) {
                case Modify:
                    Toast.makeText(getContext(), "글 수정", Toast.LENGTH_SHORT).show();
                    break;
                case Delete:
                    Toast.makeText(getContext(), "글 삭제", Toast.LENGTH_SHORT).show();
                    break;
                case Report:
                    Toast.makeText(getContext(), "신고하기", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else
            createUserLogin();
    }

    private enum ContentOption {
        Modify,
        Delete,
        Report
    }

    // 로그인 Fragment 작성
    private void createUserLogin() {
        CommunityLogin communityLogin = new CommunityLogin();
        communityLogin.setParentFragment(this);

        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction()
                .addToBackStack("f_com")
                .setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.left_in, R.anim.right_out)
                .add(R.id.fTab_main_community, communityLogin, "f_community_login")
                .commit();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    public void loginUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }
}
