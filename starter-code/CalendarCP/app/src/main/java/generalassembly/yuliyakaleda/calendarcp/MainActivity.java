package generalassembly.yuliyakaleda.calendarcp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends Activity implements View.OnClickListener {
  private static final String TAG = "ga.contentproviders";
  private EditText title;
  private EditText description;
  private EditText location;
  private Button getEvents;
  private Button addEvent;
  private Button updateEvent;
  private Button deleteEvent;
  private ListView lv;
  private long recentlyAddedEventId;

  public static final String[] EVENT_PROJECTION = new String[] {
          CalendarContract.Calendars._ID,                           // 0
          CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
          CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
          CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
  };

  // The indices for the projection array above.
  private static final int PROJECTION_ID_INDEX = 0;
  private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
  private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
  private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    title = (EditText) findViewById(R.id.title);
    description = (EditText) findViewById(R.id.description);
    location = (EditText) findViewById(R.id.location);
    getEvents = (Button) findViewById(R.id.get_events);
    addEvent = (Button) findViewById(R.id.add_event);
    deleteEvent = (Button) findViewById(R.id.delete_event);
    updateEvent = (Button) findViewById(R.id.update_event);
    lv = (ListView) findViewById(R.id.lv);

    addEvent.setOnClickListener(this);
    getEvents.setOnClickListener(this);
    deleteEvent.setOnClickListener(this);
    updateEvent.setOnClickListener(this);
    fetchCalendars();
  }

  // The method returns all the calendars associated with your email. The property ID is a
  // calendar id. You have to choose one type of calendar you would love to work on in the
  // methods below
  public void fetchCalendars() {
    Uri uri = CalendarContract.Calendars.CONTENT_URI;
    String[] columns = new String[] {
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT
    };

    Cursor cursor = getContentResolver().query(
        uri,
        columns,
        CalendarContract.Calendars.ACCOUNT_NAME + " = ?",
        new String[] {"mercuryorangepurple@gmail.com"},
        null
    );

    while (cursor.moveToNext()) {
      long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));
      String accountName = cursor.getString(1);
      String displayName = cursor.getString(2);
      String owner = cursor.getString(3);
      Log.d("ContentProvider", "ID: " + id +
              ", account: " + accountName +
              ", displayName: " + displayName +
              ", owner: " + owner
      );
    }
  }

  public void insertEventInCalendar(String title, String description, String location) {
 //TODO:
 // 1. get 2 calendar instances: startTime and endTime in milliseconds and set March 1 as the
 // date of the event. The event can last as long as you want, so you can set any time.
    Calendar startTime = Calendar.getInstance();
    startTime.set(2016, 2, 1, 8, 0, 0);
    long startMillis = startTime.getTimeInMillis();

    Calendar endTime = Calendar.getInstance();
    endTime.set(2016, 2, 1, 9, 0, 0);
    long endMillis = endTime.getTimeInMillis();

 // 2. set the following properties of the event and save the event in the provider
 //   - CalendarContract.Events.DTSTART
 //   - CalendarContract.Events.DTEND
 //   - CalendarContract.Events.TITLE
 //   - CalendarContract.Events.DESCRIPTION
 //   - CalendarContract.Events.CALENDAR_ID (the value 1 should give the default calendar)
 //   - CalendarContract.Events.EVENT_TIMEZONE
    ContentResolver cr = getContentResolver();
    ContentValues values = new ContentValues();
    values.put(CalendarContract.Events.DTSTART, startMillis);
    values.put(CalendarContract.Events.DTEND, endMillis);
    values.put(CalendarContract.Events.TITLE, title);
    values.put(CalendarContract.Events.DESCRIPTION, description);
    values.put(CalendarContract.Events.EVENT_LOCATION, location);
    values.put(CalendarContract.Events.CALENDAR_ID, 1);
    values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

//  3. after inserting the row in the provider, retrieve the id of the event using the method below.
// Just uncomment the line below. You will need this id to update and delete this event later.
    recentlyAddedEventId = Long.parseLong(uri.getLastPathSegment());
    fetchEvents();
  }

  //This method should return all the events from your calendar from February 29th till March 4th
  // in the year 2016.
  public void fetchEvents() {
  //TODO:
  // 1. get 2 calendar instances: startTime (Feb 29) and endTime (March 4) in milliseconds
    Calendar startTime = Calendar.getInstance();
    startTime.set(2016, 1, 29, 12, 0, 0);
    long startMillis = startTime.getTimeInMillis();

    Calendar endTime = Calendar.getInstance();
    endTime.set(2016, 2, 4, 12, 0, 0);
    long endMillis = endTime.getTimeInMillis();

  // 2. set the limit of 100 events and order DESC
    String limitAndOrder = CalendarContract.Events.DTSTART + " DESC LIMIT 100";

  // 3. get all the events within that period using a cursor object
    Cursor cursor;
    ContentResolver cr = getContentResolver();
    Uri uri = CalendarContract.Events.CONTENT_URI;
    String selection = "((" + CalendarContract.Events.DTSTART + " >= ?) AND ("
            + CalendarContract.Events.DTEND + " <= ?))";
    String[] selectionArgs = new String[] {String.valueOf(startMillis), String.valueOf(endMillis)};
// Submit the query and get a Cursor object back.
    String[] columns = new String[] {CalendarContract.Events._ID, CalendarContract.Events.TITLE,
                                     CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND};
    cursor = cr.query(uri, columns, selection, selectionArgs, limitAndOrder);

  // 4. once you get a cursor object, uncomment the code below to see the events displayed in the
  // list view.

    ListAdapter listAdapter = new SimpleCursorAdapter(
        this,
        android.R.layout.simple_expandable_list_item_2,
        cursor,
        new String[] {CalendarContract.Events._ID, CalendarContract.Events.TITLE},
        new int[] {android.R.id.text1, android.R.id.text2},
        0
    );

    lv.setAdapter(listAdapter);
  }

  public void update(String title, String description, String location) {
    //TODO: Using the number eventID from the method insertEventInCalendar(), update the event
    // that was added in that method
    ContentResolver cr = getContentResolver();
    ContentValues values = new ContentValues();
    values.put(CalendarContract.Events.TITLE, title);
    values.put(CalendarContract.Events.DESCRIPTION, description);
    values.put(CalendarContract.Events.EVENT_LOCATION, location);
    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, recentlyAddedEventId);
    int rows = getContentResolver().update(updateUri, values, null, null);
    Log.i(TAG, "Rows updated: "+ rows);
    fetchEvents();

  }

  public void delete() {
    //TODO: Using the number eventID from the method insertEventInCalendar(), delete the event
    // that was added in that method
    ContentResolver cr = getContentResolver();
    ContentValues values = new ContentValues();
    Uri deleteUri = null;
    deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, recentlyAddedEventId);
    int rows = getContentResolver().delete(deleteUri, null, null);
    Log.i(TAG, "Rows deleted: " + rows);
    fetchEvents();
  }

  @Override
  public void onClick(View v) {
    String titleString = title.getText().toString();
    String descriptionString = description.getText().toString();
    String locationString = location.getText().toString();

    switch (v.getId()) {
      case R.id.add_event:
        insertEventInCalendar(titleString, descriptionString, locationString);
        break;
      case R.id.delete_event:
        delete();
        break;
      case R.id.update_event:
        update(titleString, descriptionString, locationString);
        break;
      case R.id.get_events:
        fetchEvents();
        break;
      default:
        break;
    }
  }
}


