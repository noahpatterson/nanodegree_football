package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by webteam on 10/12/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayScoresWidgetRemoteViewService extends RemoteViewsService {
    public final String LOG_TAG = TodayScoresWidgetRemoteViewService.class.getSimpleName();

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    static final int INDEX_SCORES_ID = 0;
    static final int INDEX_HOME_TEAM = 1;
    static final int INDEX_AWAY_TEAM = 2;
    static final int INDEX_HOME_GOALS = 3;
    static final int INDEX_AWAY_GOALS = 4;
    static final int INDEX_MATCH_DATE = 5;
    static final int INDEX_MATCH_ID = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Date fragmentdate = new Date(System.currentTimeMillis());
                Uri scoresForDateUri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(scoresForDateUri,
                        MATCH_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                int scoresId = data.getInt(INDEX_SCORES_ID);
                int matchId = data.getInt(INDEX_MATCH_ID);
                String homeName = data.getString(INDEX_HOME_TEAM);
                String awayName = data.getString(INDEX_AWAY_TEAM);
                int homeGoals = data.getInt(INDEX_HOME_GOALS);
                int awayGoals = data.getInt(INDEX_AWAY_GOALS);

                long dateInMillis = data.getLong(INDEX_MATCH_DATE);
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = mformat.format(dateInMillis);

                //set proper versus content description
                String homeNameWithVersus = getString(R.string.first_team_name_description, homeName, awayName);
                views.setContentDescription(R.id.widget_home_name, homeNameWithVersus);
                views.setTextViewText(R.id.widget_home_name, homeName);

                mHolder.home_name.setContentDescription(homeNameWithVersus);
                mHolder.home_name.setText(cursor.getString(COL_HOME));

                mHolder.away_name.setText(cursor.getString(COL_AWAY));

                //set matchtime content description
                String matchTimeDescription = context.getString(R.string.match_time_description, cursor.getString(COL_MATCHTIME));
                mHolder.date.setContentDescription(matchTimeDescription);
                mHolder.date.setText(cursor.getString(COL_MATCHTIME));

                //set score content description
                String scoreDescription = context.getString(R.string.score_description, cursor.getString(COL_HOME) + cursor.getInt(COL_HOME_GOALS), cursor.getString(COL_AWAY) + cursor.getInt(COL_AWAY_GOALS));
                mHolder.score.setContentDescription(scoreDescription);
                mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));

                mHolder.match_id = cursor.getDouble(COL_ID);
                mHolder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                        cursor.getString(COL_HOME)));
                mHolder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName(
                        cursor.getString(COL_AWAY)
                ));

                if (weatherArtImage != null) {
                    views.setImageViewBitmap(R.id.widget_icon, weatherArtImage);
                } else {
                    views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.widget_date, formattedDate);
                views.setTextViewText(R.id.widget_description, description);
                views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
                views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);

                final Intent fillInIntent = new Intent();
                String locationSetting =
                        Utility.getPreferredLocation(DetailWidgetRemoteViewService.this);
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,
                        dateInMillis);
                fillInIntent.setData(weatherUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_WEATHER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
