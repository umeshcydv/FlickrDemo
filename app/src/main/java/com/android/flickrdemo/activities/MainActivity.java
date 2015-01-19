package com.android.flickrdemo.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.flickrdemo.FlickrIntentService;
import com.android.flickrdemo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import network.model.FlickrFeedDTO;
import network.model.MediaDTO;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.lv_flicker_photos)
    @Optional ListView mFlickrListView;
    @InjectView(R.id.gv_flicker_photos)
    @Optional GridView mFlickrGridView;

    private FlickrFeedDTO mFlickrFeedDTO;
    private FlickrPhotosAdapter mFLickrPhotosAdapter;
    private static int mItemToBeShown;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initializeImageOptions();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mFlickrFeedDTO == null) {
            startService(new Intent(MainActivity.this, FlickrIntentService.class));
        }
        registerReceiver(receiver, new IntentFilter(FlickrIntentService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionType = intent.getAction();
            if (actionType.equals(FlickrIntentService.NOTIFICATION)) {
                mFlickrFeedDTO = (FlickrFeedDTO) intent.getExtras().getSerializable("Data");
                setDataToView();
            }
        }
    };

    private void initializeImageOptions() {
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_bg)
                .showImageForEmptyUri(R.drawable.default_bg)
                .showImageOnFail(R.drawable.default_bg)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private void setDataToView() {
        if (mFLickrPhotosAdapter != null) {
            setDataToListView();
        } else {
            mFLickrPhotosAdapter = new FlickrPhotosAdapter();
            setDataToListView();
        }
    }

    private void setDataToListView() {
        if (mFlickrListView != null) {
            mFlickrListView.setAdapter(mFLickrPhotosAdapter);
            // Setting item position to be shown similar to what it was showing previous view.
            mFlickrListView.setSelection(mItemToBeShown);
            mFlickrListView.setOnScrollListener(onScrollListener);
        } else {
            mFlickrGridView.setAdapter(mFLickrPhotosAdapter);
            // Setting item position to be shown similar to what it was showing previous view.
            mFlickrGridView.setSelection(mItemToBeShown + 1);
            mFlickrGridView.setOnScrollListener(onScrollListener);
        }
    }


    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mItemToBeShown = firstVisibleItem;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("data", mFlickrFeedDTO);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFlickrFeedDTO = (FlickrFeedDTO) savedInstanceState.getSerializable("data");
        setDataToView();
    }

    public class FlickrPhotosAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFlickrFeedDTO.getItems().size();
        }

        @Override
        public Object getItem(int position) {
            return mFlickrFeedDTO.getItems().get(position).getMedia();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.row_imageview, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ImageLoader.getInstance().displayImage(((MediaDTO) getItem(position)).getImage(), viewHolder.flickerImageView, options, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    viewHolder.progressBar.setProgress(0);
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    viewHolder.progressBar.setProgress(Math.round(100.0f * current / total));
                }
            });
            return convertView;
        }

        public class ViewHolder {
            @InjectView(R.id.iv_flickr_pic)
            ImageView flickerImageView;

            @InjectView(R.id.pb_loading)
            ProgressBar progressBar;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }

        }
    }


}
