package com.example.eugene.dddb.activities;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.eugene.dddb.Item;
import com.example.eugene.dddb.ItemAdapter;
import com.example.eugene.dddb.R;
import com.example.eugene.dddb.provider.ItemsProvider;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ItemAdapter.OnItemChangeListener {

    private static  final int ITEM_COUNT = 100;
    private static  final int ITEM_LOADER = 1;
    private ArrayList<Item> data = new ArrayList<>();
    private ItemAdapter adapter;

    private View pbLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbLoader = findViewById(R.id.pbLoader);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvList);

        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();

        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(data, this);
        recyclerView.setAdapter(dragMgr.createWrappedAdapter(adapter));

        dragMgr.attachRecyclerView(recyclerView);

        getData();
    }

    private void getData() {
        getSupportLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    private void initItemList(Cursor cursor) {
        if (data.size() != 0)
            return;
        int idIndex = cursor.getColumnIndex(ItemsProvider.ID);
        int valueIndex = cursor.getColumnIndex(ItemsProvider.VALUE);
        int prevIdIndex = cursor.getColumnIndex(ItemsProvider.PREV_ID);
        int nextIdIndex = cursor.getColumnIndex(ItemsProvider.NEXT_ID);
        cursor.moveToFirst();
        do {
            long id = cursor.getLong(idIndex);
            String value = cursor.getString(valueIndex);
            long prevId = cursor.getLong(prevIdIndex);
            long nextId = cursor.getLong(nextIdIndex);
            data.add(new Item(id, value, prevId, nextId));
        } while (cursor.moveToNext());

        sortItems();
        adapter.notifyDataSetChanged();
        pbLoader.setVisibility(View.GONE);
    }

    private void sortItems() {
        ArrayList<Item> temp = new ArrayList<>();
        temp.add(data.get(0));
        while (temp.size() != data.size()) {
            temp.add(getNext(temp.get(temp.size() - 1)));
        }

        data.clear();
        data.addAll(temp);
    }

    private Item getNext(Item lastAdded) {
        for (Item item : data) {
            if (lastAdded.getNextId() == item.getId())
                return item;
        }

        return null;
    }

    private void firstInitItemList() {
        for (int i = 1; i <= ITEM_COUNT; i++) {
           data.add(new Item(i, "Item " + i, i - 1, i == ITEM_COUNT ? 0 : i + 1));
        }

        adapter.notifyDataSetChanged();
        pbLoader.setVisibility(View.GONE);
        sendFirstDataToDB();
    }

    private void sendFirstDataToDB() {
        DBInsertThread dbInsertThread = new DBInsertThread(data, getContentResolver());
        dbInsertThread.start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ITEM_LOADER:
                return new CursorLoader(this, ItemsProvider.CONTENT_URI, null,
                        null, null, ItemsProvider.PREV_ID);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            firstInitItemList();
        } else {
            initItemList(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemsChanged(List<Item> items) {
        DBUpdateThread dbUpdateThread = new DBUpdateThread(data, getContentResolver());
        dbUpdateThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.removeOnItemChangeListener();
    }

    private static class DBInsertThread extends Thread {

        private List<Item> items;
        private ContentResolver contentResolver;

        public DBInsertThread(List<Item> items, ContentResolver contentResolver) {
            this.items = items;
            this.contentResolver = contentResolver;
        }

        @Override
        public void run() {
            ArrayList<ContentProviderOperation> batch = new ArrayList<>();
            for (Item item : items) {
                ContentValues cv = new ContentValues();
                cv.put(ItemsProvider.ID, item.getId());
                cv.put(ItemsProvider.VALUE, item.getValue());
                cv.put(ItemsProvider.PREV_ID, item.getPrevId());
                cv.put(ItemsProvider.NEXT_ID, item.getNextId());
                batch.add(ContentProviderOperation.newInsert(
                        ItemsProvider.CONTENT_URI).withValues(cv).build());
            }
            try {
                contentResolver.applyBatch(ItemsProvider.AUTHORITY, batch);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    private static class DBUpdateThread extends Thread {

        private List<Item> items;
        private ContentResolver contentResolver;

        public DBUpdateThread(List<Item> items, ContentResolver contentResolver) {
            this.items = items;
            this.contentResolver = contentResolver;
        }

        @Override
        public void run() {
            ArrayList<ContentProviderOperation> batch = new ArrayList<>();
            for (Item item : items) {
                ContentValues cv = new ContentValues();
                cv.put(ItemsProvider.VALUE, item.getValue());
                cv.put(ItemsProvider.PREV_ID, item.getPrevId());
                cv.put(ItemsProvider.NEXT_ID, item.getNextId());
                batch.add(ContentProviderOperation.newUpdate(
                        ContentUris.withAppendedId(ItemsProvider.CONTENT_URI, item.getId()))
                        .withValues(cv).build());
            }
            try {
                contentResolver.applyBatch(ItemsProvider.AUTHORITY, batch);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }
}
