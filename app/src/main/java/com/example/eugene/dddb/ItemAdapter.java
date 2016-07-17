package com.example.eugene.dddb;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>
        implements DraggableItemAdapter<ItemAdapter.ItemViewHolder> {
    private ArrayList<Item> data;
    private OnItemChangeListener onItemChangeListener;

    public ItemAdapter(ArrayList<Item> data, OnItemChangeListener onItemChangeListener) {
        this.data = data;
        this.onItemChangeListener = onItemChangeListener;
        setHasStableIds(true); // this is required for D&D feature.
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId(); // need to return stable (= not change even after reordered) value
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_for_drag_minimal, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = data.get(position);
        holder.textView.setText(item.getValue() + " prev = " + item.getPrevId() + " next = " + item.getNextId()
                    + " position = " + position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private Item getItemById(long id) {
        for (Item item : data) {
            if (item.getId() == id)
                return item;
        }

        return null;
    }

    @Override
    public void onMoveItem(final int fromPosition, final int toPosition) {
        List<Item> changedItems = new ArrayList<>();
        Item from = data.get(fromPosition);
        Item to = data.get(toPosition);
        final long fromNextId = from.getNextId();
        final long fromPrevId = from.getPrevId();
        final long toNextId = to.getNextId();
        final long toPrevId = to.getPrevId();
        Item fromPrev = getItemById(data.get(fromPosition).getPrevId());
        Item fromNext = getItemById(data.get(fromPosition).getNextId());
        Item toPrev = getItemById(data.get(toPosition).getPrevId());
        Item toNext = getItemById(data.get(toPosition).getNextId());
        if (fromPrev != null) {
            fromPrev.setNextId(fromNextId);
            changedItems.add(fromPrev);
        }
        if (fromNext != null) {
            fromNext.setPrevId(fromPrevId);
            changedItems.add(fromNext);
        }

        if (fromPosition < toPosition) {
            if (toNext != null) {
                toNext.setPrevId(from.getId());
                changedItems.add(toNext);
            }
            to.setNextId(from.getId());
            from.setNextId(toNextId);
            from.setPrevId(to.getId());
        } else {
            if (toPrev != null) {
                toPrev.setNextId(from.getId());
                changedItems.add(toPrev);
            }
            to.setPrevId(from.getId());
            from.setNextId(to.getId());
            from.setPrevId(toPrevId);
        }
        changedItems.add(to);
        changedItems.add(from);

        if (onItemChangeListener != null)
            onItemChangeListener.onItemsChanged(changedItems);

        Item movedItem = data.remove(fromPosition);
        data.add(toPosition, movedItem);
        notifyItemMoved(fromPosition, toPosition);
        notifyDataSetChanged();
    }

    @Override
    public boolean onCheckCanStartDrag(ItemViewHolder holder, int position, int x, int y) {
        return true;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ItemViewHolder holder, int position) {
        return null;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    public void removeOnItemChangeListener() {
        onItemChangeListener = null;
    }

    static class ItemViewHolder extends AbstractDraggableItemViewHolder {
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnItemChangeListener {
        void onItemsChanged(List<Item> items);
    }
}