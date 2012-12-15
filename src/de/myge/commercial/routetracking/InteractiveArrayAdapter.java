package de.myge.commercial.routetracking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import de.myge.commercial.routetracking.arrayadapter.Model;
import de.myge.commercial.routetracking.database.Profile;

public class InteractiveArrayAdapter extends ArrayAdapter<Model> {

	private final List<Model> list;
	private final Activity context;
	public InteractiveArrayAdapter(Activity context, List<Model> list) {
		super(context, R.layout.rowbuttonlayout, list);
		this.context = context;
		this.list = list;
	}

	static class ViewHolder {
		protected TextView text;
		protected CheckBox checkbox;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.rowbuttonlayout, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) view.findViewById(R.id.label);
			viewHolder.text.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Model element = list.get(position);
					try {
						RouteTrackingDialog.showDialog((DrawRouteActivity)element.getC(), RouteTrackingDialog.DIALOGTYPE_ROUTE_PROPERTIES, element.getProfile());
					} catch (SQLException e) {
						Log.e(element.getC().getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
					} catch (InterruptedException e) {
						Log.e(element.getC().getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
					}
				}
			});
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
			
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(final CompoundButton buttonView,	boolean isChecked) {
							final Model element = (Model) viewHolder.checkbox.getTag();
							final DrawRouteActivity activity = (DrawRouteActivity)element.getC();							
							
							SlidingDrawer drawer = (SlidingDrawer) activity.findViewById(R.id.drawer);
							
							if (!drawer.isMoving() && drawer.isOpened()) { 
								element.setSelected(buttonView.isChecked());
								buttonView.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										try {
											if (buttonView.isChecked()) {
													activity.drawRoute(element.getProfile());
											} else {
												// Alle Profile ermitteln, die noch gezeichnet werden sollen
												ArrayList<Profile> profileList = new ArrayList<Profile>();
												 
												for (Model l : list) {
													if (l.isSelected()) profileList.add(l.getProfile());
												}
												
												activity.deleteRoute(profileList);
											}
										} catch (SQLException e) {
											Log.e(element.getC().getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
										}
									}
								});
							}
						}
					});
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(list.get(position));
		} else {
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(list.get(position).getProfile().getProfileName());
		holder.checkbox.setChecked(list.get(position).isSelected());
		return view;
	}
}
