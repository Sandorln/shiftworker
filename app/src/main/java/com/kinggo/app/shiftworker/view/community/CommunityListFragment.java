package com.kinggo.app.shiftworker.view.community;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.database.DatabaseHelper;
import com.kinggo.app.shiftworker.view.community.adapter.PostAdapter;
import com.kinggo.app.shiftworker.view.community.dialog.PostSearchDialog;
import com.kinggo.app.shiftworker.view.community.domain.Post;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;
import com.kinggo.app.shiftworker.view.community.service.CommunityApiService;

import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityListFragment extends Fragment {

    public static final int REQUEST_CODE = 2001;

    private PostAdapter postAdapter;
    private Long mLastClickTime = 0L;
    private View view;

    private DatabaseHelper db;

    // 목록 갱신 변수 값
    private String keyword = "";
    private String searchType = BoardSearchType.NONE.getSearchType();
    private Long page = 0L;
    private boolean lastItemVisibleFlag = false;

    private List<Post> postList;

    private UserInfo userInfo;

    @BindView(R.id.lv_posts)
    ListView listView;

    @BindView(R.id.community_board_toolbar)
    Toolbar toolbar;

    @BindView(R.id.srf_listView)
    SwipeRefreshLayout refreshLayout;

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = () -> {
        resetSearch();
        loadingPosts();
        refreshLayout.setRefreshing(false);
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_community_list, container, false);
        ButterKnife.bind(this, view);
        resetObject();
        resetView();

        return view;
    }

    private void resetView() {
        loadingPosts();
    }

    private void resetObject() {
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        db = new DatabaseHelper(this.getContext());

        userInfo = db.getUserInfo();

        toolbar.setTitle("");
        listView.setOnScrollListener(scrollListener);

        refreshLayout.setOnRefreshListener(onRefreshListener);
    }

    @OnItemClick(R.id.lv_posts)
    void selectPost(final int position) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        } else {
            mLastClickTime = SystemClock.elapsedRealtime();
            CommunityApiService
                    .getInstance()
                    .getPosts(((Post) postAdapter.getItem(position)).getId())
                    .enqueue(new Callback<Post>() {
                        @Override
                        public void onResponse(Call<Post> call, Response<Post> response) {
                            if (response.code() == HttpsURLConnection.HTTP_OK) {
                                createContent(response.body());
                            } else {
                                toastFailureMessage();
                            }
                        }

                        @Override
                        public void onFailure(Call<Post> call, Throwable t) {
                            toastFailureMessage();
                        }
                    });
        }
    }

    public void loadingPosts() {
        PostLoadingAsyncTask postLoadingAsyncTask = new PostLoadingAsyncTask();
        postLoadingAsyncTask.execute();
    }

    private void toastFailureMessage() {
        Toast.makeText(getContext(), "게시물 조회에 실패했습니다", Toast.LENGTH_LONG).show();
    }


    // Menu 생성
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.community_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Menu Item 선택 시
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.community_menu_login:
                createUserLogin();
                break;
            case R.id.community_menu_write:
                if (userInfo != null && userInfo.getToken() != null && userInfo.getToken().length() > 0)
                    createBoardWrite();
                else {
                    Toast.makeText(getContext(), "로그인을 하셔야 합니다", Toast.LENGTH_SHORT).show();
                    createUserLogin();
                }
                break;
            case R.id.community_menu_logout:
                logoutUserInfo();
                break;
            case R.id.community_menu_search:
                createSearchDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createSearchDialog() {
        PostSearchDialog postSearchDialog = new PostSearchDialog();
        postSearchDialog.setTargetFragment(CommunityListFragment.this, REQUEST_CODE);
        postSearchDialog.show(getFragmentManager(), "PostSearchDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            keyword = data.getStringExtra("keyword");
            searchType = data.getStringExtra("searchType");
            Toast.makeText(getContext(), "\"" + keyword + "\" 검색 결과 ", Toast.LENGTH_SHORT).show();
            page = 0L;
            postList = null;

            loadingPosts();
        }
    }

    // 게시판 글 작성 Fragment 생성
    private void createBoardWrite() {
        CommunityBoardWrite communityBoardWrite = new CommunityBoardWrite();

        Bundle bundle = new Bundle();
        bundle.putString("token", userInfo.getToken());
        communityBoardWrite.setArguments(bundle);

        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction()
                .addToBackStack("f_com")
                .setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.left_in, R.anim.right_out)
                .add(R.id.fTab_main_community, communityBoardWrite, "f_community_write")
                .commit();
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

    // 게시물 상세 보기
    public void createContent(Post post) {
        CommunityContentFragment communityContentFragment = new CommunityContentFragment();
        communityContentFragment.setParentFragment(this);

        if (post != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("post", post);
            if (userInfo != null && userInfo.getToken() != null) {
                bundle.putSerializable("userInfo", userInfo);
            }
            communityContentFragment.setArguments(bundle);

            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction()
                    .addToBackStack("f_com")
                    .setCustomAnimations(R.anim.left_in, R.anim.right_out, R.anim.left_in, R.anim.right_out)
                    .add(R.id.fTab_main_community, communityContentFragment, "f_community_content")
                    .commit();
        } else
            toastFailureMessage();
    }

    // userInfo 값 저장
    public void loginUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        // db 에 값 저장
        db.updateUser_Login(userInfo);
        // 메뉴 로그인 / 로그 아웃 값 변경
    }

    public void logoutUserInfo() {
        this.userInfo = null;
        UserInfo deleteValue = new UserInfo("", "", "");
        db.updateUser_Login(deleteValue);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (userInfo != null &&
                userInfo.getUsername().length() > 0 &&
                userInfo.getPassword().length() > 0) {
            menu.findItem(R.id.community_menu_login).setVisible(false);
            menu.findItem(R.id.community_menu_logout).setVisible(true);
        } else {
            menu.findItem(R.id.community_menu_login).setVisible(true);
            menu.findItem(R.id.community_menu_logout).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("StaticFieldLeak")
    class PostLoadingAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (postList != null)
                page += 1;
        }

        @Override
        protected Void doInBackground(String... strings) {
            CommunityApiService
                    .getInstance()
                    .getPosts(page, searchType, keyword)
                    .enqueue(new Callback<List<Post>>() {
                        @Override
                        public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                            if (response.code() == HttpsURLConnection.HTTP_OK) {
                                // 첫 목록일 시
                                if (postList == null) {
                                    postList = response.body();
                                    postAdapter = new PostAdapter(Objects.requireNonNull(getContext()), R.layout.f_community_list_item, postList);
                                    listView.setAdapter(postAdapter);
                                } else if (response.body() != null && response.body().size() > 0) {
                                    postList.addAll(response.body());
                                    postAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getContext(), "마지막 페이지 입니다", Toast.LENGTH_SHORT).show();
                                    page -= 1;
                                }
                            } else {
                                toastFailureMessage();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Post>> call, Throwable t) {
                            toastFailureMessage();
                        }
                    });
            return null;
        }
    }

    // 무한 스크롤 listener
    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
            //즉 스크롤이 바닦에 닿아 멈춘 상태에 처리를 하겠다는 뜻
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                PostLoadingAsyncTask postLoadingAsyncTask = new PostLoadingAsyncTask();
                postLoadingAsyncTask.execute();
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem) + 현재 화면에 보이는 리스트 아이템의 갯수(visibleItemCount)가 리스트 전체의 갯수(totalItemCount) -1 보다 크거나 같을때
            lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
        }
    };

    public enum BoardSearchType {
        NONE("NONE"),
        POST("POST"),
        AUTHOR("AUTHOR");
        private String searchType;

        BoardSearchType(String searchType) {
            this.searchType = searchType;
        }

        public String getSearchType() {
            return searchType;
        }
    }

    public void resetSearch() {
        postList = null;
        page = 0L;
        keyword = "";
        searchType = BoardSearchType.NONE.getSearchType();
    }
}