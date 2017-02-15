package com.psklf.forcestop;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by zhuyuanxuan on 13/02/2017.
 * ForceStop
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter
        .MyViewHolder> {
    private AppServiceInfo[] mNameArray;
    private Handler mHandler;
    private Context mCtx;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        private Switch mItemSwitch;

        public MyViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tv_service_name);
            mItemSwitch = (Switch) view.findViewById(R.id.switch_item);
        }
    }

    public MyRecyclerViewAdapter(AppServiceInfo[] appServiceInfoArray, Handler handler, Context
            ctx) {
        mNameArray = appServiceInfoArray;
        mHandler = handler;
        mCtx = ctx;
    }

    /**
     * Called when RecyclerView needs a new {@link MyViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new MyViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new MyViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(MyViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MyViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(MyViewHolder, int)
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view,
                parent, false);
        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link MyViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link MyViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(MyViewHolder, int)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The MyViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        PackageManager packageManager = mCtx.getPackageManager();
        CharSequence label = mNameArray[position].getApplicationInfo().loadLabel(packageManager);
        holder.mTextView.setText(label);

        holder.mItemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // CharSequence text = "Close!";
                    // Toast toast = Toast.makeText(, text, LENGTH_LONG);
                    Log.i("xx", "Close: " + holder.getAdapterPosition());
                    Message msg = mHandler.obtainMessage(PublicConstants.MSG_FORCE_STOP_APP);
                    msg.arg1 = holder.getAdapterPosition();
                    msg.sendToTarget();
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mNameArray.length;
    }
}
