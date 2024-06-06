package com.mirea.kt.ribo.contactscw;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private ArrayList<Person> persons;
    private OnItemSelectedListener listener;
    private boolean withContextMenu = true;

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {

        private final TextView nameView;
        private final TextView numberView;
        private final ImageView imageView;

        ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            nameView = view.findViewById(R.id.tvPersonName);
            numberView = view.findViewById(R.id.tvPersonNumber);
            imageView = view.findViewById(R.id.ivAvatar);
            if (withContextMenu) {
                view.setOnCreateContextMenuListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.person_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                Person person = persons.get(getAdapterPosition());
                listener.onMenuAction(person, item);
            }
            return false;
        }
    }

    public PersonAdapter(ArrayList<Person> persons, OnItemSelectedListener listener, boolean withContextMenu) {
        this.listener = listener;
        this.persons = persons;
        this.withContextMenu = withContextMenu;
    }

    public interface OnItemSelectedListener {

        void onSelected(Person person);

        void onMenuAction(Person person, MenuItem item);
    }

    @Override
    public PersonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PersonAdapter.ViewHolder holder, int position) {
        Person person = persons.get(position);
        holder.nameView.setText(person.getName());
        holder.numberView.setText(person.getPhone());
        holder.imageView.setImageResource(R.drawable.ic_empty_ava);
        if(!person.getAvatar().isEmpty()) {
            setAvatar(person, holder.imageView);
        }
    }
    public void setAvatar(Person person, ImageView imageView){
        String imagename = person.getPhone();
        if(ImageStorage.checkifImageExists(imagename)) {
            File file = ImageStorage.getImage("/"+imagename+".jpg");
            String path = file.getAbsolutePath();
            if (path != null){
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
            }
        } else {
            new GetImages(person.getAvatar(), imageView, imagename).execute() ;
        }
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }
    @SuppressLint("NewApi")
    public void setPersons(ArrayList<Person> persons){
        Collections.sort(persons, Comparator.comparing(Person::getName));
        this.persons = persons;
    }
}




