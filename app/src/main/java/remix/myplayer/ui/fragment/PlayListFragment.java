package remix.myplayer.ui.fragment;

import static remix.myplayer.ui.adapter.HeaderAdapter.LIST_MODE;

import android.content.Context;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import butterknife.BindView;
import java.util.Collections;
import java.util.List;
import remix.myplayer.R;
import remix.myplayer.db.room.DatabaseRepository;
import remix.myplayer.db.room.model.PlayList;
import remix.myplayer.db.room.model.PlayQueue;
import remix.myplayer.misc.asynctask.WrappedAsyncTaskLoader;
import remix.myplayer.misc.interfaces.LoaderIds;
import remix.myplayer.misc.interfaces.OnItemClickListener;
import remix.myplayer.ui.activity.ChildHolderActivity;
import remix.myplayer.ui.adapter.HeaderAdapter;
import remix.myplayer.ui.adapter.PlayListAdapter;
import remix.myplayer.ui.widget.fastcroll_recyclerview.FastScrollRecyclerView;
import remix.myplayer.util.Constants;
import remix.myplayer.util.SPUtil;
import remix.myplayer.util.ToastUtil;
import timber.log.Timber;

/**
 * @ClassName
 * @Description
 * @Author Xiaoborui
 * @Date 2016/10/8 09:46
 */
public class PlayListFragment extends LibraryFragment<PlayList, PlayListAdapter> {

  public static final String TAG = PlayListFragment.class.getSimpleName();
  @BindView(R.id.recyclerView)
  FastScrollRecyclerView mRecyclerView;

  @Override
  protected int getLayoutID() {
    return R.layout.fragment_playlist;
  }

  @Override
  protected void initAdapter() {
    mAdapter = new PlayListAdapter(mContext, R.layout.item_playlist_recycle_grid, mChoice,
        mRecyclerView);
    mAdapter.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        final PlayList playList = getAdapter().getDatas().get(position);
        if (!TextUtils.isEmpty(playList.getName()) && getUserVisibleHint() && !mChoice
            .click(position, playList)) {
          if (playList.getAudioIds().isEmpty()) {
            ToastUtil.show(mContext, getStringSafely(R.string.list_is_empty));
            return;
          }
          ChildHolderActivity.start(mContext, Constants.PLAYLIST, playList.getId(), playList.getName());
        }
      }

      @Override
      public void onItemLongClick(View view, int position) {
        if (getUserVisibleHint()) {
          mChoice.longClick(position, mAdapter.getDatas().get(position));
        }
      }
    });
  }

  @Override
  protected void initView() {
    int model = SPUtil
        .getValue(mContext, SPUtil.SETTING_KEY.NAME, SPUtil.SETTING_KEY.MODE_FOR_PLAYLIST,
            HeaderAdapter.GRID_MODE);
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    mRecyclerView.setLayoutManager(model == LIST_MODE ? new LinearLayoutManager(mContext)
        : new GridLayoutManager(getActivity(), getSpanCount()));
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(true);
  }

  @Override
  public PlayListAdapter getAdapter() {
    return mAdapter;
  }

  @Override
  public void onPlayListChanged(String name) {
    if(name.equals(PlayList.TABLE_NAME)){
      onMediaStoreChanged();
    }
  }

  @Override
  protected Loader<List<PlayList>> getLoader() {
    return new AsyncPlayListLoader(mContext);
  }

  @Override
  protected int getLoaderId() {
    return LoaderIds.PLAYLIST_FRAGMENT;
  }

  public static class AsyncPlayListLoader extends WrappedAsyncTaskLoader<List<PlayList>> {

    public AsyncPlayListLoader(Context context) {
      super(context);
    }

    @Override
    public List<PlayList> loadInBackground() {
      List<PlayList> playLists = DatabaseRepository.getInstance()
          .getAllPlaylist()
          .onErrorReturn(throwable -> {
            Timber.v(throwable);
            return Collections.emptyList();
          })
          .blockingGet();
      return playLists;
    }
  }
}
