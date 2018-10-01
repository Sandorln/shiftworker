package com.kinggo.app.shiftworker.view.community;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.domain.UserInfo;
import com.kinggo.app.shiftworker.view.community.service.CommunityApiService;

import java.net.HttpURLConnection;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityLogin extends Fragment {

    private final String TAG = "LOGINASYNCTASK";

    private InputMethodManager imm;
    private Fragment parentFragment;
    private boolean isSuccessLogin = false;

    private UserInfo userInfo;

    @BindView(R.id.ck_login_create)
    CheckBox ck_login_create;

    @BindView(R.id.tx_login_okay)
    TextView tx_login_okay;

    @BindView(R.id.etx_login_id)
    EditText etx_id;

    @BindView(R.id.etx_login_password)
    EditText etx_password;

    @BindView(R.id.etx_login_checkPassword)
    EditText etx_checkPassword;

    @BindView(R.id.tr_checkPassword)
    TableRow tr_checkPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_community_login, null);
        ButterKnife.bind(this, view);

        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return view;
    }

    @OnClick({R.id.layout_board_login_back, R.id.tx_login_okay})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.layout_board_login_back:
                break;
            case R.id.tx_login_okay:
                String id = etx_id.getText().toString().trim();
                String pwd = etx_password.getText().toString().trim();
                String ckPwd = etx_checkPassword.getText().toString().trim();

                if (id.length() > 0 && pwd.length() > 0) {
                    if (ck_login_create.isChecked()) {
                        if (ckPwd.length() > 0 && pwd.contains(ckPwd)) {
                            LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
                            loginAsyncTask.execute(true);
                        } else
                            Toast.makeText(getContext(), "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        userInfo = new UserInfo(id, pwd);
                        LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
                        loginAsyncTask.execute(false);
                    }
                } else
                    Toast.makeText(getContext(), "아이디/비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    // 로그인 AsyncTask
    class LoginAsyncTask extends AsyncTask<Boolean, Void, Void> implements Callback<String> {

        @Override
        protected Void doInBackground(Boolean... booleans) {
            if (booleans[0]) {
                CommunityApiService
                        .getInstance()
                        .join(userInfo)
                        .enqueue(this);
            } else {
                CommunityApiService
                        .getInstance()
                        .login(userInfo)
                        .enqueue(this);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            userInfo = new UserInfo(etx_id.getText().toString().trim(), etx_password.getText().toString().trim());
        }

        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            if (response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_CREATED) {
                userInfo.setToken(response.body());

                isSuccessLogin = true;
                Toast.makeText(getContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                backFragment();
            } else
                Toast.makeText(getContext(), "아이디 혹은 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(Call<String> call, Throwable t) {
            Toast.makeText(getContext(), "서버 오류", Toast.LENGTH_SHORT).show();
        }
    }

    // 프래그먼트 삭제 시
    public void backFragment() {
        imm.hideSoftInputFromWindow(etx_id.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(etx_password.getWindowToken(), 0);

        assert getFragmentManager() != null;
        // 해당 Fragment 삭제 애니메이션
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.right_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 해당 Fragment 삭제
                getFragmentManager().beginTransaction()
                        .remove(CommunityLogin.this)
                        .commit();

                if (isSuccessLogin) {
                    if (parentFragment instanceof CommunityListFragment)
                        ((CommunityListFragment) parentFragment).loginUserInfo(userInfo);
                    else if (parentFragment instanceof CommunityContentFragment)
                        ((CommunityContentFragment) parentFragment).loginUserInfo(userInfo);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // 애니메이션 시작
        Objects.requireNonNull(getView()).startAnimation(animation);
    }

    // 아이디 생성 시
    @OnCheckedChanged({R.id.ck_login_create})
    void onChecked(boolean checked) {
        if (checked) {
            etx_checkPassword.setText("");
            tr_checkPassword.setVisibility(View.VISIBLE);
            tr_checkPassword.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.left_in));
            tx_login_okay.setText("생성");
        } else {
            etx_checkPassword.setText("");
            tr_checkPassword.setVisibility(View.INVISIBLE);
            tr_checkPassword.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.right_out));
            tx_login_okay.setText("로그인");
        }
    }

    public void setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
    }
}
