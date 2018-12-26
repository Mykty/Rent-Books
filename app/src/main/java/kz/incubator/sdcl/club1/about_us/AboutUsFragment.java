package kz.incubator.sdcl.club1.about_us;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.R;

public class AboutUsFragment extends Fragment {
    View view;
    GridView gridView;
    ArrayList<moderator> moderators = new ArrayList<>();
    ModeratorsAdapter adapter;

    public AboutUsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_moderator, container, false);
        gridView = view.findViewById(R.id.gridView);
        initGrid();
        return view;
    }

    public void initGrid(){
        initializeWidgets();
        adapter = new ModeratorsAdapter(getActivity(),moderators);
        gridView.setAdapter(adapter);
    }

    public void initializeWidgets(){
        moderators.add(new moderator(R.drawable.jiger,"Telyukanov Zhiger","CEO\nReading Club",R.color.second));
        moderators.add(new moderator(R.drawable.me,"Myktybayev Bakhytzhan","IT support",R.color.first));
        moderators.add(new moderator(R.drawable.bauka,"Sarbas Baurzhan","Moderator",R.color.first));
        moderators.add(new moderator(R.drawable.bauka2,"Baurzhan Qadyrqul","Moderator",R.color.first));
    }

}
