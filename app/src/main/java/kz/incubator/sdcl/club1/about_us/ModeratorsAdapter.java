package kz.incubator.sdcl.club1.about_us;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;

public class ModeratorsAdapter extends BaseAdapter {
    Context context;
    ArrayList<moderator> moderators = new ArrayList<>();
    public ModeratorsAdapter(Context context, ArrayList<moderator> moderators){
        this.context = context;
        this.moderators = moderators;
    }
    @Override
    public int getCount() {
        return moderators.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.moderator_desing,null);
            TextView name = convertView.findViewById(R.id.name);
            TextView desc = convertView.findViewById(R.id.desc);
            CircleImageView imageView = convertView.findViewById(R.id.imageView);
            RelativeLayout relativeLayout = convertView.findViewById(R.id.backgroundColor);

            desc.setText(moderators.get(position).getName());
            name.setText(moderators.get(position).getDesc());
            imageView.setImageResource(moderators.get(position).getImage());

//            Glide.with(context).load(moderators.get(position).getImage()).into(imageView);
            relativeLayout.setBackgroundColor(context.getResources().getColor(moderators.get(position).getColor()));

        }
        return convertView;
    }
}
