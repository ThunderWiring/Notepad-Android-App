package com.thunderwiring.kitaba.views.notesLibrary.contetnt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thunderwiring.kitaba.R;
import com.thunderwiring.kitaba.views.INotesActivity;
import com.thunderwiring.kitaba.views.notesLibrary.presenter.IEntriesPresenter;

public class TabLibraryFragment extends Fragment implements IContentFragment {

    private TabLayout mTabLayout;
    private TabAdapter mTabAdapter;
    private IContentFragment mCurrentContentFragment;

    public TabLibraryFragment() {
        super();
        mCurrentContentFragment = new AllNotesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.note_library_tab_layout, container, false);
        ViewPager viewPager = root.findViewById(R.id.viewPager);
        mTabLayout = root.findViewById(R.id.tabLayout);
        mTabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());
        mTabAdapter.addFragment((Fragment) mCurrentContentFragment, "All");
        mTabAdapter.addFragment(new AllFoldersFragment(), "Folders");
        viewPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                Fragment currentFragment = mTabAdapter.getItem(i);
                if (!(currentFragment instanceof IContentFragment)) {
                    return;
                }
                mCurrentContentFragment.getActivityListener().clearNotesSelection();
                mCurrentContentFragment = (IContentFragment) currentFragment;
                mCurrentContentFragment.onFragmentSelected();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        return root;
    }

    @Override
    public int getActionButtonResource() {
        return mCurrentContentFragment.getActionButtonResource();
    }

    @Override
    public View.OnClickListener getActionButtonClickListener() {
        return mCurrentContentFragment.getActionButtonClickListener();
    }

    @Override
    public IEntriesPresenter getEntriesPresenter() {
        return mCurrentContentFragment.getEntriesPresenter();
    }

    @Override
    public boolean hasSelection() {
        return mCurrentContentFragment.hasSelection();
    }

    @Override
    public void orderContentBy(int orderSelector) {
        mCurrentContentFragment.orderContentBy(orderSelector);
    }

    @Override
    public void setNotesActivityInterface(INotesActivity bridge) {
        mCurrentContentFragment.setNotesActivityInterface(bridge);
    }

    @Override
    public IActivityListener getActivityListener() {
        return mCurrentContentFragment.getActivityListener();
    }

    @Override
    public void onFragmentSelected() {
        mCurrentContentFragment.onFragmentSelected();
    }
}
