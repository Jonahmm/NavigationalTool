package uk.ac.bris.cs.spe.navigationaltool;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import uk.ac.bris.cs.spe.navigationaltool.graph.Location;

public class LocationListAdapter extends BaseAdapter {
    private final Context context;
    private final List<Location> locations;

    public LocationListAdapter(Context c, List<Location> ls) {
        context = c;
        locations = ls;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Override
    public Object getItem(int i) {
        return locations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater li = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view==null) view = li.inflate(R.layout.location_list_item, viewGroup, false);
        Location l = locations.get(i);
        TextView tv = view.findViewById(R.id.location_list_item_title);
        tv.setText(l.hasName() ? l.getName() : l.getCode());
        tv = view.findViewById(R.id.location_list_item_code);
        if (l.hasName()) {
            tv.setText(l.getCode());
            tv.setVisibility(View.VISIBLE);
        }
        else tv.setVisibility(View.GONE);

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
