package com.kinggo.app.shiftworker.view.community.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kinggo.app.shiftworker.R;
import com.kinggo.app.shiftworker.view.community.CommunityListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostSearchDialog extends DialogFragment {

    @BindView(R.id.etx_dl_search)
    EditText etx_dl_search;
    @BindView(R.id.rg_dl)
    RadioGroup rg_dl;

    private String searchType;
    private String keyword;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_board_search, null);
        ButterKnife.bind(this, view);

        resetObject();

        return view;
    }

    private void resetObject() {

        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        rg_dl.check(R.id.rbtn_dl_post);
        searchType = CommunityListFragment.BoardSearchType.POST.getSearchType();
        keyword = "";

        rg_dl.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                case R.id.rbtn_dl_author:
                    searchType = CommunityListFragment.BoardSearchType.AUTHOR.getSearchType();
                    Toast.makeText(getContext(), "작성자 기준", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.rbtn_dl_post:
                    searchType = CommunityListFragment.BoardSearchType.POST.getSearchType();
                    Toast.makeText(getContext(), "게시물 기준", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @OnClick({R.id.tx_dl_okay})
    public void clickEvent(View view) {
        keyword = etx_dl_search.getText().toString().trim();
        if (keyword.length() > 0) {
            Intent intent = new Intent();
            intent.putExtra("keyword", keyword);
            intent.putExtra("searchType", searchType);
            getTargetFragment().onActivityResult(CommunityListFragment.REQUEST_CODE, 0, intent);
            dismiss();
            imm.hideSoftInputFromWindow(etx_dl_search.getWindowToken(), 0);
        } else
            Toast.makeText(getContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
    }
}
