package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * Created by webteam on 10/12/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayScoresWidgetRemoteViewService extends RemoteViewsService {
    public final String LOG_TAG = TodayScoresWidgetRemoteViewService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }
}
